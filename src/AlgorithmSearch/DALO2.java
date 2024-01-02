package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Main.MainSimulator;
import Messages.*;

import java.util.*;

public class DALO2 extends AgentVariableSearch implements SelfCounterable {
    private static int boundForTimer =1000;
    private Status myStatues;
    private ArrayList<NodeId> coalitions; // coalitions
    private Map<NodeId,KOptInfo> neighborsInfo;

    // reset when release
    private Integer lockedValue;
    private SolutionPackage solutionPackageReceived;
    private HashMap<NodeId, Boolean> prevLockSet;
    private HashMap<NodeId, Boolean> lockSet;
    private HashMap<NodeId, Boolean> isFringeNeighbor;
    private NodeId lockedPartner;


    // randoms
    private Random randomForCoalition;
    private Random randomForTimer;
    private Integer changeCounter;
    //private Set<NodeId> nodesInTree;
    private MsgDALOValueAssignmnet msgDaloValueAssignmnet;
    private Find2Opt solution;
    private NodeId lockedPartnerToReject;
    private HashMap<NodeId, Boolean> lockSetAsPartner;

    enum Status {
        waitForInitialValues,sendLocalInfo,
        turnOnRelockClock,changeFlag, waitForInfo, sendUnlockAndTimer, waitUnlocked,
        sendLockAsLeader, waitLocked,
        sendAccept,
        sendRejectBackToLock,
        waitLockedForPartner,
        sendRejectBackToLockForPartner,
        sendLockAsPartner,
        sendAcceptToPartner,
        sendCommit, waitLockedAsLeader, sendRejectBackToLockedAsLeader, sendValue, reSolveAndSendLocalViewUpdate, sendUnlockAndTimerAndLocalView, sendUnlock;
    }

    public void resetWhenMoveFromLockToUnlock(){
        lockedValue = null;
        lockSet = null;
        lockedPartner = null;

    }
    public DALO2(int dcopId, int d, int agentId) {
        super(dcopId,d,agentId);
        resetAgentGivenParametersV3();
    }


    @Override
    protected void resetAgentGivenParametersV3() {
        updateAlgorithmHeader();
        updateAlgorithmData();
        updateAlgorithmName();
        //------------------------
        myStatues = Status.waitForInitialValues;
        createCoalitions();
        neighborsInfo = new HashMap<NodeId,KOptInfo>();
        isFringeNeighbor = new HashMap<NodeId,Boolean>();
        for (NodeId nId:this.neighborsConstraint.keySet()) {
            this.neighborsInfo.put(nId,null);
            isFringeNeighbor.put(nId,true);
        }

        randomForCoalition = new Random((this.dcopId+1)*17+(this.nodeId.getId1()+1)*177+this.neighborsConstraint.keySet().size()*17);
        randomForTimer = new Random((this.dcopId+1)*18+(this.nodeId.getId1()+1)*173+this.neighborsConstraint.keySet().size()*143);
        resetWhenMoveFromLockToUnlock();
        changeCounter=0;
        msgDaloValueAssignmnet=null;
        solution = null;
        lockedPartnerToReject = null;
    }


    @Override
    public void initialize() {
        resetAgentGivenParametersV3();
        this.sendValueInit();

        for (NodeId nId:
                this.neighborsConstraint.keySet()) {
            this.neighborsValueAssignment.put(nId,null);
        }
    }




    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {


        if (msgAlgorithm instanceof MsgValueAssignmnet){
            this.updateMsgInContextValueAssignment(msgAlgorithm);
        }


        if (msgAlgorithm instanceof MsgDALOLocalViewUpdate){
            updateSenderLocalView(msgAlgorithm);
        }
        if (msgAlgorithm instanceof  MsgDALOValueAssignmnet){
            this.msgDaloValueAssignmnet = (MsgDALOValueAssignmnet)msgAlgorithm;
            updateInfoOfMsg();
            if (MainSimulator.isDalo2Debug){
                System.out.println(msgAlgorithm);
            }
        }


        if (msgAlgorithm instanceof MsgDALOInfo){
            this.updateMsgInContextInfo(msgAlgorithm);

        }

        if (this.myStatues == Status.waitUnlocked && msgAlgorithm instanceof MsgDALOLockRequest){
            this.lockedPartner = msgAlgorithm.getSenderId();
        }

        if (this.myStatues == Status.waitUnlocked && msgAlgorithm instanceof MsgDALOSolutionPackage){
            this.solutionPackageReceived = (SolutionPackage) msgAlgorithm.getContext();
            this.lockedPartner = msgAlgorithm.getSenderId();
            this.lockedValue = solutionPackageReceived.getLockedValue();
            this.lockSetAsPartner = new HashMap<>();

            for (NodeId nId:solutionPackageReceived.getBfsChildren()) {
                this.lockSetAsPartner.put(nId,false);
            }

        }



        if (msgAlgorithm instanceof MsgDALOAccept && this.myStatues == Status.waitLockedAsLeader){
            this.lockSet.put(msgAlgorithm.getSenderId(),true);
        }

        if (msgAlgorithm instanceof MsgDALOAccept && this.myStatues == Status.waitLockedForPartner){
            this.lockSetAsPartner.put(msgAlgorithm.getSenderId(),true);
        }



        return true;
    }




    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

       Status oldStatues = this.myStatues;

        if (this.myStatues == Status.waitForInitialValues && isAllValuesReceived()){
            this.myStatues = Status.sendLocalInfo;
        }
        if (msgAlgorithm instanceof MsgDALOUnlock && msgAlgorithm.getSenderId().equals(lockedPartner) && this.myStatues!=Status.waitUnlocked){
            this.myStatues = Status.sendUnlock;
        }

        if (msgAlgorithm instanceof MsgDALOcommit){
            this.myStatues = Status.sendValue;
        }

        if (this.myStatues == Status.waitForInfo && isAllInfoReceived() ){
            this.myStatues = Status.changeFlag;
        }
        if (msgAlgorithm instanceof MsgDALOLocalViewUpdate){
            this.myStatues = Status.changeFlag;
        }

        if (msgAlgorithm instanceof MsgDALOAccept && this.myStatues == Status.waitLockedForPartner && isAllLockSetAccept(this.lockSetAsPartner)){
            this.myStatues = Status.sendAccept;
            this.lockSetAsPartner = new HashMap<>();

        }

        if (msgAlgorithm instanceof MsgDALOAccept  && this.myStatues == Status.waitLockedAsLeader && isAllLockSetAccept(this.lockSet)){
            this.myStatues = Status.sendCommit;
        }

        if (msgAlgorithm instanceof  MsgDALOValueAssignmnet && this.myStatues != Status.sendLocalInfo &&
                this.myStatues != Status.waitForInitialValues && this.myStatues != Status.waitForInfo ){
            this.myStatues = Status.reSolveAndSendLocalViewUpdate;
        }


        if (msgAlgorithm instanceof MsgDALOSelfTimer && this.myStatues ==Status.waitUnlocked){
            this.myStatues = Status.sendLockAsLeader;
            if (MainSimulator.isDalo2Debug){
                System.out.println(this.nodeId+" clock is over, msg time:"+ msgAlgorithm.getTimeOfMsg());
            }
        }




        if (this.myStatues == Status.waitUnlocked && msgAlgorithm instanceof MsgDALOLockRequest){
            this.myStatues = Status.sendAccept;
        }
/*
        if (msgAlgorithm instanceof MsgDALOLockRequest){
            boolean flag= false;
            if(this.myStatues == Status.waitLocked ){
                this.myStatues = Status.sendRejectBackToLock;
                flag= true;
            }
            if (this.myStatues == Status.waitLockedForPartner){
                this.myStatues = Status.sendRejectBackToLockForPartner;
                flag= true;
            }
            if (this.myStatues == Status.waitLockedAsLeader ){
                this.myStatues = Status.sendRejectBackToLockedAsLeader;
                flag= true;
            }
            if (flag){
                this.lockedPartnerToReject = msgAlgorithm.getSenderId();

            }
        }

*/




        if (this.myStatues == Status.waitUnlocked && msgAlgorithm instanceof MsgDALOSolutionPackage){
            this.myStatues = Status.sendLockAsPartner;
        }

        if (msgAlgorithm instanceof MsgDALOReject){
            this.myStatues = Status.sendUnlockAndTimer;
        }

/*
        if (this.myStatues != Status.waitUnlocked && this.myStatues != Status.sendLockAsPartner && !msgAlgorithm.getSenderId().equals(this.lockedPartner)
                && msgAlgorithm instanceof MsgDALOSolutionPackage){
            this.lockedPartnerToReject = msgAlgorithm.getSenderId();

            if(this.myStatues == Status.waitLocked){
                this.myStatues = Status.sendRejectBackToLock;
            }
            if (this.myStatues == Status.waitLockedForPartner){
                this.myStatues = Status.sendRejectBackToLockForPartner;
            }
            if (this.myStatues == Status.waitLockedAsLeader ){
                this.myStatues = Status.sendRejectBackToLockedAsLeader;
            }
            this.lockedPartnerToReject = msgAlgorithm.getSenderId();

        }

 */










            if (MainSimulator.isDalo2Debug && this.myStatues != oldStatues){
            System.out.println(this.nodeId+ " start with statues: "+this.myStatues+"____ time = "+this.time);
        }


    }


    @Override
    public boolean getDidComputeInThisIteration() {
        return myStatues == Status.sendLocalInfo || this.myStatues == Status.changeFlag || this.myStatues == Status.sendLockAsLeader
                || this.myStatues == Status.sendAccept||this.myStatues == Status.sendRejectBackToLock
                || this.myStatues == Status.sendLockAsPartner
                || this.myStatues == Status.sendRejectBackToLockForPartner
                || this.myStatues == Status.sendValue||
                this.myStatues == Status.sendRejectBackToLockedAsLeader||this.myStatues == Status.sendCommit
                || this.myStatues == Status.reSolveAndSendLocalViewUpdate||
                this.myStatues == Status.sendUnlock;


    }

    @Override
    public boolean compute() {
        if (this.myStatues == Status.changeFlag ||  this.myStatues == Status.reSolveAndSendLocalViewUpdate ){
            computeWhenFringeNodeValueChange_solve_construct_tree();
        }

        if (this.myStatues == Status.sendCommit){
            this.valueAssignment = this.lockedValue;
            this.changeCounter = this.changeCounter+1;
            NodeId partner = this.solution.getNodeId2();
            int partnerValue = this.solution.getValueAssignmnet2();
            this.computerUpdatePartnerValueInLocalView(partner,partnerValue);
            this.updateInfoOfMyValue();
            this.updateInfoOfPartner(partner);

        }

    if (this.myStatues == Status.sendValue) {
        this.valueAssignment = this.lockedValue;
        this.changeCounter = this.changeCounter + 1;
        NodeId partner = this.solutionPackageReceived.getPartnerId();
        int partnerValue = this.solutionPackageReceived.getPartnerValue();

        this.computerUpdatePartnerValueInLocalView(partner, partnerValue);
        this.updateInfoOfMyValue();
        this.updateInfoOfPartner(partner);

    }

        return false;
    }



    private void computerUpdatePartnerValueInLocalView(NodeId partnerNId,int partnerVal) {
        MsgReceive<Integer> msgReceive = new MsgReceive<Integer>(partnerVal, 0);
        this.neighborsValueAssignment.put(partnerNId, msgReceive);
    }


    private void updateInfoOfMyValue() {
        for (NodeId nId:this.neighborsInfo.keySet() ) {
            KOptInfo nInfo = this.neighborsInfo.get(nId);
            int myVal = new Integer(this.valueAssignment);
            nInfo.updateLocalViewSingle(this.nodeId,new MsgReceive<Integer>(myVal,0));
        }
    }

    private void updateInfoOfPartner(NodeId partner) {
        for (NodeId nId:this.neighborsInfo.keySet() ) {
            KOptInfo nInfo = this.neighborsInfo.get(nId);
            int myVal = new Integer(this.neighborsValueAssignment.get(partner).getContext());
            nInfo.updateLocalViewSingle(partner,new MsgReceive<Integer>(myVal,0));
        }
    }



    @Override
    public void sendMsgs() {

        if (this.myStatues == Status.sendUnlock){
            sendUnlockMsgs();
            sendSelfTimerMsg();
        }


        if(this.myStatues == Status.sendLocalInfo){
            sendLocalInfoMsgs();
        }

        if (this.myStatues == Status.sendUnlockAndTimer || this.myStatues ==Status.sendUnlockAndTimerAndLocalView){
            sendSelfTimerMsg();
            sendUnlockMsgs();

            if (this.myStatues ==Status.sendUnlockAndTimerAndLocalView){
                sendLocalView();
            }
        }

        if (this.myStatues == Status.sendLockAsLeader){
            sendLockRequestToLockSetAndSolutionPackage();
        }

        if (myStatues == Status.sendAccept){
            sendAcceptToLockedPartner();
        }


        if (this.myStatues == Status.sendLockAsPartner){
            sendLockRequestToLockSet();
        }


        if (this.myStatues == Status.sendRejectBackToLockForPartner || this.myStatues == Status.sendRejectBackToLock || this.myStatues == Status.sendRejectBackToLockedAsLeader) {
            sendReject();
            sendUnlockMsgs();
            sendSelfTimerMsg();
        }

        if (this.myStatues == Status.sendCommit){
            sendCommitAndValue();
        }

        if (this.myStatues == Status.sendValue){
            sendValue();
        }





    }




    @Override
    public void changeReceiveFlagsToFalse() {

        Status oldStatus = this.myStatues;
        if (this.myStatues == Status.sendLocalInfo && isAllInfoReceived()){
            this.myStatues = Status.turnOnRelockClock;
        }

        if (this.myStatues == Status.sendLocalInfo && !isAllInfoReceived()){
            this.myStatues = Status.waitForInfo;
        }

        if (this.myStatues == Status.sendUnlock){
            this.myStatues = Status.waitUnlocked;
        }
        if (this.myStatues == Status.sendUnlockAndTimer){
            this.myStatues = Status.waitUnlocked;
        }

        if (this.myStatues ==Status.sendUnlockAndTimerAndLocalView){
            this.myStatues = Status.waitUnlocked;
        }

        if (this.myStatues == Status.sendRejectBackToLock){
            this.myStatues = Status.waitLocked;
        }

        if (myStatues == Status.sendRejectBackToLockForPartner){
            this.myStatues = Status.waitLockedForPartner;
        }
        if (myStatues == Status.sendRejectBackToLockedAsLeader){
            this.myStatues = Status.waitLockedAsLeader;
        }

        if (myStatues == Status.sendLockAsPartner){
            myStatues = Status.waitLockedForPartner;
        }

        if (this.myStatues == Status.sendAccept){
            myStatues = Status.waitLocked;
        }

        if (this.myStatues == Status.sendLockAsLeader){
            myStatues = Status.waitLockedAsLeader;
        }


        if (this.myStatues == Status.sendCommit){
            myStatues = Status.waitUnlocked;
        }

        if (this.myStatues == Status.sendValue){
            myStatues = Status.waitUnlocked;

        }


        if (MainSimulator.isDalo2Debug && this.myStatues != oldStatus){
            System.out.println(this.nodeId+ " finish with statues: "+this.myStatues+"____ time = "+this.time);
        }
    }



    ///*************************---initialize---*************************


    private void createCoalitions() {
        this.coalitions = new ArrayList<NodeId>();
        for (NodeId nId : this.neighborsConstraint.keySet()) {
            if (this.nodeId.getId1() < nId.getId1()) {
                this.coalitions.add(nId);
            }
        }
    }



    ///*************************---updateMessageInContext---*************************


    private void updateInfoOfMsg() {

        for (NodeId nId: this.neighborsInfo.keySet()) {
            NodeId sender = msgDaloValueAssignmnet.getSenderId();
            int  senderVal = new Integer((Integer) msgDaloValueAssignmnet.getContext());
            if (this.myStatues != Status.waitForInfo && this.myStatues != Status.waitForInitialValues) {
                KOptInfo nInfo = this.neighborsInfo.get(sender);
                if (sender.equals(nId)) {
                    nInfo.updateValueAssignmnet(senderVal);
                } else {
                    nInfo.updateLocalViewSingle(nId, new MsgReceive<Integer>(senderVal, msgDaloValueAssignmnet.getTimeStamp()));
                }
            }

        }
    }

    private void updateMsgInContextInfo(MsgAlgorithm msgAlgorithm) {
        MsgDALOInfo msg = (MsgDALOInfo) msgAlgorithm;
        KOptInfo info = (KOptInfo)msg.getContext();
        NodeId nId = msg.getSenderId();
        this.neighborsInfo.put(nId,info);
        for (NodeId nodeId: info.getNeighborsConstraint().keySet()) {
            this.isFringeNeighbor.put(nodeId,false);
        }
    }

/*
    private void updateMsgInContextSolutionPackage(MsgAlgorithm msgAlgorithm) {
        MsgDALOSolutionPackage msg = (MsgDALOSolutionPackage) msgAlgorithm;
        this.mySolutionPackage = (SolutionPackage) msg.getContext();
        lockedPartner= this.mySolutionPackage.getPartnerId();
    }
*/


    private void updateMsgInContextAccept(MsgAlgorithm msgAlgorithm) {
        NodeId sender = msgAlgorithm.getSenderId();
        if (this.lockSet.containsKey(sender)){
            this.lockSet.put(sender,true);
        }
    }


    private void updateSenderLocalView(MsgAlgorithm msgAlgorithm) {
        MsgDALOLocalViewUpdate msg = (MsgDALOLocalViewUpdate) msgAlgorithm;
        TreeMap<NodeId,Integer> context = (TreeMap<NodeId, Integer>) msg.getContext();
        NodeId nodeIdChanger = context.firstKey();
        Integer valChanger = context.get(nodeIdChanger);
        MsgReceive<Integer>msgReceive = new MsgReceive<Integer>(valChanger,msgAlgorithm.getTimeStamp());
        for (NodeId nId:this.neighborsInfo.keySet()) {
            KOptInfo kInfo = neighborsInfo.get(nId);
            kInfo.updateLocalViewSingle(nodeIdChanger,msgReceive);
        }


    }


    ///*************************---changeReceiveFlagsToTrue---*************************
    private boolean isAllValuesReceived() {
        for (NodeId nId:this.neighborsValueAssignment.keySet()) {
            if(this.neighborsValueAssignment.get(nId) == null) {
                return false;
            }
        }
        return true;
    }

    private boolean isAllLockSetAccept(HashMap<NodeId, Boolean> input) {

            for (NodeId nId : input.keySet()) {
                if (!input.get(nId)) {
                    return false;
                }
            }

        return true;
    }

    private boolean isAllInfoReceived() {

        //if (MainSimulator.isDalo2Debug && nodeId.getId1()==9){
        //    System.out.println(this.neighborsInfo);
        //}
        for (NodeId nId:this.neighborsInfo.keySet()) {
            if(this.neighborsInfo.get(nId) == null) {
                return false;
            }
        }
        return true;
    }

    //*************************---compute---*************************
    private void computeWhenFringeNodeValueChange_solve_construct_tree() {

        if (!this.coalitions.isEmpty()) {
            int randomIndex = this.randomForCoalition.nextInt(this.coalitions.size());
            NodeId selectedPartnerNodeId = this.coalitions.get(randomIndex);
            updateFringeNodeHashSet(selectedPartnerNodeId);
            this.solution = new Find2Opt(makeLocalOptInfo(), this.neighborsInfo.get(selectedPartnerNodeId));

            this.atomicActionCounter = solution.getAtomicActionCounter();
            if (solution.getLR() > 0) {
                lockedValue = solution.getValueAssignmnet1();
                this.prevLockSet = this.lockSet;
                createLockSet();
                if (this.myStatues == Status.reSolveAndSendLocalViewUpdate) {
                    this.myStatues = Status.sendUnlockAndTimerAndLocalView;

                }else {
                    this.myStatues = Status.sendUnlockAndTimer;
                }
                if (MainSimulator.isDalo2Debug) {
                    System.out.println(this.nodeId + " selected: " + solution.getNodeId2() + " status is: " + this.myStatues);
                }
                return;
            }
        }
        this.myStatues = Status.waitUnlocked;
        if (MainSimulator.isDalo2Debug) {
            System.out.println(this.nodeId + " status is: "+this.myStatues+" try to solve but failed");
        }

    }




    private void createLockSet() {
        Set<NodeId> nodes = this.solution.createSolutionPackageFor1().getBfsChildren();
        this.lockSet = new HashMap<NodeId, Boolean>();
        for (NodeId node : nodes) {
            this.lockSet.put(node, false);
        }
    }
    private void updateFringeNodeHashSet(NodeId selectedPartnerNodeId) {
        for (NodeId nId:this.isFringeNeighbor.keySet() ) {
            if (nId.equals(selectedPartnerNodeId)){
                this.isFringeNeighbor.put(nId,true);
            }else {
                this.isFringeNeighbor.put(nId, false);
            }
        }
    }

    ///*************************---sendMsgs---*************************

    private void sendLocalInfoMsgs() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId nId: this.neighborsConstraint.keySet()) {
            MsgDALOInfo msg = new MsgDALOInfo(this.nodeId, nId, makeLocalOptInfo(), this.timeStampCounter, this.time);
            msgsToInsertMsgBox.add(msg);
        }
        outbox.insert(msgsToInsertMsgBox);
    }


    private void sendReject() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        msgsToInsertMsgBox.add( new MsgDALOSolutionPackage(this.nodeId,this.lockedPartnerToReject,null,this.timeStampCounter,this.time));
        outbox.insert(msgsToInsertMsgBox);
        lockedPartnerToReject = null;
    }

    private void sendLockRequestToLockSetAndSolutionPackage() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        boolean flag = false;
        NodeId partnerId = this.solution.getNodeId2();
        SolutionPackage sp = this.solution.createSolutionPackageFor2();
        for (NodeId node:this.lockSet.keySet()) {
            Msg m = null;
            if (node.equals(partnerId)){
                m = new MsgDALOSolutionPackage(this.nodeId,node,sp,this.timeStampCounter,this.time);
                flag = true;
            }else{
                m = new MsgDALOLockRequest(this.nodeId,node,null,this.timeStampCounter,this.time);
            }
            msgsToInsertMsgBox.add(m);
        }
        if (!flag){
            throw new RuntimeException("partner id needs to be in the tree");
        }

        outbox.insert(msgsToInsertMsgBox);

    }

    private void sendSelfTimerMsg() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();

        int rndTimer = randomForTimer.nextInt(this.boundForTimer);
        MsgDALOSelfTimer msg = new MsgDALOSelfTimer(this.nodeId, this.nodeId, null, this.timeStampCounter, this.time, rndTimer);
        msgsToInsertMsgBox.add(msg);
        if (MainSimulator.isDalo2Debug){
            System.out.println(this.nodeId+ " sent timer message at self_time:"+this.time+" with delay of: "+rndTimer );
        }
        outbox.insert(msgsToInsertMsgBox);

    }

    private void sendUnlockMsgs() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();

        for (NodeId node:this.neighborsConstraint.keySet()) {
            msgsToInsertMsgBox.add(new MsgDALOUnlock(this.nodeId,node,null,this.timeStampCounter,this.time));
        }
        outbox.insert(msgsToInsertMsgBox);

    }




    private void sendLockRequestToLockSet() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId node:this.lockSetAsPartner.keySet()) {
            msgsToInsertMsgBox.add(new MsgDALOLockRequest(this.nodeId,node,null,this.timeStampCounter,this.time));
        }
        outbox.insert(msgsToInsertMsgBox);
    }


    private void sendAcceptToLockedPartner() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        MsgDALOAccept msg = new MsgDALOAccept(this.nodeId, this.lockedPartner, null, this.timeStampCounter, this.time);
        msgsToInsertMsgBox.add(msg);
        outbox.insert(msgsToInsertMsgBox);
    }

 /*
    private void sendSpamToRemoveRelockTimerFromMailer() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        MsgDALOSpamToCloseTimer msg = new MsgDALOSpamToCloseTimer(this.nodeId, this.nodeId, null, this.timeStampCounter, this.time);
        msg.setWithDelayToFalse();
        msgsToInsertMsgBox.add(msg);
        outbox.insert(msgsToInsertMsgBox);
    }

*/

    private void sendCommitAndValue() {

        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId nId: this.neighborsConstraint.keySet()) {
            Msg msg = null;
            if (nId.equals(this.solution.getNodeId2())){
                msg = new MsgDALOcommit(this.nodeId,nId,null,this.timeStampCounter,this.time);
            }else{
                msg = new MsgDALOValueAssignmnet(this.nodeId,nId,this.valueAssignment,this.timeStampCounter,this.time,this.solution.createSolutionPackageFor1().getAllNodesInTree());
            }

            msgsToInsertMsgBox.add(msg);

        }
        outbox.insert(msgsToInsertMsgBox);
    }



    private void sendValueInit() {

        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId nId: this.neighborsConstraint.keySet()) {

                Msg msg = new MsgDALOValueAssignmnet(this.nodeId,nId,this.valueAssignment,this.timeStampCounter,this.time,null);
                msgsToInsertMsgBox.add(msg);

        }
        outbox.insert(msgsToInsertMsgBox);
    }

    private void sendValue() {

        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId nId: this.neighborsConstraint.keySet()) {
            if (!nId.equals(this.solutionPackageReceived.getPartnerId())){
                Msg msg = new MsgDALOValueAssignmnet(this.nodeId,nId,this.valueAssignment,this.timeStampCounter,this.time,this.solutionPackageReceived.getAllNodesInTree());
                msgsToInsertMsgBox.add(msg);
            }
        }
        outbox.insert(msgsToInsertMsgBox);
        //solutionPackageReceived = null;
    }





    private void sendLocalView() {

        List<Msg> msgsToInsertMsgBox = new ArrayList<>();


            for (NodeId nId :
                    this.neighborsConstraint.keySet()) {
                if (!msgDaloValueAssignmnet.getNodesInTree().contains(nId)) {
                    TreeMap<NodeId, Integer> context = new TreeMap<>();
                    context.put(msgDaloValueAssignmnet.getSenderId(), (Integer) msgDaloValueAssignmnet.getContext());
                    Msg msg = new MsgDALOLocalViewUpdate(this.nodeId, nId, context, this.timeStampCounter, this.time);
                    msgsToInsertMsgBox.add(msg);
                }
            }
            this.msgDaloValueAssignmnet = null;
            outbox.insert(msgsToInsertMsgBox);


    }

    ///*********************************************************************************************


    @Override
    public int getSelfCounterable() {

        return changeCounter;
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

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }

}

