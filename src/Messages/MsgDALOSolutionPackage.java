package Messages;

import AgentsAbstract.NodeId;

public class MsgDALOSolutionPackage extends MsgAlgorithm{

    public MsgDALOSolutionPackage(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
        super(sender, reciever, context, timeStamp, time);
    }


    @Override
    public String toString() {
        return this.getRecieverId()+" received SolutionPackage from "+this.getSenderId();
    }

}
