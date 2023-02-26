package Messages;

import AgentsAbstract.NodeId;

public class MsgMGM2V2_toPhase1_ValueAssignment extends MsgValueAssignmnet{
    public MsgMGM2V2_toPhase1_ValueAssignment(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }
}
