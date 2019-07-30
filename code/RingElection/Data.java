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

class Data implements Serializable {
  int port;
  //Node data;
  String key;

  public Data(int port, String key) {
    this.port = port;
    //this.data = data;
    this.key = key;
  }

/* Maybe not needed!!!
  public void setSender(Node sender) {
    this.sender = sender;
  }
  public void setData(Node data) {
    this.data = data;
  }
  public void setKey(String key) {
    this.key = key;
  }
  */
  public int getPort() {
    return port;
  }

  public String getKey() {
    return key;
  }
}
