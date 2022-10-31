package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.Msg;
import Messages.MsgAMDLSColor;
import Messages.MsgAlgorithm;
import Messages.MsgValueAssignmnet;

import java.util.*;

public class LAMDLS extends AgentVariableSearch {
    public static  char typeDecision = 'c' ;
    private int selfCounter;
    private int myColor;
    private Map<NodeId,Integer> neighborColors;
    private boolean flagSelectColor;
    private int counter;

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
    protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        NodeId sender = msgAlgorithm.getSenderId();
        if (msgAlgorithm instanceof MsgValueAssignmnet) {
            updateMsgInContextValueAssignmnet(msgAlgorithm);
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
    protected boolean compute() {
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
}