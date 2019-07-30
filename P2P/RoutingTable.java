import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;

public class RoutingTable implements Serializable {
    private static final long serialVersionUID = 1L;
    NodeInfo[][] table;
    NodeInfo myInfo;
    int size;

    public RoutingTable(int size, NodeInfo myInfo) {
        this.size = size;
        table = new NodeInfo[size][16];
        this.myInfo = myInfo;

        for(int i = 0; i < myInfo.id.length(); i++) {
            int col = UUIDUtility.convertCharAt(myInfo.id, i);
            table[i][col] = myInfo;
        }
    }

    public boolean isMe(NodeInfo nodeInfo) {
        return nodeInfo.id.equals(myInfo.id);
    }

    public boolean addToTable(NodeInfo newInfo) {
        int index = calculateIndex(newInfo.id);
        int column = UUIDUtility.convertCharAt(newInfo.id, index);
        if(table[index][column] == null && !(newInfo.id.equals(myInfo.id))) {
            System.out.println("Adding " + newInfo.id + " to table");
            table[index][column] = newInfo;
            return true;
        }
        return false;
    }

    public void delFromTable(String id){
        int row = calculateIndex(id);
        int column = UUIDUtility.convertCharAt(id, row);
        if (table[row][column] != null && table[row][column].id.equals(id)){
            table[row][column] = null;
            System.out.println("Removed " + id + "from Routing table!");
        }
    }

    public void updateRoutingTable(RoutingTable from) {
        for (int n = 0; n < size; n++) {
            for (int i = 0; i < 16; i++) {
                if((table[n][i] == null && from.table[n][i] != null) && !from.table[n][i].id.equals(myInfo.id)) {
                    addToTable(from.table[n][i]);
                }
            }
        }
    }

    public int calculateIndex(String joinId) {
        String myId = myInfo.id;
        int n = 0;

        for (int i = 0; i < table.length; i++) {
            n = i;
            if(joinId.charAt(i) != myId.charAt(i)) break;
        }
        return n;
    }

    public void printTable() {
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("i/j |   0   |   1   |   2   |   3   |   4   |   5   |   6   |   7   |   8   |   9   |   a   |   b   |   c   |   d   |   e   |   f   |");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
        for (int n = 0; n <  table.length; n++){
            if (n < 10) System.out.print("  " + n + " |");
            else System.out.print(" " + n + " |");
            for (int i = 0; i < table[n].length; i++){
                String nid;
                if (table[n][i] != null) {
                    if (table[n][i].id.equals(myInfo.id)) nid = "   -   ";
                    else nid = calcPadding(table[n][i].id); //TODO: Make this dynamic. NEVER!
                }
                else nid = "   x   ";
                System.out.print(nid + "|");
            }
            System.out.println("");
        }
    }

    //Very hacky implementation
    private String calcPadding(String id) {
        String res = "";
        if(id.length() > 7) {
            return id.substring(0, 7);
        } else {
            int pad;
            if((id.length() % 2) == 0) {
                pad = (7-id.length()-1)/2;
                res = id.substring(0, id.length());
            } else {
                pad = (7-(id.length()))/2;
                res = id.substring(0, id.length());
            }
            for(int i = 0; i < pad; i++) {
                res = " " + res + " ";
            }
            return res;
        }
    }

    public void iterateTable(Consumer<NodeInfo> func){
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                if(table[i][j] != null && !(table[i][j].id.equals(myInfo.id))){
                    func.accept(table[i][j]);
                }
            }
        }
    }
}