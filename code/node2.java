import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.UUID;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.net.UnknownHostException;

public class Node2 {
    UUID id;
    ArrayList<NodeInfo>[] routingTable;
    ServerSocket suckit;
    Socket clientSocket;
    NodeInfo[] leafSet;
    String ip;
    int port;


    Node2 (int port){
        id = UUID.randomUUID();
        System.out.println(id.toString());
        makeRoutingTable();
        run(port);
    }
    Node2 (String ip, int port){
        id = UUID.randomUUID();
        this.ip = ip;
        this.port = port;
        makeRoutingTable();
        join(ip, port);
    }

    public void run (int port){
        try {
            suckit = new ServerSocket(port);

            System.out.println("Node ready");
            while(true) {

                // node waits for new connection
                    // and waits for message about which type has connected
                    Socket newClient = suckit.accept();
                    ObjectInputStream ois = new ObjectInputStream(newClient.getInputStream());
                    try {
                      if(ois.readObject().equals("join")) {
                          System.out.println("Received Join request");

                          JoinData joinData = (JoinData) ois.readObject();
                          if(joinData instanceof JoinData)
                          System.out.println("JoinData recieved");
                          //join(joinData);
                        }
                        else {
                            newClient.close();
                        }

                    } catch(ClassNotFoundException | IOException e) {System.out.println(e.getMessage());}


                    }
                    //has to be moved before te else statement!!!
                    /* else if(ois.readObject().equals("update")) {
                        update(newClient);
                        System.out.println("Received Update connection");
                    } else if(ois.readObject().equals("put")) {
                        put(newClient);
                        System.out.println("Received Put connection");
                    } else if(ois.readObject().equals("get")) {
                        get(newClient);
                        System.out.println("Received Get connection");
                    } */

            }
            catch (IOException e ) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    if(suckit != null) suckit.close();
                } catch (IOException e) {
                    System.out.println("Something happened while closing server");
                }
            }

        }

        public void join(String ip, int port) {
          try{
              DataOutputStream out = clientSocket(ip, port);
              String msg = "join";
              out.writeBytes(msg+"\n");
              out.flush();

              ObjectOutputStream out2 = new ObjectOutputStream(clientSocket.getOutputStream());
              out2.writeObject(new JoinData(ip, id));
              out.flush();
              try{
                  TimeUnit.SECONDS.sleep(1);
              } catch(InterruptedException e) {System.out.println(e.getMessage());}


          } catch (IOException | IllegalMonitorStateException e) {System.out.println("Socket: " + e.getMessage());}
          finally {
              try {
                  if(clientSocket != null)
                  clientSocket.close();
              } catch (IOException e) {
                  System.out.println("Something happened while closing Socket");
              }
          }

        }
        //Need to figure out how to find the server
        public DataOutputStream clientSocket(String ip, int port ) {
          try {
            clientSocket = new Socket(InetAddress.getByName(ip), port);
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            return out;
          } catch (IOException e) {System.out.println("Socket: " + e.getMessage());}
          return null;
        }

        public void makeRoutingTable() {
          for (int i = 0; i<16; i++)
          routingTable[i] = new ArrayList<NodeInfo>();
        }

        public class JoinData{
            ArrayList<ArrayList<NodeInfo>> joinTable;
            InetAddress ip;
            UUID id; //what is this

            public JoinData(String ip, UUID id) {
            try {
              this.ip = InetAddress.getByName(ip);
            }catch(UnknownHostException e){System.out.println(e.getMessage());}

            this.id = id;
            joinTable = new ArrayList<>();
            }

            public InetAddress getAddress(){
              return ip;

            }

            public UUID getID() {
              return id;
            }


            public ArrayList<ArrayList<NodeInfo>> getList() {
              return joinTable;
            }
      }

      public class NodeInfo {
          InetAddress ip;
          String id;
      }


  }
