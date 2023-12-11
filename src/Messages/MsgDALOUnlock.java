package Messages;

import AgentsAbstract.NodeId;

public class MsgDALOUnlock extends MsgAlgorithm{
    public MsgDALOUnlock(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }
}
