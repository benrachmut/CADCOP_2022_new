package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Main.MainSimulator;
import Messages.*;

import java.util.*;

public class DALO2 extends AgentVariableSearch implements SelfCounterable {
    private Find2Opt twoOpt;
    private static int boundForTimer =10000;
    private NodeId commitedTo;
    private HashMap<NodeId, Boolean> acceptMap;

    enum Status {
       waitForAllInitValues, createInitKInfo, createKopt, createInitKInfoAndKopt, waitForClockOrLock, sendLocksInitateCoalition, agreeToLock, sendLockToAgree, waitForCommitMsg, waitForAcceptExecptCommitedTo, waitForAccept, waitForAllKInfo
    }
    private Random randomForCoalition;
    private Random randomForTimer;
    private List<NodeId> coalitions ;
    private Status myStatues;
    private Map<NodeId,KOptInfo> infoOfAgents;

    public DALO2(int dcopId, int d, int agentId) {
        super(dcopId,  d,  agentId);
        updateAlgorithmHeader();
        updateAlgorithmData();
        updateAlgorithmName();
        resetAgentGivenParametersV3();

    }


    @Override
    public void initialize() {
        //formKGroups();
        myStatues = Status.waitForAllInitValues;
        createCoalitions();
        super.initialize();
        randomForCoalition = new Random((this.dcopId+1)*17+(this.nodeId.getId1()+1)*177+this.neighborsConstraint.keySet().size()*17);
        randomForTimer = new Random((this.dcopId+1)*18+(this.nodeId.getId1()+1)*173+this.neighborsConstraint.keySet().size()*143);
        twoOpt = null;



    }

    private void createCoalitions() {

        this.coalitions = new ArrayList<>();
        for (NodeId nId : this.neighborsConstraint.keySet()) {
            if (this.nodeId.getId1() < nId.getId1()) {
                this.coalitions.add(nId);
            }
        }
    }

    @Override
    protected void resetAgentGivenParametersV3() {
        createCoalitions();
        infoOfAgents = new HashMap<NodeId,KOptInfo>();
        for (NodeId nodeId:
             this.neighborsConstraint.keySet()) {
            infoOfAgents.put(nodeId,null);
        }
        twoOpt = null;

        commitedTo=null;
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgValueAssignmnet){
            updateMsgInContextValueAssignment(msgAlgorithm);
        }

        if (msgAlgorithm instanceof MsgDALOkInfo){
            updateMsgDALOkInfoInContext(msgAlgorithm);
        }

        if (msgAlgorithm instanceof MsgDALOAccept && (this.myStatues == Status.waitForAcceptExecptCommitedTo
                || this.myStatues == Status.waitForAccept)){
            this.acceptMap.put(msgAlgorithm.getSenderId(),true);
        }




        //if (msgAlgorithm instanceof MsgDALOkLock){
            //updateMsgDALOkLock(msgAlgorithm);
        //}

        return true;
    }



    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
        if (this.myStatues == Status.waitForAllInitValues && isAllValuesReceived()){
            this.myStatues = Status.createInitKInfo;
        }

        if ( this.myStatues == Status.waitForAllKInfo && isAllInfoInMemory()){
            this.myStatues = Status.createKopt;
        }

        if (this.myStatues == Status.waitForAllInitValues && isAllValuesReceived() && isAllInfoInMemory()){
            this.myStatues = Status.createInitKInfoAndKopt;
        }

        if (this.myStatues == Status.waitForClockOrLock){
            updateStatusGivenMsgType(msgAlgorithm);
        }



        if (msgAlgorithm instanceof MsgDALOkLockRequest && (this.myStatues == Status.waitForCommitMsg || this.myStatues == Status.waitForCommitMsg
                || this.myStatues == Status.waitForAcceptExecptCommitedTo)){
            //todo
            System.out.println(this.nodeId+" will send reject");
        }

        if (msgAlgorithm instanceof MsgDALOAccept && this.myStatues == Status.waitForAccept && isAllAccept()){
            //todo
            System.out.println(this.nodeId+" can send commit");

        }

        if (msgAlgorithm instanceof MsgDALOAccept && this.myStatues == Status.waitForAcceptExecptCommitedTo && isAllAccept() ){
            System.out.println(this.nodeId+" can send accept for commited");
        }



    }

    private boolean isAllAccept() {
        for (NodeId nId:this.acceptMap.keySet()) {
            if (!this.acceptMap.get(nId)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean getDidComputeInThisIteration() {
        return this.myStatues == Status.createInitKInfo || this.myStatues == Status.createKopt || this.myStatues == Status.createInitKInfoAndKopt        ||
                this.myStatues == Status.agreeToLock||this.myStatues == Status.sendLockToAgree||this.myStatues == Status.sendLocksInitateCoalition ||         this.myStatues == Status.sendLockToAgree ||  this.myStatues == Status.agreeToLock || this.myStatues  ==Status.sendLocksInitateCoalition;

    }

    @Override
    public boolean compute() {
        if (this.myStatues == Status.createKopt || this.myStatues == Status.createInitKInfoAndKopt){
            update2Opt();
            return true;


        }
        return false;
    }



    @Override
    public void sendMsgs() {
        if (this.myStatues == Status.createInitKInfo || this.myStatues == Status.createInitKInfoAndKopt){
            sendKInfoMsgs();
        }
        if ((this.myStatues == Status.createKopt ||this.myStatues == Status.createInitKInfoAndKopt) && this.twoOpt!=null){
            sendSelfTimerMsg();

        }

        if (this.myStatues == Status.agreeToLock){
            sendAccept();
            //this.myStatues = Status.waitForCommitMsg;
        }
        if (this.myStatues == Status.sendLockToAgree){
            sendLockRequestExceptCommited();
            // this.myStatues = Status.waitForAcceptExecptCommitedTo;
            //resetAcceptMap(this.commitedTo);
        }

        if (this.myStatues == Status.sendLocksInitateCoalition){
            sendLockRequest();
        }



    }




    @Override
    public void changeReceiveFlagsToFalse() {
        if (this.myStatues == Status.createInitKInfo){
            if (isAllInfoInMemory()){
                //initCoalition(); TODO
            }else {
                this.myStatues = Status.waitForAllKInfo;
            }
        }

        if ((this.myStatues == Status.createKopt ||this.myStatues == Status.createInitKInfoAndKopt)){
            this.myStatues = Status.waitForClockOrLock;
        }



        if (this.myStatues == Status.agreeToLock){
            this.myStatues = Status.waitForCommitMsg;
        }

        if (this.myStatues == Status.sendLockToAgree){
            this.myStatues = Status.waitForAcceptExecptCommitedTo;
            resetAcceptMap(this.commitedTo);
        }

        if (this.myStatues == Status.sendLocksInitateCoalition){
            this.myStatues = Status.waitForAccept;
            resetAcceptMap();
        }

        if(MainSimulator.isDalo2Debug) {
            System.out.println(this.nodeId+" statues: "+this.myStatues);
        }
        //if (this.myStatues == Status.createKopt ||this.myStatues == Status.createInitKInfoAndKopt && !this.isLockRequetSetEmpty()){
        //    this.myStatues = Status.locked;
         //   sendLockAcceptMessage();
        //}




    }


    private void sendInitMsg() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId receiverNodeId : neighborsConstraint.keySet()) {
            MsgDALOkInfo msg = new MsgDALOkInfo(this.nodeId, receiverNodeId, makeMyKOptInfo(), this.timeStampCounter, this.time);
            msgsToInsertMsgBox.add(msg);
        }
        outbox.insert(msgsToInsertMsgBox);
    }




    //*******  updateMessageInContext *******


    private void resetAcceptMap(NodeId senderId) {
        this.acceptMap= new HashMap<NodeId,Boolean>();
        for (NodeId nId:this.neighborsConstraint.keySet()) {
            if (nId.equals(senderId)){
                this.acceptMap.put(nId,false);
            }
        }
    }

    private void resetAcceptMap() {
        this.acceptMap= new HashMap<NodeId,Boolean>();
        for (NodeId nId:this.neighborsConstraint.keySet()) {
            this.acceptMap.put(nId,false);

        }
    }

    private void updateMsgDALOkInfoInContext(MsgAlgorithm msgAlgorithm) {
        MsgDALOkInfo msg = (MsgDALOkInfo) msgAlgorithm;
        KOptInfo info = (KOptInfo)msg.getContext();
        NodeId nId = msg.getSenderId();
        this.infoOfAgents.put(nId,info);
    }

    private void updateStatusGivenMsgType(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgDALOSelfTimerMsg ){
            this.myStatues = Status.sendLocksInitateCoalition;
            if (MainSimulator.isDalo2Debug){
                System.out.println(this.nodeId+" clock is over");
            }
        }
        else if (msgAlgorithm instanceof  MsgDALOkLockRequest ) {
            boolean isSendLockToAgree = (boolean) msgAlgorithm.getContext();
            this.twoOpt= null;
            if (isSendLockToAgree) {
                this.myStatues = Status.sendLockToAgree;
            } else {
                this.myStatues = Status.agreeToLock;
            }
            this.commitedTo = msgAlgorithm.getSenderId();

            if (MainSimulator.isDalo2Debug){
                System.out.println(this.nodeId+" receive lock before lock expired");
            }
        }
        else{
            throw new RuntimeException("msg type recieve doesnt make sense");
        }

    }



    //******* changeReceiveFlagsToTrue *******

    private boolean isAllValuesReceived() {
        for (NodeId nId:this.neighborsValueAssignment.keySet()) {
            if(this.neighborsValueAssignment.get(nId) == null) {
                return false;
            }
        }
        return true;
    }


    //******* compute *******
    private void update2Opt() {
        boolean isCreating2Opt = !this.coalitions.isEmpty();
        if (isCreating2Opt){
            int randomIndex = this.randomForCoalition.nextInt(this.coalitions.size());
            NodeId selectedPartnerNodeId = this.coalitions.get(randomIndex);
            this.twoOpt = new Find2Opt(makeMyKOptInfo(),this.infoOfAgents.get(selectedPartnerNodeId));
            this.atomicActionCounter = this.twoOpt.getAtomicActionCounter();

            if(MainSimulator.isDalo2Debug) {
                System.out.println(this.nodeId+" selected: "+selectedPartnerNodeId);
            }
            if (this.twoOpt.getLR()<0){
                this.twoOpt = null;
            }
        }else{
            this.twoOpt = null;
        }
    }




    //******* sendMsgs *******

    private void sendKInfoMsgs() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId nId: this.neighborsConstraint.keySet()) {
            MsgDALOkInfo msg = new MsgDALOkInfo(this.nodeId, nId, makeMyKOptInfo(), this.timeStampCounter, this.time);
            msgsToInsertMsgBox.add(msg);
        }
        outbox.insert(msgsToInsertMsgBox);
    }

    private void sendSelfTimerMsg() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();

        int rndTimer = randomForTimer.nextInt(this.boundForTimer);
        MsgDALOSelfTimerMsg msg = new MsgDALOSelfTimerMsg(this.nodeId, this.nodeId, null, this.timeStampCounter, this.time, rndTimer);
        msgsToInsertMsgBox.add(msg);
        if (MainSimulator.isDalo2Debug){
            System.out.println(this.nodeId+ " sent timer message at self_time:"+this.time+" with delay of: "+rndTimer );
        }
        outbox.insert(msgsToInsertMsgBox);

    }
    private void sendAccept() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        MsgDALOAccept msg = new MsgDALOAccept(this.nodeId, commitedTo, false, this.timeStampCounter, this.time);
        msgsToInsertMsgBox.add(msg);
        //commitedTo = null;
        outbox.insert(msgsToInsertMsgBox);

    }

    private void sendLockRequestExceptCommited() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId nId: this.neighborsConstraint.keySet()) {
            if (!nId.equals(commitedTo)){

                MsgDALOkLockRequest msg = new MsgDALOkLockRequest(this.nodeId, nId, false, this.timeStampCounter, this.time);
                msgsToInsertMsgBox.add(msg);
            }

        }
        //commitedTo = null;
        outbox.insert(msgsToInsertMsgBox);
    }



    private void sendLockRequest() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId nId: this.neighborsConstraint.keySet()) {
            boolean isPartner = false;
            if (this.twoOpt.getNodeId2().equals(nId)){
                isPartner = true;
            }
            MsgDALOkLockRequest msg = new MsgDALOkLockRequest(this.nodeId, nId, isPartner, this.timeStampCounter, this.time);
            msgsToInsertMsgBox.add(msg);
        }
        outbox.insert(msgsToInsertMsgBox);
    }

    //******* changeReceiveFlagsToFalse *******

    private boolean isAllInfoInMemory() {
        for (NodeId nId:this.infoOfAgents.keySet()) {
            if (this.infoOfAgents.get(nId) == null) {
                return false;
            }
        }
        return true;
    }

    //******* workspace *******
    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }



    @Override
    public int getSelfCounterable() {
        //TODO
        return 15;
    }



    @Override
    public void updateAlgorithmName() {
        AgentVariable.AlgorithmName = "DALO2";
    }


    public void updateAlgorithmData() {
        AgentVariable.algorithmData = this.boundForTimer+"";
    }

    public void updateAlgorithmHeader() {
        AgentVariable.algorithmHeader = "bound_for_timer";
    }
}
