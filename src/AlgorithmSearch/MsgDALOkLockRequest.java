package AlgorithmSearch;

import AgentsAbstract.NodeId;
import Messages.MsgAlgorithm;

public class MsgDALOkLockRequest extends MsgAlgorithm {
    private  Integer valToLock;
    private  boolean isPartner;

    public MsgDALOkLockRequest(NodeId sender, NodeId reciever, boolean isPartner, int timeStampCounter, long time, Integer valToLock) {
        super(sender, reciever, isPartner, timeStampCounter,time);
        this.valToLock = valToLock;
        this.isPartner = isPartner;
    }
    public Integer getValToLock(){
        return valToLock;
    }


}
