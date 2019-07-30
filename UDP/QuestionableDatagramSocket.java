import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class QuestionableDatagramSocket extends DatagramSocket {
    DatagramPacket limbo = null;
    Random random = new Random(20);

    public QuestionableDatagramSocket () 
        throws SocketException {
        
    }
    protected QuestionableDatagramSocket(DatagramSocketImpl impl){
        super(impl);
    }
    public QuestionableDatagramSocket(SocketAddress bindaddr)
               throws SocketException {
                   super(bindaddr);
    }

    public QuestionableDatagramSocket(int port)
    throws SocketException{
        super(port);
        
    }
    public QuestionableDatagramSocket(int port,InetAddress laddr)
               throws SocketException {
                    super(port, laddr);
    }
               
    public void send(DatagramPacket p) throws IOException {
        int n = random.nextInt(4);

        switch (n) {
            case 0:   
            System.out.println("drop: " + new String(p.getData()));              
                break;
            case 1:
            System.out.println("dublicate: " + new String(p.getData()));              
                super.send(p);
                super.send(p);
                break;
            case 2:
            System.out.println("reorder: " + new String(p.getData()));              
                if (limbo != null) super.send(limbo);
                limbo = p;
                return;
            default:
            System.out.println("send: " + new String(p.getData()));     
                super.send(p);
        }
        if (limbo != null) {
            super.send(limbo);
            limbo = null;
        }
    }
}