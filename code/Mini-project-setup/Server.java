import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Server {

    public List<Socket> sinks;
    Executor serverThreadHandler;
    ReentrantReadWriteLock lock;
    ReentrantReadWriteLock.ReadLock readLock;
    ReentrantReadWriteLock.WriteLock writeLock;

    /**
     * This class listen
     */
    private class Listener implements Runnable {
        Socket client;

        Listener(Socket client) {
            this.client = client;
        }

        public void run() {
            try {
                BufferedReader inputStream =  new BufferedReader(new InputStreamReader(client.getInputStream()));
                while (client.isConnected()) {
                    String input = inputStream.readLine();
                    System.out.println("Server received: " + input);
                    serverThreadHandler.execute(new SendMessage(input.trim()));
                }
                inputStream.close();
            } catch (IOException e) {
                System.out.println("A Source connection died");
            }
        }
    }

    /**
     * 
     */
    private class SendMessage implements Runnable {
        String msg;

        SendMessage(String msg) {
            this.msg = msg;
        }

        public void run() {
            List<Socket> clients = getClientList();
            for (Socket sink : clients) {
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sink.getOutputStream()));
                    writer.write(msg + '\n');
                    writer.flush();
                } catch (IOException e) {
                    System.out.println("Sink appears dead");
                    updateClientList(sink, true);
                }
            }
        }
    }

    private void updateClientList(Socket client, boolean remove) {
        writeLock.lock();
        try {
            if(remove) sinks.remove(client);
            else sinks.add(client);
        } finally {
            writeLock.unlock();
        }
    }

    private List<Socket> getClientList() {
        readLock.lock();
        try{
            return new ArrayList<>(sinks);
        } finally {
            readLock.unlock();
        }
    }

    Server() {
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();

        sinks = new ArrayList<>();
        serverThreadHandler = Executors.newCachedThreadPool();
        ServerSocket server = null;

        try {
            server = new ServerSocket(7007);

            System.out.println("Server ready");
            while(true) {
                Socket newClient = server.accept();
                BufferedReader inputStream =  new BufferedReader(new InputStreamReader(newClient.getInputStream()));
                String type = inputStream.readLine().trim();
                if(type.equals("sink")) {
                    System.out.println("Server Received sink connection");
                    updateClientList(newClient, false);
                } else if(type.equals("source")) {
                    System.out.println("Server Received source connection");
                    serverThreadHandler.execute(new Listener(newClient));
                } else {
                    newClient.close();
                }
            }
        } catch (IOException e ) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if(server != null) server.close();
            } catch (IOException e) {
                System.out.println("Something happened while closing server");
            }
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}