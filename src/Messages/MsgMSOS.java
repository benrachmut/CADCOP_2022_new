package Messages;

import AgentsAbstract.NodeId;


public class MsgMSOS extends MsgAlgorithm{

    private double docId;
    private int counter;

    public MsgMSOS(NodeId sender, NodeId reciever, Object context, int timeStamp, long time,double docId, int counter) {
        super(sender, reciever, context, timeStamp, time);
        this.docId = docId;
        this.counter = counter;
    }

    public int getCounter(){
        return this.counter;
    }

    public double getDocId(){
        return this.docId;
    }
}
