package Messages;

import AgentsAbstract.NodeId;

public class MsgDALOSelfTimer extends MsgAlgorithm{
    public MsgDALOSelfTimer(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int randomTimeDeliver) {
        super(sender, reciever, context, timeStamp, time);
        this.setManualDelay(randomTimeDeliver);
    }

    @Override
    public String toString() {
        return this.getRecieverId()+" received SelfTimerMsg";
    }
}
