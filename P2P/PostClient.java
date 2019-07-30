import java.util.*;
import java.net.*;
import java.io.*;


public class PostClient implements Serializable {
    private static final long serialVersionUID = 1L;    
    String key, data, ip;
    int port;

    PostClient(String keyString, String data, int port){
        UUID idd = UUID.nameUUIDFromBytes(keyString.getBytes());
        key = idd.toString().replaceAll("-", "");
        key = key.substring(0, 5);

        SaveData saveData = new SaveData();
        saveData.key = key;
        saveData.value = data;
        saveData.jumps = 0;

        send(saveData, "localhost", port);
    }

    public void send(SaveData post, String ip, int port){
        try{
            Socket socket = new Socket(ip, port);
            System.out.println("Sending: " + key);
            OutputStream out = socket.getOutputStream();
            ObjectOutputStream obout = new ObjectOutputStream(out);
            obout.writeObject(post);
            obout.writeByte('\n');
            obout.flush();
            socket.close();
        } catch (IOException e) {System.out.println("Send error: " + e.getMessage() + ". IP: " + ip + " PORT: " + port); e.printStackTrace();}  
    }

    
    public static void main(String args[]){
        String key = args[0];
        String data = args[1];
        int port = Integer.parseInt(args[2]);
        //String ip =  args[2];
        
        new PostClient(key, data, port);
    }
}