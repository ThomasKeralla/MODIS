import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.*;
import java.util.Map.*;


public class StoredData implements Serializable{
    private static final long serialVersionUID = 1L;
    private HashMap<String, String> data;

    StoredData() {
        data = new HashMap<>();
    }
    public boolean empty(){
        return data.isEmpty();
    }

    public void addData(SaveData saveData) {
        data.put(saveData.key, saveData.value);
    }

    public void addAll(StoredData saveData){
        this.data.putAll(saveData.data);
    }

    public String getData(String key) {
        return data.get(key);
    }

    public Set<Entry<String,String>> getAll(){
        return data.entrySet();
    }

    public boolean hasData(String key) {
        return data.containsKey(key);
    }
}