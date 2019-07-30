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

class Client implements Serializable {

public static void main(String[] args) {
  if(args.length == 1) {
    String key = "print";
    int port = Integer.parseInt(args[0]);
    Send s = new Send("localHost", port , 0, new Data(port,key));
    Thread t = new Thread(s);
    t.start();

  }
  else if(args.length == 2) {

    int homePort = Integer.parseInt(args[0]);
    int port = Integer.parseInt(args[1]);
    run(port, homePort);
  }
  else {System.out.println("You must give a homeport and connectport only");}
}



public static void run(int port, int homePort) {


Thread listner = new Thread(()-> {
  try {
      final ServerSocket sock = new ServerSocket(homePort);
      System.out.println("Client listning at port: "+homePort);

      while(true) {
          Socket client = sock.accept();
          InputStream input = client.getInputStream();
          ObjectInputStream objectInput = new ObjectInputStream(input);
          Data recieved = (Data) objectInput.readObject();
          //What type of message, and how to handle it in the switch
          String decisionKey = (String) recieved.getKey();
          if(decisionKey.equals("kill")) {
            System.out.println("killed");
            break;
          }  
          else {
            System.out.println(""+recieved.getPort()+ " "+decisionKey);
            }
          }
        } catch (IOException e ) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Exception caught: " + e.getMessage());
        }

});
listner.start();

        Scanner msgScan = new Scanner(System.in);

        System.out.println("Connected: Write commands.. Quit for terminating");
        while (true) {
            String input = msgScan.nextLine();
            if(input.equals("Quit")) {
              Thread kill = new Thread(new Send("localhost", homePort, homePort, new Data(homePort, "kill")));
              kill.start();
              break;
            } else {
              Thread t = new Thread(new Send("localhost", port, homePort, new Data(homePort, input)));
              t.start();
      }
    }
  }
}
