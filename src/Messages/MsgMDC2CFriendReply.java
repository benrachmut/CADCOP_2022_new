package Messages;

import AgentsAbstract.NodeId;

public class MsgMDC2CFriendReply  extends MsgAMDLSColorAndDoc {
    public MsgMDC2CFriendReply(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int counter, Integer color,double docId) {
        super(sender, reciever, context, timeStamp, time, counter, color,docId);
    }
}