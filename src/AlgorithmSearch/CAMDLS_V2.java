package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.*;

import java.util.*;

import static Delays.ProtocolDelayWithK.k_public;

public class CAMDLS_V2 extends AgentVariableSearch {
    protected Map<NodeId, Map<NodeId, Integer>> timestampMap;
    protected TreeMap<NodeId, Integer> neighborColors;
    protected Integer myColor;
    protected Integer myCounter;
    private TreeMap<NodeId, Integer>neighborCounter;
    protected boolean canSelectValueAssignment;
    protected boolean canSetColor;
    private TreeMap<NodeId, Integer> largerColors;
    private TreeMap<NodeId, Integer> smallerColors;
    private Random rForShuffle;
    private Random rForK;
    public CAMDLS_V2(int dcopId, int D, int agentId) {
        super(dcopId, D, agentId);
        resetNeighborColorsAndCounter();
        myColor = null;
        Map<NodeId, Map<NodeId, Integer>> timestampMap = new HashMap<NodeId, Map<NodeId, Integer>>();
        resetTimestampMap();
        canSetColor = false;
        canSelectValueAssignment = false;

        largerColors = new TreeMap<NodeId,Integer>();
        smallerColors = new TreeMap<NodeId,Integer>();
        rForShuffle= new Random(this.dcopId*100 + this.id*10);
        rForK= new Random(this.dcopId*100 + this.id*10);
        this.myCounter = 0;


    }

    @Override
    protected void resetAgentGivenParametersV3() {
        myColor = null;
        resetNeighborColorsAndCounter();
        resetTimestampMap();
        largerColors = new TreeMap<NodeId,Integer>();
        smallerColors = new TreeMap<NodeId,Integer>();
        canSetColor = false;
        canSelectValueAssignment = false;
        rForShuffle= new Random(this.dcopId*100 + this.id*10);
        rForK= new Random(this.dcopId*100 + this.id*10);
        this.myCounter = 0;

    }

    private void resetTimestampMap() {
        this.timestampMap = new HashMap<NodeId, Map<NodeId, Integer>>();
        this.timestampMap.put(this.nodeId,new HashMap<NodeId,Integer>());
        for (NodeId nodeId: this.neighborsConstraint.keySet()) {
            this.timestampMap.get(this.nodeId).put(nodeId,0);
        }
    }

    private void resetNeighborColorsAndCounter() {
        neighborColors = new TreeMap<NodeId, Integer>();
        neighborCounter =  new TreeMap<NodeId, Integer>();
        for (NodeId nodeId : this.neighborsConstraint.keySet()) {
            neighborColors.put(nodeId, null);
            neighborCounter.put(nodeId, 0);
        }
    }

    @Override
    public void updateAlgorithmName() {
        AgentVariable.AlgorithmName = "CAMDLS V2";
    }

    @Override
    public void updateAlgorithmHeader() {
        AgentVariable.algorithmHeader = "k algo";
    }

    @Override
    public void updateAlgorithmData() {
        AgentVariable.algorithmData = Integer.toString(k_public);
    }




    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgValueAssignmnet) {
            return getTimestampOfValueAssignmnets(msgAlgorithm);

        } else {
            throw new RuntimeException();
        }
    }
    @Override
    protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgAMDLSColorKAware) {
            MsgAMDLSColorKAware msg = (MsgAMDLSColorKAware) msgAlgorithm;
            updateColor(msg.getSenderId(),msg.getColor());
            updateCounter(msg.getSenderId(),msg.getCounter());
            updateMsgInContextValueAssignmnet(msgAlgorithm);
            updateTimestamp(msg.getTimestampMap());

            if (MainSimulator.isCAMDLS_V2) {
                System.out.println("A_" + this.id + " recieve msg from "+msg.getSenderId() );
            }
            return true;
        }else{
            throw new RuntimeException("we send only MsgAMDLSColorKAware");
        }
    }

    private void updateCounter(NodeId senderId, Integer counter) {
        this.neighborCounter.put(senderId,counter);
        if (this.timestampMap.containsKey(nodeId))
            this.timestampMap.get(this.nodeId).put(senderId,counter);

    }


    private void updateColor(NodeId senderId, Integer color) {
        this.neighborColors.put(senderId,color);
        if (this.myColor!=null){
            if (this.myColor<color){
                this.largerColors.put(senderId,color);
            }else{
                this.smallerColors.put(senderId,color);
            }
        }
    }

    private void updateTimestamp(Map<NodeId, Map<NodeId, Integer>> timestampMapFromMsg) {
        for (NodeId nodeId1:timestampMapFromMsg.keySet()) {
            if (!this.timestampMap.containsKey(nodeId1)){
                this.timestampMap.put(nodeId1,new HashMap<NodeId,Integer>());
            }
            Map<NodeId,Integer> map1= timestampMapFromMsg.get(nodeId1);
            for (NodeId nodeId2:map1.keySet()){
                int counterFromMsg =map1.get(nodeId2);
                if (!this.timestampMap.get(nodeId1).containsKey(nodeId2)){
                    this.timestampMap.get(nodeId1).put(nodeId2,0);
                }
                int counterInMemory =this.timestampMap.get(nodeId1).get(nodeId2);
                if (counterFromMsg>counterInMemory){
                    this.timestampMap.get(nodeId1).put(nodeId2,counterFromMsg);
                }
            }
        }

    }

    @Override
    public boolean getDidComputeInThisIteration() {
        return canSelectValueAssignment || canSetColor;
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


    @Override
    protected boolean compute() {
        if(canSetColor){
            chooseColor();
            placeInAboveAndBelowMaps();
        }
        if (canSetColor|| canSelectValueAssignment){
            this.valueAssignment = getCandidateToChange_C();
            updateMyCounter();
        }

        return false;
    }

    private void placeInAboveAndBelowMaps() {
        for (NodeId nodeId: this.neighborColors.keySet()){
            int color = this.neighborColors.get(nodeId);
            if (this.myColor<color){
                this.largerColors.put(nodeId,color);
            }else{
                this.smallerColors.put(nodeId,color);
            }
        }


    }

    @Override
    public void sendMsgs() {
        List<NodeId> neighborsToSend = getNeighborsToSend();new ArrayList<>();

        int rndK = this.rForK.nextInt(k_public);
        List<Msg> msgsToOutbox = new ArrayList<Msg>();
        for (int i = 0; i < neighborsToSend.size(); i++) {
            NodeId recieverNodeId = neighborsToSend.get(i);
            MsgAMDLSColorKAware mva = new MsgAMDLSColorKAware(this.nodeId, recieverNodeId, this.valueAssignment,
                    this.timeStampCounter, this.time, this.myCounter, this.timestampMap, this.myColor);
            if (rndK == i) {
                if (MainSimulator.isCAMDLS_V2) {
                    System.out.println("A_" + this.id + " sent message to A_" + recieverNodeId.getId1() + " color");
                }
                mva.changeToNoLoss();
            }
            msgsToOutbox.add(mva);
        }
        outbox.insert(msgsToOutbox);
    }

    private List<NodeId> getNeighborsToSend() {

        List<NodeId> neighborsToSend = new ArrayList<>();

        Set<NodeId> toSendHash = getHashToSend();
        List<NodeId> toSendList = new ArrayList<>(toSendHash);
        Collections.shuffle(toSendList, this.rForShuffle);
        int counter = 0;
        while (counter < k_public) {
            for (int i = 0; i < toSendList.size(); i++) {
                NodeId nodeId = toSendList.get(i);
                neighborsToSend.add(nodeId);
                counter = counter + 1;
                if (counter == k_public) {
                    break;
                }
            }
        }
        return neighborsToSend;
    }

    private Set<NodeId> getHashToSend() {
        boolean isInColorProcess = getIsInColorProcess();
        Set<NodeId> iDontKnowYet = getIDontKnowYet(isInColorProcess);
        Set<NodeId> theyDontKnowYet = getTheyDontKnowYet(isInColorProcess);
        Set<NodeId> ans = new HashSet<NodeId>();
        ans.addAll(iDontKnowYet);
        ans.addAll(theyDontKnowYet);

        return ans;
    }

    private Set<NodeId> getTheyDontKnowYet(boolean isInColorProcess) {

        Set<NodeId> ans = new HashSet<NodeId>();
        for (NodeId nodeId:this.neighborsConstraint.keySet()) {
            Map<NodeId,Integer>map = this.timestampMap.get(nodeId);
            if (map == null){
                ans.add(nodeId);
            }else {
                int whatItThinkOfMe = -1;
                if (map.containsKey(this.nodeId)) {
                    whatItThinkOfMe = map.get(this.nodeId);
                }
                if (whatItThinkOfMe != this.myCounter) {
                    ans.add(nodeId);
                }
            }
        }
        return ans;

    }

    private Set<NodeId> getIDontKnowYet(boolean isInColorProcess) {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (NodeId nodeId:this.neighborsConstraint.keySet()) {
            int whatIThinkOfGivenId = this.timestampMap.get(this.nodeId).get(nodeId);
            if (whatIThinkOfGivenId!=this.neighborCounter.get(nodeId)){
                throw new RuntimeException("whatIThinkOfGivenId!=this.neighborCounter.get(nodeId)");
            }

            if (isInColorProcess) {
                if (whatIThinkOfGivenId == 0) {
                    ans.add(nodeId);
                }
            }else{
                if (this.smallerColors.containsKey(nodeId)){
                    if (whatIThinkOfGivenId+1!=this.myCounter){
                        ans.add(nodeId);
                    }
                }else{
                    if (whatIThinkOfGivenId!=this.myCounter){
                        ans.add(nodeId);
                    }

                }
            }
        }
        return ans;
    }

    private boolean getIsInColorProcess() {
        boolean ans;
        if (this.myColor == null){
            return true;
        }
        for (Integer i :this.neighborColors.values()) {
            if (i == null){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
        if(this.myCounter == 0 && canSetColor()){
            canSetColor = true;
        }
        boolean allHaveColor = !getIsInColorProcess();
        
        if (allHaveColor && !canSetColor){
            boolean isAllSmallerColorConsistent = getIsAllSmallerColorConsistent();
            if (isAllSmallerColorConsistent){
                boolean isAllLargerColorConsistent = getIsAllLargerColorConsistent();
                if (isAllLargerColorConsistent) {
                    canSelectValueAssignment = true;
                }
            }

        }
    }

    private boolean getIsAllSmallerColorConsistent() {
        for (NodeId nodeId:this.smallerColors.keySet()) {
            int neighborCurrentCounter = this.neighborCounter.get(nodeId);
            if (this.myCounter+1 != neighborCurrentCounter){
                return false;
            }
        }
        return true;
    }
    private boolean getIsAllLargerColorConsistent() {
        for (NodeId nodeId:this.smallerColors.keySet()) {
            int neighborCurrentCounter = this.neighborCounter.get(nodeId);
            if (this.myCounter != neighborCurrentCounter){
                return false;
            }
        }
        return true;
    }

    @Override
    public void changeReceiveFlagsToFalse() {
        canSelectValueAssignment=false;
        canSetColor=false;
    }

    private boolean determineByIndexInit() {
        for (NodeId nodeId : this.neighborsConstraint.keySet()) {
            if (this.id > nodeId.getId1()) {
                return false;
            }
        }
        return true;
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


    protected void chooseColor() {
        Integer currentColor = 1;
        while (true) {
            if (isColorValid(currentColor)) {
                break;
            }
            currentColor = currentColor + 1;
        }
        this.myColor = currentColor;
        if (MainSimulator.isCAMDLS_V2) {
            System.out.println("A_" + this.id + " selected color "+this.myColor);
        }


    }



    protected void updateMyCounter() {
        this.myCounter = this.myCounter + 1;
        changeTimestampMapMyCounterInfo();
    }

    private void changeTimestampMapMyCounterInfo() {
        if (!this.timestampMap.containsKey(this.nodeId)) {
            this.timestampMap.put(this.nodeId, new HashMap<NodeId, Integer>());
        }
        Map<NodeId, Integer> v = this.timestampMap.get(this.nodeId);
        v.put(this.nodeId, this.myCounter);
    }

    @Override
    public void initialize() {

        this.isWithTimeStamp = false;
        if (determineByIndexInit()) {
            chooseColor();
            updateMyCounter();
            sendMsgs();
            //firstFlag = true;
        } else {
            this.valueAssignment = Integer.MIN_VALUE;
            this.myCounter = 0;
        }


    }
}
