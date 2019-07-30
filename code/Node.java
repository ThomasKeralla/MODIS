import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.UUID;
import java.io.*;
import java.util.ArrayList;

public class Node {
    UUID id;
    ArrayList<ArrayList<NodeInfo>> routingTable;
    ServerSocket suckit;
    NodeInfo[] leafSet;

    
    Node (int port){
        id = UUID.randomUUID();
        System.out.println(id.toString());
        routingTable = new ArrayList<>();
    }



    public void updateRoutingTable(ArrayList<NodeInfo> row) {
        routingTable.add(row);
    }
    
    Node (String ip, int port){
        id = UUID.randomUUID();
    }
    
    public void join (JoinData joinData){
        System.out.println("joining...");
        int path = route(joinData.id.replaceAll("-", ""));        

        switch (path) {
            case 1000000:
                joinRoutingTable(joinData);
                break;
            case 2000000:
                joinLeafSet(joinData);  
                break;     
            default:
                prefixRoute(joinData, path);
                break;
        }   
    }
    
    public int route(String joinId){
        System.out.println("routing");
        String myId = id.toString();
        int i = 0;
        id.replaceAll("-", "");

        for (int n = 0; n < myId.length() ; n++){
            if (myId.charAt(n) == id.charAt(n)){
                i++;
            } else break;
        }        
        if (i == routingTable.size()){
            return 1000000;
        } else if (i > routingTable.size()){
            return 2000000;
        }

        else {
            return i;
        }

    }


    public void preFixRoute(String id){

    }
    
    public void run (int port){
        try {
            suckit = new ServerSocket(port);
            
            System.out.println("Node ready");
            while(true) {
                
                // node waits for new connection
                    // and waits for message about which type has connected
                    Socket newClient = suckit.accept();
                    
                    if(type.equals("join")) {
                        System.out.println("Received Join request");
                        ObjectInputStream ois = new ObjectInputStream(newClient.getInputStream());
                        JoinData joinData = (JoinData) ois.readObject();
                        join(joinData);
                    } /* else if(type.equals("update")) {
                        update(newClient);
                        System.out.println("Received Update connection");
                    } else if(type.equals("put")) {
                        put(newClient);
                        System.out.println("Received Put connection");
                    } else if(type.equals("get")) {
                        get(newClient);
                        System.out.println("Received Get connection"); 
                    } */ else {
                        newClient.close();
                    }
                }

            } catch (IOException e ) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    if(suckit != null) suckit.close();
                } catch (IOException e) {
                    System.out.println("Something happened while closing server");
                }
            }
            
        }

        public class JoinData{
            ArrayList<ArrayList<NodeInfo>> joinTable;
            InetAddress ip;
            String id;

        }
        
        public class NodeInfo {
            InetAddress ip;
            String id;
        }
        public static void main (String[] args){
            new Node(7007);
        }
        
    }