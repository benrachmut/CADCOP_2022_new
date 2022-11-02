package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.*;
import org.w3c.dom.Node;

import java.util.*;

public class MonotonicDeterministicColors2Coordination extends AgentVariableSearch {
    public static  char typeDecision = 'c' ;
    private int selfCounter;
    private Random rndForPartners;
    private int myColor;
    private Map<NodeId,Integer> neighborColors;
    private boolean flagSelectColor;
    private int counter;
    private boolean flagSelectNeighborAndSendInfo;
    private Map<NodeId,Integer> neighborCounters;
    private Map<NodeId,Integer> neighborPartnerCounters;
    private Map<NodeId,Integer>infoToSend2Opt;

    //private Find2Opt optInfo;

    //private KOptInfo myInfo;

    private NodeId partnerNodeId;

    private List<KOptInfo> neighborsInfo;
    private boolean flagCreateKoptValueAssignmentChanges;

    public MonotonicDeterministicColors2Coordination(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        AMDLS_V1.typeDecision = 'c';
        updateAlgorithmHeader();
        updateAlgorithmData();
        updateAlgorithmName();
        resetAgentGivenParametersV3();
    }

    //******************************************************************
    //********--------------------initialize--------------------********
    //******************************************************************

    @Override
    protected void resetAgentGivenParametersV3() {
        rndForPartners = new Random(this.nodeId.getId1()*99999);
        this.selfCounter = 1;
        this.myColor = -1;
        this.flagSelectColor = false;
        this.counter = 0;
        updateAlgorithmName();
        this.neighborColors = new HashMap<NodeId,Integer>();
        this.neighborCounters = new HashMap<NodeId,Integer>();
        neighborPartnerCounters = new HashMap<NodeId,Integer>();
        for (NodeId nId: this.neighborsConstraint.keySet()){
            this.neighborColors.put(nId,null);
            this.neighborCounters.put(nId,0);
            neighborPartnerCounters.put(nId,0);
        }
        infoToSend2Opt = new HashMap<NodeId, Integer>();
        flagSelectNeighborAndSendInfo = false;
        flagCreateKoptValueAssignmentChanges = false;
        partnerNodeId = null;
        //myInfo=null;
        neighborsInfo = new ArrayList<KOptInfo>();
    }
    @Override
    public void initialize() {
        if (canSetColor()){
            this.myColor = 1;
            this.sendColorMsgs();

            if (MainSimulator.isMDC2CDebug){
                System.out.println(this.nodeId+", color: "+ this.myColor+", value assignment: "+ this.valueAssignment+", "+this.neighborsConstraint.keySet());
            }
        }
    }


    //******************************************************************
    //********--------------------color stuff-------------------********
    //******************************************************************
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
    private Set<NodeId> neighborsWithSmallerIndexThenMe() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (NodeId nodeId : neighborsConstraint.keySet()) {
            if (nodeId.getId1() < this.id) {
                ans.add(nodeId);
            }
        }
        return ans;
    }
    private Set<NodeId> getNeighborsThatHaveColor() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (Map.Entry<NodeId, Integer> e : this.neighborColors.entrySet()) {
            if (e.getValue() != null) {
                ans.add(e.getKey());
            }
        }
        return ans;
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
    }
    private boolean isColorValid(Integer currentColor) {
        for (Integer nColor : neighborColors.values()) {
            if (nColor != null) {
                if (nColor.equals(currentColor)) {
                    return false;
                }
            }
        }
        return true;
    }


    //*****************************************************************************
    //********--------------------updateMessageInContext-------------------********
    //*****************************************************************************

    @Override
    protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        NodeId sender = msgAlgorithm.getSenderId();
        if (msgAlgorithm instanceof MsgValueAssignmnet && !(msgAlgorithm instanceof MsgMDC2CFriendRequest)) {
            updateMsgInContextValueAssignmnet(msgAlgorithm);
        }

        if (msgAlgorithm instanceof MsgAMDLSColor) {
            int neighborColor = ((MsgAMDLSColor) msgAlgorithm).getColor();
            this.neighborColors.put(sender,neighborColor);

            int neighborCounter = ((MsgAMDLSColor) msgAlgorithm).getCounter();
            this.neighborCounters.put(sender,neighborCounter);
        }

        if (msgAlgorithm instanceof MsgMDC2CFriendRequest){
            updateValueAWithKOptInfo(msgAlgorithm);
            KOptInfo nInfo =  ((KOptInfo) msgAlgorithm.getContext());
            this.neighborsInfo.add(nInfo);
        }

        return true;
    }

    private void updateValueAWithKOptInfo(MsgAlgorithm msgAlgorithm) {
        KOptInfo nInfo =  ((KOptInfo) msgAlgorithm.getContext());
        int context = nInfo.getValueAssingment();
        int timestamp = msgAlgorithm.getTimeStamp();
        MsgReceive<Integer> msgReceive = new MsgReceive<Integer>(context, timestamp);
        this.neighborsValueAssignmnet.put(msgAlgorithm.getSenderId(), msgReceive);
    }

    @Override
    public boolean getDidComputeInThisIteration() {

        return flagSelectColor || flagSelectNeighborAndSendInfo;
    }

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        this.counter = this.counter+1;
        return this.counter;
    }

    //**************************************************************
    //********--------------------compute-------------------********
    //**************************************************************

    @Override
    protected boolean compute() {
        if (flagSelectColor){

        }
        if (flagSelectNeighborAndSendInfo){
            NodeId neighborSelect = this.selectNeighborForPartnership();
            this.partnerNodeId = neighborSelect;
        }
        if (flagCreateKoptValueAssignmentChanges){
            KOptInfo selectedNeighborInfo = this.neighborsInfo.get(0);
            Find2Opt optInfo = new Find2Opt(makeMyKOptInfo(), selectedNeighborInfo);
            this.infoToSend2Opt = new HashMap<NodeId,Integer>();
            infoToSend2Opt.put(this.nodeId, optInfo.getValueAssignmnet1());
            infoToSend2Opt.put( this.neighborsInfo.get(0).getNodeId(), optInfo.getValueAssignmnet2());
            this.selfCounter = this.selfCounter+1;
        }
        return true;
    }


    //**********************************************************************
    //********--------------------select neighbor-------------------********
    //**********************************************************************

    private KOptInfo makeMyKOptInfo() {
        // TODO Auto-generated method stub
        return new KOptInfo(this.valueAssignment, nodeId, neighborsConstraint, domainArray,
                this.neighborsValueAssignmnet);
    }

    private NodeId selectNeighborForPartnership() {
        Set <NodeId> potentialNeighbors= getPotentialNeighbors();
        ArrayList <NodeId> nodeIdsWithMinCounter = this.getNodeIdsWithMinCounter(potentialNeighbors);
        if (nodeIdsWithMinCounter.isEmpty()){
            return null;
        }
        if (nodeIdsWithMinCounter.size()==1){
            return nodeIdsWithMinCounter.get(0);
        }
        Collections.shuffle(nodeIdsWithMinCounter,this.rndForPartners);
        int selectedIndex = this.rndForPartners.nextInt(nodeIdsWithMinCounter.size());
        return nodeIdsWithMinCounter.get(selectedIndex);
    }

    private ArrayList<NodeId> getNodeIdsWithMinCounter(Set<NodeId> potentialNeighbors) {
        Map<NodeId,Integer> mapTemp = new HashMap<NodeId,Integer>();
        for (NodeId nodeId:potentialNeighbors) {
            int partnerCounter = this.neighborPartnerCounters.get(nodeId);
            mapTemp.put(nodeId,partnerCounter);
        }
        int minPartnerCounter = Collections.min(mapTemp.values());
        ArrayList<NodeId>nodeIds = new ArrayList<NodeId>();

        for (NodeId nodeId:mapTemp.keySet()) {
            if (mapTemp.get(nodeId) == minPartnerCounter){
                nodeIds.add(nodeId);
            }
        }
        return nodeIds;
    }

    private Set<NodeId> getPotentialNeighbors() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (NodeId neighbor : this.neighborColors.keySet()) {
            int nColor = this.neighborColors.get(neighbor);
            if (nColor - 1 == this.myColor) {
                ans.add(neighbor);
            }
        }
        return ans;
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


    //****************************************************************
    //********--------------------send Msgs-------------------********
    //****************************************************************

    @Override
    public void sendMsgs() {
        if (flagSelectColor){
            sendColorMsgs();
            if (MainSimulator.isMDC2CDebug){
                System.out.println(this.nodeId+", color: "+ this.myColor+", value assignment: "+ this.valueAssignment+", "+this.neighborsConstraint.keySet());
            }
        }

        if (flagSelectNeighborAndSendInfo){
            sendFriendRequest();
        }

        if (flagCreateKoptValueAssignmentChanges){
            sendInfoBackToSelectedNeighbor();
            sendValuesToAllTheRestOfTheNeighbors();
        }
    }

    private void sendInfoBackToSelectedNeighbor() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
        NodeId receiver = neighborsInfo.get(0).getNodeId();
        MsgMDC2CFriendReply msg= new MsgMDC2CFriendReply(this.nodeId, receiver, infoToSend2Opt ,  this.timeStampCounter, this.time,this.selfCounter,this.myColor);
        msgsToInsertMsgBox.add(msg);
        outbox.insert(msgsToInsertMsgBox);
    }
    private void sendFriendRequest() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
        if (partnerNodeId == null){
            throw new RuntimeException();
        }

        MsgMDC2CFriendRequest msg= new MsgMDC2CFriendRequest(this.nodeId, partnerNodeId, makeMyKOptInfo(),  this.timeStampCounter, this.time,this.selfCounter,this.myColor);
        msgsToInsertMsgBox.add(msg);
        outbox.insert(msgsToInsertMsgBox);
    }

    private void sendColorMsgs() {

        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
        for (NodeId receiverNodeId : neighborsConstraint.keySet()) {
            MsgAMDLSColor msg= new MsgAMDLSColor(this.nodeId, receiverNodeId, this.valueAssignment,  this.timeStampCounter, this.time, this.selfCounter , this.myColor);
            msgsToInsertMsgBox.add(msg);
        }
        outbox.insert(msgsToInsertMsgBox);
    }



    //****************************************************************************
    //********----------------change Receive Flags To True----------------********
    //****************************************************************************

    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
        if (this.myColor == -1 && canSetColor() ){
            this.flagSelectColor = true;
            this.chooseColor();
            changeValueAssignment();
        }

        if (allNeighborsHaveColor() && areCounterConsistent()&& this.myColor != -1){
            this.flagSelectNeighborAndSendInfo = true;
        }

        if ( checkNeighborsByIndexConstraint()){
            this.flagCreateKoptValueAssignmentChanges = true;
        }
    }

    private boolean checkNeighborsByIndexConstraint() {
        if (!this.neighborsInfo.isEmpty()){
            Set<NodeId> larger = new HashSet<NodeId>();
            Set<NodeId> smaller = new HashSet<NodeId>();
            this.makeLargerSmall(larger, smaller);
            this.clearSmallerIHaveInfo(smaller);
            boolean smallerColorWithSelfCounterPlusOne = isSmallerWithSelfCounterPlusOne(smaller);

            return smallerColorWithSelfCounterPlusOne;
    }else
        return false;

    }

    private void clearSmallerIHaveInfo(Set<NodeId> smaller) {
        Collection<NodeId> toDelete= new HashSet<NodeId>();
        for (KOptInfo koptinfo:this.neighborsInfo) {
            toDelete.add(koptinfo.getNodeId());
        }
        smaller.removeAll(toDelete);
    }

    private boolean areCounterConsistent() {
        Set<NodeId>larger = new HashSet<NodeId>();
        Set<NodeId>smaller = new HashSet<NodeId>();
        this.makeLargerSmall(larger,smaller);
        boolean largerColorWithSelfCounterEqual  = isLargerColorWithSelfCounterEqual(larger);
        boolean smallerColorWithSelfCounterPlusOne = isSmallerWithSelfCounterPlusOne(smaller);
        return largerColorWithSelfCounterEqual && smallerColorWithSelfCounterPlusOne;
    }

    private boolean isSmallerWithSelfCounterPlusOne(Set<NodeId> smaller) {
        for (NodeId nodeId:smaller) {
            int nCounter = this.neighborCounters.get(nodeId);
            if (nCounter!=this.selfCounter+1) {
                return false;
            }
        }
        return true;
    }

    private boolean isLargerColorWithSelfCounterEqual(Set<NodeId> larger) {

        for (NodeId nodeId:larger) {
            int nCounter = this.neighborCounters.get(nodeId);
            if (nCounter!=this.selfCounter) {
                return false;
            }
        }
        return true;
    }

    private void makeLargerSmall(Set<NodeId> larger, Set<NodeId> smaller) {
        for (NodeId nodeId:this.neighborColors.keySet()) {
            int nColor = this.neighborColors.get(nodeId);
            if (this.myColor<nColor){
                larger.add(nodeId);
            }
            if (this.myColor>nColor){
                smaller.add(nodeId);
            }
            if (this.myColor==nColor){
                throw new RuntimeException(this+" has neighbor "+nodeId+ " with the same color");
            }
        }

    }

    private boolean allNeighborsHaveColor() {
        for (Integer integer: this.neighborColors.values()){
            if (integer == null) {
                return false;
            }
        }
        return true;
    }

    //*****************************************************************************
    //********----------------change Receive Flags To False----------------********
    //*****************************************************************************
    @Override
    public void changeReceiveFlagsToFalse() {
        flagSelectColor=false;
        flagSelectNeighborAndSendInfo= false;
        partnerNodeId = null;
        if (flagCreateKoptValueAssignmentChanges){
            neighborsInfo.clear();
        }
        flagCreateKoptValueAssignmentChanges = false;
        infoToSend2Opt = new HashMap<NodeId, Integer>();

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
        AgentVariable.AlgorithmName = "MDC2C";
    }
}