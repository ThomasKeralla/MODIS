import java.net.*;
import java.io.*;
import java.util.*;

public class Estimator {
    
    private static int serverPort = 8007; 
    private int interval; 
    private int size;
    private DatagramSocket sendSocket = null; 
    private DatagramSocket receiveSocket = null; 
    InetAddress host;

    private int duplicatePackets = 0;
    private int reorderedPackets = 0;
    private int droppedPackets = 0;

    // Messages
    // TODO: Can be done differently
    private ArrayList<String> messages;
    
    // Received messages
    // TODO: Can be done differently
    private ArrayList<Integer> received; 


    private class Send implements Runnable {
        public void run() {
            for(String msg : messages){
                try {
                    DatagramPacket request = new DatagramPacket(msg.getBytes(), msg.getBytes().length, host, serverPort);
                    sendSocket.send(request);
                    Thread.sleep(interval);
                } catch (SocketException e) { System.out.println("SocketException at send thread..." + e.getMessage()); }
                catch (IOException e) { System.out.println("IOException at send thread..." + e.getMessage()); }
                catch (InterruptedException e) { System.out.println("InterruptedException e" + e.getMessage());}

            }
        }
    }

    private class Receive implements Runnable {
        public void run() {
            while(true){
                try {
                    byte[] buffer = new byte[size]; // Hardcoded length right now
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);	
                    receiveSocket.receive(reply);

                    // TODO: Figure out how to handle received data. This example code converts the data to Strings
                    String res = new String(reply.getData());
                    received.add(Integer.parseInt(res.substring(0, 4)));
                    //System.out.println("Recieved: " + Integer.parseInt(res.substring(0, 4)));
                } catch(SocketTimeoutException e) {
                    System.out.println("Socket timeout. Closing...");
                    break;
                } catch (SocketException e) { System.out.println("SocketException at receive thread..." + e.getMessage()); }
                catch (IOException e) { System.out.println("IOException at receive thread..." + e.getMessage()); }
            }
        }
    }

    public void estimator(ArrayList<String> messages, int interval, int DatagramSize, int count, String hostName) {
        try {
            sendSocket = new DatagramSocket(); 
            
            receiveSocket = new DatagramSocket(serverPort);
			receiveSocket.setSoTimeout(1000);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        }

        try {
            host = InetAddress.getByName(hostName);
        } catch (Exception e) {System.out.println("Cant find host");}

        this.messages = messages; 
        this.interval = interval; 
        this.size = DatagramSize;
        received = new ArrayList<>();

        Thread receiveThread = new Thread(new Receive());
        Thread sendThread = new Thread(new Send()); 

        receiveThread.start();
        sendThread.start();

        try {
            receiveThread.join();
        } catch (InterruptedException e) {System.out.print("Thread: " + e.getMessage());}

        try {
            sendSocket.close();
            receiveSocket.close();
        } catch (Exception e) { System.out.println("Exception during closing"); }

        Map<Integer, Integer> dataMap = new HashMap<>();

        for (int i = 0; i < messages.size(); i++) {
            dataMap.put(i, 0);
        }

        int lastID = -1;
        for (int r : received) {
            if (lastID > r) reorderedPackets++;
            if (lastID == r) {duplicatePackets++; System.out.println("Duplicate: " + r);}
            lastID = r;
        }

        droppedPackets = count - (received.size() - duplicatePackets);

        // TODO: Print statistics
        double dupPer = percentageGen(duplicatePackets, count);
        double rePer = percentageGen(reorderedPackets, count);
        double disPer = percentageGen(droppedPackets, count);
        double recPer = percentageGen(received.size(), count);
        
        System.out.println("######## STATISTICS ########");
        System.out.println("Duplicates: " + duplicatePackets + " | " + dupPer + "%");
        System.out.println("Reordered: " + reorderedPackets + " | " + rePer + "%");
        System.out.println("Discarded: " + droppedPackets + " | " + disPer + "%");
        System.out.println("Packets received: " + received.size() + " | " + recPer + "%");
        System.out.println("Packets sent: " + count);
    }

    private double percentageGen (int packets, int count) {
        return ((double)packets / (double)count) * 100;
    }
   
    public static void main(String[] args) {
        int datagramSize = Integer.parseInt(args[0]);
        int count = Integer.parseInt(args[1]);
        int interval = Integer.parseInt(args[2]); //In miliseconds 
        String host = "localhost";        

        //if (args[3] != "") host = args[3];

        ArrayList<String> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String message = "";
            int idLen = new String("" + i).length();
            if (idLen < 4) message += addPadding(4-idLen);
            message += i;
            message += createMessage(datagramSize);
            messages.add(message);
        }

        // Run the estimator
        Estimator e = new Estimator(); 
        e.estimator(messages, interval, datagramSize, count, host);
    }

    private static String addPadding(int count) {
        String pad = "";
        for (int i = 0; i < count; i++) {
            pad += "0";
        }
        return pad;
    }

    private static String createMessage(int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size - 4; i++) sb.append('a');
        return sb.toString();
    }

}