package AlgorithmSearch;

import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Messages.*;

import java.util.*;

public class MGM2_SY_V2 extends AgentVariableSearch {


    private double offerProb = 0.5;
    private boolean isPartnerTakeBest = true;

    enum Status {
        idle_451_waitForValueAssignments,
        idle_12_waitForOffers,
        idle_13_waitForReply,
        idle_234_waitForLocalReductionFromAll,
        idle_45_waitForLocalReductionFromPartner,
        phase1_initiateOfferPartner1,
        phase2_acceptOfferPartner2,
        phase3_broadCastLReductionPartner1,
        phase4_receiveLReductionsPartner12,
        phase5_coordinatePartners;
    }

    private Status myStatus;

    private Map<NodeId, Integer> toPhase1_valueAssignmentsMsgs;
    private Map<NodeId, Boolean> toPhase2_boolean;
    private Map<NodeId, KOptInfo> toPhase2_info;


    private int candidateValueAssignment;
    private int lr;
    private NodeId partner;

    private Random randomIsOfferToPartner;
    private Random randomPartnerSelection;


    public MGM2_SY_V2(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        resetAgentGivenParametersV3();
    }


    @Override
    protected void resetAgentGivenParametersV3() {

        partner = null;


        this.myStatus = Status.idle_451_waitForValueAssignments;
        randomIsOfferToPartner = new Random((dcopId + 1) * 17 + this.nodeId.getId1() * 177);
        randomPartnerSelection = new Random((dcopId + 1) * 32452317 + this.nodeId.getId1() * 6575678);
        toPhase1_valueAssignmentsMsgs = new HashMap<NodeId,Integer>();
        toPhase2_boolean = new HashMap<NodeId,Boolean>();
        toPhase2_info= new HashMap<NodeId,KOptInfo>();
        candidateValueAssignment = -1;
        lr = Integer.MAX_VALUE;

        for (NodeId nodeId:this.neighborsConstraint.keySet()) {
            toPhase1_valueAssignmentsMsgs.put(nodeId,null);
            //toPhase2_info.put(nodeId,null);
            toPhase2_boolean.put(nodeId,null);
        }
    }

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgValueAssignmnet){
            MsgValueAssignmnet msg = (MsgValueAssignmnet)msgAlgorithm;
            recieveValueAssignment(msg);
        }
        if ( msgAlgorithm instanceof MsgMGM2V2_toPhase2_infoAndFalse){
            toPhase2_boolean.put(msgAlgorithm.getSenderId(),true);

            Object context  = msgAlgorithm.getContext();
            if (context instanceof KOptInfo){
                toPhase2_info.put(msgAlgorithm.getSenderId(), (KOptInfo) context);
            }
        }
        return true;
    }

    private void recieveValueAssignment(MsgValueAssignmnet msg) {
        Integer va = (int)msg.getContext();
        this.toPhase1_valueAssignmentsMsgs.put(msg.getSenderId(),va);
        this.updateMsgInContextValueAssignment(msg);
    }

    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
        if (this.myStatus == Status.idle_451_waitForValueAssignments && gotAllMsgs(this.toPhase1_valueAssignmentsMsgs.values())){
            this.myStatus = Status.phase1_initiateOfferPartner1;
        }
        if (this.myStatus == Status.idle_12_waitForOffers && gotAllMsgs(this.toPhase2_boolean.values())){
            this.myStatus = Status.phase2_acceptOfferPartner2;
        }

        if (this.myStatus == Status.idle_13_waitForReply&& false){
            this.myStatus = Status.phase3_broadCastLReductionPartner1;
        }
        if (this.myStatus == Status.idle_234_waitForLocalReductionFromAll && false){
            this.myStatus = Status.phase4_receiveLReductionsPartner12;
        }
        if (this.myStatus == Status.idle_45_waitForLocalReductionFromPartner && false){
            this.myStatus = Status.phase5_coordinatePartners;
        }



    }

    private static boolean gotAllMsgs(Collection<?> info) {
        for (Object o:info) {
            if (o == null){
                return false;
            }
        }
        return true;


    }


    @Override
    public boolean getDidComputeInThisIteration() {
        return this.myStatus == Status.phase1_initiateOfferPartner1 ||
                this.myStatus == Status.phase2_acceptOfferPartner2 ||
                this.myStatus == Status.phase3_broadCastLReductionPartner1||
                this.myStatus == Status.phase4_receiveLReductionsPartner12||
                this.myStatus == Status.phase5_coordinatePartners;
    }

    @Override
    public boolean compute() {
        if ( this.myStatus == Status.phase1_initiateOfferPartner1){
            checkIfOffers();
            computeMyLR();
        }
        if (this.myStatus == Status.phase2_acceptOfferPartner2 && this.partner==null && !this.toPhase2_info.isEmpty()){

            if (isPartnerTakeBest){
                Map<NodeId,Find2Opt> twoOpts = getTwoOpts();
                for (Find2Opt singleTwoOpt: twoOpts.values()) {
                    this.atomicActionCounter = this.atomicActionCounter + singleTwoOpt.getAtomicActionCounter();
                }
                Map.Entry<NodeId,Find2Opt> bestPartner = getLocalReductions(twoOpts);
            }else{
                this.partner = getRandomPartnerFromInfo();
            }
        }
        return true;
    }

    private Map.Entry<NodeId,Integer> getLocalReductions(Map<NodeId, Find2Opt> twoOpts) {
        int minLr = Integer.MIN_VALUE;
        Map.Entry<NodeId,Integer> ans = null;
        for (Map.Entry<NodeId,Find2Opt> f2o:
             twoOpts.entrySet()) {
            stop here
        }


    }

    private Map<NodeId, Find2Opt> getTwoOpts() {
        Map<NodeId, Find2Opt> ans = new HashMap<NodeId, Find2Opt>();
        for (NodeId nodeId:this.toPhase2_info.keySet()) {
            KOptInfo kOptInfo = this.toPhase2_info.get(nodeId);
            Find2Opt f2o = new Find2Opt(makeMyKOptInfo(),kOptInfo);
            ans.put(nodeId,f2o);
        }
        return ans;
    }


    private NodeId getRandomPartnerFromInfo() {
        Collection<NodeId>neighbors = this.toPhase2_info.keySet();
        int i = this.randomPartnerSelection.nextInt(neighbors.size());
        return (NodeId) neighbors.toArray()[i];
    }

    private void checkIfOffers() {
        double p = this.randomIsOfferToPartner.nextDouble();
        if (p<this.offerProb){
            this.partner = getRandomPartner();
        }
    }



    private NodeId getRandomPartner() {
        Collection<NodeId>neighbors = this.neighborsConstraint.keySet();
        int i = this.randomPartnerSelection.nextInt(neighbors.size());
        return (NodeId) neighbors.toArray()[i];
    }


    private boolean computeMyLR() {
        int candidate = getCandidateToChange_C();
        if (candidate != this.valueAssignment) {
            this.candidateValueAssignment = candidate;
            int lrToCheck = findLr(candidate);
            return changeLr(lrToCheck);
        }else {
            return changeLrToZero();
        }
    }


    private boolean changeLrToZero() {
        if (lr == 0) {
            return false;
        } else {
            this.lr = 0;
            return true;
        }
    }

    private boolean changeLr(int lrToCheck) {
        if (lrToCheck < 0) {
            throw new RuntimeException();
        }
        if (this.lr != lrToCheck) {
            this.lr = lrToCheck;
            return true;
        } else {
            return false;
        }
    }
    @Override
    public void sendMsgs() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();

        if (this.myStatus == Status.phase1_initiateOfferPartner1) {
            sendMsgsPhaseToPhase2(msgsToInsertMsgBox);
        }


        if (!msgsToInsertMsgBox.isEmpty()) {
            outbox.insert(msgsToInsertMsgBox);
        }
    }

    private void sendMsgsPhaseToPhase2(List<Msg> msgsToInsertMsgBox) {
        for (NodeId receiverNodeId:this.neighborsConstraint.keySet()) {
            MsgMGM2V2_toPhase2_infoAndFalse msg = null;
            if (this.partner != null && this.partner.equals(receiverNodeId)){
                 msg = new MsgMGM2V2_toPhase2_infoAndFalse
                        (this.nodeId, receiverNodeId, makeMyKOptInfo(), this.timeStampCounter, this.time);
            }else{
                 msg = new MsgMGM2V2_toPhase2_infoAndFalse
                        (this.nodeId, receiverNodeId, false, this.timeStampCounter, this.time);
            }
            msgsToInsertMsgBox.add(msg);

        }

    }



    private KOptInfo makeMyKOptInfo() {
        return new KOptInfo(this.valueAssignment, nodeId, neighborsConstraint, domainArray,
                this.neighborsValueAssignmnet);
    }

    @Override
    public void changeReceiveFlagsToFalse() {

        if (this.myStatus == Status.phase1_initiateOfferPartner1){
            this.clearValueAssignmentMap();
            if (this.partner==null) {
                this.myStatus = Status.idle_12_waitForOffers;
            }else{
                this.myStatus = Status.idle_13_waitForReply;
            }


        }

        if (this.myStatus == Status.phase2_acceptOfferPartner2){
            clearToPhase2_boolean();
            this.toPhase2_info.clear();
            this.myStatus = Status.idle_234_waitForLocalReductionFromAll;

        }
        if (this.myStatus == Status.phase3_broadCastLReductionPartner1){

        }
        if (this.myStatus == Status.phase4_receiveLReductionsPartner12){

        }
        if (this.myStatus == Status.phase5_coordinatePartners){
            this.partner = null;
        }


    }

    private void clearToPhase2_boolean(){
        for (NodeId nId:this.neighborsConstraint.keySet()) {
            this.toPhase2_boolean.put(nId,null);
        }
    }

    private void clearValueAssignmentMap() {
        for (NodeId nId:this.neighborsConstraint.keySet()) {
            this.toPhase1_valueAssignmentsMsgs.put(nId,null);
        }
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
