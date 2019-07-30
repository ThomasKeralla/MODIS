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

class SendError implements Runnable {
Object object;
NodeInfo myInfo;
String errorID;
Socket socket = null;

public SendError(Object object, NodeInfo myInfo, String errorID) {
this.object = object;
this.myInfo = myInfo;
this.errorID = errorID;
}

public SendError(Object object, NodeInfo myInfo) {
  this.object = object;
  this.myInfo = myInfo;

}

public void run() {
  try {
      if(errorID != null) {
        socket = new Socket(myInfo.ip, myInfo.port);
        System.out.println("Sending Death message to myself: " + myInfo.id);
        //OutputStream stream = socket.getOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(new Death(errorID));
        out.flush();
        out.close();
        socket.close();
      }
      
      TimeUnit.SECONDS.sleep(1);
      socket = new Socket(myInfo.ip, myInfo.port);
      ObjectOutputStream out2 = new ObjectOutputStream(socket.getOutputStream());
      out2.writeObject(object);
      out2.flush();
      out2.close();
      System.out.println("Send error-handling success ");
  } catch(IOException ex) {System.out.println("Something happened while handling SENDE-ERROR for message send by: "
                    + errorID + " handled by " + myInfo.id); ex.printStackTrace(); }

        catch(InterruptedException exn) {System.out.println("Sleep error");}
    finally {
      try {
        if(socket != null)
        socket.close();
      } catch (IOException e) {
          System.out.println("Something happened while closing Socket");
      }
    }

  }
}
