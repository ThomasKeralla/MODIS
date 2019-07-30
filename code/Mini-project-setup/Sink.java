import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class Sink {

    Sink(int port, String address) {
        Socket client = null;
        Random rand = new Random();
        try {

            InetAddress ip = InetAddress.getByName(address);
            client = new Socket(ip, port);
            BufferedReader inputStream =  new BufferedReader(new InputStreamReader(client.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));            

            if(client.isConnected()) { 
                writer.write("sink\n");
                writer.newLine();
                writer.flush();
                System.out.println("Connected");
            }
            while (client.isConnected()) {
                String input = inputStream.readLine();
                System.out.println(input);
                if(rand.nextInt(500) > 496) break;
            }

            client.close();
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (IOException e ) {
            System.out.println("IOException in sink: " + e.getMessage());
        }
    }

    public static void main(String args[]) {
        if (args.length == 0){
            new Sink(7007, "localhost");
        }
        else{
            int port = Integer.parseInt(args[0]);
            String address = args[1];
            new Sink(port, address); 
        }
    }
}