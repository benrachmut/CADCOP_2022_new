package Messages;

import AgentsAbstract.NodeId;

public class MsgTokenTree extends MsgAlgorithm{
    public MsgTokenTree(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }
}
