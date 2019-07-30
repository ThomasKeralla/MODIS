import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner; 
import java.nio.*;

public class ReliableUDPServer{
    private static int serverPort = 7007; 
    static HashMap<String, ArrayList<String>> printed;

    public static void main(String args[]){ 
        DatagramSocket aSocket = null;
        printed = new HashMap<>();

        try {
            // Opens listener socket at specific port
            aSocket = new DatagramSocket(serverPort);
            while(true) {             
                // Recieve package
                byte[] buffer = new byte[514];
                DatagramPacket receive = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(receive);
                String host = receive.getAddress().getHostAddress();

                // Init new arraylist if new ip address
                if(!printed.containsKey(host)) printed.put(host, new ArrayList<>()); 

                // Get Id of package
                byte[] id = Arrays.copyOfRange(receive.getData(), 0, 4);

                // If we haven't printed this specific IP's package we do now
                // and updates our map with that package ID. Then it sends an 
                // acknowlegdement back to the sender. 
                // If we already have printed the package we just send an
                // additional acknowlegdement to the sender
                if(!printed.get(host).contains(new String(id))) {
                    String message = new String(Arrays.copyOfRange(receive.getData(), id.length, receive.getData().length)).trim();
                    printed.get(host).add(new String(id));
                    System.out.println("Received: " + message);
                    DatagramPacket acknowlegde = new DatagramPacket(id, id.length, receive.getAddress(), receive.getPort());
                    aSocket.send(acknowlegde);          
                } else {
                    DatagramPacket acknowlegde = new DatagramPacket(id, id.length, receive.getAddress(), receive.getPort());
                    aSocket.send(acknowlegde);         
                }
            }   
        } catch (SocketException e) {System.out.println("Socket: " + e.getMessage()); 
        } catch (IOException e) {System.out.println("IO: " + e.getMessage());
        } finally {if(aSocket != null) aSocket.close();}    
	}	      	
}