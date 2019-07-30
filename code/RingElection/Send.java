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

class Send implements Runnable {
Data data;
int port;
String ip;
Socket socket = null;

  public Send(String ip, int port, Data data){
      this.data = data;
      this.port = port;
      this.ip = ip;
  }

  public void run() {
    try{
        socket = new Socket(ip, port);
        System.out.println(data.getPort() + " Sending: " + data.getKey() + " to " + port);
        OutputStream out = socket.getOutputStream();
        ObjectOutputStream obout = new ObjectOutputStream(out);
        obout.writeObject(data);
        obout.flush();
        socket.close();
    } catch (IOException e) {System.out.println("Send error: " + e.getMessage() + ". Sender: " + data.getPort() + " PORT: " + port); //e.printStackTrace();
/* fix this!!!
          String errorID = nodeInfo.id;
          Thread t;
          if(errorID == null) {
            t = new Thread(new SendError(object, myInfo));
          }
          else {
            t = new Thread(new SendError(object, myInfo, errorID));
          }
          try {
            t.start();
            t.join();
          } catch(InterruptedException ex) {System.out.println("Thread problem");}
          */
        }
finally {
    try {
        if(socket != null)
        socket.close();
    } catch (IOException exn) {
        System.out.println("Something happened while closing Socket");
    }
  }
} // run closing


}//send closing
