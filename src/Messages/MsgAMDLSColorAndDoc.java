package Messages;

import AgentsAbstract.NodeId;

public class MsgAMDLSColorAndDoc extends  MsgAMDLSColor{
    private boolean isReply;
    private double docId;

    public MsgAMDLSColorAndDoc(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int counter, Integer color,Double docId) {
        super(sender, reciever, context, timeStamp, time, counter, color);
        this.docId = docId;
        this.isReply = false;
    }

    public double getDocId() {
        return this.docId;
    }
    public boolean getIsReply(){
        return this.isReply;
    }

    public void changeAbleToReply() {
        this.isReply = true;
    }
}
