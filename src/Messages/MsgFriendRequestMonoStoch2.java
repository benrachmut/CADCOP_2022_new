package Messages;

import AgentsAbstract.NodeId;

public class MsgFriendRequestMonoStoch2 extends MsgAlgorithm {

    private int selfCounter;
    public MsgFriendRequestMonoStoch2(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int selfCounter) {
        super(sender, reciever, context, timeStamp, time);
        this.selfCounter = selfCounter;
    }

    public int getSelfCounter(){
        return this.selfCounter;
    }
}