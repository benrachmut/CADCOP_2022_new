package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Main.MainSimulator;
import Messages.*;

import java.util.*;

public class DALO2_workspace_v2 extends AgentVariableSearch implements SelfCounterable {
    private static int boundForTimer =10000;
    private Status myStatues;
    private ArrayList<NodeId> coalitions; // coalitions
    private Map<NodeId,KOptInfo> neighborsInfo;
    //private HashMap<NodeId, Integer> selectedPartnerLockedValue; // if i try to create coalition

    // reset when release
    private Integer lockedValue;
    private SolutionPackage mySolutionPackage;
    private SolutionPackage partnerSolutionPackage;
    private HashMap<NodeId, Boolean> lockSet;
    private HashMap<NodeId, Boolean> isFringeNeighbor;

    private NodeId lockedPartner;


    // randoms
    private Random randomForCoalition;
    private Random randomForTimer;
    private Integer changeCounter;
    //private Set<NodeId> nodesInTree;
    private MsgDALOValueAssignmnet msgDaloValueAssignmnet;
    private boolean gainFlag;
    private boolean lockingFlag;

    enum Status {
        waitForInitialValues,sendLocalInfo,
        turnOnRelockClock, waitForInfo, waitUnlocked,
        clockExpired, waitToConfirmAsLeader, lockGotSelected, lockNotSelected,
        waitToConfirmForPartner, waitLockedForValue, waitForCommit,
        acceptForPartner, sendCommitAndLockedValue, sendLocalViewUpdateAndResolve, sendLockedValue;
    }

    public void resetWhenMoveFromLockToUnlock(){
        lockedValue = null;
        mySolutionPackage= null;
        partnerSolutionPackage= null;
        lockSet = null;
        lockedPartner = null;

    }
    public DALO2_workspace_v2(int dcopId, int d, int agentId) {
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
        gainFlag = false;
    }


    @Override
    public void initialize() {
        resetAgentGivenParametersV3();
        super.initialize();
    }



    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgValueAssignmnet){

            this.updateMsgInContextValueAssignment(msgAlgorithm);
        }

        if (msgAlgorithm instanceof  MsgDALOValueAssignmnet){
            this.msgDaloValueAssignmnet = (MsgDALOValueAssignmnet)msgAlgorithm;
            updateInfoOfMsg();

        }
        if (msgAlgorithm instanceof MsgDALOInfo){
            this.updateMsgInContextInfo(msgAlgorithm);
        }

        if (msgAlgorithm instanceof MsgDALOSolutionPackage && this.myStatues == Status.waitUnlocked){
            this.updateMsgInContextSolutionPackage(msgAlgorithm);
        }

        if (msgAlgorithm instanceof MsgDALOAccept){
            this.updateMsgInContextAccept(msgAlgorithm);
            if (MainSimulator.isDalo2Debug){
                System.out.println(this.nodeId+" receive accept from: "+msgAlgorithm.getSenderId());
            }
        }

        if (msgAlgorithm instanceof MsgDALOLocalViewUpdate){
            updateSenderLocalView(msgAlgorithm);
        }

        /* what if not locked???
        if (msgAlgorithm instanceof MsgDALOSolutionPackage && this.myStatues != Status.waitUnlocked){
            this.updateMsgInContextSolutionPackage(msgAlgorithm);
        }
         */

        return true;
    }




    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
        Status oldStatues = this.myStatues;
        if (isLocked()){
            lockingFlag = true;
        }else{
            lockingFlag = false;
        }

        if (this.myStatues == Status.waitForInitialValues && isAllValuesReceived()){
            this.myStatues = Status.sendLocalInfo;
        }

        if ( msgAlgorithm instanceof MsgDALOValueAssignmnet){

            this.myStatues = Status.sendLocalViewUpdateAndResolve;

        }

        if (msgAlgorithm instanceof MsgDALOLocalViewUpdate){
            this.myStatues = Status.waitUnlocked;

        }

        if (this.myStatues == Status.waitForInfo && isAllInfoReceived() && !this.coalitions.isEmpty()){
            this.myStatues = Status.turnOnRelockClock;
        }


        /*
        if (msgAlgorithm instanceof MsgDALOLocalViewUpdate && !this.coalitions.isEmpty()) {
            boolean flag = false;

            if (this.myStatues == Status.waitUnlocked){
                this.myStatues = Status.turnOnRelockClock;
                flag=true;
            }

            if (this.isLocked() && flag){
                throw new RuntimeException("something with statues is not correct");
            }

            if(this.lockSet==null||this.lockSet.isEmpty()) {
                this.myStatues = Status.turnOnRelockClock;
            }

            if (this.lockSet!=null && !this.lockSet.isEmpty()){
                //this.myStatues = Status.turnOnRelockClockAndRelease; todo
            }
        }
        */


        if (this.myStatues == Status.waitForInfo && isAllInfoReceived() && this.coalitions.isEmpty()){
            this.myStatues = Status.waitUnlocked;
        }

        if (this.myStatues == Status.waitUnlocked)
        {
            if(msgAlgorithm instanceof MsgDALOSelfTimer){
                this.myStatues = Status.clockExpired;
                if (MainSimulator.isDalo2Debug){
                    System.out.println(this.nodeId+" clock is over, msg time:"+ msgAlgorithm.getTimeOfMsg());
                }
            }

            if(msgAlgorithm instanceof MsgDALOLockRequest){
                this.myStatues = Status.lockNotSelected;
                this.lockedPartner = msgAlgorithm.getSenderId();
            }

            if(msgAlgorithm instanceof MsgDALOSolutionPackage){
                this.myStatues = Status.lockGotSelected;
                this.lockedPartner = msgAlgorithm.getSenderId();
            }

        }

        if (msgAlgorithm instanceof MsgDALOAccept && isAllLockSetAccept()){
            if (this.myStatues == Status.waitToConfirmForPartner){
                this.myStatues = Status.acceptForPartner;
            }

            if (this.myStatues == Status.waitToConfirmAsLeader){
                this.myStatues = Status.sendCommitAndLockedValue;
            }
        }

        if (msgAlgorithm instanceof MsgDALOcommit){
            this.myStatues = Status.sendLockedValue;
        }



        if (MainSimulator.isDalo2Debug && this.myStatues != oldStatues){
            System.out.println(this.nodeId+ " start with statues: "+this.myStatues+"____ time = "+this.time);
        }



    }

    public boolean isLocked(){
        return this.myStatues == Status.waitToConfirmAsLeader ||
                this.myStatues == Status.waitToConfirmForPartner ||
                this.myStatues == Status.waitForCommit||
                this.myStatues == Status.waitLockedForValue ||
                this.myStatues == Status.sendLocalViewUpdateAndResolve ||
                this.myStatues == Status.sendLockedValue ||
                this.myStatues == Status.lockNotSelected ||
                this.myStatues == Status.lockGotSelected;

    }


    @Override
    public boolean getDidComputeInThisIteration() {
        return myStatues == Status.sendLocalInfo ||myStatues == Status.turnOnRelockClock||
                myStatues == Status.clockExpired || myStatues == Status.lockGotSelected ||
                this.myStatues == Status.lockNotSelected|| this.myStatues == Status.acceptForPartner||
                this.myStatues == Status.sendCommitAndLockedValue||
                this.myStatues == Status.sendLockedValue||
                this.myStatues == Status.sendLocalViewUpdateAndResolve;

    }

    @Override
    public boolean compute() {
        if (this.myStatues == Status.turnOnRelockClock ){
            computeWhenFringeNodeValueChange_solve_construct_tree();
        }
        if (myStatues == Status.clockExpired){

            computeWhenFringeNodeValueChange_solve_construct_tree_fix();
            computeCreateLockSet();
            this.lockedValue = this.mySolutionPackage.getLockedValue();
            if (MainSimulator.isDalo2Debug){
                System.out.println(this.nodeId+" current value is: "+this.valueAssignment+" and locked value is: "+this.lockedValue);
            }
        }

        if (myStatues == Status.lockGotSelected){
            computeCreateLockSet();
            this.lockedValue = this.mySolutionPackage.getLockedValue();
            if (MainSimulator.isDalo2Debug){
                System.out.println(this.nodeId+" current value is: "+this.valueAssignment+" and locked value is: "+this.lockedValue);
            }
        }

        if (this.myStatues == Status.lockNotSelected){
            this.lockedValue = this.valueAssignment;
            if (MainSimulator.isDalo2Debug){
                System.out.println(this.nodeId+" current value is: "+this.valueAssignment+" and locked value is: "+this.lockedValue);
            }
        }

        if (myStatues ==Status.sendCommitAndLockedValue ||  this.myStatues == Status.sendLockedValue){
            this.valueAssignment = this.lockedValue;
            this.changeCounter = this.changeCounter+1;
            this.computerUpdatePartnerValueInLocalView();
            this.updateInfoOfMyValue();
        }

        return false;
    }


    private void computerUpdatePartnerValueInLocalView() {
        int partnerVal = this.mySolutionPackage.getPartnerValue();
        NodeId partnerNId = this.mySolutionPackage.getPartnerId();
        MsgReceive<Integer> msgReceive = new MsgReceive<Integer>(partnerVal, 0);
        this.neighborsValueAssignment.put(partnerNId, msgReceive);
    }


    @Override
    public void sendMsgs() {
        if(this.myStatues == Status.sendLocalInfo){
            sendLocalInfoMsgs();
        }

        if(this.myStatues == Status.turnOnRelockClock){
            if (gainFlag) {
                sendSelfTimerMsg();
            }
        }

        if (myStatues == Status.clockExpired){
            sendLockRequestToLockSetAndSolutionPackage();
        }

        if (myStatues == Status.lockGotSelected){
            sendLockRequestToLockSet();
        }

        if (myStatues == Status.lockNotSelected){
            sendAcceptToLockedPartner();
        }

        if (myStatues == Status.acceptForPartner){
            sendAcceptToLeader();
        }

        if (myStatues ==Status.sendCommitAndLockedValue){
            sendCommitAndValue();
            gainFlag= false;
        }

        if(this.myStatues == Status.sendLockedValue){
            sendValue();
        }
        if(this.myStatues == Status.sendLocalViewUpdateAndResolve){
            sendLocalView();
            if (gainFlag) {
                sendSelfTimerMsg();
            }
        }




        if (myStatues == Status.lockGotSelected ){
            sendSpamToRemoveRelockTimerFromMailer();
        }






    }




    @Override
    public void changeReceiveFlagsToFalse() {

        Status oldStatus = this.myStatues;
        if (this.myStatues == Status.sendLocalInfo && isAllInfoReceived()){
            this.myStatues = Status.turnOnRelockClock;
            reactionToAlgorithmicMsgs();
        }

        if (this.myStatues == Status.sendLocalInfo && !isAllInfoReceived()){
            this.myStatues = Status.waitForInfo;
        }

        if(this.myStatues == Status.turnOnRelockClock){
            this.myStatues = Status.waitUnlocked;
        }
        if(this.myStatues == Status.clockExpired){
            this.myStatues = Status.waitToConfirmAsLeader;
        }
        if (myStatues == Status.lockGotSelected){
            this.myStatues = Status.waitToConfirmForPartner;
        }
        if (myStatues == Status.lockNotSelected){
            this.myStatues = Status.waitLockedForValue;
        }
        if (myStatues == Status.acceptForPartner){
            this.myStatues = Status.waitForCommit;
        }


        if (myStatues ==Status.sendCommitAndLockedValue || myStatues ==Status.sendLockedValue){
            this.myStatues = Status.waitUnlocked;
            this.resetWhenMoveFromLockToUnlock();
        }

        if (myStatues ==Status.sendLocalViewUpdateAndResolve){
            this.myStatues = Status.waitUnlocked;
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
            KOptInfo nInfo = this.neighborsInfo.get(sender);
            if (sender.equals(nId)){
                nInfo.updateValueAssignmnet(senderVal);
            }else{
                nInfo.updateLocalViewSingle(nId,new MsgReceive<Integer>(senderVal,msgDaloValueAssignmnet.getTimeStamp()));
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


    private void updateMsgInContextSolutionPackage(MsgAlgorithm msgAlgorithm) {
        MsgDALOSolutionPackage msg = (MsgDALOSolutionPackage) msgAlgorithm;
        this.mySolutionPackage = (SolutionPackage) msg.getContext();
        lockedPartner= this.mySolutionPackage.getPartnerId();
    }



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

    private boolean isAllLockSetAccept() {
        for (NodeId nId:this.lockSet.keySet()) {
            if(!this.lockSet.get(nId)) {
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

        if (!this.coalitions.isEmpty()){
            int randomIndex = this.randomForCoalition.nextInt(this.coalitions.size());
            NodeId selectedPartnerNodeId = this.coalitions.get(randomIndex);
            updateFringeNodeHashSet(selectedPartnerNodeId);
            Find2Opt solution = new Find2Opt(makeLocalOptInfo(), this.neighborsInfo.get(selectedPartnerNodeId));
            if (solution.getLR() > 0) {
                this.atomicActionCounter = solution.getAtomicActionCounter();
                this.mySolutionPackage = solution.createSolutionPackageFor1();
                this.partnerSolutionPackage = solution.createSolutionPackageFor2();
                this.gainFlag = true;
            } else {
                this.gainFlag = false;
            }
            if(MainSimulator.isDalo2Debug) {
                System.out.println(this.nodeId+" selected: "+selectedPartnerNodeId);
            }
        }else{
            this.gainFlag = false;
        }




    }
    private void computeWhenFringeNodeValueChange_solve_construct_tree_fix() {

        if (!this.coalitions.isEmpty()){
            int randomIndex = this.randomForCoalition.nextInt(this.coalitions.size());
            NodeId selectedPartnerNodeId = this.coalitions.get(randomIndex);
            updateFringeNodeHashSet(selectedPartnerNodeId);
            Find2Opt solution = new Find2Opt(makeLocalOptInfo(), this.neighborsInfo.get(selectedPartnerNodeId));
            if (solution.getLR() > 0) {
                //this.atomicActionCounter = solution.getAtomicActionCounter();
                this.mySolutionPackage = solution.createSolutionPackageFor1();
                this.partnerSolutionPackage = solution.createSolutionPackageFor2();
                this.gainFlag = true;
            } else {
                this.gainFlag = false;
            }
            if(MainSimulator.isDalo2Debug) {
                System.out.println(this.nodeId+" selected: "+selectedPartnerNodeId);
            }
        }else{
            this.gainFlag = false;
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


    private void computeCreateLockSet() {

        try {
            Set<NodeId> nodes = this.mySolutionPackage.getBfsChildren();
            this.lockSet = new HashMap<NodeId, Boolean>();
            for (NodeId node : nodes) {
                this.lockSet.put(node, false);
            }
        }catch (Exception e){
            int x = 3;
        }


    }



    private void updateInfoOfMyValue() {
        for (NodeId nId:this.neighborsInfo.keySet() ) {
            KOptInfo nInfo = this.neighborsInfo.get(nId);
            int myVal = new Integer(this.valueAssignment);
            nInfo.updateLocalViewSingle(this.nodeId,new MsgReceive<Integer>(myVal,0));
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

    private void sendLockRequestToLockSetAndSolutionPackage() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        boolean flag = false;
        NodeId partnerId = this.mySolutionPackage.getPartnerId();
        for (NodeId node:this.lockSet.keySet()) {
            Msg m = null;
            if (node.equals(partnerId)){
                m = new MsgDALOSolutionPackage(this.nodeId,node,this.partnerSolutionPackage,this.timeStampCounter,this.time);
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


    private void sendLockRequestToLockSet() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId node:this.lockSet.keySet()) {
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

    private void sendAcceptToLeader() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        MsgDALOAccept msg = new MsgDALOAccept(this.nodeId, this.mySolutionPackage.getPartnerId(), null, this.timeStampCounter, this.time);
        msgsToInsertMsgBox.add(msg);
        outbox.insert(msgsToInsertMsgBox);
    }
    private void sendSpamToRemoveRelockTimerFromMailer() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        MsgDALOSpamToCloseTimer msg = new MsgDALOSpamToCloseTimer(this.nodeId, this.nodeId, null, this.timeStampCounter, this.time);
        msg.setWithDelayToFalse();
        msgsToInsertMsgBox.add(msg);
        outbox.insert(msgsToInsertMsgBox);
    }



    private void sendCommitAndValue() {

        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId nId: this.neighborsConstraint.keySet()) {
            Msg msg = null;
            if (nId.equals(this.mySolutionPackage.getPartnerId())){
                msg = new MsgDALOcommit(this.nodeId,nId,null,this.timeStampCounter,this.time);
            }else{
                msg = new MsgDALOValueAssignmnet(this.nodeId,nId,this.valueAssignment,this.timeStampCounter,this.time,this.mySolutionPackage.getAllNodesInTree());
            }

            msgsToInsertMsgBox.add(msg);

        }
        outbox.insert(msgsToInsertMsgBox);
    }



    private void sendValue() {

        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        for (NodeId nId: this.neighborsConstraint.keySet()) {
            if (!nId.equals(this.mySolutionPackage.getPartnerId())){
                Msg msg = new MsgDALOValueAssignmnet(this.nodeId,nId,this.valueAssignment,this.timeStampCounter,this.time,this.mySolutionPackage.getAllNodesInTree());
                msgsToInsertMsgBox.add(msg);
            }
        }
        outbox.insert(msgsToInsertMsgBox);
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

