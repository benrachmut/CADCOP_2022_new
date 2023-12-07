package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Messages.*;
import org.w3c.dom.Node;

import java.util.*;

public class DALO2 extends AgentVariableSearch implements SelfCounterable {
    enum Status {
       waitForAllInitValues, createInitKInfo, createKopt, createInitKInfoAndKopt, unlocked, locked, waitForAllKInfo
    }

    private Set<NodeId> coalitions ;
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




    }

    private void createCoalitions() {

        this.coalitions = new HashSet<NodeId>();
        for (NodeId nId : this.neighborsConstraint.keySet()) {
            if (this.nodeId.getId1() < nId.getId1()) {
                this.coalitions.add(nId);
            }
        }
    }

    @Override
    protected void resetAgentGivenParametersV3() {

        infoOfAgents = new HashMap<NodeId,KOptInfo>();
        for (NodeId nodeId:
             this.neighborsConstraint.keySet()) {
            infoOfAgents.put(nodeId,null);
        }
    }


    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgValueAssignmnet){
            updateMsgInContextValueAssignment(msgAlgorithm);
        }

        if (msgAlgorithm instanceof MsgDALOkInfo){
            updateMsgDALOkInfoInContext(msgAlgorithm);
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




    }

    @Override
    public boolean getDidComputeInThisIteration() {
        return this.myStatues == Status.createInitKInfo || this.myStatues == Status.createKopt || this.myStatues == Status.createInitKInfoAndKopt;
    }

    @Override
    public boolean compute() {
        if (this.myStatues == Status.createKopt || this.myStatues == Status.createInitKInfoAndKopt){
            boolean foundPartner = create2Opt();
            if (!foundPartner){
                this.myStatues = Status.unlocked;
            }
        }
        return false;
    }




    @Override
    public void sendMsgs() {
        if (this.myStatues == Status.createInitKInfo || this.myStatues == Status.createInitKInfoAndKopt){
            sendKInfoMsgs();
        }
        if (this.myStatues == Status.createKopt ||this.myStatues == Status.createInitKInfoAndKopt){
            sendLockRequest();
        }

    }




    @Override
    public void changeReceiveFlagsToFalse() {
        if (this.myStatues == Status.createInitKInfo){
            if (isAllInfoInMemory()){
                initCoalition();
            }else {
                this.myStatues = Status.waitForAllKInfo;
            }
        }

        if (this.myStatues == Status.createKopt ||this.myStatues == Status.createInitKInfoAndKopt){ //&& this.isLockRequetSetEmpty()){
            this.myStatues = Status.unlocked;
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




    //*******  updateMsgInContext *******

    private void updateMsgDALOkInfoInContext(MsgAlgorithm msgAlgorithm) {
        MsgDALOkInfo msg = (MsgDALOkInfo) msgAlgorithm;
        KOptInfo info = (KOptInfo)msg.getContext();
        NodeId nId = msg.getSenderId();
        this.infoOfAgents.put(nId,info);
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
    private boolean create2Opt() {


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



}
