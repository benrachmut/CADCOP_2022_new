package Messages;

import AgentsAbstract.NodeId;

public class MsgDALOkInfo extends MsgAlgorithm {

    private int selfCounter;

    public MsgDALOkInfo(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }
}

