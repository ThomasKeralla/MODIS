import java.net.InetAddress;
import java.net.*;
import java.net.Socket;
import java.util.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Pinging implements Runnable {
    NodeInfo[] copietLeafset;
    Socket socket = null;
    NodeInfo myInfo;
    LeafSet leafSet;
/**
* Here comes the internal Pinging class
* This one will be running on a thread alongside the run loop of the Node
* Pinging the nodes in the leafset and informing the Node in case one of them dies.
*/
    public Pinging(NodeInfo myInfo, LeafSet leafSet) {
    copietLeafset = leafSet.copyLeafSet();
    this.myInfo = myInfo;
    this.leafSet = leafSet;
    }

    public void run() {
        while(true) {
            if(leafSet.leafSetUpdated) {
                updateCopiedleafSet();
                System.out.println("pingleafset got updated too");
                leafSet.leafSetUpdated = false;// figure out how to manage this!!!
            }

            //cycles the leafset..
            for(int i = 0; i<copietLeafset.length; i++) {
                if(copietLeafset[i] == null) continue;

                                
                Ping ping = new Ping();
                ping.id = myInfo.id;

                //pings the leafset
                try{
                    socket = new Socket(copietLeafset[i].ip, copietLeafset[i].port);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(ping);
                    out.flush();
                    TimeUnit.SECONDS.sleep(2);

                //if one of them dies it sends a death object to the Node
                } catch (IOException  e) {System.out.println("Node " + copietLeafset[i].id + " seems dead " +e.getMessage());
                    try {
                        if(socket != null)
                        socket.close();
                        socket = new Socket(myInfo.ip, myInfo.port);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(new Death(copietLeafset[i].id));
                        out.flush();
                        //removes the node from the copietleafset so as to avoid multiple death messages just in case
                        copietLeafset[i] = null;
                        TimeUnit.SECONDS.sleep(1);
                        //updateCopiedleafSet();
                    } catch (IOException ex) {
                        System.out.println("Node dead: "+ copietLeafset[i].id + " Death message not send " +ex.getMessage());
                    } catch(InterruptedException ie) {}
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

    private void updateCopiedleafSet() {
        NodeInfo[] updatedLeafSet = leafSet.copyLeafSet();
        if(!copietLeafset.equals(updatedLeafSet)) {
            copietLeafset = updatedLeafSet;
            System.out.println("copietLeafset updated");
        } else {
            try{
                TimeUnit.SECONDS.sleep(1);
            } catch(InterruptedException e) {System.out.println(e.getMessage());}
            updateCopiedleafSet();
        }
    }
}