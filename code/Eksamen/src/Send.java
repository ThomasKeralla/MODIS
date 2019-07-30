
import java.net.*;
import java.net.Socket;
import java.util.*;
import java.io.*;


class Send implements Runnable {
//Data being sent
Data data;
//Where it is being sent to
int port;
//Where it is being sent from
int homePort;
String ip;
Socket socket = null;

  public Send(String ip, int port, int homePort, Data data){
      this.data = data;
      this.port = port;
      this.ip = ip;
      this.homePort = homePort;
  }


  public void run() {
    try{
      //initialises Socket, creates and ObjectOutputStream and sents Data.
        socket = new Socket(ip, port);
        //System.out.println(data.getPort() + " Sending: " + data.getKey() + " to " + port);
        OutputStream out = socket.getOutputStream();
        ObjectOutputStream obout = new ObjectOutputStream(out);
        obout.writeObject(data);
        obout.flush();
        socket.close();
    } catch (IOException e) {System.out.println("Send error: " + e.getMessage() + ". Sender: " + data.getPort() + " PORT: " + port); //e.printStackTrace();
    try {
        if(socket != null)
        socket.close();
        //handles sendError by recursively calling the Send method and using Data's overloaded constructor
        Thread recoveryThread = new Thread(new Send(ip, homePort, port, new Data(data.getPort(), "sendError", data.getKey())));
        recoveryThread.start();

    } catch (IOException ex) {
        System.out.println("Node dead: "+port + " Death message not send " +ex.getMessage());

     }
        }
finally {
    try {
        if(socket != null)
        socket.close();
    } catch (IOException exn) {
        System.out.println("Something happened while closing Socket");
    }
   }
  }
}
