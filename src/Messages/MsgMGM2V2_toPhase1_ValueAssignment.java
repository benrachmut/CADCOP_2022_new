package Messages;

import AgentsAbstract.NodeId;

public class MsgMGM2V2_toPhase1_ValueAssignment extends MsgValueAssignmnet{
    private int iteration;
    public MsgMGM2V2_toPhase1_ValueAssignment(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int iteration) {
        super(sender, reciever, context, timeStamp, time);
        this.iteration = iteration;
    }
    public int getIteration(){
        return this.iteration;
    }
}
