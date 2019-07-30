import java.util.Random;
import java.net.InetAddress;
import java.net.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.Collection;

class RingElection {
//Node[] nodes;

  public RingElection(int nodes) {
    //nodes = new Nodes[nodes];
    createNodes(nodes);
    //Have user start the election
  }

  public void createNodes(int numbers) {

    Random rd = new Random();
    int homeport = rd.nextInt(10)+500;
    int connect = rd.nextInt(10)+600;
    Node nodeNext = null;
    Node origNode = null;
    for(int i = 1; i <= numbers; i++) {
      Node newNode = new Node(i, homeport+i, connect+i);
      System.out.println("Node with id: "+ newNode.getId() + " Created..");

      if(origNode == null) {
        origNode = newNode;
      }
      if(i != numbers) {
        newNode.setNodeNext(nodeNext);
      }  else {
        newNode.setNodeNext(nodeNext);
        origNode.setNodeNext(newNode);
      }
      nodeNext = newNode;
      //nodes[i] = newNode;
    }
    //System.out.println(""+ origNode.getId());
    //System.out.println(""+origNode.getNext().getId());
    //System.out.println(""+origNode.getNext().getNext().getId());


    boolean running = true;
    Node check = origNode;
    while(running) {
      if(origNode.getNext() != null) {
        origNode.setNextNext(origNode.getNext().getNext());
        origNode = origNode.getNext();
      }
      else {// Handle This
      }
      if(origNode.getId() == check.getId()) {
        running = false;
      }
    }
    /*
    int count = 0;
    while(count < 10) {
      System.out.println(""+origNode.getNextNext().getId());
      //System.out.println(""+origNode.getNext().getId());
      count++;
      origNode = origNode.getNext();
    }
    */
    //System.out.println("Orig ID:"+origNode.getId()+" Next= "+origNode.getNext().getId()+ " Orig NextNext = "+origNode.getNextNext()+" Should be the same as: "+origNode.getNext().getNext());

  }

  public static void main (String[] args) {
    int numberOfNodes = Integer.parseInt(args[0]);
    RingElection r = new RingElection(numberOfNodes);

  }
}// End of RingElection


class Node implements Serializable {
private final int id;
private final int homePort;
private final int connectPort;
private final Socket socket = null;
private Node nodeBefore;
private Node nodeNext;
private Node nextNext;

private int leader;
private boolean participant;

  public Node(int id, int homePort, int connectPort) {
    this.id = id;
    this.homePort = homePort;
    this.connectPort = connectPort;
  }

  public int getId() {
    return id;
  }
  public void setNodeNext(Node node) {
    nodeNext = node;
  }
  public void setNextNext(Node node) {
    nextNext = node;
  }
  public Node getNext() {
    return nodeNext;
  }
  public Node getNextNext() {
    return nextNext;
  }

}

class election implements Serializable {}
