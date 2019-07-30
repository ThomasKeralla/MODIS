import java.net.*;
import java.io.*;
import java.util.Scanner; 

public class UDPServer{
	private static int serverPort = 7007; 


    public static void main(String args[]){ 
        DatagramSocket aSocket = null;

        try {
            // Listener Socket
            aSocket = new DatagramSocket(serverPort);
            while(true) { 
                    // Recieve and trim message to avoid empty space
                    byte[] buffer = new byte[1000];
                    DatagramPacket recieve = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(recieve);
                    String replyData = new String(recieve.getData()).trim();
                    System.out.println("Recieved: " + replyData);
                    
                    
                    // Return the message to the senders address and port
                    InetAddress aHost = recieve.getAddress();                                               
                    int returnPort = recieve.getPort();
                    DatagramPacket request =
                        new DatagramPacket(replyData.getBytes(),  replyData.length(), aHost, returnPort);
                    aSocket.send(request);			                            
            }   
        } catch (SocketException e) {System.out.println("Socket: " + e.getMessage()); 
        } catch (IOException e) {System.out.println("IO: " + e.getMessage());
        } finally {if(aSocket != null) aSocket.close();}    
	}	      	
}
