package AlgorithmSearch;

import AgentsAbstract.*;
import Main.MainSimulator;
import Messages.*;

import java.util.*;

public class DALO2 extends AgentVariableSearch implements SelfCounterable {
    private Find2Opt twoOpt;
    private static int boundForTimer =10000;
    private NodeId commitedTo;
    private HashMap<NodeId, Boolean> acceptMap;
    private Status tempStatus;
    private NodeId tempSender;
    private Map<NodeId,MsgReceive<Integer>> tempNeighborValueA;

    private boolean partOfCoalition;

    enum Status {
       waitForAllInitValues, createInitKInfo, createKopt, createInitKInfoAndKopt, waitForClockOrLock, sendLocksInitateCoalition, agreeToLock, sendLockOnBehalfOfCommit, waitForCommitMsg, waitForAcceptExecptCommitedTo, waitForAccept, sendReject, waitForValue, sendCommit, sendLocalViewUpdate, waitForAllKInfo
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
        tempNeighborValueA = new HashMap<NodeId,MsgReceive<Integer>>();
        commitedTo=null;
        partOfCoalition =false;
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

        if (msgAlgorithm instanceof MsgDALOcommit){
            if (MainSimulator.isDalo2Debug){
                System.out.println(this.nodeId+" received commit msg from: "+msgAlgorithm.getSenderId());
            }
            TODO
            //TODO ALL THE WAY
        }

        if (msgAlgorithm instanceof MsgValueAssignmnet && this.myStatues!=Status.waitForAllInitValues){
            if (MainSimulator.isDalo2Debug) {
                System.out.println(this.nodeId + " received value msg from: " + msgAlgorithm.getSenderId());
            }
            updateMsgInContextValueAssignment(msgAlgorithm);
            NodeId senderId = msgAlgorithm.getSenderId();
            tempNeighborValueA.put(senderId,this.neighborsValueAssignment.get(senderId));
        }



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



        if (msgAlgorithm instanceof MsgDALOkLockRequest && (this.myStatues == Status.waitForCommitMsg
                || this.myStatues == Status.waitForAccept
                || this.myStatues == Status.waitForAcceptExecptCommitedTo)){

            this.tempSender = msgAlgorithm.getSenderId();
            this.tempStatus = this.myStatues;
            this.myStatues = Status.sendReject;

        }

        if (msgAlgorithm instanceof MsgDALOAccept && this.myStatues == Status.waitForAcceptExecptCommitedTo && isAllAccept() ){
            this.myStatues = Status.agreeToLock;
        }

        if (msgAlgorithm instanceof MsgDALOAccept && this.myStatues == Status.waitForAccept && isAllAccept()){
            this.myStatues = Status.sendCommit;

            System.out.println(this.nodeId+" can send commit");

        }

        if (msgAlgorithm instanceof MsgDALOReject){
            if (MainSimulator.isDalo2Debug){
                System.out.println(this+" receive reject from "+msgAlgorithm.getSenderId()+" TODO!!!!!!!!");
            }
            //TODO
        }

        if (msgAlgorithm instanceof MsgValueAssignmnet && this.myStatues!=Status.waitForAllInitValues){
            this.myStatues = Status.sendLocalViewUpdate;
        }





    }


    @Override
    public boolean getDidComputeInThisIteration() {
        return this.myStatues == Status.createInitKInfo || this.myStatues == Status.createKopt || this.myStatues == Status.createInitKInfoAndKopt        ||
                this.myStatues == Status.agreeToLock||this.myStatues == Status.sendLockOnBehalfOfCommit ||this.myStatues == Status.sendLocksInitateCoalition ||         this.myStatues == Status.sendLockOnBehalfOfCommit ||  this.myStatues == Status.agreeToLock
                || this.myStatues  ==Status.sendLocksInitateCoalition ||this.myStatues  ==Status.sendReject || this.myStatues  ==Status.sendCommit || this.myStatues == Status.sendLocalViewUpdate;

    }

    @Override
    public boolean compute() {
        if (this.myStatues == Status.createKopt || this.myStatues == Status.createInitKInfoAndKopt){
            update2Opt();
            return true;
        }

        if (this.myStatues == Status.sendCommit  ) {
            this.valueAssignment = this.twoOpt.getValueAssignmnet1();

            NodeId partnerNId = twoOpt.getNodeId2();
            int partnerVal = twoOpt.getValueAssignmnet2();
            MsgReceive<Integer> input_ = new MsgReceive<Integer>(partnerVal,0);
            this.neighborsValueAssignment.put(partnerNId,input_);
            updateInfos(partnerNId,input_);
            return true;
        }

        if(this.myStatues == Status.sendLocalViewUpdate){

            NodeId nodeId = getTheSingleNodeIdFromMap(tempNeighborValueA);
            MsgReceive<Integer> msgReceive = this.neighborsValueAssignment.get(nodeId);
            updateInfos(nodeId,msgReceive);


        }
        return false;
    }

    public static NodeId getTheSingleNodeIdFromMap(Map<NodeId, MsgReceive<Integer>> input_){
        boolean flag= false;
        NodeId ans = null;
        for (NodeId nodeId: input_.keySet()) {
            if (flag){
                throw new RuntimeException("NEEDS TO HAVE ONLY ONE VARIABLE");
            }
            ans = nodeId;

            flag=true;
        }
        return ans;

    }

    private void updateInfos(NodeId partnerNId, MsgReceive<Integer> input_) {
        for (NodeId nId: this.infoOfAgents.keySet()) {
            KOptInfo info = this.infoOfAgents.get(nId);
            info.updateLocalView(partnerNId,input_);
        }
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
        if (this.myStatues == Status.sendLockOnBehalfOfCommit){
            sendLockRequestExceptCommited();
            partOfCoalition = true;
            // this.myStatues = Status.waitForAcceptExecptCommitedTo;
            //resetAcceptMap(this.commitedTo);
        }

        if (this.myStatues == Status.sendLocksInitateCoalition){
            sendLockRequest();
        }

        if ( this.myStatues ==Status.agreeToLock ||this.myStatues == Status.sendLockOnBehalfOfCommit){
            sendSpamToRemoveRelockTimerFromMailer();

        }

        if (this.myStatues  == Status.sendReject) {
            sendRejectMsg();
            if (MainSimulator.isDalo2Debug){
                System.out.println(this.nodeId+" sent reject to "+this.tempSender);
            }
        }

        if (this.myStatues == Status.sendCommit){
            sendCommitMsg();
            sendValue();

        }

        if (this.myStatues == Status.sendLocalViewUpdate){
            NodeId whoChange = getTheSingleNodeIdFromMap(tempNeighborValueA);
            MsgReceive<Integer> whatIsTheChange = this.neighborsValueAssignment.get(nodeId);
            sendLocalViewUpdate(whoChange, whatIsTheChange);// TODO: need to send local view update; node id = who changed
            tempNeighborValueA = new HashMap<NodeId,MsgReceive<Integer>>();
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
            if (partOfCoalition){
                this.myStatues = Status.waitForCommitMsg;
            }else {
                this.myStatues = Status.waitForValue;
            }
        }

        if (this.myStatues == Status.sendLockOnBehalfOfCommit){
            this.myStatues = Status.waitForAcceptExecptCommitedTo;
            resetAcceptMap(this.commitedTo);
        }

        if (this.myStatues == Status.sendLocksInitateCoalition){
            this.myStatues = Status.waitForAccept;
            resetAcceptMap();
        }

        if (this.myStatues == Status.sendReject){
            this.myStatues= this.tempStatus;
        }

        if(MainSimulator.isDalo2Debug) {
            System.out.println(this.nodeId+" statues: "+this.myStatues+", agent time:"+this.time);
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
        Set<NodeId>commonNeighbors = getCommonNeighbors(senderId);

        for (NodeId nId:this.neighborsConstraint.keySet()) {
            if (!commonNeighbors.contains(nId) && !senderId.equals(nId)){
                this.acceptMap.put(nId,false);
            }
        }

    }

    private Set<NodeId> getCommonNeighbors(NodeId other) {

        Set<NodeId> ans = new HashSet<NodeId>();
        Set<NodeId> neighborsOfOther = this.infoOfAgents.get(other).getNeighborsConstraint().keySet();
        Set<NodeId> neighborsMe = this.neighborsConstraint.keySet();
        for (NodeId nId:neighborsMe) {
            if (neighborsOfOther.contains(nId)){
                ans.add(nId);
            }
        }

        return ans;
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
                System.out.println(this.nodeId+" clock is over, msg time:"+ msgAlgorithm.getTimeOfMsg());
            }
        }

        else if (msgAlgorithm instanceof  MsgDALOkLockRequest ) {
            MsgDALOkLockRequest msg = (MsgDALOkLockRequest)msgAlgorithm;
            boolean isSendLockToAgree = (boolean) msg.getContext();
            this.twoOpt= null;
            if (isSendLockToAgree) {
                this.myStatues = Status.sendLockOnBehalfOfCommit;

            } else {
                this.myStatues = Status.agreeToLock;
                if (MainSimulator.isDalo2Debug){
                    System.out.println(this.nodeId+" receive lock before lock expired from "+msgAlgorithm.getSenderId()+" msg time:"+ msgAlgorithm.getTimeOfMsg());
                }
            }

            this.commitedTo = msgAlgorithm.getSenderId();

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

    private boolean isAllAccept() {
        for (NodeId nId:this.acceptMap.keySet()) {
            if (!this.acceptMap.get(nId)){
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

        if (MainSimulator.isDalo2Debug){
            System.out.println(this.nodeId+" send an accept to: "+ commitedTo);
        }
    }

    private void sendLockRequestExceptCommited() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        Set<NodeId>commonNeighbors = getCommonNeighbors(commitedTo);

        Set<NodeId> toDebug = new HashSet<NodeId>();
        for (NodeId nId: this.neighborsConstraint.keySet()) {
            if (!commonNeighbors.contains(nId) && !this.commitedTo.equals(nId)){
                toDebug.add(nId);
                MsgDALOkLockRequest msg = new MsgDALOkLockRequest(this.nodeId, nId, false, this.timeStampCounter, this.time,null);
                msgsToInsertMsgBox.add(msg);
            }

        }
        if (MainSimulator.isDalo2Debug){
            System.out.println(this.nodeId+ " send lock request as a sub leader to: "+toDebug);
        }
        //commitedTo = null;
        outbox.insert(msgsToInsertMsgBox);
    }



    private void sendLockRequest() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        Set<NodeId> toDebug = new HashSet<NodeId>();
        for (NodeId nId: this.neighborsConstraint.keySet()) {
            MsgDALOkLockRequest msg = null;
            if (this.twoOpt.getNodeId2().equals(nId)){
                msg = new MsgDALOkLockRequest(this.nodeId, nId, true, this.timeStampCounter, this.time, twoOpt.getValueAssignmnet2());
                if (MainSimulator.isDalo2Debug){
                    System.out.println(this.nodeId+ " send lock request as a LEADER to: "+nId);
                }
            }else{
                msg = new MsgDALOkLockRequest(this.nodeId, nId, false, this.timeStampCounter, this.time, null);
                toDebug.add(nId);
            }
            msgsToInsertMsgBox.add(msg);
        }
        if (MainSimulator.isDalo2Debug){

        }
        System.out.println(this.nodeId+ " send lock request as a LEADER to: "+toDebug);
        outbox.insert(msgsToInsertMsgBox);
    }



    private void sendCommitMsg() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();

        NodeId receiver = twoOpt.getNodeId2();
        MsgDALOcommit msg = new MsgDALOcommit(this.nodeId, receiver, twoOpt, timeStampCounter, this.time);
        msgsToInsertMsgBox.add(msg);
        outbox.insert(msgsToInsertMsgBox);
    }

    private void sendValue() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        NodeId partner = twoOpt.getNodeId2();
        //Map<NodeId,Integer> context = new HashMap<NodeId,Integer>();
       // context.put(this.nodeId, this.valueAssignment);
        for (NodeId nId:this.neighborsConstraint.keySet()) {
            if (!nId.equals(partner)){

                MsgValueAssignmnet msg = new MsgValueAssignmnet(this.nodeId,nId,  this.valueAssignment, this.timeStampCounter,this.time);
                msgsToInsertMsgBox.add(msg);
            }
        }
        outbox.insert(msgsToInsertMsgBox);

    }
    private void sendUnlock() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        Set<NodeId>commonNeighbors = getCommonNeighbors(commitedTo);
        Set<NodeId> toDebug = new HashSet<NodeId>();

        for (NodeId nId:this.neighborsConstraint.keySet()) {
            if (!commonNeighbors.contains(nId)){
                MsgDALOUnlock msg = new MsgDALOUnlock(this.nodeId, nId, null, this.timeStampCounter, this.time);
                msgsToInsertMsgBox.add(msg);
            }
        }
        outbox.insert(msgsToInsertMsgBox);


    }


    private void sendRejectMsg() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        MsgDALOReject msg = new MsgDALOReject(this.nodeId, this.tempSender, null, this.timeStampCounter, this.time);
        msgsToInsertMsgBox.add(msg);
        outbox.insert(msgsToInsertMsgBox);
        if (MainSimulator.isDalo2Debug){
            System.out.println(this.nodeId+" send reject to "+this.tempSender);
        }
        this.tempSender = null;


    }

    private void sendSpamToRemoveRelockTimerFromMailer() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        MsgDALOkSpamToCloseTimer msg = new MsgDALOkSpamToCloseTimer(this.nodeId, this.nodeId, null, this.timeStampCounter, this.time);
        msg.setWithDelayToFalse();
        msgsToInsertMsgBox.add(msg);
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
