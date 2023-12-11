package Messages;

import AgentsAbstract.NodeId;

public class MsgDALOReject extends MsgAlgorithm{
    public MsgDALOReject(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }
}
