package Messages;

import AgentsAbstract.NodeId;

public class MsgPT2opt extends MsgAlgorithm{
    public MsgPT2opt(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }
}
