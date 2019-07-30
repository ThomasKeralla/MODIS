import java.net.*;
import java.io.*;
import java.util.Scanner; 

public class UDPClient{
	private static int serverPort = 7007; 


    public static void main(String args[]){ 
		QuestionableDatagramSocket aSocket = null;
		Scanner msgScan = new Scanner(System.in);
		try {
			aSocket = new QuestionableDatagramSocket();
			while(true) { //Keep ask user for messages. 
					InetAddress aHost = InetAddress.getByName("localhost");
					String msg = msgScan.nextLine();
					byte [] m = msg.getBytes();		                                                 
					DatagramPacket request =
						new DatagramPacket(m,  msg.length(), aHost, serverPort);
					aSocket.send(request);			                        
			}
		} catch (SocketException e) {System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {System.out.println("IO: " + e.getMessage());
		} finally {if(aSocket != null) aSocket.close(); if(msgScan != null) msgScan.close();}
	}	      	
}
