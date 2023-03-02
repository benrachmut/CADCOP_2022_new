package Messages;

import AgentsAbstract.NodeId;

public class MsgLAMDLS extends MsgAlgorithm{
    public MsgLAMDLS(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }
}
