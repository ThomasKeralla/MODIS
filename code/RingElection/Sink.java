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

class Sink implements Serializable {

//int port;
//String key;

public static void main(String[] args) {
  if(args.length == 1) {
    String key = "print";
    int port = Integer.parseInt(args[0]);
    Send s = new Send("localHost", port ,new Data(port,key));
    Thread t = new Thread(s);
    t.start();

    System.out.println("Sending "+ key + " message to Node at port: "+ port);
  }
  else if(args.length == 2) {
    String key = args[1];
    int port = Integer.parseInt(args[0]);

      Send s2 = new Send("localHost", port ,new Data(port,key));
      Thread t2 = new Thread(s2);
      t2.start(); 
    System.out.println("Sending "+ key + " message to Node at port: "+ port);
  }
}
}
