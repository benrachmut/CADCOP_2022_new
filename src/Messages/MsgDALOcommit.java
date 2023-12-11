package Messages;

import AgentsAbstract.NodeId;

public class MsgDALOcommit extends MsgAlgorithm{
    public MsgDALOcommit(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }
}
