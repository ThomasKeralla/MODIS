import java.net.InetAddress;
import java.net.*;
import java.net.Socket;
import java.util.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Pinging implements Runnable {

    Socket socket = null;
    private int leader;
    private int port;
    private String ip;
    private int homePort;


    public Pinging(String ip, int leader, int pingPort, int homePort) {
    this.leader = leader;
    this.port = pingPort;
    this.ip = ip;
    this.homePort = homePort;
    }

    public void run() {
        while(true) {

                //pings the head (port)
                try{
                    socket = new Socket(ip, port);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(new Data(homePort, "ping"));
                    out.flush();
                    //pings every second
                    TimeUnit.SECONDS.sleep(1);

                //if one of them dies it sends a death object to the Node
              } catch (IOException  e) {System.out.println("Node at port: " + port + " seems dead " +e.getMessage());
                    try {
                        if(socket != null)
                        socket.close();
                        //Socket set to either homePort or leader depending on if it the leader that has died
                        //if not the leader sent to leader so 'death' message can be propagated through the system
                        //if the leader died the note is new leader and anoints itself.
                        socket = (port == leader) ? new Socket(ip, homePort) : new Socket(ip, leader);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        //Likewise the decisionKey is set to different values depending on whether it is a 'regular'
                        //death or a leader
                        Data message = (port == leader) ? new Data(port, "youLead") : new Data(port, "death");
                        out.writeObject(message);
                        out.flush();
                        socket.close();
                        break;

                    } catch (IOException ex) {
                        System.out.println("Node dead: "+port + " Death message not send " +ex.getMessage());
                        break;
                    }
                }  catch(InterruptedException e) {System.out.println(e.getMessage());
                } finally {
                    try {
                        if(socket != null)
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Something happened while closing Socket");
                    }
                }
            }
        }
    }
