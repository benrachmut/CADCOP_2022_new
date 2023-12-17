package Messages;

import AgentsAbstract.NodeId;

public class MsgDALOLocalViewUpdate extends MsgAlgorithm{
    public MsgDALOLocalViewUpdate(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }

    @Override
    public String toString() {
        return this.getRecieverId()+" received LocalViewUpdate from "+this.getSenderId();
    }
}
