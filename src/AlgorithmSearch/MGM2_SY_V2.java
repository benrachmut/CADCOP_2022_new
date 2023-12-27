package AlgorithmSearch;

import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Main.MainSimulator;
import Messages.*;

import java.util.*;

public class MGM2_SY_V2 extends AgentVariableSearch implements SelfCounterable {

    private double offerProb = 0.5;
    private boolean isPartnerTakeBest = true;
    enum Status {
        idle,

        phase1_initiateOfferPartner1,

        phase2_receiver_acceptOfferPartner2,
        phase2_receiver_chooseToChangeAlone,
        phase2_receiver_noOffersReceived,

        phase3_offerer_partnerRejectMe,
        phase3_offerer_broadCastLReductionPartner1,
        phase3_offerer_broadCastLReductionAlone,
        phase3_receiver_broadcastLRtoAllWithoutPartner,

        phase4_receiver_and_offerer_receiveLReductionsMoveToCoordinate,


        phase5_tryToChangeAlone,
        phase5_tryToChangeTogether

    }

    private Status myStatus;

    private Map<NodeId, Boolean> toPhase1_boolean_valueAssignmentsMsgs;
    private Map<NodeId, Boolean> toPhase2_boolean_infoOrFalse;
    private Map<NodeId, KOptInfo> toPhase2_info;
    private HashMap<NodeId,Boolean> toPhase4_booleanLR;
    private HashMap<NodeId,Integer> toPhase4_LR;
    private Find2Opt toPhase34_partnerReply;
    private Integer aloneCandidateValueAssignment;
    private Integer aloneLr;
    private Integer withPartnerCandidateValueAssignment;
    private Integer withPartnerLr;
    private NodeId partner;
    private boolean isCommitted;
    private boolean partnerBestInNeighborhood;
    private boolean iAmBestInNeighborhood;



    //private Boolean toPhase4_isAcceptPartner;







    private Map<NodeId, Integer> neighborsIterations;
    private Random randomIsOfferToPartner;
    private Random randomPartnerSelection;
    private int iteration;
    private int selfCounter;



    public MGM2_SY_V2(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        resetAgentGivenParametersV3();
    }


    @Override
    protected void resetAgentGivenParametersV3() {
        partner = null;
        iteration = 0;
        selfCounter = 0;
        this.myStatus = Status.idle;
        randomIsOfferToPartner = new Random((dcopId + 1) * 17 + this.nodeId.getId1() * 177);
        randomPartnerSelection = new Random((dcopId + 1) * 32452317 + this.nodeId.getId1() * 6575678);
        toPhase1_boolean_valueAssignmentsMsgs = new HashMap<NodeId,Boolean>();
        toPhase2_boolean_infoOrFalse = new HashMap<NodeId,Boolean>();
        toPhase2_info= new HashMap<NodeId,KOptInfo>();

        toPhase4_booleanLR =  new HashMap<NodeId,Boolean>() ;
        toPhase4_LR=  new HashMap<NodeId,Integer> ();
        toPhase34_partnerReply=  null ;

        withPartnerLr = Integer.MIN_VALUE;
        aloneLr = Integer.MIN_VALUE;
        withPartnerCandidateValueAssignment = null;
        aloneCandidateValueAssignment = null;

        isCommitted = false;
        neighborsIterations = new HashMap<NodeId,Integer>();
        partnerBestInNeighborhood = false;
        for (NodeId nodeId:this.neighborsConstraint.keySet()) {
            toPhase1_boolean_valueAssignmentsMsgs.put(nodeId,false);
            //toPhase2_info.put(nodeId,null);
            toPhase2_boolean_infoOrFalse.put(nodeId,false);
            toPhase4_booleanLR.put(nodeId,false);
            toPhase4_LR.put(nodeId,null);
            neighborsIterations.put(nodeId,-1);
        }
    }

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgMGM2V2_toPhase1_ValueAssignment){
            MsgValueAssignmnet msg = (MsgValueAssignmnet)msgAlgorithm;
            receive_toPhase1_valueAssignment(msg);
            int nIteration = ((MsgMGM2V2_toPhase1_ValueAssignment)msg).getIteration();
            this.neighborsIterations.put(msgAlgorithm.getSenderId(),nIteration);
        }

        if ( msgAlgorithm instanceof MsgMGM2V2_toPhase2_infoAndFalse){
            receive_toPhase2_infoOrFalse(msgAlgorithm);
        }

        if ( msgAlgorithm instanceof MsgMGM2V2_toPhase3_Opt2){
            receive_toPhase3_2Opt(msgAlgorithm);
        }

        if ( msgAlgorithm instanceof MsgMGM2V2_toPhase4_LR){
            receive_toPhase4_LR(msgAlgorithm);
            //System.out.println(this+": "+this.toPhase4_booleanLR);
        }

        if (msgAlgorithm instanceof  MsgMGM2V2_toPhase5_amIBest){
            MsgMGM2V2_toPhase5_amIBest msg = (MsgMGM2V2_toPhase5_amIBest) msgAlgorithm;
            this.partnerBestInNeighborhood = (boolean) msg.getContext();
        }

        return true;
    }




    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {



        if (msgAlgorithm instanceof  MsgMGM2V2_toPhase5_amIBest){
            this.myStatus = Status.phase5_tryToChangeTogether;
        }


        if (gotAllMsgs(this.toPhase1_boolean_valueAssignmentsMsgs.values()) && allIterationEqualToMe()){
            this.myStatus = Status.phase1_initiateOfferPartner1;
            return;
        }

        if (!isCommitted && gotAllMsgs(this.toPhase2_boolean_infoOrFalse.values())){

            if (this.toPhase2_info.isEmpty()){
                this.myStatus = Status.phase2_receiver_noOffersReceived;
            }else {
                this.myStatus = Status.phase2_receiver_acceptOfferPartner2;
            }
            return;
        }

        if (msgAlgorithm!=null && msgAlgorithm instanceof MsgMGM2V2_toPhase3_Opt2 &&  msgAlgorithm.getSenderId().equals(partner)){
            this.myStatus = Status.phase3_offerer_broadCastLReductionPartner1;
            return;
        }

        boolean isPartnerRejectMe =  msgAlgorithm!=null && msgAlgorithm instanceof MsgMGM2V2_toPhase4_LR && msgAlgorithm.getSenderId().equals(partner);

        if(msgAlgorithm!=null && msgAlgorithm instanceof MsgMGM2V2_toPhase4_LR && valueIsTheSameAsMyPartner(msgAlgorithm)){
            this.myStatus = Status.phase3_receiver_broadcastLRtoAllWithoutPartner;
            return;
        }

        if (isPartnerRejectMe){
            this.myStatus = Status.phase3_offerer_partnerRejectMe;
            return;
        }

        if (msgAlgorithm instanceof MsgMGM2V2_toPhase4_LR && gotAllMsgs(this.toPhase4_booleanLR.values())){
            changeStatuesAfterReceiveLR();
            return;
        }




        /*

        if (this.myStatus == Status.idle_13_waitForReply && sender.equals(partner) && contextIsOpt2Info(msgAlgorithm) && toPhase34_partnerReply!=null){
            this.myStatus = Status.phase3_broadCastLReductionPartner1;
        }

         */

    }

    private boolean allIterationEqualToMe() {
        for (NodeId nId: this.neighborsIterations.keySet()) {
            int nIteration = this.neighborsIterations.get(nId);
            if (nIteration!=this.iteration){
                return false;
            }

        }
        return true;
    }


    private boolean valueIsTheSameAsMyPartner(MsgAlgorithm msgAlgorithm) {
        NodeId nId = msgAlgorithm.getSenderId();
        if (nId.equals(partner)){
            int lrFromPartner = this.toPhase4_LR.get(partner);
            if (this.withPartnerLr == lrFromPartner){
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean compute() {
        if ( this.myStatus == Status.phase1_initiateOfferPartner1){
            computePhase1();
        }

        if (this.myStatus == Status.phase2_receiver_acceptOfferPartner2){
            computePhase2();
        }

        if (this.myStatus == Status.phase3_offerer_broadCastLReductionPartner1){
            computePhase3();
        }

        if (this.myStatus == Status.phase5_tryToChangeAlone){

            if (checkIfIamBest()){
                this.valueAssignment = this.aloneCandidateValueAssignment;
                this.selfCounter = this.selfCounter+1;
                if (MainSimulator.isMGM2v2Debug) {
                    System.out.println(this + " change VA");
                }
            }

            if (MainSimulator.isMGM2v2Debug) {
                System.out.println(this + " finish iteration " + this.iteration);
            }
            this.iteration = this.iteration +1;
        }
        if(this.myStatus == Status.phase5_tryToChangeTogether){
            if (this.partnerBestInNeighborhood && this.iAmBestInNeighborhood){
                this.valueAssignment = this.withPartnerCandidateValueAssignment;
                this.selfCounter = this.selfCounter+1;
                if (MainSimulator.isMGM2v2Debug) {
                    System.out.println(this + " change VA");
                }
            }

            if (MainSimulator.isMGM2v2Debug) {
                System.out.println(this + " finish iteration " + this.iteration);
            }
            this.iteration = this.iteration +1;

        }









                     /*
    aloneCandidateValueAssignment;
   aloneLr;
   withPartnerCandidateValueAssignment;
   withPartnerLr;
     */
        /*
        if (this.myStatus == Status.phase3_broadCastLReductionPartner1){
            computePhase3();
        }

        if (this.myStatus == Status.phase4_rejectedByOfferer){
            computePhase4_byMySelf();
        }
         */

        return true;
    }



    @Override
    public void sendMsgs() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();

        if (this.myStatus == Status.phase1_initiateOfferPartner1) {
            sendMsgs_toPhase2_sendInfoAndFalse(msgsToInsertMsgBox);
        }

        if (this.myStatus == Status.phase2_receiver_acceptOfferPartner2){
            sendMsgs_toPhase3_send2Opt(msgsToInsertMsgBox);
        }

        if (this.myStatus == Status.phase2_receiver_chooseToChangeAlone || this.myStatus == Status.phase2_receiver_noOffersReceived ||  this.myStatus == Status.phase3_offerer_partnerRejectMe || this.myStatus == Status.phase3_offerer_broadCastLReductionAlone){
            sendMsgs_toPhase4_aloneLRtoAll(msgsToInsertMsgBox);
        }

        if (this.myStatus == Status.phase3_offerer_broadCastLReductionPartner1){
            sendMsgs_toPhase4_withPartnerLRtoAll(msgsToInsertMsgBox);
        }

        if(this.myStatus == Status.phase3_receiver_broadcastLRtoAllWithoutPartner){
            sendMsgs_toPhase4_broadcastLRtoAllWithoutPartner(msgsToInsertMsgBox);
        }

        if (this.myStatus == Status.phase4_receiver_and_offerer_receiveLReductionsMoveToCoordinate){
            sendMsgs_toPhase5_sendToPartnerIfBestInNeighborhood(msgsToInsertMsgBox);
        }

        if (this.myStatus == Status.phase5_tryToChangeAlone || this.myStatus == Status.phase5_tryToChangeTogether){
            //resetBeforeRestartIteration();
            sendMsgs_toPhase1_sendVAandIterationNumber(msgsToInsertMsgBox);
        }


/*
        if (this.myStatus == Status.phase2_changeAlone || this.myStatus == Status.phase3_broadCastLReductionAlone){
            sendMsgs_toPhase4_LRtoAll(msgsToInsertMsgBox);
        }

        if (this.myStatus == Status.phase3_broadCastLReductionPartner1){
            sendMsgs_toPhase4_positiveToPartnerAndLRtoTheRest(msgsToInsertMsgBox);
        }

        if (this.myStatus == Status.phase3_broadCastLReductionAlone){
            sendMsgs_toPhase4_negativeToPartnerAndLRtoTheRest(msgsToInsertMsgBox);


        }
        if (this.myStatus == Status.phase4_rejectedByOfferer){
        }
*/

        if (!msgsToInsertMsgBox.isEmpty()) {
            outbox.insert(msgsToInsertMsgBox);
        }
    }

    private void sendMsgs_toPhase1_sendVAandIterationNumber(List<Msg> msgsToInsertMsgBox) {
        for (NodeId receiverNodeId:this.neighborsConstraint.keySet()) {
                MsgMGM2V2_toPhase1_ValueAssignment msg = new MsgMGM2V2_toPhase1_ValueAssignment
                        (this.nodeId, receiverNodeId, this.valueAssignment, this.timeStampCounter, this.time, this.iteration);
                msgsToInsertMsgBox.add(msg);

        }
    }

    private void sendMsgs_toPhase5_sendToPartnerIfBestInNeighborhood(List<Msg> msgsToInsertMsgBox) {
        boolean amIBest = checkIfIamBest();
        MsgMGM2V2_toPhase5_amIBest msg =  new MsgMGM2V2_toPhase5_amIBest
                (this.nodeId, this.partner, amIBest, this.timeStampCounter, this.time);
        this.iAmBestInNeighborhood = amIBest;
        if (MainSimulator.isMGM2v2Debug) {
            System.out.println(this + " is the best in the neighborhood");
        }
        msgsToInsertMsgBox.add(msg);
    }

    private boolean checkIfIamBest() {
        for (NodeId nId: this.toPhase4_LR.keySet()) {
            if (!nId.equals(this.partner)){
                int nIdLR = this.toPhase4_LR.get(nId);
                if (nIdLR>=this.withPartnerLr){
                    return false;
                }
            }
        }
        return true;
    }

    private void sendMsgs_toPhase4_broadcastLRtoAllWithoutPartner(List<Msg> msgsToInsertMsgBox) {
        for (NodeId receiverNodeId:this.neighborsConstraint.keySet()) {
            if (!this.partner.equals(receiverNodeId)) {
                MsgMGM2V2_toPhase4_LR msg = new MsgMGM2V2_toPhase4_LR
                        (this.nodeId, receiverNodeId, this.withPartnerLr, this.timeStampCounter, this.time);
                msgsToInsertMsgBox.add(msg);
            }
        }
    }

    private void sendMsgs_toPhase4_withPartnerLRtoAll(List<Msg> msgsToInsertMsgBox) {
        for (NodeId receiverNodeId:this.neighborsConstraint.keySet()) {
            MsgMGM2V2_toPhase4_LR msg = new MsgMGM2V2_toPhase4_LR
                    (this.nodeId, receiverNodeId, this.withPartnerLr, this.timeStampCounter, this.time);
            msgsToInsertMsgBox.add(msg);
        }

    }

    @Override
    public void changeReceiveFlagsToFalse() {

        if (this.myStatus == Status.phase1_initiateOfferPartner1){
            clearBooleanMap(this.toPhase1_boolean_valueAssignmentsMsgs);

            if (this.partner==null) {
                if (MainSimulator.isMGM2v2Debug){
                    System.out.println(this+" waits for offers");
                }
            }else{
                if (MainSimulator.isMGM2v2Debug){
                    System.out.println(this+" waits for reply from "+this.partner);
                }
            }
        }

        if (this.myStatus == Status.phase2_receiver_acceptOfferPartner2 || this.myStatus == Status.phase2_receiver_chooseToChangeAlone || this.myStatus == Status.phase2_receiver_chooseToChangeAlone || this.myStatus == Status.phase2_receiver_noOffersReceived){
            clearBooleanMap(this.toPhase2_boolean_infoOrFalse);
        }

        if (this.myStatus == Status.phase3_offerer_partnerRejectMe || this.myStatus == Status.phase3_offerer_broadCastLReductionAlone || this.myStatus == Status.phase2_receiver_chooseToChangeAlone|| this.myStatus == Status.phase2_receiver_noOffersReceived){
            isCommitted = false;
            partner = null;
        }

        if ( this.myStatus == Status.phase4_receiver_and_offerer_receiveLReductionsMoveToCoordinate ){
            clearBooleanMap(this.toPhase4_booleanLR);
        }
        if (this.myStatus == Status.phase5_tryToChangeAlone ||  this.myStatus == Status.phase5_tryToChangeTogether ){
            clearBooleanMap(this.toPhase4_booleanLR);
            clearBeforeNewIteration();
        }





        /*
        if (this.myStatus == Status.phase3_offerer_broadCastLReductionAlone){
            int x = 3;
        }
        */

 /*
        if (this.myStatus == Status.phase3_partnerRejectMe || this.myStatus == Status.phase3_broadCastLReductionPartner1 || this.myStatus == Status.phase3_broadCastLReductionAlone){
            //clearBooleanMap(this.toPhase2_boolean_infoOrFalse);
        }
*/
        /*
        if (this.myStatus == Status.phase3_broadCastLReductionPartner1){

        }
        if (this.myStatus == Status.phase4_receiveLReductionsPartner12){

        }
        if (this.myStatus == Status.phase5_coordinatePartners){
            this.partner = null;
        }
         */
/*
        if (MainSimulator.isMGM2v2Debug){
            System.out.println("****<"+this + " finish statues " + this.myStatus+">****. self counter:" +this.selfCounter);
        }
*/
        this.myStatus = Status.idle;
        //checkTodoAnotherIteration();
    }

    private void clearBeforeNewIteration() {


        toPhase2_info.clear();
        //private HashMap<NodeId,Boolean> toPhase4_booleanLR;
        toPhase34_partnerReply = null;
        aloneCandidateValueAssignment = null;
        aloneLr= Integer.MIN_VALUE;
        withPartnerCandidateValueAssignment= null;
        withPartnerLr= null;
        partner= null;
        isCommitted =false;
        partnerBestInNeighborhood =false;
        iAmBestInNeighborhood=false;


        for (NodeId nId:
             this.toPhase4_LR.keySet()) {
            this.toPhase4_LR.put(nId,null);
            toPhase2_boolean_infoOrFalse.put(nId,false);
        }

    }

    private void checkTodoAnotherIteration() {

            changeReceiveFlagsToTrue(null);
            if (getDidComputeInThisIteration()) {
                compute();
                sendMsgs();
                changeReceiveFlagsToFalse();
            }

    }

    private void clearBooleanMap(Map<NodeId, Boolean> map) {
        for (NodeId nodeId: map.keySet()) {
            map.put(nodeId,false);
        }

    }
/*
    private void clearToPhase2_boolean(){
        for (NodeId nId:this.neighborsConstraint.keySet()) {
            this.toPhase2_boolean_infoOrFalse.put(nId,null);
        }
    }

 */
  /*
    private void clearValueAssignmentMap() {
        for (NodeId nId:this.neighborsConstraint.keySet()) {
            this.toPhase1_valueAssignmentsMsgs.put(nId,null);
        }
    }
    */


    @Override
    public void updateAlgorithmHeader() {

    }

    @Override
    public void updateAlgorithmData() {

    }

    @Override
    public void updateAlgorithmName() {

    }

    @Override
    public int getSelfCounterable() {
        return selfCounter;
    }



    @Override
    public boolean getDidComputeInThisIteration() {
        return

                this.myStatus == Status.phase1_initiateOfferPartner1||
                this.myStatus == Status.phase2_receiver_acceptOfferPartner2||
                this.myStatus == Status.phase2_receiver_chooseToChangeAlone||
                this.myStatus == Status.phase2_receiver_noOffersReceived||

                this.myStatus == Status.phase3_offerer_partnerRejectMe||
                this.myStatus == Status.phase3_offerer_broadCastLReductionPartner1||
                this.myStatus == Status.phase3_offerer_broadCastLReductionAlone||
                this.myStatus == Status.phase3_receiver_broadcastLRtoAllWithoutPartner||

                this.myStatus == Status.phase4_receiver_and_offerer_receiveLReductionsMoveToCoordinate||

                this.myStatus == Status.phase5_tryToChangeAlone||
                this.myStatus == Status.phase5_tryToChangeTogether;
                /*
                this.myStatus == Status.phase1_initiateOfferPartner1||
                        this.myStatus == Status.phase2_receiver_acceptOfferPartner2 ||
                        this.myStatus == Status.phase2_receiver_chooseToChangeAlone ||
                        this.myStatus == Status.phase3_offerer_partnerRejectMe ||
                        this.myStatus == Status.phase3_offerer_broadCastLReductionPartner1 ||
                        this.myStatus == Status.phase3_offerer_broadCastLReductionAlone ||
                        this.myStatus == Status.phase4_receiver_rejectedByOfferer ||
                        this.myStatus == Status.phase4_receiver_and_offerer_receiveLReductionsMoveToCoordinate ||
                        this.myStatus == Status.phase5_coordinatePartners||  Status.phase3_receiver_broadcastLRtoAllWithoutPartner;
                        */

    }

    @Override
    public void initialize() {
        this.myStatus = Status.phase5_tryToChangeAlone;
        sendMsgs();
        this.myStatus = Status.idle;
    }



    //////////////////*********************************************///////////////////////////
    //////////////////*********************************************///////////////////////////
    //////////////////*********************************************///////////////////////////
    //////////////////*********************************************///////////////////////////

    //*****************************************************************//
    ///////////////////// update message in context/////////////////////
    //*****************************************************************//

    private void receive_toPhase1_valueAssignment(MsgValueAssignmnet msg) {

        this.toPhase1_boolean_valueAssignmentsMsgs.put(msg.getSenderId(),true);
        this.updateMsgInContextValueAssignment(msg);
    }

    private void receive_toPhase2_infoOrFalse(MsgAlgorithm msgAlgorithm) {
        toPhase2_boolean_infoOrFalse.put(msgAlgorithm.getSenderId(),true);
        Object context  = msgAlgorithm.getContext();
        if (context instanceof KOptInfo){
            toPhase2_info.put(msgAlgorithm.getSenderId(), (KOptInfo) context);
        }
    }

    private void receive_toPhase3_2Opt(MsgAlgorithm msgAlgorithm) {
        NodeId sender = msgAlgorithm.getSenderId();
        toPhase4_booleanLR.put(msgAlgorithm.getSenderId(),true);
        Object context  = msgAlgorithm.getContext();
        if (!partner.equals(sender)){
            throw new RuntimeException();
        }
        this.toPhase34_partnerReply = (Find2Opt)context;
        this.toPhase4_LR.put(partner,toPhase34_partnerReply.getLR());
        if (MainSimulator.isMGM2v2Debug){
            System.out.println(this+ " receives info back from: "+sender);
        }
    }


    private void receive_toPhase4_LR(MsgAlgorithm msgAlgorithm) {
        MsgMGM2V2_toPhase4_LR msg = (MsgMGM2V2_toPhase4_LR)msgAlgorithm;
        Integer nLR = (Integer) msg.getContext();
        this.toPhase4_LR.put(msg.getSenderId(),nLR);
        this.toPhase4_booleanLR.put(msg.getSenderId(),true);
    }


    //*****************************************************************//
    ///////////////////// change flag to true/////////////////////
    //*****************************************************************//

    private void changeStatuesAfterReceiveLR() {
        if (this.isCommitted) {
            int partnerLr = this.toPhase4_LR.get(this.partner);
            if (partnerLr == this.withPartnerLr){
                this.myStatus = Status.phase4_receiver_and_offerer_receiveLReductionsMoveToCoordinate;
                if (MainSimulator.isMGM2v2Debug ){
                    System.out.println(this+" can coordinate with partner ("+this.partner+")"+ this.toPhase4_LR);
                }

            }else {
                this.myStatus = Status.phase5_tryToChangeAlone;
                if (MainSimulator.isMGM2v2Debug ){
                    System.out.println(this+ " can try to change alone");
                }
            }
            return;
        }else{
            this.myStatus = Status.phase5_tryToChangeAlone;
            if(MainSimulator.isMGM2v2Debug){
                System.out.println(this+ " can try to change alone");
            }
        }
    }



    private boolean contextIsOpt2Info(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgMGM2V2_toPhase4_LR) {
            Object context = msgAlgorithm.getContext();
            if (context instanceof Find2Opt) {
                return true;
            }
        }
        return false;
    }

    private static boolean gotAllMsgs(Collection<Boolean> info) {
        for (Boolean o:info) {
            if (o == false){
                return false;
            }
        }
        return true;
    }



    //*****************************************************************//
    ///////////////////// compute /////////////////////
    // *****************************************************************//


    private void computePhase1() {
        checkIfOffers();
        computeAlone();


    }

    private void checkIfOffers() {
        double p = this.randomIsOfferToPartner.nextDouble();
        p = this.randomIsOfferToPartner.nextDouble();
        if (p<this.offerProb){
            this.partner = getRandomPartner();
            this.isCommitted = true;
        }else {
            this.isCommitted = false;
        }
    }


    private void computeAlone() {
        int candidate = getCandidateToChange_C();
        if (candidate != this.valueAssignment) {
            this.aloneCandidateValueAssignment = candidate;
            int lrToCheck = findLr(candidate);
            changeLrAlone(lrToCheck);
        }else {
            changeLrToZero();
        }
    }

    private void computePhase3() {
        if (this.partner!=null){
            int lrFromNeighbor = this.toPhase34_partnerReply.getLR();
            if (this.aloneLr <= lrFromNeighbor){
                this.withPartnerCandidateValueAssignment = this.toPhase34_partnerReply.getValueAssignmnet2();
                this.withPartnerLr = lrFromNeighbor;
                this.aloneCandidateValueAssignment = null;
                this.aloneLr = null;
                if (MainSimulator.isMGM2v2Debug){
                    System.out.println(this+" received approval from "+ this.partner);
                }
            }else{
                this.myStatus = Status.phase3_offerer_broadCastLReductionAlone;
                this.isCommitted = false;
                if (MainSimulator.isMGM2v2Debug){
                    System.out.println(this+" received approval from "+ this.partner+" but personal LR ("+this.aloneLr+") is better then partner LR ("+lrFromNeighbor+")"  );
                }
            }
        }
    }

    private void computePhase2() {
        if (isPartnerTakeBest){
            computePhase2IfPartnerTakeBest();
        }else{
            computePhase2SelectNeighborRandomly();
        }

    }

    private void computePhase2SelectNeighborRandomly() {
        NodeId potentialPartner = getRandomPartnerFromInfo();
        KOptInfo koi = this.toPhase2_info.get(potentialPartner);
        Find2Opt f2o =  new Find2Opt(makeLocalOptInfo(),koi);
        this.atomicActionCounter = this.atomicActionCounter + f2o.getAtomicActionCounter();

        if (this.withPartnerLr<= f2o.getLR()) {
            this.partner = potentialPartner;
            this.withPartnerCandidateValueAssignment = f2o.getValueAssignmnet1();
            this.withPartnerLr = f2o.getLR();
            if (MainSimulator.isMGM2v2Debug){
                System.out.println(this+ " selected: "+ partner);
            }
            this.isCommitted = true;

        }else{
            this.myStatus = Status.phase2_receiver_chooseToChangeAlone;
            this.isCommitted = false;
            if (MainSimulator.isMGM2v2Debug) {
                System.out.println(this + " decided to change despite the offers it received");
            }
        }
    }

    private void computePhase2IfPartnerTakeBest() {
        Map<NodeId,Find2Opt> twoOpts = getTwoOpts();
        for (Find2Opt singleTwoOpt: twoOpts.values()) {
            this.atomicActionCounter = this.atomicActionCounter + singleTwoOpt.getAtomicActionCounter();
        }
        Map.Entry<NodeId, Find2Opt> bestPartner = getLocalReductions(twoOpts);
        Find2Opt f2o = bestPartner.getValue();


        if (this.aloneLr <= f2o.getLR()) {
            partner = bestPartner.getKey();
            this.withPartnerCandidateValueAssignment = bestPartner.getValue().getValueAssignmnet1();
            this.withPartnerLr = f2o.getLR();
            if (MainSimulator.isMGM2v2Debug){
                System.out.println(this+ " selected: "+ partner);
            }
            this.isCommitted = true;
        }else {
            this.myStatus = Status.phase2_receiver_chooseToChangeAlone;
            this.isCommitted = false;
            if (MainSimulator.isMGM2v2Debug){
                System.out.println(this+ " decided to change despite the offers it received");

            }

        }
    }



    private Map.Entry<NodeId,Find2Opt> getLocalReductions(Map<NodeId, Find2Opt> twoOpts) {
        int maxLr = Integer.MIN_VALUE;
        Map.Entry<NodeId,Find2Opt> ans = null;
        for (Map.Entry<NodeId,Find2Opt> e:twoOpts.entrySet()) {
            Find2Opt f2o = e.getValue();
            int lrOfF2O = f2o.getLR();
            if (lrOfF2O>maxLr){
                ans = e;
                maxLr = lrOfF2O;
            }
        }
        return ans;
    }

    private Map<NodeId, Find2Opt> getTwoOpts() {
        Map<NodeId, Find2Opt> ans = new HashMap<NodeId, Find2Opt>();
        for (NodeId nodeId:this.toPhase2_info.keySet()) {
            KOptInfo kOptInfo = this.toPhase2_info.get(nodeId);
            Find2Opt f2o = new Find2Opt(makeLocalOptInfo(),kOptInfo);
            ans.put(nodeId,f2o);
        }
        return ans;
    }


    private NodeId getRandomPartnerFromInfo() {
        Collection<NodeId>neighbors = this.toPhase2_info.keySet();
        int i = this.randomPartnerSelection.nextInt(neighbors.size());
        return (NodeId) neighbors.toArray()[i];
    }

    private NodeId getRandomPartner() {
        Collection<NodeId>neighbors = this.neighborsConstraint.keySet();
        int i = this.randomPartnerSelection.nextInt(neighbors.size());
        return (NodeId) neighbors.toArray()[i];
    }

    private boolean changeLrToZero() {
        if (aloneLr == 0) {
            return false;
        } else {
            this.aloneLr = 0;
            return true;
        }
    }

    private boolean changeLrAlone(int lrToCheck) {
        if (lrToCheck < 0) {
            throw new RuntimeException();
        }
        if (this.aloneLr != lrToCheck) {
            this.aloneLr = lrToCheck;
            return true;
        } else {
            return false;
        }
    }



    //*****************************************************************//
    ///////////////////// send msgs /////////////////////
    // *****************************************************************//


    private void sendMsgs_toPhase4_aloneLRtoAll(List<Msg> msgsToInsertMsgBox) {
        for (NodeId receiverNodeId:this.neighborsConstraint.keySet()) {
            MsgMGM2V2_toPhase4_LR msg = new MsgMGM2V2_toPhase4_LR
                    (this.nodeId, receiverNodeId, this.aloneLr, this.timeStampCounter, this.time);
            msgsToInsertMsgBox.add(msg);
        }


    }

    private void sendMsgs_toPhase3_send2Opt(List<Msg> msgsToInsertMsgBox) {
        for (NodeId receiverNodeId:this.neighborsConstraint.keySet()) {
            if (this.partner != null && this.partner.equals(receiverNodeId)) {
                Find2Opt f2o = new Find2Opt(makeLocalOptInfo(),toPhase2_info.get(partner));
                MsgMGM2V2_toPhase3_Opt2 msg = new MsgMGM2V2_toPhase3_Opt2(this.nodeId, receiverNodeId, f2o, this.timeStampCounter, this.time);
                msgsToInsertMsgBox.add(msg);
            }
        }
    }

    private void sendMsgs_toPhase2_sendInfoAndFalse(List<Msg> msgsToInsertMsgBox) {
        for (NodeId receiverNodeId:this.neighborsConstraint.keySet()) {
            MsgMGM2V2_toPhase2_infoAndFalse msg = null;
            if (this.partner != null && this.partner.equals(receiverNodeId)){
                msg = new MsgMGM2V2_toPhase2_infoAndFalse
                        (this.nodeId, receiverNodeId, makeLocalOptInfo(), this.timeStampCounter, this.time);
            }else{
                msg = new MsgMGM2V2_toPhase2_infoAndFalse
                        (this.nodeId, receiverNodeId, false, this.timeStampCounter, this.time);
            }
            msgsToInsertMsgBox.add(msg);

        }

    }


}
