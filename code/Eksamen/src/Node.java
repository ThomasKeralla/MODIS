
import java.net.*;
import java.net.Socket;
import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collection;


public class Node implements Serializable{

    private final String ip;
    private final int homePort;
    private ServerSocket socket;
    private int tail;
    private int tailTail;
    private int head;
    private int leader;
    private AtomicInteger count;
    private int backup;
    //List to store Data objects in if the leader(server) has crashed.
    ArrayList<Data> queue = new ArrayList<>();



    public Node(int port, int connect){

    //Hardcoded "localhost", would be an actual IP address if not run locally
    this.ip = "localhost";
    this.homePort = port;
    this.backup = 0;
    this.head = 0;
    this.tail = 0;
    this.tailTail = 0;
    System.out.println("Sending join");
    //Join the network
    Thread t = new Thread(new Send(ip, connect, homePort, new Data(homePort, "join")));
    t.start();
    //ServerSocket that listens for messages and applies the logic of the system
    run(homePort);
    }
    //Constructor applied on the first node of the system.
    public Node(int port)   {
      this.ip = "localhost";
      this.backup = 0;
      this.head = 0;
      this.tail = 0;
      this.tailTail = 0;
      this.homePort = port;
      this.leader = homePort;
      //Knows it is leader(server) and initialises count
      count = new AtomicInteger(0);
      run(homePort);
    }


    public void run (int homePort){

        try {

            socket = new ServerSocket(homePort);
            System.out.println("Node listning at port: "+homePort);
            //loops continously until killed, making a new socket when accepting
            while(true) {
                Socket client = socket.accept();
                InputStream input = client.getInputStream();
                // initialises an inputstream to read Data objects being send by threads
                ObjectInputStream objectInput = new ObjectInputStream(input);
                Data recieved = (Data) objectInput.readObject();
                //What type of message, and how to handle it in the switch
                String decisionKey = (String) recieved.getKey();

                //The logic of the Node
                switch(decisionKey) {
                  /*
                  Join has four cases that it covers; 1. if the node is alone and another joins
                  2. if it is not the leader(server) and has no tail
                  3. if the node has no tailTail
                  4. if the node is covered and just have to propagate the message in the system
                  In general it makes sure that the right relationsships are formed
                  */
                    case "join":
                      System.out.println("Join");
                      if(leader == homePort && tail == 0) {
                      tail = recieved.getPort();
                      Thread tCountBackup = new Thread(new Send(ip, tail, homePort, new Data(homePort, "countBackup")));
                      tCountBackup.start();
                      System.out.println("countBackup message");
                      } else if (leader != homePort && tail == 0) {
                      tail = recieved.getPort();
                      Thread tWelcome = new Thread(new Send(ip, tail, homePort, new Data(leader, "welcome")));
                      tWelcome.start();
                      Thread tWelcome2 = new Thread(new Send(ip, tail, homePort, new Data(homePort, "yourHead")));
                      tWelcome2.start();
                      System.out.println("welcome message");
                      } else  {
                      if(tailTail == 0) { tailTail = recieved.getPort(); }
                      Thread tToTail = new Thread(new Send(ip, tail, homePort, new Data(recieved.getPort(), "newTail")));
                      tToTail.start();
                      }
                      break;
                      //lets a node know that it has to act as the backup, sets the leader and the head to leader,
                      //thus this node has to be the second node in the chain of nodes
                    case "countBackup":
                      leader = recieved.getPort();
                      head = leader;
                      Thread pingLeader = new Thread(new Pinging(ip, leader, head, homePort));
                      pingLeader.start();
                      //initialises the count field as it now has to be the replication server
                      count = new AtomicInteger();
                      //If the nodes has a tail it starts a newleader campaign so all nodes updates the leader
                      if(tail != 0) {
                        Thread lead = new Thread(new Send(ip,tail,homePort, new Data(leader, "newLeader")));
                        lead.start();
                      }
                      break;
                      //Gives the node the address of the anointed leader
                    case "welcome":
                      leader = recieved.getPort();
                      break;
                      //spawn from a join message, this is how the newly joined node gets placed at the end of the chain
                      //placed as tail of the former latest joined node and gives it the leader and itself as its head
                    case "newTail":
                      if(tail == 0) {
                      tail = recieved.getPort();
                      Thread tHallo = new Thread(new Send(ip, tail, homePort, new Data(leader, "welcome")));
                      tHallo.start();
                      Thread tHallo2 = new Thread(new Send(ip, tail, homePort, new Data(homePort, "yourHead")));
                      tHallo2.start();
                      break;
                      }
                      //update tailTail if equal to 0 and pass the message along to the tail
                      else{
                        if(tailTail == 0) {tailTail = recieved.getPort();}
                        Thread tRecieved = new Thread(new Send(ip, tail, homePort, recieved));
                        tRecieved.start();
                      }
                      break;
                      //Informes the node of its head. Especially important when a node dies and the system has to recover
                    case "yourHead":
                      head = recieved.getPort();
                      Thread newTailTail= new Thread(new Send(ip, head,homePort, new Data(tail, "newTailTail")));
                      newTailTail.start();
                      //starts pinging the head to check if its alive, otherwise the pinging class will sent death message for recovery
                      Thread pingHead = new Thread(new Pinging(ip, leader, head, homePort));
                      pingHead.start();
                      break;
                      //print simple status/state of the node, can be sent by a Client. Sents it to the leader if node not the newLeader
                      //ensuring that the entire systems prints its state.
                    case "print":
                      if(homePort != leader) {
                      Thread tPrintLeader = new Thread(new Send(ip, leader, homePort, new Data(homePort, "print")));
                      tPrintLeader.start();
                      } else {
                      Thread tPrintTail = new Thread(new Send(ip, tail, homePort, new Data(homePort, "printRequest")));
                      tPrintTail.start();
                      }
                      break;
                      //prints state, stops at node with no tail
                    case "printRequest":
                      if(tail == 0) {
                      System.out.println("My port= "+homePort+" my tail= "+tail+" my tailTail="+tailTail+" my head= "+head+ "my leader= "+leader);
                      break;
                      } else {
                      Thread tPrintTail = new Thread(new Send(ip, tail, homePort, recieved));
                      tPrintTail.start();
                      System.out.println("My port= "+homePort+" my tail= "+tail+" my tailTail="+tailTail+" my head= "+head+ "my leader= "+leader);
                      }
                      break;
                      //if recives ping just ignore
                    case "ping":
                      //System.out.println("pinged by "+recieved.getPort());
                      break;
                      //if the dead node is the tail; set tail to tailTail and inform new tail that this node is its new pingHead
                      //otherwise just forward message to tail
                    case "death":
                      if(tail == recieved.getPort()) {
                        tail = tailTail;
                        Thread newNext = new Thread(new Send(ip, tail, homePort, new Data(homePort, "yourHead")));
                        newNext.start();
                      } else {
                        Thread forwardDeath = new Thread(new Send(ip, tail, homePort, recieved));
                        forwardDeath.start();
                      }
                      break;
                      //anoints this node to be the leader, send countBackup message to tail so it becomes new redoncy count
                    case "youLead":
                      if(recieved.getPort() == head) {
                        leader = homePort;
                        System.out.println("success "+tail);
                      }
                      if(tail != 0) {
                        Thread newLeader = new Thread(new Send(ip, tail, homePort, new Data(homePort, "countBackup")));
                        newLeader.start();
                        break;
                        }
                      System.out.println("Last one alive!!! my port= "+homePort +" leader= "+leader);
                      break;
                      //node being given a new tailTail, important when nodes crash
                    case "newTailTail":
                      tailTail = recieved.getPort();
                      break;
                      //nodes being informed of a newly anointed leader
                    case "newLeader":
                      leader = recieved.getPort();
                      if(tail != 0) {
                        Thread forwardLeader = new Thread(new Send(ip, tail, homePort, recieved));
                        forwardLeader.start();
                      }
                      //check if this node has stored any messages that it could not deliver and sent them to new leader
                      if(queue.size() != 0) {
                        for(Data d : queue) {
                          Thread t = new Thread(new Send(ip, leader, homePort, d));
                          t.start();
                        }
                      }
                      break;
                      //if a send failure has happened with a "get" or "inc" message then it is stored in the queue
                    case "sendError":
                    //More could be added if needed to be more fault tolerant
                      if(recieved.getExtraKey().equals("get") || recieved.getExtraKey().equals("inc")) {
                        queue.add(new Data(recieved.getPort(), recieved.getExtraKey()));
                      }
                      break;
                      //if not the leader sent it to leader. If leader increment count, sent a update to tail/redoncy countBackup
                      //and sent response to Client
                    case "inc":
                      if(homePort == leader) {
                        count.getAndIncrement();
                        Thread t = new Thread(new Send(ip, tail, homePort, new Data(1, "update")));
                        t.start();
                        Thread incResponse = new Thread(new Send(ip, recieved.getPort(), homePort, new Data(count.get(), "incReply")));
                        incResponse.start();
                      } else {
                        Thread t = new Thread(new Send(ip, leader, homePort, recieved));
                        t.start();
                      }
                      break;
                      //forward message to leader if not the leader. Get the value of count and return it to Client
                    case "get":
                      if(homePort == leader) {
                        Thread t = new Thread(new Send(ip, recieved.getPort(), homePort,new Data(count.get(), "getReply")));
                        t.start();
                      } else {
                        Thread t2 = new Thread(new Send(ip, leader, homePort, recieved));
                        t2.start();
                      }
                      break;
                      //When recieved the redoncy/backup count increments its count
                    case "update":
                      if(head == leader && recieved.getPort() == 1) {
                        count.getAndIncrement();
                      }
                      break;
                    default:
                        System.out.println("Unknown message " +recieved.getKey()+" "+recieved.getPort());
                        client.close();
                }
            }

        } catch (IOException e ) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Exception caught: " + e.getMessage());
        } finally {
            try {
                if(socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("Something happened while closing server");
            }
        }
    }

    public static void main (String[] args){
        if (args.length == 0) {
          System.out.println("You must provide a port number to start the system");
        }
//Applying the overloaded constructors so that first node will start the system and subsequent nodes can join.
        else if (args.length == 2) {
            int homePort = Integer.parseInt(args[0]);
            int ConnectPort = Integer.parseInt(args[1]);
            new Node(homePort, ConnectPort);
        } else if (args.length == 1) {
            int homePort = Integer.parseInt(args[0]);
            new Node(homePort);
        } else {System.out.println("You have given more then the 2 arguments requeried");}
    }
}
