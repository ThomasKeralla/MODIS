import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
/**
 * Source
 * this mhere things gonna send what you type in console to the Server you hear
 */

public class Source {
    Boolean run = true;
    DataOutputStream out;
    Socket socket;

    public Source (String host, int port) {
        try{
            this.socket = new Socket(host, port);
            this.out = new DataOutputStream(socket.getOutputStream());
            if(socket.isConnected()) {
                out.writeBytes("source\n");
                out.flush();
                System.out.println("Connected");
            }
        }catch (IOException e) {System.out.println("Socket: " + e.getMessage());}   
         
    }
    
    public void scan (){
        Scanner msgScan = new Scanner(System.in);
        try {
            while (run) {
                String msg = msgScan.nextLine();
                if (msg.equals("/quit")) {
                    this.run = false;
                    break;
                }
                send(msg);               
            }   
        } catch (IOException e) {
            System.out.println("Connection to server has died");
        } finally {
            msgScan.close();
        }
    }
  
    public void send(String msg) throws IOException {
        out.writeBytes(msg + "\n");
        out.flush();        
    }

    private void messageGenerator(String msg, int n){
        int i = 1;
        System.out.println(msg + " " + i);
       try{ 
            while (i < n + 1) {
                send(msg + " " + i);
                System.out.println("" + i);
                TimeUnit.SECONDS.sleep(1);
                i++;
            }
        }catch (InterruptedException e) {System.out.println("Interrupted: " + e.getMessage());
        }catch (IOException e) {System.out.println("Connection to server has died");}
    }

    public static void main(String args[]) {
        Source sauce;
        int i;
            switch (args.length) {
                case 0: 
                sauce = new Source("localhost", 7007);
                sauce.scan();
                case 3:
                switch (args[0]) {
                    case "-h":
                    i = Integer.parseInt(args[2]);
                    sauce = new Source(args[1],i);
                    sauce.scan();
                    break;
                    case "-m":
                    i = Integer.parseInt(args[2]);
                    sauce = new Source("localhost", 7007);
                    sauce.messageGenerator(args[1], i);
                    //sauce.scan();
                    break;
                    default:
                    break;
                }
                default:
                System.out.println("Invalid input");
                break;
            }
    }
}