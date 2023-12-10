package AlgorithmSearch;

import AgentsAbstract.NodeId;
import Messages.MsgAlgorithm;

public class MsgDALOkLockRequest extends MsgAlgorithm {
    private  boolean isPartner;

    public MsgDALOkLockRequest(NodeId sender, NodeId reciever, boolean isPartner, int timeStampCounter, long time) {
        super(sender, reciever, isPartner, timeStampCounter,time);
        this.isPartner = isPartner;
    }


}
