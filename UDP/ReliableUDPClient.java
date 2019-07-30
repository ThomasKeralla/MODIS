import java.net.*;
import java.io.*;
import java.util.*;

//Reliable UDPClient
public class ReliableUDPClient{
    private Boolean run = true;
    private DatagramSocket aSocket = null;
    private byte[] b = new byte[4];
       
    /**
     * The constructor takes a String addres, int port and String message supplied from the input array
     * 
     */
    public ReliableUDPClient (String address, int port, String message) {
        //sets the random byte array for the header
        new Random().nextBytes(this.b);

         // applying the header to the message 
         byte[] m = new byte[4 + message.length()];
         System.arraycopy(b, 0, m, 0, 4);
         System.arraycopy(message.getBytes(), 0, m, 4, message.length());
        //starts sending  
        this.send(address, port, m);        
        
    }
    
    /** the send method takes a string address, int port, string message from the constructor
     *  while run is true it will keep sending after the set timout
     */
    public void send (String address, int port, byte[] m){
        
        try {
            this.aSocket = new DatagramSocket();
            InetAddress aHost = InetAddress.getByName(address);
            aSocket.setSoTimeout(100);
            //while loop sends untill run is set to false, when the recieve() receives the correct byte array
            while(this.run) {
               
                
                DatagramPacket request = 
                new DatagramPacket(m,  m.length, aHost, port);
                aSocket.send(request);
                
                this.recieve();
                
            }
        } catch (SocketException e) {System.out.println("Socket: " + e.getMessage());
    } catch (IOException e) {System.out.println("IO: " + e.getMessage());
} finally {if(aSocket != null) aSocket.close();}

}

/**
 * The receive method waits for a reply and checks if it matches the random byte array b
 * if it does it flips the run boolean to false and the send while-loop cuts.
 */
public void recieve (){
    
        try {
            byte[] buffer = new byte[4];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);

            if (Arrays.equals(reply.getData(), this.b)) {
                this.run = false;
                System.out.println("Success");
            }
        } catch(SocketTimeoutException e) {
                    System.out.println("Socket timeout. Closing...");
                } catch (SocketException e) { System.out.println("SocketException at receive thread..." + e.getMessage()); }
                catch (IOException e) { System.out.println("IOException at receive thread..." + e.getMessage()); }

    }

/**
 * main method takes the three required inputs
 * address, port and message
 */
    public static void main(String args[]){
        String address = args[0];
        int port = Integer.parseInt(args[1]);
        String message = args[2];

        ReliableUDPClient oldFaithfull = new ReliableUDPClient(address, port, message);

        System.out.println("Terminating");
	}	      	
}