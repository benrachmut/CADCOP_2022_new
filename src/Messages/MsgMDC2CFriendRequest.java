package Messages;

import AgentsAbstract.NodeId;

public class MsgMDC2CFriendRequest extends MsgAMDLSColor{
    public MsgMDC2CFriendRequest(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int counter, Integer color) {
        super(sender, reciever, context, timeStamp, time, counter, color);
    }
}
