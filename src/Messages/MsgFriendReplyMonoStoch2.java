package Messages;

import AgentsAbstract.NodeId;

import java.util.Map;

public class MsgFriendReplyMonoStoch2 extends MsgAlgorithm{



    public MsgFriendReplyMonoStoch2(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }
}
