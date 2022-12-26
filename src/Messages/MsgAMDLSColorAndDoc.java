package Messages;

import AgentsAbstract.NodeId;

public class MsgAMDLSColorAndDoc extends  MsgAMDLSColor{

    private double docId;
    public MsgAMDLSColorAndDoc(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int counter, Integer color,Double docId) {
        super(sender, reciever, context, timeStamp, time, counter, color);
        this.docId = docId;
    }

    public double getDocId() {
        return this.docId;
    }
}
