import java.net.InetAddress;
import java.net.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.Collection;


public class Node implements Serializable{


    private final int id;
    private final String ip;
    private final int homePort;
    private ServerSocket socket;
    private int nodeNext;
    private int nextNext;
    private int tail;
    private boolean printed = false;
    private Random rd = new Random();

/*
    private int leader;
    private boolean participant;
*/



    public Node(int port, int connect){

    //Hardcoded "localhost", would be an actual IP address if not tested locally
    this.ip = "localhost";
    //Faulty as two nodes can have same port, but low risk
    id = rd.nextInt(1000);

    this.homePort = port;


    System.out.println("Sending join");
    Send s = new Send(ip, connect, new Data(homePort, "join"));
    Thread t = new Thread(s);
    t.start();
    run(homePort);
    }
    public Node(int port)   {
      this.ip = "localhost";
      id = rd.nextInt(1000);
      this.homePort = port;
      run(homePort);

      //System.out.println("Homeport : "+ homePort);

    }
    public Node() {
      this.ip = "localhost";
      id = rd.nextInt(1000);
      homePort = rd.nextInt(500)+5000;
      //System.out.println("Homeport : "+homePort);
      run(homePort);
      }


    public int getId() {
      return id;
    }
    public String getIp() {
      return ip;
    }
    public int getHomePort() {
      return homePort;
    }
    public int getNext() {
      return nodeNext;
    }




    public void run (int homePort){

        try {
            System.out.println("Node: "+id+ " listning on port "+ homePort);

            socket = new ServerSocket(homePort);

            while(true) {
                Socket client = socket.accept();
                InputStream input = client.getInputStream();
                // node waits for new connection
                // and waits for message about which type has connected
                ObjectInputStream objectInput = new ObjectInputStream(input);
                Data recieved = (Data) objectInput.readObject();

                String decisionKey = (String) recieved.getKey();

                //MAKE CASES
                switch(decisionKey) {
                    case "join":
                      System.out.println("Join");
                      if(nodeNext == 0) {
                        nodeNext = homePort;
                      }
                        Send yourNext = new Send(ip, recieved.getPort(), new Data(nodeNext, "yourNext"));
                        Thread uNext = new Thread(yourNext);
                        uNext.start();
                        nextNext = nodeNext;
                        nodeNext = recieved.getPort();
                        System.out.println("nextNext "+nextNext);
                        System.out.println("nodeNext "+nodeNext);
                        System.out.println("Done Join");
                        break;
                    case "yourNext":
                        System.out.println("yourNext");
                        nodeNext = recieved.getPort();
                        System.out.println("nodeNext "+nodeNext);
                        Send conga = new Send(ip, nodeNext, new Data(homePort, "conga"));
                        Thread con = new Thread(conga);
                        con.start();
                        break;
                    case "myNext":
                        System.out.println("myNext");
                        nextNext = recieved.getPort();
                        System.out.println("nextNext "+nextNext);
                        break;
                    case "conga":
                        System.out.println("conga");
                        Send myNext = new Send(ip, recieved.getPort(), new Data(nodeNext, "myNext"));
                        Thread myNxt = new Thread(myNext);
                        myNxt.start();
                        try{
                          Thread.sleep(10);
                        } catch(InterruptedException e) {}
                        Thread t = new Thread (()-> {
                        Send check = new Send(ip, nodeNext, new Data(nextNext, "check"));
                        });
                        t.start();
                        //send update - next &nextnext
                        break;
                    case "print":
                        System.out.println("print");
                        if(!printed) {
                        Send print = new Send(ip, nodeNext, new Data(nextNext, "print"));
                        Thread pr = new Thread(print);
                        pr.start();
                        System.out.println("NodeID: "+id+ " NodeNext = "+nodeNext +" NodeNextNext= "+ nextNext);
                        printed = true;
                        }
                        break;
                    case "update":
                    System.out.println("update");
                        nextNext = recieved.getPort();
                        break;
                    case "check":
                        System.out.println("check");
                        Thread t5 = (recieved.getPort() == nodeNext) ?  new Thread (()-> {
                        Send check = new Send(ip, nodeNext, new Data(nextNext, "check")); }) : new Thread(()-> {
                        Send up = new Send(ip, client.getPort(), new Data(nodeNext, "update"));
                        });
                        t5.start();
                        break;


                    default:
                        System.out.println("Unknown message");
                        client.close();
                }
            }
        } catch (IOException e ) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Monkey balls: " + e.getMessage());
        } finally {
            try {
                if(socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("Something happened while closing server");
            }
        }
    }

//adjust
    public static void main (String[] args){
        if (args.length == 0) new Node();
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
