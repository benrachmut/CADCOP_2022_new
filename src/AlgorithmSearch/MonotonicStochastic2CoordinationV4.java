package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Main.MainSimulator;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgInfoStatuesMonoStoch2;
import Messages.MsgReceive;

import java.util.*;

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
    private HashMap<NodeId, Integer> neighborsColorsT1;

    private Map<NodeId, Double> neighborsDocIdsT;
    private Map<NodeId, Double> neighborsDocIdsT1;
    private  Random docIdRandom;
    private Double myDocId;
    private Double myDocIdT1;

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
        selfCounter = 0;
        this.neighborsDocIdsT = new HashMap<NodeId,Double>();
        this.neighborsDocIdsT1 = new HashMap<NodeId,Double>();
        this.neighborsColors = new HashMap<NodeId,Integer>();
        this.neighborsColorsT1= new HashMap<NodeId,Integer>();
        this.neighborCounters = new HashMap<NodeId,Integer>();
        for (NodeId nId:this.neighborsConstraint.keySet()) {
            this.neighborsDocIdsT.put(nId,Double.valueOf(nId.getId1()));
            this.neighborsDocIdsT1.put(nId,null);
            this.neighborsColors.put(nId,null);
            this.neighborsColorsT1.put(nId,null);
            this.neighborCounters.put(nId,0);
        }
        docIdRandom = new Random((dcopId+1)*17+this.nodeId.getId1()*177);
        this.myDocIdT1 = null; //docIdRandom.nextDouble();
        this.myDocId = Double.valueOf(this.nodeId.getId1());
        if (MainSimulator.isMonoStochInit && !this.neighborsConstraint.keySet().isEmpty()){
            printNeighbors();
        }

    }

    private void printNeighbors() {
        System.out.print("N(A_"+this.nodeId.getId1()+"):");
        for (NodeId nId: this.neighborsConstraint.keySet()) {
            System.out.print("A_"+nId.getId1()+",");
        }
        System.out.println();
    }





    @Override
    public String toString() {
        return "A"+this.nodeId.getId1()+": {statues:"+this.myStatues+"}, "+"{docId:"+this.myDocId+"},"+"{color:"+myColor+"}" ;
                /*
                "MonotonicStochastic2CoordinationV4{" +
                "selfCounter=" + selfCounter +
                ", myColor=" + myColor +
                ", neighborsColors=" + neighborsColors +
                ", neighborsColorsT1=" + neighborsColorsT1 +
                ", neighborsDocIdsT=" + neighborsDocIdsT +
                ", neighborsDocIdsT1=" + neighborsDocIdsT1 +
                ", docIdRandom=" + docIdRandom +
                ", myDocId=" + myDocId +
                ", myStatues=" + myStatues +
                ", partnerNodeId=" + partnerNodeId +
                ", rndForPartners=" + rndForPartners +
                ", neighborsInfo=" + neighborsInfo +
                ", neighborCounters=" + neighborCounters +
                ", neighborPartnerCounters=" + neighborPartnerCounters +
                '}';
                */

    }

    @Override
    public void initialize() {

        if (iAmMinDocId()){
            //canSetColor();
            int tempValAssignment = this.valueAssignment;
            basicValueChange(true);
            this.valueAssignment =tempValAssignment ;
            this.myColor = 1;
            this.myStatues = Status.canSelectColor;
            this.sendMsgs();
            if (MainSimulator.isMonoStochComputationDebug){
                System.out.println(this.toString());
            }
            this.myStatues = Status.waitForAllColors;
        }
    }

    private void basicValueChange(boolean isChangeAlone) {
        this.myDocId = this.myDocIdT1;
        this.myDocIdT1 = docIdRandom.nextDouble();
        selfCounter = selfCounter +1;
        if (isChangeAlone) {
            changeValueAssignment();
        }else{
            doWhatPartnerSay();
        }
    }

    private void doWhatPartnerSay() {
        System.err.println("DIDNT DO doWhatPartnerSay");
    }

    private void changeValueAssignment() {
        if (typeDecision == 'a'|| typeDecision == 'A') {
            this.valueAssignment = getCandidateToChange_A();
        }
        if (typeDecision == 'b'|| typeDecision == 'B') {
            this.valueAssignment = getCandidateToChange_B();
        }
        if (typeDecision == 'c' || typeDecision == 'C') {
            this.valueAssignment = getCandidateToChange_C();
        }
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        NodeId sender = msgAlgorithm.getSenderId();

        if (msgAlgorithm instanceof MsgInfoStatuesMonoStoch2){
            MsgInfoStatuesMonoStoch2 msg = (MsgInfoStatuesMonoStoch2)msgAlgorithm;
            Map<String, Double> info = (Map<String, Double>) msg.getContext();
            updateColor(info,sender);
            updateValueAssignment(info,sender, msg.getTimeStamp());
            updateSelfCounter(info,sender);
            updateDocId(info,sender);
        }

        return true;
    }



    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

        if (this.myStatues == Status.waitToSelectColor){
            if (this.canSetColor()){
                this.myStatues = Status.canSelectColor;
            }
        }
        if (this.myStatues == Status.waitForAllColors){
            if (allNeighborsHaveColor()) {
                if (isConsistent()) {
                    this.myStatues = Status.consistent;
                }else{
                    this.myStatues = Status.idle;
                }
            }

        }

    }


    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {


        return 0;
    }

    private void updateDocId(Map<String, Double> info, NodeId sender) {
        Double nDocId = info.get("doc id");
        if (this.neighborsDocIdsT.get(sender)==null){
            this.neighborsDocIdsT.put(sender,nDocId);
        }else{
            this.neighborsDocIdsT1.put(sender,nDocId);
        }

    }

    private void updateSelfCounter(Map<String, Double> info, NodeId sender) {

        int nCounter = info.get("self counter").intValue();
        this.neighborCounters.put(sender,nCounter);
    }

    private void updateValueAssignment(Map<String, Double> info, NodeId sender,int timestamp) {
        int valAssignment = info.get("value assignment").intValue();
        MsgReceive<Integer> msgReceive = new MsgReceive<Integer>(valAssignment, timestamp);
        this.neighborsValueAssignmnet.put(sender, msgReceive);
    }

    private void updateColor(Map<String, Double> info, NodeId sender) {
        if (info.containsKey("color")){
            if (this.myStatues != Status.waitToSelectColor && this.myStatues != Status.waitForAllColors) {
                throw new RuntimeException("should not receive color in this statues");
            }
            int color = info.get("color").intValue();
            if (this.neighborsColors.get(sender) == null) {
                this.neighborsColors.put(sender, color);
            } else {
                this.neighborsColorsT1.put(sender, color);
            }

        }
    }


    @Override
    public boolean getDidComputeInThisIteration() {
        return this.myStatues == Status.canSelectColor || this.myStatues == Status.consistent || this.myStatues == Status.changeAlone
                || this.myStatues == Status.doWhatPartnerSay || this.myStatues == Status.replyToOffer ;
    }

    @Override
    public boolean compute() {
        if (this.myStatues == Status.canSelectColor){
            basicValueChange(true);
            chooseColor();
        }
        return true;
    }



    @Override
    public void sendMsgs() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();

        if (this.myStatues == Status.canSelectColor){
            createMsgsWithColor(msgsToInsertMsgBox);
        }

        if(!msgsToInsertMsgBox.isEmpty()) {
            outbox.insert(msgsToInsertMsgBox);
        }

    }

    private void createMsgsWithColor(List<Msg> msgsToInsertMsgBox) {
        Map<String,Double> info = createInfo();
        for (NodeId receiverNodeId : neighborsConstraint.keySet()) {
            MsgInfoStatuesMonoStoch2 msg = new MsgInfoStatuesMonoStoch2
                    (this.nodeId, receiverNodeId, info, this.timeStampCounter, this.time);
            msgsToInsertMsgBox.add(msg);
        }
    }

    private Map<String, Double> createInfo() {
        Map<String, Double> ans = new HashMap<String, Double>();
        ans.put("color", Double.valueOf(this.myColor));
        ans.put("value assignment", Double.valueOf(this.valueAssignment));
        ans.put("self counter", Double.valueOf(this.selfCounter));
        ans.put("doc id", this.myDocId);
        return ans;
    }



    @Override
    public void changeReceiveFlagsToFalse() {
        Status prevStatus = this.myStatues;

        changeAfterCanSelectColor();



        if (MainSimulator.isMonoStochComputationDebug){
            System.out.println("A_"+this.nodeId.getId1()+" status: <"+prevStatus+"-->"+this.myStatues);
        }




    }

    private void changeAfterCanSelectColor() {
        if (this.myStatues == Status.canSelectColor){
            if (!allNeighborsHaveColor()){
                this.myStatues = Status.waitForAllColors;
            }else{
                if (isConsistent()) {
                    this.myStatues = Status.consistent;
                }else{
                    this.myStatues = Status.idle;
                }
            }
        }
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
// -------------------******************************************-------------------
// -------------------******************************************-------------------
// -------------------************ TOP DOWN METHODS ************-------------------
// -------------------******************************************-------------------
// -------------------******************************************-------------------


// -------------------************ colors  ************-------------------

    protected boolean canSetColor() {
        if (!haveDocIdFromAll()){
            return false;
        }

        Set<NodeId> neighborsThatHaveColor = getNeighborsThatHaveColor();
        Set<NodeId> neighborsIRequireToWait = neighborsWithSmallerIndexThenMe();

        for (NodeId nodeId : neighborsIRequireToWait) {
            if (!neighborsThatHaveColor.contains(nodeId)) {
                return false;
            }
        }
        return true;
    }

    protected Set<NodeId> getNeighborsThatHaveColor() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (Map.Entry<NodeId, Integer> e : this.neighborsColors.entrySet()) {
            if (e.getValue() != null) {
                ans.add(e.getKey());
            }
        }
        return ans;
    }

    private Set<NodeId> neighborsWithSmallerIndexThenMe() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (NodeId nodeId : neighborsConstraint.keySet()) {
            if (neighborsDocIdsT.get(nodeId) < this.myDocId) {
                ans.add(nodeId);
            }
        }
        return ans;
    }

    private boolean haveDocIdFromAll() {
        for (NodeId nodeId:  this.neighborsDocIdsT.keySet()) {
            if (this.neighborsDocIdsT.get(nodeId) == null){
                return false;
            }
        }
        return true;
    }



    private boolean iAmMinDocId() {
        Collection <Double> docIds = this.neighborsDocIdsT.values();

        Double minDocIdFromNeighbors = Collections.min(docIds);
        return this.myDocId < minDocIdFromNeighbors;

    }

    protected void chooseColor() {
        Integer currentColor = 1;
        while (true) {
            if (isColorValid(currentColor)) {
                break;
            }
            currentColor = currentColor + 1;
        }
        this.myColor = currentColor;
        if (MainSimulator.isMonoStochComputationDebug){
            System.out.println(this.nodeId+", color: "+ this.myColor);
        }
    }

    private boolean isColorValid(Integer currentColor) {
        for (Integer nColor : neighborsColors.values()) {
            if (nColor != null) {
                if (nColor.equals(currentColor)) {
                    return false;
                }
            }
        }
        return true;
    }

// -------------------************ consistency  ************-------------------

    private boolean isConsistent() {
        return allNeighborsHaveColor()&& allLargerColorsCountersAreEqual() && allSmallerColorsPlusOne();
    }
    private boolean allLargerColorsCountersAreEqual() {
        Set<NodeId>largerColorNeighbors = getLargerColorNeighbors();
        if (largerColorNeighbors == null){ // not all have colors
            return false;
        }
        for (NodeId ni: largerColorNeighbors) {
            int niCounter = this.neighborCounters.get(ni);
            if (niCounter == this.selfCounter  || niCounter-1 == this.selfCounter){

            }else{
                return false;
            }
        }
        return true;
    }
    private boolean allSmallerColorsPlusOne() {
        Set<NodeId>smallerColorNeighbors = getSmallerColorNeighbors();
        for (NodeId ni: smallerColorNeighbors) {
            int niCounter = this.neighborCounters.get(ni);
            if (niCounter != this.selfCounter+1){
                return false;
            }
        }
        return true;
    }
    protected Set<NodeId> getSmallerColorNeighbors() {

        Set<NodeId>smallerColorNeighbors = new HashSet<NodeId>();

        for (NodeId ni:this.neighborsColors.keySet()) {
            if (this.neighborsColors.get(ni)<this.myColor){
                smallerColorNeighbors.add(ni);
            }

        }


        return  smallerColorNeighbors;
    }
    protected Set<NodeId> getLargerColorNeighbors() {
        Set<NodeId>largerColorNeighbors = new HashSet<NodeId>();

        for (NodeId ni:this.neighborsColors.keySet()) {
            try {
                if (this.neighborsColors.get(ni) > this.myColor) {
                    largerColorNeighbors.add(ni);
                }
            }catch (Exception e){
                return null;
            }
        }
        return  largerColorNeighbors;

    }
    protected boolean allNeighborsHaveColor() {

        for (NodeId ni:this.neighborsColors.keySet()) {
            if (this.neighborsColors.get(ni)==null){//&&!okNotToHaveColor.contains(ni)){
                return false;
            }
        }
        return true;
    }

}
