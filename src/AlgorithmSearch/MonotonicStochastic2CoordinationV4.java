package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Main.MainSimulator;
import Messages.MsgAlgorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MonotonicStochastic2CoordinationV4 extends AgentVariableSearch implements SelfCounterable {

    enum Status {
        waitToSelectColor,
        canSelectColor,
        waitForAllColors,
        consistent,
        idle,
        waitForReply,
        changeAlone,
        doWhatPartnerSay,
        receiveOffer,
        replyToOffer,
        waitToFinishIteration
    }

    protected int selfCounter;
    protected Integer myColor;
    private HashMap<NodeId, Integer> neighborsColors;
    private Map<NodeId, Double> neighborsDocIdsT;
    private Map<NodeId, Double> neighborsDocIdsT1;
    private  Random docIdRandom;
    private double myDocId;
    private Status myStatues;


    private NodeId partnerNodeId;
    public static  char typeDecision = 'c' ;
    public static int moduleChangeColor=1;

    private Random rndForPartners;
    private HashMap<NodeId, KOptInfo> neighborsInfo;

    HashMap<NodeId, Integer> neighborCounters;
    private HashMap<NodeId, Integer> neighborPartnerCounters;



    public MonotonicStochastic2CoordinationV4(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        AMDLS_V1.typeDecision = 'c';
        updateAlgorithmHeader();
        updateAlgorithmData();
        updateAlgorithmName();
        resetAgentGivenParametersV3();
    }


    @Override
    protected void resetAgentGivenParametersV3() {
        myStatues = Status.waitToSelectColor;
        this.myColor = null;
        this.myDocId = this.nodeId.getId1();
        docIdRandom = new Random((dcopId+1)*17+this.nodeId.getId1()*177);
        selfCounter = 0;
        this.neighborsDocIdsT = new HashMap<NodeId,Double>();
        this.neighborsDocIdsT1 = new HashMap<NodeId,Double>();
        this.neighborsColors = new HashMap<NodeId,Integer>();
        neighborCounters = new HashMap<NodeId,Integer>();
        for (NodeId nId:this.neighborsConstraint.keySet()) {
            this.neighborsDocIdsT.put(nId,Double.valueOf(nId.getId1()));
            this.neighborsDocIdsT1.put(nId,null);
            this.neighborsColors.put(nId,null);
            neighborCounters.put(nId,0);
        }

    }


    @Override
    public void initialize() {
        if (canSetColor()){
            this.myColor = 1;
            this.myStatues = Status.canSelectColor;
            this.sendMsgs();
            if (MainSimulator.isMDC2CDebug){
                System.out.println(this.nodeId+", color: "+ this.myColor);
            }
            this.myStatues = Status.canSelectColor;

        }
    }



    protected boolean canSetColor() {

        Set<NodeId> neighborsThatHaveColor = getNeighborsThatHaveColor();
        Set<NodeId> neighborsIRequireToWait = neighborsWithSmallerIndexThenMe();

        for (NodeId nodeId : neighborsIRequireToWait) {
            if (!neighborsThatHaveColor.contains(nodeId)) {
                return false;
            }
        }
        return true;
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
        AgentVariable.algorithmHeader = "";
    }

    @Override
    public void updateAlgorithmData() {
        AgentVariable.algorithmData = "";
    }

    @Override
    public void updateAlgorithmName() {
        AgentVariable.AlgorithmName = "MS2C";
    }



    @Override
    public int getSelfCounterable() {
        return this.selfCounter;
    }
}
