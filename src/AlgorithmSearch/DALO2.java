package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Main.MainSimulator;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgDALOInfo;
import Messages.MsgValueAssignmnet;

import java.util.*;

public class DALO2 extends AgentVariableSearch implements SelfCounterable {
    private static int boundForTimer =10000;
    private Status myStatues;
    private ArrayList<NodeId> coalitions; // coalitions
    private Map<NodeId,KOptInfo> neighborsInfo;
    //private HashMap<NodeId, Integer> selectedPartnerLockedValue; // if i try to create coalition
    private Integer lockedValue;
    private Find2Opt solution;



    // randoms
    private Random randomForCoalition;
    private Random randomForTimer;

    enum Status {
        waitForInitialValues,sendLocalInfo, turnOnRelockClock, waitForInfo;
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

        for (NodeId nId:this.neighborsConstraint.keySet()) {
            this.neighborsInfo.put(nId,null);
        }

        randomForCoalition = new Random((this.dcopId+1)*17+(this.nodeId.getId1()+1)*177+this.neighborsConstraint.keySet().size()*17);
        randomForTimer = new Random((this.dcopId+1)*18+(this.nodeId.getId1()+1)*173+this.neighborsConstraint.keySet().size()*143);
        //selectedPartnerLockedValue = new HashMap<NodeId,Integer>();


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

        if (msgAlgorithm instanceof MsgDALOInfo){
            this.updateInfoContext(msgAlgorithm);
        }


        return true;
    }


    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
        Status oldStatues = this.myStatues;
        if (this.myStatues == Status.waitForInitialValues && isAllValuesReceived()){
            this.myStatues = Status.sendLocalInfo;
        }

        if (this.myStatues == Status.waitForInfo && isAllInfoReceived()){
            this.myStatues = Status.turnOnRelockClock;
        }

        if (MainSimulator.isDalo2Debug && this.myStatues != oldStatues){
            System.out.println(this.nodeId+ " start with statues: "+this.myStatues+"____ time = "+this.time);
        }



    }

    @Override
    public boolean getDidComputeInThisIteration() {
        return myStatues == Status.sendLocalInfo || myStatues == Status.turnOnRelockClock;
    }

    @Override
    public boolean compute() {
        if (this.myStatues == Status.turnOnRelockClock){
            computeWhenFringeNodeValueChange_solve_construct_tree();

            return false;
        }
        return false;
    }

    @Override
    public void sendMsgs() {
        if(this.myStatues == Status.sendLocalInfo){
            sendLocalInfoMsgs();
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
    private void updateInfoContext(MsgAlgorithm msgAlgorithm) {
        MsgDALOInfo msg = (MsgDALOInfo) msgAlgorithm;
        KOptInfo info = (KOptInfo)msg.getContext();
        NodeId nId = msg.getSenderId();
        this.neighborsInfo.put(nId,info);
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

    private boolean isAllInfoReceived() {
        for (NodeId nId:this.neighborsInfo.keySet()) {
            if(this.neighborsInfo.get(nId) == null) {
                return false;
            }
        }
        return true;
    }

    //*************************---compute---*************************
    private void computeWhenFringeNodeValueChange_solve_construct_tree() {
        boolean isCreating2Opt = !this.coalitions.isEmpty();
        if (isCreating2Opt){
            int randomIndex = this.randomForCoalition.nextInt(this.coalitions.size());
            NodeId selectedPartnerNodeId = this.coalitions.get(randomIndex);
            solution = new Find2Opt(makeLocalOptInfo(), this.neighborsInfo.get(selectedPartnerNodeId));
            this.atomicActionCounter = solution.getAtomicActionCounter();



            if(MainSimulator.isDalo2Debug) {
                System.out.println(this.nodeId+" selected: "+selectedPartnerNodeId);
            }
            if (solution.getLR()<0){
                this.solution = null;
            }else{
                solution.constructBFSTree();
            }

        }else{
            this.solution = null;
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

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }

}

