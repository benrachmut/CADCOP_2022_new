package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Main.MainSimulator;
import Messages.*;

import java.util.*;

public class MonoDeterministicColorChange extends AgentVariableSearch implements SelfCounterable {
    private NodeId partnerNodeId;
    public static  char typeDecision = 'c' ;



    enum status {
        selectColor,
        consistent,
        consistentAndColor,
        waitForReply, changeAlone, receiveOffer, ableToReply, unableToReply, doWhatPartnerSay, idle
    }
    private int counter;
    protected int selfCounter;
    protected int myColor;
    private Random rndForPartners;
    private HashMap<NodeId, KOptInfo> neighborsInfo;
    private HashMap<NodeId, Integer> neighborsColor;
    HashMap<NodeId, Integer> neighborCounters;
    private HashMap<NodeId, Integer> neighborPartnerCounters;
    //private boolean statSelectColor;
    //private boolean isInconsistent;
    private status myStatues;

    public MonoDeterministicColorChange(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        AMDLS_V1.typeDecision = 'c';
        updateAlgorithmHeader();
        updateAlgorithmData();
        updateAlgorithmName();
        resetAgentGivenParametersV3();
    }

    @Override
    public int getSelfCounterable() {
        return this.selfCounter;
    }
    protected void resetAgentGivenParametersV3() {
        counter = 0;
        this.selfCounter = 1;
        this.myColor = -1;
        this.rndForPartners = new Random(1+this.nodeId.getId1()*17);

        this.neighborsInfo = new HashMap<NodeId,KOptInfo>();
        this.neighborsColor = new HashMap<NodeId,Integer>();
        this.neighborCounters = new HashMap<NodeId,Integer>();
        this.neighborPartnerCounters = new HashMap<NodeId,Integer>();

        for (NodeId nId: this.neighborsConstraint.keySet()){
            this.neighborsColor.put(nId,null);
            this.neighborCounters.put(nId,0);
            this.neighborPartnerCounters.put(nId,0);
        }


        myStatues = status.idle;
        isWithTimeStamp = false;
        partnerNodeId = null;
    }


    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

        NodeId sender = msgAlgorithm.getSenderId();
        boolean flag = false;
        if (this.myColor == -1 && canSetColor()) {
            flag = true;
            this.myStatues = status.selectColor;
            chooseColor();
        }


        if ( checkConsistency() &&  (this.myStatues != status.waitForReply) ) {
            this.myStatues = status.consistent;
        }

        if (flag &&  this.myStatues == status.consistent  &&  (this.myStatues != status.waitForReply)){
            this.myStatues = status.consistentAndColor;
        }

        if (msgAlgorithm instanceof MsgMDC2CFriendRequest && this.selfCounter==((MsgMDC2CFriendRequest) msgAlgorithm).getCounter()){
            this.myStatues = status.receiveOffer;
        }

        if (msgAlgorithm instanceof MsgMDC2CFriendReply && this.myStatues == status.waitForReply){

            this.myStatues = status.doWhatPartnerSay;

        }


        if (this.myStatues==status.waitForReply && this.partnerNodeId.getId1() == sender.getId1() && !(msgAlgorithm instanceof MsgMDC2CFriendReply)){
            this.neighborsInfo.clear();

            this.myStatues = status.changeAlone;
            this.selfCounter = this.selfCounter+1;
            changeValueAssignment();
            if (MainSimulator.isMDC2CDebug && this.myStatues != status.idle) {
                System.out.println(this + " is "+ this.myStatues+", nCounters:"+this.neighborCounters+", selfCounter:"+this.selfCounter+ " ***END");
            }

        }



        if (MainSimulator.isMDC2CDebug ) {
            System.out.println(this + " is "+ this.myStatues+", nCounters:"+this.neighborCounters+", selfCounter:"+this.selfCounter+" sender id: "+msgAlgorithm.getSenderId()+" **changeToTrue");
        }



    }




    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        this.counter = this.counter+1;
        return this.counter;
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        NodeId sender = msgAlgorithm.getSenderId();

        int neighborCounter = ((MsgAMDLSColor) msgAlgorithm).getCounter();
        int currentNeighborCounter = this.neighborCounters.get(sender);
        if (currentNeighborCounter<neighborCounter) {
            if (msgAlgorithm instanceof MsgAMDLSColor) {
                int neighborColor = ((MsgAMDLSColor) msgAlgorithm).getColor();
                this.neighborsColor.put(sender, neighborColor);

                this.neighborCounters.put(sender, neighborCounter);
                if (this.neighborsInfo.containsKey(sender)) {
                    this.neighborsInfo.remove(sender);
                }
                //if (counterCheck()){
                //  throw new RuntimeException();
                //}
            }
        }
        if (msgAlgorithm instanceof MsgValueAssignmnet && !(msgAlgorithm instanceof MsgMDC2CFriendRequest)&& !(msgAlgorithm instanceof MsgMDC2CFriendReply)) {
            updateMsgInContextValueAssignment(msgAlgorithm);
        }

        if (msgAlgorithm instanceof MsgMDC2CFriendRequest){
            updateValueAWithKOptInfo(msgAlgorithm);
            KOptInfo nInfo = ((KOptInfo) msgAlgorithm.getContext());
            if (!neighborsInfo.isEmpty()){
                for (NodeId nodeId:this.neighborsInfo.keySet()) {
                    if (nodeId.getId1()>msgAlgorithm.getSenderId().getId1()){
                        this.neighborsInfo.remove(nodeId);
                        this.neighborsInfo.put(sender, nInfo);
                    }
                }
            }else {
                this.neighborsInfo.put(sender, nInfo);
            }
            //if (MainSimulator.isMDC2CDebug) {
              //  System.out.println(this+" receive friend request from "+ msgAlgorithm.getSenderId()+", nCounters:"+this.neighborCounters+", selfCounter:"+this.selfCounter);
            //}
        }

        if (this.partnerNodeId!=null && sender.getId1() == this.partnerNodeId.getId1()) {
            if (msgAlgorithm instanceof MsgMDC2CFriendReply) {
                updateNeighborsValueAssignmnet(msgAlgorithm);
                updateMyValue(msgAlgorithm);
                int ccc = this.neighborPartnerCounters.get(sender);
                this.neighborPartnerCounters.put(sender, ccc + 1);
                this.selfCounter = this.selfCounter + 1;
            }
        }
            //}else{
            // changeValueAssignment();
            //}



            return  true;
    }

    private boolean counterCheck() {

        int min = Collections.min(this.neighborCounters.values());
        int max = Collections.max(this.neighborCounters.values());
        int delta = max - min;
        if (delta>=2){
            return true;
        }
        return false;
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
        return myStatues ==status.doWhatPartnerSay || myStatues ==status.consistent ||  myStatues ==status.selectColor ||
                this.myStatues == status.consistentAndColor || this.myStatues == status.receiveOffer || this.myStatues == status.unableToReply || this.myStatues == status.changeAlone;
    }

    @Override
    public boolean compute() {
        if (this.myStatues == status.selectColor || this.myStatues == status.consistentAndColor){

        }

        if (this.myStatues == status.consistent|| this.myStatues == status.consistentAndColor){
            NodeId neighborSelect = this.selectNeighborForPartnership();
            this.partnerNodeId = neighborSelect;
            if (MainSimulator.isMDC2CDebug){
                System.out.println(this+ " selected " +partnerNodeId);
            }

            if (partnerNodeId == null){
                this.myStatues = status.changeAlone;
                this.selfCounter = this.selfCounter+1;
                this.neighborsInfo.clear();
                changeValueAssignment();
                if (MainSimulator.isMDC2CDebug && this.myStatues != status.idle) {
                    System.out.println(this + " is "+ this.myStatues+", nCounters:"+this.neighborCounters+", selfCounter:"+this.selfCounter+ " ***END");
                }
                //if (MainSimulator.isMDC2CDebug){
                  //  System.out.println(this+" cannot select neighbor for partner so it changes value alone"+", nCounters:"+this.neighborCounters+", selfCounter:"+this.selfCounter);
                //}
            }else{
                //if (MainSimulator.isMDC2CDebug){
                  //  System.out.println(this+" selected "+ this.partnerNodeId +" as a partner"+", nCounters:"+this.neighborCounters+", selfCounter:"+this.selfCounter);
                //}
            }
        }
        
        if (this.myStatues == status.receiveOffer ||  this.myStatues == status.unableToReply){
            if (isNeighborsByIndexConstraintOk() && allLargerColorsCountersAreEqual()){
                this.selfCounter = this.selfCounter+1;
                this.myStatues = status.ableToReply;
            }else{
                this.myStatues = status.unableToReply;
            }
        }

        if(this.myStatues == status.doWhatPartnerSay){

        }
        if (MainSimulator.isMDC2CDebug ) {
            System.out.println(this + " is "+ this.myStatues+", nCounters:"+this.neighborCounters+", selfCounter:"+this.selfCounter+" **compute");
        }
        return true;
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
        if (this.myStatues == status.selectColor || this.myStatues == status.consistentAndColor || this.myStatues == status.changeAlone ||this.myStatues == status.doWhatPartnerSay) {
            this.neighborsInfo.clear();
            sendColorMsgs();
        }
        if (this.myStatues == status.consistent || this.myStatues == status.consistentAndColor) {
            sendInfoToPartner();
        }
        if (this.myStatues == status.ableToReply){
            NodeId whoNotToSendColor = sendPartnerReply();
            if (MainSimulator.isMDC2CDebug) {
                System.out.println(this + " sends all the rest (except for partner" + whoNotToSendColor+"), nCounters:" + this.neighborCounters + ", selfCounter:" + this.selfCounter);
            }
            sendColorToTheRest(whoNotToSendColor);
        }
    }



    private boolean checkConsistency() {
        return allNeighborsHaveColor()&& allLargerColorsCountersAreEqual() && allSmallerColorsPlusOne();
    }

    @Override
    public void changeReceiveFlagsToFalse() {
        if (this.myStatues == status.consistentAndColor ||this.myStatues == status.consistent){
            this.myStatues = status.waitForReply;
        }


        if(this.myStatues == status.doWhatPartnerSay){
            this.partnerNodeId = null;
        }
        if (this.myStatues == status.doWhatPartnerSay|| this.myStatues == status.idle || this.myStatues == status.selectColor || this.myStatues == status.changeAlone || this.myStatues == status.ableToReply){
            this.myStatues = status.idle;
        }
/*
        if (this.myStatues == status.unableToReply){
            this.myStatues = status.unableToReply;
        }
*/

        if (MainSimulator.isMDC2CDebug ) {
            System.out.println(this + " is "+ this.myStatues+", nCounters:"+this.neighborCounters+", selfCounter:"+this.selfCounter+ " ***END");
        }

    }





    @Override
    public void initialize() {
        if (canSetColor()){
            this.myColor = 1;
            this.sendColorMsgs();
            if (MainSimulator.isMDC2CDebug){
                System.out.println(this.nodeId+", color: "+ this.myColor);
            }
        }
    }



    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ///////////////////////////---top down methods---///////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    //********--------------------messages-------------------********

    protected void sendColorMsgs() {

        if (this.checkConsistencyV2()&& this.selfCounter!=1){
            this.selfCounter = this.selfCounter +1;

        }
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
        for (NodeId receiverNodeId : neighborsConstraint.keySet()) {
            MsgAMDLSColor msg= new MsgAMDLSColor(this.nodeId, receiverNodeId, this.valueAssignment,  this.timeStampCounter, this.time, this.selfCounter , this.myColor);
            msgsToInsertMsgBox.add(msg);
        }
        outbox.insert(msgsToInsertMsgBox);
    }

    protected boolean checkConsistencyV2() {
        return allNeighborsHaveColor()&& allLargerColorsCountersAreEqualV2() && allSmallerColorsPlusOne();
    }

    private void sendInfoToPartner() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();

            MsgMDC2CFriendRequest msg = new MsgMDC2CFriendRequest(this.nodeId, partnerNodeId, makeMyKOptInfo(), this.timeStampCounter, this.time, this.selfCounter, this.myColor);
            msgsToInsertMsgBox.add(msg);
            outbox.insert(msgsToInsertMsgBox);
            //if (MainSimulator.isMDC2CDebug){
              //  System.out.println(this+" sent MsgMDC2CFriendRequest"+", nCounters:"+this.neighborCounters+", selfCounter:"+this.selfCounter);
            //}

    }

    private KOptInfo makeMyKOptInfo() {
        return new KOptInfo(this.valueAssignment, nodeId, neighborsConstraint, domainArray,
                this.neighborsValueAssignmnet);
    }


    private NodeId sendPartnerReply() {
        //-------------------
        NodeId whoToReply = getWhoToReply();
        this.neighborPartnerCounters.put(whoToReply,this.neighborPartnerCounters.get(whoToReply)+1);
        Find2Opt optInfo = new Find2Opt(makeMyKOptInfo(), neighborsInfo.get(whoToReply));
        this.atomicActionCounter = optInfo.getAtomicActionCounter();
        this.neighborsInfo.clear();
        Map<NodeId,Integer>infoToSend2Opt = new HashMap<NodeId,Integer>();
        infoToSend2Opt.put(this.nodeId, optInfo.getValueAssignmnet1());
        infoToSend2Opt.put( whoToReply, optInfo.getValueAssignmnet2());
        this.valueAssignment = optInfo.getValueAssignmnet1();
        this.neighborCounters.put(whoToReply,this.neighborCounters.get(whoToReply)+1);
        this.neighborsValueAssignmnet.put(whoToReply, new MsgReceive<Integer>(optInfo.getValueAssignmnet2(),0));
        //-------------------
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
        NodeId receiver = whoToReply;
        MsgMDC2CFriendReply msg= new MsgMDC2CFriendReply(this.nodeId, receiver, infoToSend2Opt ,  this.timeStampCounter, this.time,this.selfCounter,this.myColor,this.nodeId.getId1());
        msgsToInsertMsgBox.add(msg);
        outbox.insert(msgsToInsertMsgBox);


        //if (MainSimulator.isMDC2CDebug) {
          //  System.out.println(this+" sent MsgMDC2CFriendReply to "+ whoToReply+", nCounters:"+this.neighborCounters+", selfCounter:"+this.selfCounter);
        //}
        return whoToReply;
    }


    protected void sendColorToTheRest(NodeId whoNotToSendColor) {
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
        Set<NodeId>whoISent = new HashSet<NodeId>();
        for (NodeId receiverNodeId : neighborsConstraint.keySet()) {
            if (receiverNodeId.getId1()!=whoNotToSendColor.getId1()){
                MsgAMDLSColor msg= new MsgAMDLSColor(this.nodeId, receiverNodeId, this.valueAssignment,  this.timeStampCounter, this.time, this.selfCounter , this.myColor);
                msgsToInsertMsgBox.add(msg);
                whoISent.add(receiverNodeId);
            }
        }
        if(!msgsToInsertMsgBox.isEmpty()) {
            outbox.insert(msgsToInsertMsgBox);
        }

        //if (MainSimulator.isMDC2CDebug){
        //  System.out.println(this+" sent color msg to the rest: "+whoISent);
        //}

    }


    private NodeId getWhoToReply() {
        //ArrayList<NodeId> nodeIdsWithMinCounter = getNodeIdsWithMinCounter(this.neighborsInfo.keySet());
        return Collections.min(this.neighborsInfo.keySet());
        /*
        if (nodeIdsWithMinCounter.size()==1){
            return nodeIdsWithMinCounter.get(0);
        }
        Collections.shuffle(nodeIdsWithMinCounter,this.rndForPartners);
        int selectedIndex = this.rndForPartners.nextInt(nodeIdsWithMinCounter.size());
        return nodeIdsWithMinCounter.get(selectedIndex);
        */

    }

    //********--------------------consistency-------------------********

    protected boolean allNeighborsHaveColor() {
        for (NodeId ni:this.neighborsColor.keySet()) {
            if (this.neighborsColor.get(ni)==null){
                return false;
            }
        }
        return true;
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


    private boolean allLargerColorsCountersAreEqualV2() {
        Set<NodeId>largerColorNeighbors = getLargerColorNeighbors();
        if (largerColorNeighbors == null){ // not all have colors
            return false;
        }
        for (NodeId ni: largerColorNeighbors) {
            int niCounter = this.neighborCounters.get(ni);
            if (niCounter == this.selfCounter ){

            }else{
                return false;
            }
        }
        return true;
    }


    protected Set<NodeId> getLargerColorNeighbors() {
        Set<NodeId>largerColorNeighbors = new HashSet<NodeId>();

        for (NodeId ni:this.neighborsColor.keySet()) {
            try {
                if (this.neighborsColor.get(ni) > this.myColor) {
                    largerColorNeighbors.add(ni);
                }
            }catch (Exception e){
                return null;
            }
        }
        return  largerColorNeighbors;

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

        for (NodeId ni:this.neighborsColor.keySet()) {
                if (this.neighborsColor.get(ni)<this.myColor){
                    smallerColorNeighbors.add(ni);
                }

        }


        return  smallerColorNeighbors;
    }
    //********--------------------color stuff-------------------********
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
    protected Set<NodeId> getNeighborsThatHaveColor() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (Map.Entry<NodeId, Integer> e : this.neighborsColor.entrySet()) {
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
        if (MainSimulator.isMDC2CDebug){
            System.out.println(this.nodeId+", color: "+ this.myColor);
        }
    }
    private boolean isColorValid(Integer currentColor) {
        for (Integer nColor : neighborsColor.values()) {
            if (nColor != null) {
                if (nColor.equals(currentColor)) {
                    return false;
                }
            }
        }
        return true;
    }

    //********--------------------partner selection-------------------********

    private NodeId selectNeighborForPartnership() {
        Set <NodeId> potentialNeighbors= getPotentialNeighbors();
        ArrayList <NodeId> nodeIdsWithMinCounter = this.getNodeIdsWithMinCounter(potentialNeighbors);
        if (nodeIdsWithMinCounter == null || nodeIdsWithMinCounter.isEmpty()){
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



    protected Set<NodeId> getPotentialNeighbors() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (NodeId neighbor : this.neighborsColor.keySet()) {
            int nColor = this.neighborsColor.get(neighbor);
            if (nColor - 1 == this.myColor && this.neighborCounters.get(neighbor)== this.selfCounter ){
                ans.add(neighbor);
            }
        }
        return ans;
    }



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


    private void extractNodeIdIHaveInfo(Set<NodeId> smallerColor) {
        Iterator<NodeId>it = smallerColor.iterator();
        while (it.hasNext()){
            NodeId nodeId = it.next();
            if (this.neighborsInfo.keySet().contains(nodeId)){
                it.remove();
            }
        }
    }


    private void extractColorIndexMinusOneWithLargerDocIndex(Set<NodeId> smallerColor) {
        Set<NodeId>colorMinusOne = getColorMinusOne(smallerColor);

        Collection<NodeId> toRemove = new HashSet<NodeId>();

        NodeId infoMaxIndex = getInfoMaxIndex();
        if (infoMaxIndex!=null) {
            for (NodeId ni : colorMinusOne) {
                if (infoMaxIndex.getId1() < ni.getId1()) {
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
            if (ni.getId1()>x){
                ans = ni;
            }
        }
        return ans;
    }

    protected Set<NodeId> getColorMinusOne(Set<NodeId> smallerColor) {
        Set<NodeId>colorMinusOne = new HashSet<NodeId>();
        for (NodeId ni:smallerColor) {
            int nColor = this.neighborsColor.get(ni);
            if (this.myColor -1  == nColor){
                colorMinusOne.add(ni);
            }

        }
        return colorMinusOne;
    }

    private void updateNeighborsValueAssignmnet(MsgAlgorithm msgAlgorithm) {
        Map<NodeId,Integer> infoOfMeAndPartner = (Map<NodeId, Integer>) ((MsgMDC2CFriendReply) msgAlgorithm).getContext();
        int nV = infoOfMeAndPartner.get(msgAlgorithm.getSenderId());
        int context = nV;
        int timestamp = msgAlgorithm.getTimeStamp();
        MsgReceive<Integer> msgReceive = new MsgReceive<Integer>(context, timestamp);
        this.neighborsValueAssignmnet.put(msgAlgorithm.getSenderId(), msgReceive);
    }


    private void updateMyValue(MsgAlgorithm msgAlgorithm) {
        Map<NodeId,Integer> infoOfMeAndPartner = (Map<NodeId, Integer>) ((MsgMDC2CFriendReply) msgAlgorithm).getContext();
        this.valueAssignment = infoOfMeAndPartner.get(this.nodeId);
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
