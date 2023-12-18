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
    private boolean flag;

    enum Status {

        waitForAllInitValues,
        createInitKInfo,
        createKopt,
        waitForAllKInfo,
        createInitKInfoAndKopt,
        waitForClockOrLock,
        sendLocksInitiateCoalition,
        agreeToLock,
        sendLockOnBehalfOfAnotherLeader,
        waitForCommitMsg,
        waitForAcceptExceptCommitedTo,
        waitForAccept,
        sendReject,
        waitForValue,
        sendCommitAndValue,
        sendLocalViewUpdate,
        sendValueAndChangeValue,
        createKoptAfterLocalViewUpdate, sendUnlockAndStartRelockTimer, sendUnlock, idle
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
        flag=false;
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgValueAssignmnet){
            updateMsgInContextValueAssignment(msgAlgorithm);
        }

        if (msgAlgorithm instanceof MsgDALOkInfo){
            updateMsgDALOkInfoInContext(msgAlgorithm);
        }

        if (msgAlgorithm instanceof MsgDALOAccept && (this.myStatues == Status.waitForAcceptExceptCommitedTo
                || this.myStatues == Status.waitForAccept)){
            this.acceptMap.put(msgAlgorithm.getSenderId(),true);
        }


        if (msgAlgorithm instanceof MsgDALOcommit){

            //updateValAndTempNeighborValueA(msgAlgorithm);

            MsgDALOcommit msg = (MsgDALOcommit)msgAlgorithm;
            twoOpt = (Find2Opt) msg.getContext();

            MsgReceive<Integer> msgReceive = new MsgReceive<Integer>(twoOpt.getValueAssignmnet1(), 0);
            NodeId senderId = msgAlgorithm.getSenderId();

            this.neighborsValueAssignment.put(senderId, msgReceive);
            tempNeighborValueA.put(senderId,this.neighborsValueAssignment.get(senderId));


            //updateMsgInContextValueAssignment(msgAlgorithm);
        }

        if ((msgAlgorithm instanceof MsgValueAssignmnet && this.myStatues!=Status.waitForAllInitValues)) {

            updateValAndTempNeighborValueA(msgAlgorithm);
        }

        if (msgAlgorithm instanceof MsgDALOLocalViewUpdate && this.myStatues != Status.waitForCommitMsg){
            updateLocalViewsInInfo(msgAlgorithm);

        }


        if (MainSimulator.isDalo2Debug) {

            if (msgAlgorithm instanceof MsgDALOkInfo == false && msgAlgorithm instanceof MsgValueAssignmnet == false) {
                System.out.println(msgAlgorithm + "; MSG TIME: " + msgAlgorithm.getTimeOfMsg());
            }
        }

        return true;
    }




    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {


        Status oldStatus = this.myStatues;
        if (this.myStatues == Status.waitForAllInitValues && isAllValuesReceived()){
            this.myStatues = Status.createInitKInfo;
            flag = true;
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
                || this.myStatues == Status.waitForAcceptExceptCommitedTo)){

            this.tempSender = msgAlgorithm.getSenderId();
            this.tempStatus = this.myStatues;
            this.myStatues = Status.sendReject;

        }

        if (msgAlgorithm instanceof MsgDALOUnlock && msgAlgorithm.getSenderId().equals(commitedTo)){
            this.myStatues= Status.sendUnlock;
        }

        if (msgAlgorithm instanceof MsgDALOAccept && this.myStatues == Status.waitForAcceptExceptCommitedTo && isAllAccept() ){
            this.myStatues = Status.agreeToLock;
        }

        if (msgAlgorithm instanceof MsgDALOAccept && this.myStatues == Status.waitForAccept && isAllAccept()){
            this.myStatues = Status.sendCommitAndValue;
        }

        if (msgAlgorithm instanceof MsgDALOReject){
            this.myStatues = Status.sendUnlockAndStartRelockTimer;
        }

        if (msgAlgorithm instanceof MsgValueAssignmnet && this.myStatues!=Status.waitForAllInitValues
                && this.myStatues!=Status.createInitKInfo && this.myStatues!=Status.createInitKInfoAndKopt && flag){
            this.myStatues = Status.sendLocalViewUpdate;
        }


        if (msgAlgorithm instanceof MsgDALOcommit){
            this.myStatues = Status.sendValueAndChangeValue;
        }


        if (msgAlgorithm instanceof MsgDALOLocalViewUpdate){
            this.myStatues = Status.createKoptAfterLocalViewUpdate;
        }


        if (MainSimulator.isDalo2Debug && this.myStatues != oldStatus){
            if (this.myStatues!= Status.waitForAllInitValues && this.myStatues!= Status.waitForAllKInfo && this.myStatues!= Status.waitForAllInitValues) {
                System.out.println(this.nodeId + " start from " + oldStatus + " to " + this.myStatues);
            }
        }




    }


    @Override
    public boolean getDidComputeInThisIteration() {
        return this.myStatues == Status.createInitKInfo || this.myStatues == Status.createKopt || this.myStatues == Status.createInitKInfoAndKopt        ||
                this.myStatues == Status.agreeToLock||this.myStatues == Status.sendLockOnBehalfOfAnotherLeader ||this.myStatues == Status.sendLocksInitiateCoalition ||         this.myStatues == Status.sendLockOnBehalfOfAnotherLeader ||  this.myStatues == Status.agreeToLock
                || this.myStatues  ==Status.sendLocksInitiateCoalition ||this.myStatues  ==Status.sendReject || this.myStatues  ==Status.sendCommitAndValue || this.myStatues == Status.sendLocalViewUpdate || this.myStatues == Status.sendValueAndChangeValue
                || this.myStatues == Status.createKoptAfterLocalViewUpdate ||this.myStatues == Status.sendUnlockAndStartRelockTimer ||this.myStatues == Status.sendUnlock;

    }

    @Override
    public boolean compute() {
        if (this.myStatues == Status.createKopt || this.myStatues == Status.createInitKInfoAndKopt ||  this.myStatues == Status.createKoptAfterLocalViewUpdate){
            computeUpdate2Opt();
            if (MainSimulator.isDalo2Debug && this.twoOpt!=null){
                System.out.println(this+ " computed 2 opt with "+this.twoOpt.getNodeId2());
            }
            return true;
        }

        if (this.myStatues == Status.sendCommitAndValue) {
            this.valueAssignment = this.twoOpt.getValueAssignmnet1();
            computeUpdateUsingTwoOpt(2);
            return true;
        }


        if (this.myStatues == Status.sendValueAndChangeValue){
            this.valueAssignment = this.twoOpt.getValueAssignmnet2();
            computeUpdateUsingTwoOpt(1);
            return true;
        }
        if(this.myStatues == Status.sendLocalViewUpdate){

            NodeId nodeId = computeGetTheSingleNodeIdFromMap(tempNeighborValueA);
            MsgReceive<Integer> msgReceive = this.neighborsValueAssignment.get(nodeId);
            updateInfos(nodeId, msgReceive);

        }

        return false;
    }




    @Override
    public void sendMsgs() {

        if (this.myStatues == Status.createInitKInfo || this.myStatues == Status.createInitKInfoAndKopt){
            sendKInfoMsgs();
        }
        if ((this.myStatues == Status.createKopt || this.myStatues == Status.createKoptAfterLocalViewUpdate || this.myStatues == Status.createInitKInfoAndKopt) && this.twoOpt!=null){
            sendSelfTimerMsg();
        }

        if (this.myStatues == Status.agreeToLock){
            sendAccept();
            //this.myStatues = Status.waitForCommitMsg;
        }

        if (this.myStatues == Status.sendLockOnBehalfOfAnotherLeader){
            sendLockRequestExceptCommited();
            partOfCoalition = true;
            // this.myStatues = Status.waitForAcceptExecptCommitedTo;
            //resetAcceptMap(this.commitedTo);
        }

        if (this.myStatues == Status.sendLocksInitiateCoalition){
            sendLockRequest();
        }

        if ( this.myStatues ==Status.agreeToLock ||this.myStatues == Status.sendLockOnBehalfOfAnotherLeader){
            sendSpamToRemoveRelockTimerFromMailer();

        }

        if (this.myStatues  == Status.sendReject) {
            sendRejectMsg();
        }

        if (this.myStatues == Status.sendCommitAndValue){
            sendCommitMsg();
            sendValue(twoOpt.getNodeId2());
            twoOpt = null;
        }

        if (this.myStatues == Status.sendValueAndChangeValue){
            sendValue(twoOpt.getNodeId1());
            twoOpt = null;
        }

        if (this.myStatues == Status.sendLocalViewUpdate){
            NodeId whoChange = computeGetTheSingleNodeIdFromMap(tempNeighborValueA);
            MsgReceive<Integer> whatIsTheChange = this.neighborsValueAssignment.get(whoChange);

            sendLocalViewUpdate(whoChange, whatIsTheChange);// TODO: need to send local view update; node id = who changed
            tempNeighborValueA = new HashMap<NodeId,MsgReceive<Integer>>();
        }
        if (this.myStatues == Status.sendUnlockAndStartRelockTimer || this.myStatues == Status.sendUnlock){

            if (this.myStatues == Status.sendUnlockAndStartRelockTimer){
                sendSelfTimerMsg();
            }
            sendUnlock();
            this.acceptMap = new HashMap<>();
            commitedTo = null;
        }

        if (this.myStatues == Status.sendUnlock){
            sendUnlock();
            this.acceptMap = new HashMap<>();
            commitedTo = null;
        }
    }

    private void sendLocalViewUpdate(NodeId whoChange, MsgReceive<Integer> whatIsTheChange) {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        Map<NodeId,MsgReceive<Integer>> context = new HashMap<NodeId,MsgReceive<Integer>>();
        context.put(whoChange,whatIsTheChange);
        for (NodeId nodeId: this.neighborsConstraint.keySet()){
            if (!nodeId.equals(whoChange)) {
                MsgDALOLocalViewUpdate msg = new MsgDALOLocalViewUpdate(this.nodeId, nodeId, context, this.timeStampCounter, this.time);
                msgsToInsertMsgBox.add(msg);
            }
        }


        outbox.insert(msgsToInsertMsgBox);

    }


    @Override
    public void changeReceiveFlagsToFalse() {
        Status oldStatus = this.myStatues;
        if (this.myStatues == Status.createInitKInfo){
            if (isAllInfoInMemory()){
                //initCoalition(); TODO
            }else {
                this.myStatues = Status.waitForAllKInfo;
            }
        }

        if ((this.myStatues == Status.createKopt || this.myStatues == Status.createKoptAfterLocalViewUpdate || this.myStatues == Status.createInitKInfoAndKopt)){
            this.myStatues = Status.waitForClockOrLock;
        }


        if (this.myStatues == Status.agreeToLock){
            if (partOfCoalition){
                this.myStatues = Status.waitForCommitMsg;
            }else {
                this.myStatues = Status.waitForValue;
            }
        }

        if (this.myStatues == Status.sendLockOnBehalfOfAnotherLeader){
            this.myStatues = Status.waitForAcceptExceptCommitedTo;
            resetAcceptMap(this.commitedTo);
        }

        if (this.myStatues == Status.sendLocksInitiateCoalition){
            this.myStatues = Status.waitForAccept;
            resetAcceptMap();
        }

        if (this.myStatues == Status.sendReject){
            this.myStatues= this.tempStatus;
        }

        if (this.myStatues == Status.sendCommitAndValue || this.myStatues == Status.sendValueAndChangeValue || this.myStatues == Status.sendLocalViewUpdate){
            this.myStatues = Status.idle;
            this.commitedTo = null;
            this.twoOpt=null;
            tempNeighborValueA = new HashMap<NodeId,MsgReceive<Integer>>();


        }

        if (this.myStatues == Status.sendUnlockAndStartRelockTimer){
            this.myStatues = Status.waitForClockOrLock;
        }
        if(MainSimulator.isDalo2Debug && this.myStatues!=oldStatus) {
            System.out.println(this.nodeId+" end changed from "+oldStatus+" to "+ this.myStatues);
        }





    }





    //*******  updateMessageInContext *******

    private void updateLocalViewsInInfo(MsgAlgorithm msgAlgorithm) {
        Map<NodeId,MsgReceive<Integer>> context = (Map<NodeId, MsgReceive<Integer>>) msgAlgorithm.getContext();
        NodeId nId = new ArrayList<NodeId>(context.keySet()).get(0);
        MsgReceive<Integer> msgReceive = context.get(nId);
        updateInfos(nId,msgReceive);
    }

    public void updateValAndTempNeighborValueA(MsgAlgorithm msgAlgorithm){
        updateMsgInContextValueAssignment(msgAlgorithm);
        NodeId senderId = msgAlgorithm.getSenderId();
        tempNeighborValueA.put(senderId,this.neighborsValueAssignment.get(senderId));
    }

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
            this.myStatues = Status.sendLocksInitiateCoalition;
            if (MainSimulator.isDalo2Debug){
                System.out.println(this.nodeId+" clock is over, msg time:"+ msgAlgorithm.getTimeOfMsg());
            }
        }

        else if (msgAlgorithm instanceof  MsgDALOkLockRequest ) {
            MsgDALOkLockRequest msg = (MsgDALOkLockRequest)msgAlgorithm;
            boolean isSendLockToAgree = (boolean) msg.getContext();
            this.twoOpt= null;
            if (isSendLockToAgree) {
                this.myStatues = Status.sendLockOnBehalfOfAnotherLeader;

            } else {
                this.myStatues = Status.agreeToLock;
                if (MainSimulator.isDalo2Debug){
                    System.out.println(this.nodeId+" receive lock before lock expired from "+msgAlgorithm.getSenderId()+" msg time:"+ msgAlgorithm.getTimeOfMsg());
                }
            }

            this.commitedTo = msgAlgorithm.getSenderId();

        }
        else if(msgAlgorithm instanceof MsgDALOLocalViewUpdate == false){
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
    private void computeUpdate2Opt() {
        boolean isCreating2Opt = !this.coalitions.isEmpty();
        if (isCreating2Opt){
            int randomIndex = this.randomForCoalition.nextInt(this.coalitions.size());
            NodeId selectedPartnerNodeId = this.coalitions.get(randomIndex);
            this.twoOpt = new Find2Opt(makeMyKOptInfo(), this.infoOfAgents.get(selectedPartnerNodeId));
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


    private void computeUpdateUsingTwoOpt(int whichNode) {
        NodeId partnerNId = null;
        Integer partnerVal = null;
        if (whichNode==1){
            partnerNId = twoOpt.getNodeId1();
            partnerVal = twoOpt.getValueAssignmnet1();

        }else {
            partnerNId = twoOpt.getNodeId2();
            partnerVal = twoOpt.getValueAssignmnet2();

        }

        MsgReceive<Integer> input_ = new MsgReceive<Integer>(partnerVal,0);
        this.neighborsValueAssignment.put(partnerNId,input_);
        updateInfos(partnerNId,input_);
    }

    public static NodeId computeGetTheSingleNodeIdFromMap(Map<NodeId, MsgReceive<Integer>> input_){
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

    private void sendValue(NodeId whoNotToSend) {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        NodeId partner = whoNotToSend;
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

        for (NodeId nId:this.acceptMap.keySet()) {
            MsgDALOUnlock msg = new MsgDALOUnlock(this.nodeId, nId, null, this.timeStampCounter, this.time);
            msgsToInsertMsgBox.add(msg);
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
