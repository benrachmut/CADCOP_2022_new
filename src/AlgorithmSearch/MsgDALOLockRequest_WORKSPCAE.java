package AlgorithmSearch;

import AgentsAbstract.NodeId;
import Messages.MsgAlgorithm;

public class MsgDALOLockRequest_WORKSPCAE extends MsgAlgorithm {
    private  Integer valToLock;
    private  boolean isPartner;

    public MsgDALOLockRequest_WORKSPCAE(NodeId sender, NodeId reciever, boolean isPartner, int timeStampCounter, long time, Integer valToLock) {
        super(sender, reciever, isPartner, timeStampCounter,time);
        this.valToLock = valToLock;
        this.isPartner = isPartner;
    }
    public Integer getValToLock(){
        return valToLock;
    }


}
