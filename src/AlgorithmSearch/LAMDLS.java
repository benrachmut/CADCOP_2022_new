package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Main.MainSimulator;
import Messages.*;

import java.util.*;

public class LAMDLS extends AgentVariableSearch implements SelfCounterable {




    enum Status{
        selectColor,
        idle,
        consistent
    }

    public static  char typeDecision = 'c' ;
    private int selfCounter;
    private int countChanges;
    private Integer myColor;
    private Map<NodeId,Integer> neighborsColors;
    private Map<NodeId,Integer> neighborCounters;

    private Status myStatus;

    public LAMDLS(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        AMDLS_V1.typeDecision = 'c';
        updateAlgorithmHeader();
        updateAlgorithmData();
        updateAlgorithmName();
        resetAgentGivenParametersV3();
    }

    @Override
    protected void resetAgentGivenParametersV3() {

        selfCounter = 1;
        myColor = null;
        countChanges = 0;
       neighborsColors = new HashMap<NodeId,Integer>();
        neighborCounters = new HashMap<NodeId,Integer>();
        for (NodeId nodeId: this.neighborsConstraint.keySet()) {
            neighborsColors.put(nodeId,null);
            neighborCounters.put(nodeId,0);
        }
        myStatus = Status.idle;
    }


    @Override
    public void initialize() {
        myStatus = Status.consistent;
        sendMsgs();
        myStatus = Status.idle;



        if (MainSimulator.isLAMDLSDebug){
           System.out.println(this.nodeId.getId1());
            for (NodeId nodeId:
                 this.neighborsConstraint.keySet()) {
                if (this.nodeId.getId1()<nodeId.getId1()) {
                    System.out.println(this.nodeId.getId1()+"\t"+nodeId.getId1());
                }
            }
        }

    }

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        Map<String,Integer>map = (Map<String, Integer>) msgAlgorithm.getContext();

        NodeId sender = msgAlgorithm.getSenderId();
       //----------
        int updatedValueAssignment = map.get("value assignment");
        MsgValueAssignmnet msg = new MsgValueAssignmnet(sender, msgAlgorithm.getRecieverId(), updatedValueAssignment, msgAlgorithm.getTimeStamp(), msgAlgorithm.getTimeOfMsg());
        this.updateMsgInContextValueAssignment(msg);
        //----------
        int counter = map.get("self counter");
        int currentCounter = this.neighborCounters.get(sender);

        if (counter>currentCounter ){
            this.neighborCounters.put(sender,counter);
        }
        //----------
        if (map.containsKey("color")){
            int color = map.get("color");
            this.neighborsColors.put(sender,color);
        }

        return true;
    }

    @Override
    public boolean getDidComputeInThisIteration() {
        return this.myStatus == Status.selectColor || this.myStatus == Status.consistent;
    }

    @Override
    public boolean compute() {
        if (myStatus == Status.selectColor){
            chooseColor();
            int potential = getCandidateToChange_C();
            if (potential!=this.valueAssignment){
                countChanges = countChanges+1;
            }
            this.valueAssignment = potential;
            this.selfCounter = this.selfCounter+1;
            if (allNeighborsHaveColor()&& isConsistent()){
                this.selfCounter = this.selfCounter+1;

            }
            if (MainSimulator.isLAMDLSDebug) {
                System.out.println(this + " counter is " + selfCounter);
            }
        }
        if (myStatus == Status.consistent) {

            int potential = getCandidateToChange_C();
            if (potential!=this.valueAssignment){
                countChanges = countChanges+1;
            }
            this.valueAssignment = potential;
            this.selfCounter = this.selfCounter+1;
            if (MainSimulator.isLAMDLSDebug) {
                System.out.println(this + " counter is " + selfCounter);
            }
        }

        return true;
    }

    @Override
    public void sendMsgs() {


        Map<String,Integer> info = new HashMap<String,Integer>();
        info.put("self counter",this.selfCounter);
        info.put("value assignment",this.valueAssignment);

        if (this.myStatus == Status.selectColor){
            info.put("color",this.myColor);
        }

        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();


        for (NodeId receiverNodeId:this.neighborsConstraint.keySet()) {
                MsgLAMDLS msg = new MsgLAMDLS(this.nodeId, receiverNodeId, info, this.timeStampCounter, this.time);
                msgsToInsertMsgBox.add(msg);
        }


        if (!msgsToInsertMsgBox.isEmpty()) {
            outbox.insert(msgsToInsertMsgBox);
        }

    }

    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

        boolean allCountersOkForColors = isAllCountersOkForColors();
        if (myColor == null && allCountersOkForColors && canSetColor()){
            this.myStatus = Status.selectColor;
            return;
        }
        if (myColor != null && allNeighborsHaveColor() ){
            if (isConsistent()) {
                this.myStatus = Status.consistent;
            }
        }

    }
    protected boolean allNeighborsHaveColor() {
        for (NodeId ni:this.neighborsColors.keySet()) {
            if (this.neighborsColors.get(ni)==null){//&&!okNotToHaveColor.contains(ni)){
                return false;
            }
        }
        return true;
    }

    private boolean isConsistent() {
        boolean smallerColorConsistent = isSmallerColorConsistent();
        boolean belowConsistent = isLargerColorConsistent();
        if (smallerColorConsistent && belowConsistent){
            return true;
        }
        return false;
    }

    protected boolean isSmallerColorConsistent() {
        Set<NodeId>smallerColors = getSmallerColors();
        for (NodeId nodeId : smallerColors) {
            if (this.neighborCounters.get(nodeId) != this.selfCounter + 1) {
                return false;
            }
        }
        return true;
    }

    private Set<NodeId> getSmallerColors() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (NodeId nodeId:this.neighborsColors.keySet()) {
            int nColor = this.neighborsColors.get(nodeId);
            if (myColor>nColor){
                ans.add(nodeId);
            }
        }
        return ans;
    }

    protected boolean isLargerColorConsistent() {
        Set<NodeId>largerColors = getLargerColors();
        for (NodeId nodeId : largerColors) {
            if (this.neighborCounters.get(nodeId) != this.selfCounter) {
                return false;
            }
        }
        return true;
    }

    private Set<NodeId> getLargerColors() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (NodeId nodeId:this.neighborsColors.keySet()) {
            int nColor = this.neighborsColors.get(nodeId);
            if (myColor<nColor){
                ans.add(nodeId);
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
        if (MainSimulator.isLAMDLSDebug){
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
            if (nodeId.getId1() < this.nodeId.getId1()) {
                ans.add(nodeId);
            }
        }
        return ans;
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

    private boolean isAllCountersOkForColors() {


        for (NodeId nodeId:this.neighborsColors.keySet()) {
            Integer nColor = this.neighborsColors.get(nodeId);
            int nCounter = this.neighborCounters.get(nodeId);
            if (nColor == null){
                if (nCounter!=1){
                    return false;
                }
            }else if (nCounter!=2){
                return false;
            }

        }
        return true;
    }

    @Override
    public void changeReceiveFlagsToFalse() {
        this.myStatus = Status.idle;
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
        AgentVariable.AlgorithmName = "LAMDLS";
    }

    @Override
    public int getSelfCounterable() {
        return this.countChanges;
    }

/*




    @Override
    protected void resetAgentGivenParametersV3() {
        this.selfCounter = 1;
        this.myColor = -1;
        this.flagSelectColor = false;
        this.counter = 0;
        updateAlgorithmName();
        this.neighborColors = new HashMap<NodeId,Integer>();

        for (NodeId nId: this.neighborsConstraint.keySet()){
            this.neighborColors.put(nId,null);
            this.neighborColors.put(nId,0);
        }


    }

    @Override
    public void initialize() {
        if (canSetColor()){
            this.myColor = 1;
            this.sendColorMsgs();
            if (MainSimulator.isMDC2CDebug){
                System.out.println(this.nodeId+", color: "+ this.myColor+", value assignment: "+ this.valueAssignment);
            }
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


    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        NodeId sender = msgAlgorithm.getSenderId();
        if (msgAlgorithm instanceof MsgValueAssignmnet) {
            updateMsgInContextValueAssignment(msgAlgorithm);
        }
        if (msgAlgorithm instanceof MsgAMDLSColor) {
            int neighborColor = ((MsgAMDLSColor) msgAlgorithm).getColor();
            this.neighborColors.put(sender,neighborColor);
        }


        return true;
    }

    @Override
    public boolean getDidComputeInThisIteration() {

        return flagSelectColor ;
    }



    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        this.counter = this.counter+1;
        return this.counter;
    }


    @Override
    public boolean compute() {
        if (flagSelectColor){
            this.chooseColor();
            changeValueAssignment();
        }

        return true;
    }


    private ArrayList<NodeId> getNodeIdsWithMinCounter(Set<NodeId> potentialNeighbors) {
        Map<NodeId,Integer> mapTemp = new HashMap<NodeId,Integer>();
        for (NodeId nodeId:potentialNeighbors) {
            int partnerCounter = mapTemp.get(nodeId);
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


    @Override
    public void sendMsgs() {
        if (flagSelectColor){
            sendColorMsgs();
            if (MainSimulator.isMDC2CDebug){
                System.out.println(this.nodeId+", color: "+ this.myColor+", value assignment: "+ this.valueAssignment);
            }
        }
    }

    private void sendColorMsgs() {

        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
        for (NodeId receiverNodeId : neighborsConstraint.keySet()) {
            MsgAMDLSColor msg= new MsgAMDLSColor(this.nodeId, receiverNodeId, this.valueAssignment,  this.timeStampCounter, this.time, this.selfCounter , this.myColor);
            msgsToInsertMsgBox.add(msg);
        }
        outbox.insert(msgsToInsertMsgBox);
    }


    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {


        if (this.myColor == -1 && canSetColor() ){
            this.flagSelectColor = true;
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


    @Override
    public void changeReceiveFlagsToFalse() {
        flagSelectColor=false;
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
        AgentVariable.AlgorithmName = "LAMDLS";
    }

    */

}