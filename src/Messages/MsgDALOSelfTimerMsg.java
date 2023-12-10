package Messages;

import AgentsAbstract.NodeId;

public class MsgDALOSelfTimerMsg extends MsgAlgorithm{
    public MsgDALOSelfTimerMsg(NodeId sender, NodeId reciever, Object context, int timeStamp, long time,int randomTimeDeliver) {
        super(sender, reciever, context, timeStamp, time);
        this.setManualDelay(randomTimeDeliver);
    }
}
