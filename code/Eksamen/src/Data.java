import java.io.*;

//simple data holder object that stores 'key' for instructions/logic, and port for communication
class Data implements Serializable {
  int port;
  String key;
  String extra;

  public Data(int port, String key) {
    this.port = port;
    this.key = key;
  }
  //overloading for the purpose of failure handling with send errors. Might be a little hacky.
  public Data(int port, String key, String extra) {
    this.port = port;
    this.key = key;
    this.extra = extra;
  }

  public int getPort() {
    return port;
  }

  public String getKey() {
    return key;
  }
  public String getExtraKey() {
    return extra;
  }
}
