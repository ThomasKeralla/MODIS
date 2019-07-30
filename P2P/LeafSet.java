import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Function;

import java.io.Serializable;

//numerically right associated leafset in respect to the routing table
public class LeafSet implements Serializable {
    private static final long serialVersionUID = 1L;
    
    AtomicReferenceArray<NodeInfo> leafSet;
    transient RoutingTable myTable;
    transient volatile Boolean leafSetUpdated = false;

    LeafSet(int size, RoutingTable myTable) {
        leafSet = new AtomicReferenceArray(size);
        this.myTable = myTable;
    }

    public boolean betterLeaf(String id) {
        if(leafSet.get(3) == null) return true;
        if(myTable.calculateIndex(leafSet.get(3).id) > myTable.calculateIndex(id)) return false;
        return true;
    }

    public NodeInfo updateLeafSet(NodeInfo newNode){
        NodeInfo oldNode;
        int oldSpot;
        int newSpot = myTable.calculateIndex(newNode.id);
        int old,tits;
        int oldJColm, oldMColm;
        int newJColm = UUIDUtility.convertCharAt(newNode.id, newSpot);
        int newMColm = UUIDUtility.convertCharAt(myTable.myInfo.id, newSpot);

        for (int n = 0; n < 4; n++){
            if (leafSet.get(n) != null && leafSet.get(n).id.equals(newNode.id) ) return newNode;
        }

        for (int n = 0; n < 4; n++){
            if(leafSet.get(n) != null){
                oldNode = leafSet.get(n);
                oldSpot = myTable.calculateIndex(oldNode.id);
                oldJColm = UUIDUtility.convertCharAt(oldNode.id, oldSpot);
                oldMColm = UUIDUtility.convertCharAt(myTable.myInfo.id, oldSpot);
                //greater the index more in common
                if (oldSpot < newSpot) {
                    leafSet.getAndSet(n, newNode); //maybe we should use compareAndSet
                    newNode = oldNode;
                // same index check for closest right association
                } else if (oldSpot == newSpot) {
                    if (oldJColm < oldMColm) old = oldJColm - oldMColm + 16;
                    else old = oldJColm - oldMColm;

                    if (newJColm < newMColm) tits = newJColm - newMColm + 16;
                    else tits = newJColm - newMColm;
                    //the smaller the difference the closer the right association
                    if (old > tits) {
                        leafSet.getAndSet(n, newNode);
                        newNode = oldNode;
                    }
                }
                if (n == 3){
                   /* Unfriend unfriend = new Unfriend();
                    unfriend.id = myTable.myInfo.id;
                    send(unfriend, oldNode);*/
                    return oldNode;
                }
            //if there is an empty spot chug it in
            } else {
                leafSet.getAndSet(n, newNode);
                break;
            }
        }
        leafSetUpdated = true;
        System.out.println("leaf set got updated!!");
        return null;
    }
    public void iterateLeafSet(Consumer<NodeInfo> func){
        for (int n = 0; n < 4; n++){
            if (leafSet.get(n) != null){
                func.accept(leafSet.get(n));
            }
        }
    }

    public void sadLeafset(String neoDead){
        int i = 0;
        String dead = neoDead;
        for (int n = 0; n < 3; n++){
            if(leafSet.get(n+1) != null){
                i++;
                if(leafSet.get(n).id.equals(dead)){
                    leafSet.getAndSet(n, leafSet.get(n+1));
                    dead = leafSet.get(n+1).id;
                }
            } else 
                if(leafSet.get(n) != null && leafSet.get(n).id.equals(neoDead)){
                    leafSet.getAndSet(n, null);
                }
            }
            if(dead.equals(neoDead)){
                
                leafSet.set(i, null);
            }
            if(i == 3 && !dead.equals(neoDead)){
                leafSet.set(i, null);
            }

        if(i > 0 && leafSet.get(i) == null) fillLeafSet(myTable.calculateIndex(leafSet.get(i-1).id), i);
        else fillLeafSet(myTable.size - 1, i);
        myTable.printTable(); 
    }

    public void fillLeafSet(int index, int j) {
        int mylumn;
        int p;

        for(int n = index; n >= 0 ; n--){
            mylumn = UUIDUtility.convertCharAt(myTable.myInfo.id, n);
            for (int i = mylumn + 1; i <= mylumn + 17; i++){
                if (j > 3) {
                    leafSetUpdated = true;       
                    return;
                } 
                p = i%16;
                if(p == mylumn) break;
                if (myTable.table[n][p] != null && !(myTable.table[n][p].id.equals(myTable.myInfo.id))){
                    if(leafSet.get(j) == null) {
                        if(updateLeafSet(myTable.table[n][p]) == null)  j++;
                    }
                }
            }
        }
        leafSetUpdated =true;       
    }

    public NodeInfo[] copyLeafSet() {
        NodeInfo[] newLeafSet = new NodeInfo[leafSet.length()];
        for (int i = 0; i < leafSet.length(); i++) {
            newLeafSet[i] = leafSet.get(i);
        }
        return newLeafSet;  
    }

    public void printTable() {
        System.out.print(" LEAF SET YALL !: |" );
        for (int n = 0; n < 4; n++){
            String nid;
            if (leafSet.get(n) != null) nid = leafSet.get(n).id;
            else nid = "   x   ";
            System.out.print(nid + "|");
        }
        System.out.println();
    }
}