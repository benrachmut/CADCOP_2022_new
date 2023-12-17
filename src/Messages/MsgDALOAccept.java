package Messages;

import AgentsAbstract.NodeId;

public class MsgDALOAccept extends MsgAlgorithm {
    public MsgDALOAccept(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }

    @Override
    public String toString() {
        return this.getRecieverId()+" received ACCEPT from "+this.getSenderId();
    }
}
