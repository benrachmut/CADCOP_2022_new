package Messages;

import AgentsAbstract.NodeId;

public class MsgMGM2V2_toPhase4_LR extends MsgAlgorithm{
    public MsgMGM2V2_toPhase4_LR(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }
}
