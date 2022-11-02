package Messages;

import AgentsAbstract.NodeId;

public class MsgMDC2CFriendReply  extends MsgAMDLSColor {
    public MsgMDC2CFriendReply(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int counter, Integer color) {
        super(sender, reciever, context, timeStamp, time, counter, color);
    }
}