import java.io.Serializable;

public class GetData implements DataMessage, Serializable {
    public String key;
    public int port,jumps;

    public String getKey() {
        return key;
    }
}