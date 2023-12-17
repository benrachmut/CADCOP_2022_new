package Messages;

import AgentsAbstract.NodeId;

public class MsgDALOkSpamToCloseTimer extends MsgAlgorithm{
    public MsgDALOkSpamToCloseTimer(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }
    @Override
    public String toString() {
        return this.getRecieverId()+" received SpamToCloseTimer";
    }
}
