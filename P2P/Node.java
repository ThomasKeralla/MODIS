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


import javafx.util.Pair;

public class Node implements Serializable{
    private static final long serialVersionUID = 1L;

    String id;
    RoutingTable routingTable;
    int port;
    NodeInfo myInfo;
    LeafSet leafSet;
    StoredData storedData;
    BackupData backupData;

    Node (int port){ //Might be obsolete?... You might be obsolete
        id = UUIDUtility.generateID();
        System.out.println(id);
        this.port = port;

        myInfo = new NodeInfo();
        myInfo.ip = "localhost";
        myInfo.id = id;
        myInfo.port = port;

        routingTable = new RoutingTable(id.length(), myInfo);
        routingTable.printTable();;
        run(port);
        backupData = new BackupData();

    }

    Node (int homePort, int connectPort){
        this(homePort, connectPort, UUIDUtility.generateID());
    }

    Node (int homePort, int connectPort, String id){
        this.id = id;
        System.out.println(id);

        this.port = homePort;

        myInfo = new NodeInfo();
        myInfo.ip = "localhost";
        myInfo.id = id;
        myInfo.port = port;
        backupData = new BackupData();

        //fills own id into routing table

        routingTable = new RoutingTable(id.length(), myInfo);
        leafSet = new LeafSet(4, routingTable);

        //asks to join the known address, this should be changed to ip for large-scale implementation
        join(connectPort);
        run(port);
    }

    private void printMyTables() {
        routingTable.printTable();
        leafSet.printTable();
    }

    public void send(Object object, NodeInfo nodeInfo){
        Object jobtec = object;
        Socket socket = null;

        try{
            socket = new Socket(nodeInfo.ip, nodeInfo.port);
            System.out.println(id + " Sending: " + object + " to " + nodeInfo.id);
            OutputStream out = socket.getOutputStream();
            ObjectOutputStream obout = new ObjectOutputStream(out);
            obout.writeObject(jobtec);
            obout.flush();
            socket.close();
        } catch (IOException e) {System.out.println("Send error: " + e.getMessage() + ". IP: " + nodeInfo.ip + " PORT: " + nodeInfo.port); //e.printStackTrace();
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


    public void join (int connectPort){
        System.out.println("joining...");
        JoinData joinData = new JoinData();
        joinData.joinInfo = myInfo;
        joinData.joinTable = routingTable;

        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.port = connectPort;
        nodeInfo.ip = "localhost";
        send((Object)joinData, nodeInfo);
    }

    public void addToTable(NodeInfo node, RoutingTable table) {
        if(table.addToTable(node)) {
            if (leafSet.betterLeaf(node.id)) {
                NodeInfo oldNode = leafSet.updateLeafSet(node);//can you even do this?
                if (oldNode != null){
                    Unfriend unfriend = new Unfriend();
                    unfriend.id = myInfo.id;
                    send(unfriend, oldNode);
                    if (!storedData.empty()){
                        Friend friend = new Friend();
                        friend.data = storedData;
                        friend.id = myInfo.id;
                        send(friend, node);
                    }
                }
            }
        }
    }
    public void iterateLeaf(LeafSet leafSet, Object sendObject){
        Consumer<NodeInfo> sendFunc = x -> send(sendObject, x);
        leafSet.iterateLeafSet(sendFunc);
    }

    public void iterateTableSend(RoutingTable table, Object sendObject) {
        Consumer<NodeInfo> sendFunc = x -> send(sendObject, x);
        table.iterateTable(sendFunc);
    }

    //sends welcome data to the Node that has now found it's place in the table
    public void destination(JoinData joinData){
        WelcomeData welcome = new WelcomeData();
        NodeInfo nodeInfo = joinData.joinInfo;
        welcome.joinTable = joinData.joinTable;
        System.out.println("WELCOME!");
        send(welcome, nodeInfo);
    }
    //these twwo are my favourites
    public void redirect(Object sendData, NodeInfo nodeInfo){
        send(sendData, nodeInfo);

    }

    public void informLower(NodeInfo addNode){
        iterateTableSend(routingTable, myInfo);
    }

    public void arrived(NodeInfo addNode, JoinData joinData) {
        addToTable(addNode, routingTable);
        destination(joinData);
        informLower(addNode);
    }

    public void joinRoutingTable(JoinData joinData, int path){
        int index = path;
        int column = UUIDUtility.convertCharAt(joinData.joinInfo.id, index);
        int mylumn = UUIDUtility.convertCharAt(myInfo.id, index);
        NodeInfo addNode = joinData.joinInfo;
        System.out.println("index: " + index + " | column: " + column + " = "+ column);

        joinData.joinTable.updateRoutingTable(routingTable);
        joinData.joinTable.addToTable(myInfo);//joinData.joinTable.table[index][mylumn] = myInfo;
        System.out.println("JOINTABLE FROM " + myInfo.id + " For " + addNode.id);
        joinData.joinTable.printTable();

        if (routingTable.table[index][column] == null){
            arrived(addNode, joinData);
            return;
        } else redirect(joinData, routingTable.table[index][column]);
    }

    private int calcCloseness(String key, String id) {
        int score = 0;
        int j = 1;
        for (int i = key.length() - 1; i >= 0; i--) {
            int kVal = UUIDUtility.convertChar(key.charAt(i));
            int idVal = UUIDUtility.convertChar(id.charAt(i));
            score += (Math.abs(kVal-idVal) * j);
            j *= 16;
        }
        return score;
    }

    public void dataRouting(DataMessage data, boolean get) {

        int index = routingTable.calculateIndex(data.getKey());
        int column = UUIDUtility.convertCharAt(data.getKey(), index);
        int mostSignificant = UUIDUtility.convertCharAt(data.getKey(), 0);
        int myMostSignificant =  UUIDUtility.convertCharAt(id, 0);
        int myLeastSignificant = UUIDUtility.convertCharAt(id, index);

        if (index == 0 || myMostSignificant == mostSignificant){
            if (routingTable.table[index][column] == null) {
                Integer closest = null;

                for (int i = 0; i < 16; i++) {
                    if(routingTable.table[index][i] != null) {
                        int closeness = Math.abs(column-i);
                        if(closest == null || Math.abs(closest-column) > closeness) closest = i;
                    }
                }

                if(closest == null || routingTable.isMe(routingTable.table[index][closest])) {
                    if(get) sendFoundData(data.getKey(), ((GetData)data).port, ((GetData)data).jumps);
                    else addNewData((SaveData)data);
                } else {
                    redirect(data, routingTable.table[index][closest]);
                }
            } else if(routingTable.isMe(routingTable.table[index][column])) {
                if(get) sendFoundData(data.getKey(), ((GetData)data).port, ((GetData)data).jumps);
                else addNewData((SaveData)data);
            } else redirect(data, routingTable.table[index][column]);
       //
        } else if (index > 0) {
            if (routingTable.table[index][column] == null) {
                Integer closest = null;
                for (int i = 0; i < 16; i++) {
                    if(routingTable.table[index][i] != null) {
                        int closeness = myLeastSignificant - i;
                        if(closest == null || (mostSignificant > myMostSignificant && closeness > 0) || (mostSignificant< myMostSignificant && closeness < 0)) closest = i;
                    }
                }

                if(closest == null || routingTable.isMe(routingTable.table[index][closest])) {
                    if(get) sendFoundData(data.getKey(), ((GetData)data).port, ((GetData)data).jumps);
                    else addNewData((SaveData)data);
                } else {
                    redirect(data, routingTable.table[index][closest]);
                }
            } else if(routingTable.isMe(routingTable.table[index][column])) {
                if(get) sendFoundData(data.getKey(), ((GetData)data).port, ((GetData)data).jumps);
                else addNewData((SaveData)data);
            } else redirect(data, routingTable.table[index][column]);

        }
    }

    public void sendFoundData(String key, int port, int jumps) {
        SaveData put = new SaveData();
        if(!storedData.hasData(key)) {
            put.key = null;
            put.value = null;
            put.jumps = jumps;
        } else {
            put.key = key;
            put.value = storedData.getData(key);
            put.jumps = jumps;
        }
        send(put, "localhost", port);
    }

    public void addNewData(SaveData data) {
        System.out.println("Got data: " + data.value + " with key: " + data.key);
        System.out.println("In only " + data.jumps + " jumps!");
        storedData.addData(data);
        Friend friend = new Friend();
        StoredData dataHolder = new StoredData();
        dataHolder.addData(data);
        friend.data = dataHolder;
        friend.id = myInfo.id;
        iterateLeaf(leafSet, friend);
    }

    public void greatings(WelcomeData welcome){
        routingTable = welcome.joinTable;
        leafSet.fillLeafSet(routingTable.size -1, 0);
        printMyTables();

        iterateTableSend(routingTable, myInfo);
        printMyTables();
    }

    public void mourning(KingSlayer kingSlayer){
        iterateTableSend(routingTable, kingSlayer);
        if(backupData.backupData.containsKey(kingSlayer.dead)){
            StoredData tempData = backupData.backupData.get(kingSlayer.dead);
            backupData.backupData.remove(kingSlayer.dead);


            for (Map.Entry<String,String> entry: tempData.getAll()){
                SaveData saveData = new SaveData();
                saveData.key = entry.getKey();
                saveData.value = entry.getValue();

                send(saveData, leafSet.leafSet.get(0));

                System.out.println("resending data");
            }
        }
    }

    public void run (int port){
        ServerSocket suckit = null;

        try {
            System.out.println("Node ready");
            Socket newClient;
            InputStream is;
            ObjectInputStream ois;
            Object ob;
            suckit = new ServerSocket(port);
            Thread t1 = new Thread(new Pinging(myInfo, leafSet));
            t1.start();
            while(true) {
                newClient = suckit.accept();
                is = newClient.getInputStream();
                // node waits for new connection
                // and waits for message about which type has connected
                ois = new ObjectInputStream(is);
                ob =  ois.readObject();
           //     System.out.println("CLASS: " + ob.getClass().toString().replace("class", "").trim());

                String className = ob.getClass().toString().replace("class", "").trim();

                switch(className) {
                    case "JoinData":
                        newClient.close();
                        JoinData jo = (JoinData) ob;
                        System.out.println("Received Join request");
                        System.out.println("JOINDATA PORT: " + jo.joinInfo.port);
                        joinRoutingTable(jo, routingTable.calculateIndex(jo.joinInfo.id));
                        printMyTables();
                        break;
                    case "NodeInfo":
                        newClient.close();
                        System.out.println("Received NodeInfo");
                        NodeInfo inf = (NodeInfo) ob;
                        System.out.println("NODEINFO PORT: " + inf.port);
                        int spot = routingTable.calculateIndex(inf.id);
                        //is this the right one?
                        int nylumn = UUIDUtility.convertCharAt(inf.id, spot);
                        if (spot == 0 && routingTable.table[0][nylumn] == null){
                            System.out.println("got new neighbour");
                            informLower(inf);
                        }
                        addToTable(inf, routingTable);
                        System.out.println("Table belonging to " + id);
                        printMyTables();
                        break;
                    case "WelcomeData":
                        newClient.close();
                        System.out.println("Received welcome");
                        WelcomeData wel = (WelcomeData) ob;
                        greatings(wel);
                        System.out.println("Table belonging to " + id);
                        printMyTables();
                        break;
                    case "Post":
                        System.out.println("oooh getting put data");
                        break;
                    case "Ping":
                        Ping pi = (Ping) ob;
                        System.out.println("got pinged by " + pi.id);
                        break;
                    case "Death":
                        //notify about death to all in routing table with same death Object
                        //update leafSet
                        Death death = (Death) ob;
                        KingSlayer kingSlayerD = new KingSlayer();
                        System.out.println("Node " + death.id+ " died");
                        leafSet.sadLeafset(death.id);
                        routingTable.delFromTable(death.id);
                        kingSlayerD.nodeInfo = myInfo;
                        kingSlayerD.dead = death.id;
                        mourning(kingSlayerD);
                        printMyTables();
                        break;
                    case "KingSlayer":
                        KingSlayer kingSlayer = (KingSlayer) ob;
                        routingTable.delFromTable(kingSlayer.dead);
                        leafSet.sadLeafset(kingSlayer.dead);
                        if (routingTable.addToTable(kingSlayer.nodeInfo))  mourning(kingSlayer);
                        printMyTables();
                        break;
                    case "SaveData":
                        SaveData saveData = (SaveData) ob;
                        saveData.jumps++;
                        dataRouting(saveData, false);
                        break;
                    case "GetData":
                        GetData getData = (GetData) ob;
                        getData.jumps++;
                        dataRouting(getData, true);
                        break;
                    case "Friend":
                        Friend friend = (Friend) ob;
                        System.out.println("Friend class recieved");

                        if (backupData.backupData.get(friend.id) != null){
                            StoredData std = backupData.backupData.get(friend.id);
                            std.addAll(friend.data);
                            backupData.backupData.put(friend.id, std);
                            System.out.println("updated backup data for: "+ friend.id);

                        } else {
                            backupData.backupData.putIfAbsent(friend.id, friend.data);
                            System.out.println("holding data for: " + friend.id);
                        }
                        break;
                    case "Unfriend":
                        Unfriend unfriend = (Unfriend) ob;
                        if (backupData.backupData.get(unfriend.id) != null){
                            backupData.backupData.remove(unfriend.id);
                            System.out.println("dropped backup data for: "+ unfriend.id);

                        }
                        System.out.println("Unfriend class recieved");


                        break;
                    default:
                        System.out.println("Unknown class recieved");
                        newClient.close();
                }
            }
        } catch (IOException e ) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Monkey balls: " + e.getMessage());
        } finally {
            try {
                if(suckit != null) suckit.close();
            } catch (IOException e) {
                System.out.println("Something happened while closing server");
            }
        }
    }

    public static void main (String[] args){
        if (args.length == 0) new Node(7007);
        else if (args.length == 2) {
            int homePort = Integer.parseInt(args[0]);
            int ConnectPort = Integer.parseInt(args[1]);
            new Node(homePort, ConnectPort);
        } else {
            int homePort = Integer.parseInt(args[0]);
            int ConnectPort = Integer.parseInt(args[1]);
            String id = args[2];
            new Node(homePort, ConnectPort, id);
        }
    }
}
