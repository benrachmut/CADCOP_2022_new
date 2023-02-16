package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Main.MainSimulator;
import Messages.*;

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
        neighborPartnerCounters= new HashMap<NodeId,Integer>();
        for (NodeId nId:this.neighborsConstraint.keySet()) {
            this.neighborsDocIdsT.put(nId,Double.valueOf(nId.getId1()));
            this.neighborsDocIdsT1.put(nId,null);
            this.neighborsColors.put(nId,null);
            this.neighborsColorsT1.put(nId,null);
            this.neighborCounters.put(nId,0);
            this.neighborPartnerCounters.put(nId,0);
        }
        docIdRandom = new Random((dcopId+1)*17+this.nodeId.getId1()*177);
        this.rndForPartners = new Random((dcopId+1)*147+this.nodeId.getId1()*122);
        this.myDocIdT1 = null; //docIdRandom.nextDouble();
        this.myDocId = Double.valueOf(this.nodeId.getId1());
        if (MainSimulator.isMonoStochInit && !this.neighborsConstraint.keySet().isEmpty()){
            printNeighbors();
        }

        neighborsInfo = new HashMap<NodeId,KOptInfo>();

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
            basicBeforeValueChange();
            //changeValueAssignmentAlone();
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

    private void basicBeforeValueChange() {
        this.myDocId = this.myDocIdT1;
        this.myDocIdT1 = docIdRandom.nextDouble();
        selfCounter = selfCounter +1;
    }



    private void changeValueAssignmentAlone() {
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
        if (this.nodeId.getId1() == 4) {
            System.out.println("A_4 GOT MSG FROM "+sender.getId1());
        }
        if (msgAlgorithm instanceof MsgInfoStatuesMonoStoch2){
            MsgInfoStatuesMonoStoch2 msg = (MsgInfoStatuesMonoStoch2)msgAlgorithm;
            Map<String, Double> info = (Map<String, Double>) msg.getContext();
            updateColor(info,sender);
            updateValueAssignment(info,sender, msg.getTimeStamp());
            updateSelfCounter(info,sender);
            updateDocId(info,sender);
        }

        if (msgAlgorithm instanceof MsgFriendRequestMonoStoch2){
            MsgFriendRequestMonoStoch2 msg = (MsgFriendRequestMonoStoch2)msgAlgorithm;
            KOptInfo infoKopt = (KOptInfo) msg.getContext();
            this.neighborsInfo.put(msg.getSenderId(),infoKopt);
        }

        if (msgAlgorithm instanceof MsgFriendReplyMonoStoch2){
            MsgFriendReplyMonoStoch2 msg = (MsgFriendReplyMonoStoch2)msgAlgorithm;
            Find2Opt twoOptInfo = (Find2Opt) msg.getContext();
            basicBeforeValueChange();
            updateFieldsUsing2OptInfoFromMsg(twoOptInfo);
        }

        return true;
    }

    private void updateFieldsUsing2OptInfoFromMsg(Find2Opt optInfo) {
        this.valueAssignment = optInfo.getValueAssignmnet2();
        this.neighborCounters.put(partnerNodeId,this.neighborCounters.get(partnerNodeId)+1);
        this.neighborsValueAssignmnet.put(partnerNodeId, new MsgReceive<Integer>(optInfo.getValueAssignmnet1(),0));
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
                this.myStatues = Status.idle;
                if (isConsistent()) {
                    this.myStatues = Status.consistent;
                }
            }
        }
        if (this.myStatues == Status.idle){
            if (isConsistent()) {
                this.myStatues = Status.consistent;
            }
        }


        if (msgAlgorithm instanceof MsgFriendRequestMonoStoch2) {
            if (this.myStatues == Status.consistent) {
                throw new RuntimeException("Does not make sense receive MsgFriendRequestMonoStoch2 and stochastic");
            }
            this.myStatues = Status.receiveOffer;
            if (isNeighborsByIndexConstraintOk() && allLargerColorsCountersAreEqual() && allNeighborsHaveColor()) {
                this.myStatues = Status.replyToOffer;
            }
        }

        if (msgAlgorithm.getSenderId().equals(this.partnerNodeId) && msgAlgorithm instanceof MsgFriendReplyMonoStoch2){
            this.myStatues = Status.doWhatPartnerSay;
        }

        if (msgAlgorithm.getSenderId().equals(this.partnerNodeId) && !(msgAlgorithm instanceof MsgFriendReplyMonoStoch2)){
            this.myStatues = Status.changeAlone;

        }

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
                 || this.myStatues == Status.replyToOffer ;
    }

    @Override
    public boolean compute() {
        if (this.myStatues == Status.canSelectColor){
            basicBeforeValueChange();
            if (selfCounter>1) {
                changeValueAssignmentAlone();
            }
            chooseColor();
        }
        if (this.myStatues == Status.consistent) {
            computeIfConsistent();
        }

        if (this.myStatues == Status.replyToOffer){
            computeIfReplyToOffer();
        }

        if(this.myStatues == Status.changeAlone){
            this.basicBeforeValueChange();
            this.changeValueAssignmentAlone();
        }
        return true;
    }



    @Override
    public void sendMsgs() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();

        if (this.myStatues == Status.canSelectColor){
            createMsgsWithColor(msgsToInsertMsgBox);
        }

        if (this.myStatues  == Status.changeAlone){
            createMsgsForChangeAlone(msgsToInsertMsgBox);
        }

        if (this.myStatues  == Status.consistent){
            sendInfoToPartner();
        }

        if (this.myStatues == Status.replyToOffer || this.myStatues == Status.doWhatPartnerSay){
            sendInfoToAllTheRest(msgsToInsertMsgBox);
            partnerNodeId = null;
        }

        if(!msgsToInsertMsgBox.isEmpty()) {
            outbox.insert(msgsToInsertMsgBox);
        }

    }

    private void sendInfoToAllTheRest(List<Msg> msgsToInsertMsgBox) {
        Map<String,Double> info = createInfo();
        info.remove("color");
        for (NodeId receiverNodeId : neighborsConstraint.keySet()) {
            if (!receiverNodeId.equals(this.partnerNodeId)) {
                MsgInfoStatuesMonoStoch2 msg = new MsgInfoStatuesMonoStoch2
                        (this.nodeId, receiverNodeId, info, this.timeStampCounter, this.time);
                msgsToInsertMsgBox.add(msg);
            }
        }

    }


    @Override
    public void changeReceiveFlagsToFalse() {
        Status prevStatus = this.myStatues;

        boolean isConsistentNow = changeAfterCanSelectColor();
        if (isConsistentNow){
            computeIfConsistent();
            sendMsgs();
        }
        if (this.myStatues == Status.consistent){
            this.myStatues = Status.waitForReply;
        }

        if (this.myStatues == Status.replyToOffer || this.myStatues == Status.changeAlone || this.myStatues == Status.doWhatPartnerSay ){
            clearData();
            this.myStatues = Status.waitToFinishIteration;
        }

        if (MainSimulator.isMonoStochComputationDebug){
            if (prevStatus !=this.myStatues)
            System.out.println("A_"+this.nodeId.getId1()+" status: {"+prevStatus+"-->"+this.myStatues+"}");
        }




    }

    private void clearData() {
        clearDocIds();
        clearColors();
    }

    private void clearColors() {
        for (NodeId nId:this.neighborsColors.keySet()) {
            if (this.neighborsColorsT1.get(nId)!=null) {
                int futureColor = this.neighborsColorsT1.get(nId);
                this.neighborsColors.put(nId, futureColor);
            }
            this.neighborsColorsT1.put(nId,null);
        }
    }


    private void clearDocIds() {

        for (NodeId nId:this.neighborsDocIdsT.keySet()) {
            if (this.neighborsDocIdsT1.get(nId)!=null) {
                double futureDoc = this.neighborsDocIdsT1.get(nId);
                this.neighborsDocIdsT.put(nId, futureDoc);
            }
            this.neighborsDocIdsT1.put(nId,null);
        }
    }

    private boolean changeAfterCanSelectColor() {
        if (this.myStatues != Status.canSelectColor){
            return false;
        }
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
        return this.myStatues == Status.consistent;
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

// -------------------************ msgs  ************-------------------


    private void sendInfoToPartner() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<>();
        MsgFriendRequestMonoStoch2 msg = new MsgFriendRequestMonoStoch2(this.nodeId, partnerNodeId, makeMyKOptInfo(), this.timeStampCounter, this.time);
        if (partnerNodeId == null){
            throw new RuntimeException("try to send info to partner but there is no PARTNER SELECTED");
        }
        msgsToInsertMsgBox.add(msg);
        outbox.insert(msgsToInsertMsgBox);
    }

    private KOptInfo makeMyKOptInfo() {
        return new KOptInfo(this.valueAssignment, nodeId, neighborsConstraint, domainArray,
                this.neighborsValueAssignmnet);
    }
    private void createMsgsForChangeAlone(List<Msg> msgsToInsertMsgBox) {
        Map<String,Double> info = createInfo();
        info.remove("color");
        for (NodeId receiverNodeId : neighborsConstraint.keySet()) {
            MsgInfoStatuesMonoStoch2 msg = new MsgInfoStatuesMonoStoch2
                    (this.nodeId, receiverNodeId, info, this.timeStampCounter, this.time);
            msgsToInsertMsgBox.add(msg);
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

// -------------------************ compute  ************-------------------

    private void computeIfConsistent() {
        //this.myStatues = Status.waitForReply;

        NodeId neighborSelect = this.selectNeighborForPartnership();
        this.partnerNodeId = neighborSelect;
        if (MainSimulator.isMonoStochComputationDebug) {
            System.out.println(this.nodeId + " selected " + partnerNodeId);
        }
        if (partnerNodeId == null) {
            this.myStatues = Status.changeAlone;
            this.basicBeforeValueChange();
            this.changeValueAssignmentAlone();
            if (MainSimulator.isMonoStochComputationDebug) {
                System.out.println("A_" + this.nodeId + " has no potential neighbors so change alone");
            }

        }
    }

    private NodeId selectNeighborForPartnership() {
        Set <NodeId> potentialNeighbors= getPotentialNeighbors();
        ArrayList <NodeId> nodeIdsWithMinCounter = this.getNodeIdsWithMinCounter(potentialNeighbors);
        if (nodeIdsWithMinCounter == null || nodeIdsWithMinCounter.isEmpty()){
            return null;
        }
        if (nodeIdsWithMinCounter.size()==1){
            return nodeIdsWithMinCounter.get(0);
        }

        Collections.shuffle(nodeIdsWithMinCounter, this.rndForPartners);

        int selectedIndex = this.rndForPartners.nextInt(nodeIdsWithMinCounter.size());
        return nodeIdsWithMinCounter.get(selectedIndex);
    }

    protected Set<NodeId> getPotentialNeighbors() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (NodeId neighbor : this.neighborsColors.keySet()) {
            int nColor = this.neighborsColors.get(neighbor);
            if (nColor - 1 == this.myColor && this.neighborCounters.get(neighbor)== this.selfCounter ){
                ans.add(neighbor);
            }
        }
        return ans;
    }

    private ArrayList<NodeId> getNodeIdsWithMinCounter(Set<NodeId> potentialNeighbors) {
        Map<NodeId, Integer> mapTemp = new HashMap<NodeId, Integer>();
        for (NodeId nodeId : potentialNeighbors) {
            int partnerCounter = this.neighborPartnerCounters.get(nodeId);
            mapTemp.put(nodeId, partnerCounter);
        }

        if (mapTemp.values().isEmpty()){
            return null;
        }
        int minPartnerCounter = Collections.min(mapTemp.values());

        ArrayList<NodeId> nodeIds = new ArrayList<NodeId>();

        for (NodeId nodeId : mapTemp.keySet()) {
            if (mapTemp.get(nodeId) == minPartnerCounter) {
                nodeIds.add(nodeId);
            }
        }
        return nodeIds;
    }

    // -------------------************ can reply  ************-------------------

    private boolean isNeighborsByIndexConstraintOk() {


        if(!allNeighborsHaveColor()){
            return false;
        }
        Set<NodeId>smallerColor = this.getSmallerColorNeighbors();
        extractNodeIdIHaveInfo(smallerColor);
        extractColorIndexMinusOneWithLargerDocIndex(smallerColor);

        for (NodeId nId:smallerColor) {
            int nCounter = this.neighborCounters.get(nId);
            if (nCounter!= this.selfCounter+1){
                return false;
            }
        }
        return true;
    }

    private void extractColorIndexMinusOneWithLargerDocIndex(Set<NodeId> smallerColor) {
        Set<NodeId>colorMinusOne = getColorMinusOne(smallerColor);

        Collection<NodeId> toRemove = new HashSet<NodeId>();

        NodeId infoMaxIndex = getInfoMaxIndex();
        if (infoMaxIndex!=null) {
            for (NodeId ni : colorMinusOne) {
                if (this.neighborsDocIdsT.get(infoMaxIndex) < this.neighborsDocIdsT.get(ni)) {
                    toRemove.add(ni);
                }
            }
        }
        smallerColor.removeAll(toRemove);
    }


    private NodeId getInfoMaxIndex() {
        int x = Integer.MIN_VALUE;
        NodeId ans = null;
        for (NodeId ni: this.neighborsInfo.keySet()){
            if (this.neighborsDocIdsT.get(ni)>x){
                ans = ni;
            }
        }
        return ans;
    }
    protected Set<NodeId> getColorMinusOne(Set<NodeId> smallerColor) {
        Set<NodeId>colorMinusOne = new HashSet<NodeId>();
        for (NodeId ni:smallerColor) {
            int nColor = this.neighborsColors.get(ni);
            if (this.myColor -1  == nColor){
                colorMinusOne.add(ni);
            }

        }
        return colorMinusOne;
    }

    private void extractNodeIdIHaveInfo(Set<NodeId> smallerColor) {
        Iterator<NodeId>it = smallerColor.iterator();
        while (it.hasNext()){
            NodeId nodeId = it.next();
            if (this.neighborsInfo.keySet().contains(nodeId)){
                it.remove();
            }
        }
    }


    private void computeIfReplyToOffer() {
        setPartnerNodeId();
        Find2Opt optInfo = new Find2Opt(makeMyKOptInfo(), neighborsInfo.get(this.partnerNodeId));
        this.atomicActionCounter = optInfo.getAtomicActionCounter();
        basicBeforeValueChange();
        updateFieldsUsing2OptInfo(optInfo);
        sendMsgOfReply(optInfo);
        this.neighborsInfo.clear();

    }

    private void sendMsgOfReply(Find2Opt optInfo) {
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
        NodeId receiver = partnerNodeId;
        MsgMDC2CFriendReply msg= new MsgMDC2CFriendReply(this.nodeId, receiver, optInfo ,  this.timeStampCounter, this.time,this.selfCounter,this.myColor,this.myDocId);
        msgsToInsertMsgBox.add(msg);
        outbox.insert(msgsToInsertMsgBox);
    }

    private void updateFieldsUsing2OptInfo(Find2Opt optInfo) {
        this.valueAssignment = optInfo.getValueAssignmnet1();
        this.neighborCounters.put(partnerNodeId,this.neighborCounters.get(partnerNodeId)+1);
        this.neighborsValueAssignmnet.put(partnerNodeId, new MsgReceive<Integer>(optInfo.getValueAssignmnet2(),0));
    }

    private void setPartnerNodeId() {

        Map<NodeId,Integer>whoIHaveInfoFor = new HashMap<>();
        for (NodeId nodeId:this.neighborsInfo.keySet()) {
            whoIHaveInfoFor.put(nodeId,this.neighborCounters.get(nodeId));
        }
        int minVal = Collections.min(whoIHaveInfoFor.values());
        for (NodeId nodeId:whoIHaveInfoFor.keySet()) {
            if (minVal == whoIHaveInfoFor.get(nodeId)){
                this.neighborCounters.put(nodeId,this.neighborCounters.get(nodeId)+1);
                this.partnerNodeId = nodeId;
                return;
            }
        }
        throw new RuntimeException("Was suppose to find partner");
    }

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }



}
