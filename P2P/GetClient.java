import java.util.*;
import java.net.*;
import java.io.*;


public class GetClient implements Serializable {
    private static final long serialVersionUID = 1L;    
    String key, data, ip;
    int port;

    GetClient(String key, int myPort, int port){
        UUID idd = UUID.nameUUIDFromBytes(key.getBytes());
        key = idd.toString().replaceAll("-", "").substring(0, 5);
        GetData get = new GetData();


        get.key = key;
        get.jumps = 0;
        get.port = myPort;

        send(get, "localhost", port);
        waitForData(myPort);

    }

    public void send(GetData post, String ip, int port){
        Socket socket = null;
        try{
            socket = new Socket(ip, port);
            System.out.println(key + " Sending: " + post);
            OutputStream out = socket.getOutputStream();
            ObjectOutputStream obout = new ObjectOutputStream(out);
            obout.writeObject(post);
            obout.writeByte('\n');
            obout.flush();
            socket.close();
        } catch (IOException e) {System.out.println("Send error: " + e.getMessage() + ". IP: " + ip + " PORT: " + port); e.printStackTrace();
        }  finally {
            try {
                if(socket != null)
                socket.close();
            } catch (IOException e) {
                System.out.println("Something happened while closing Socket");
            }
        }
    }

    public void waitForData(int port){
        Socket newClient = null;
        ServerSocket sogget = null;
        
        try {
            sogget = new ServerSocket(port);
            System.out.println("GetClient ready");
            ObjectInputStream ois;
            Object ob;
            newClient = sogget.accept();
            ois = new ObjectInputStream(newClient.getInputStream());
            ob =  ois.readObject();
            SaveData gotten = (SaveData) ob;
            if (gotten.value == null) {
                System.out.println("Data not found, Why are you searching for stuff that don't exist?");
            } else {
                System.out.println("Weeee! got the data: " + gotten.value);
            }
            System.out.println("In only " + gotten.jumps + " jumps!");
            newClient.close();
            sogget.close();
            

        } catch (IOException|ClassNotFoundException e) {System.out.println("Send error: " + e.getMessage() + ". IP: " + ip + " PORT: " + port); e.printStackTrace();
                
          } finally {
                    try {
                        if(newClient != null)
                        newClient.close();
                        sogget.close();                        
                    } catch (IOException e) {
                        System.out.println("Something happened while closing Socket");
                    }
                }
    }

    
    public static void main(String args[]){
        String data = args[0];
        int port = Integer.parseInt(args[1]);
        int porto = Integer.parseInt(args[2]);
        //String ip =  args[2];
        
        new GetClient(data, port, porto);
    }
}