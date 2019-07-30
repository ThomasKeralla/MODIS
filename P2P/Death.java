import java.io.Serializable;

public class Death implements Serializable{
    private static final long serialVersionUID = 1L;
    String id;
    public Death (String id) {
      this.id = id;
    }
}