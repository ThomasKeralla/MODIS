import java.io.Serializable;

public class SaveData implements DataMessage, Serializable {
    String key;
    String value;
    int jumps;

    public String getKey() {
        return key;
    }
}