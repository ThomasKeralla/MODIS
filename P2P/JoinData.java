import java.io.Serializable;

public class JoinData implements Serializable {
    private static final long serialVersionUID = 1L;

    public RoutingTable joinTable;
    NodeInfo joinInfo;
}