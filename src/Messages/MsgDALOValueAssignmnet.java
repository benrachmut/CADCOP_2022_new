package Messages;

import AgentsAbstract.NodeId;

import java.util.Set;

public class MsgDALOValueAssignmnet extends MsgValueAssignmnet{
    private final Set<NodeId> nodesInTree;

    public MsgDALOValueAssignmnet(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, Set<NodeId> nodesInTree) {
        super(sender, reciever, context, timeStamp, time);
        this.nodesInTree = nodesInTree;
    }

    public Set<NodeId> getNodesInTree() {
        return nodesInTree;
    }

    @Override
    public String toString() {
        return this.getRecieverId()+" received DALO VALUE from "+this.getSenderId();
    }
}
