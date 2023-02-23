package AlgorithmSearch;

import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Messages.MsgAlgorithm;

import java.util.Map;
import java.util.Random;

public class MGM2_SY_V2 extends AgentVariableSearch {
    enum Status {
        idle,
        phase1_initiateOfferPartner1,
        phase2_acceptOfferPartner2,
        phase3_broadCastLReductionPartner1,
        phase4_receiveLReductionsPartner12,
        phase5_coordinatePartners;
    }

    private boolean committed;
    private Random partnerSelection;
    private double offerProb;

    private Map<NodeId,Integer> phase1ValueAssignments;

    public MGM2_SY_V2(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        resetAgentGivenParametersV3()


    }


    @Override
    protected void resetAgentGivenParametersV3() {
        partnerSelection = new Random((dcopId + 1) * 17 + this.nodeId.getId1() * 177);
        committed = false;
        offerProb = 0.5;
        this.myStatus = Status.phase1_initiateOfferPartner1;
    }

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        return false;
    }

    @Override
    public boolean getDidComputeInThisIteration() {
        return false;
    }

    @Override
    public boolean compute() {
        return false;
    }

    @Override
    public void sendMsgs() {

    }

    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

    }

    @Override
    public void changeReceiveFlagsToFalse() {

    }

    @Override
    public void updateAlgorithmHeader() {

    }

    @Override
    public void updateAlgorithmData() {

    }

    @Override
    public void updateAlgorithmName() {

    }


}
