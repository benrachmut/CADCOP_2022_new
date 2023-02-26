package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.*;

import java.util.*;

import static Delays.ProtocolDelayWithK.k_public;

public class CAMDLS_TOTAL_K_v1 extends CAMDLS_NAIVE {

    private int counter_for_debug;
    private boolean flagToAvoidLoop;

    Map<NodeId, Map<NodeId, Integer>> timestampMap;


    public CAMDLS_TOTAL_K_v1(int dcopId, int D, int agentId) {
        super(dcopId, D, agentId);
        AMDLS_V1.sendWhenMsgReceive = true;
        this.counter_for_debug = 0;
        flagToAvoidLoop = false;
        timestampMap = new HashMap<NodeId, Map<NodeId, Integer>>();

    }

    @Override
    protected void changeMyCounterByOne() {
        this.myCounter = this.myCounter + 1;
        changeTimestampMap();
    }

    private void changeTimestampMap() {
        if (!this.timestampMap.containsKey(this.nodeId)) {
            this.timestampMap.put(this.nodeId, new HashMap<NodeId, Integer>());
        }
        Map<NodeId, Integer> v = this.timestampMap.get(this.nodeId);
        v.put(this.nodeId, this.myCounter);
    }

    @Override
    public void resetAgentGivenParametersV3() {
        super.resetAgentGivenParametersV3();
        timestampMap = new HashMap<NodeId, Map<NodeId, Integer>>();
    }


    @Override
    public void updateAlgorithmName() {
        AgentVariable.AlgorithmName = "CAMDLS_TOTAL_K_v1";
    }

    @Override
    public void updateAlgorithmHeader() {
        AgentVariable.algorithmHeader = "k algo";
    }

    @Override
    public void updateAlgorithmData() {
        AgentVariable.algorithmData = Integer.toString(k_public);
    }


    private Integer getCurrentCounterFromMemory(NodeId nodeId, NodeId nodeIdFromMap) {
        if (!this.timestampMap.containsKey(nodeId)) {
            this.timestampMap.put(nodeId, new HashMap<NodeId, Integer>());
            return null;
        }
        Map<NodeId, Integer> theNodeView = this.timestampMap.get(nodeId);
        if (!theNodeView.containsKey(nodeIdFromMap)) {
            theNodeView.put(nodeIdFromMap, 0);
            return null;
        } else {
            return theNodeView.get(nodeIdFromMap);
        }

    }

    private void updateTimestampMap(MsgAlgorithm msgAlgorithm) {
        Map<NodeId, Map<NodeId, Integer>> receiveTimestamp = ((MsgAMDLSKAware) msgAlgorithm).getTimestampMap();
        for (NodeId nodeId : receiveTimestamp.keySet()) {
            Map<NodeId, Integer> theNodeView = receiveTimestamp.get(nodeId);
            for (NodeId nodeIdFromMap : theNodeView.keySet()) {
                int currentCounterFromMsg = theNodeView.get(nodeIdFromMap);
                Integer currentCounterFromMemory = getCurrentCounterFromMemory(nodeId, nodeIdFromMap);
                if (currentCounterFromMemory != null) {
                    if (currentCounterFromMsg > currentCounterFromMemory) {
                        Map<NodeId, Integer> theNodeViewFromMemory = this.timestampMap.get(nodeId);
                        theNodeViewFromMemory.put(nodeIdFromMap, currentCounterFromMsg);
                    }
                }
            }
        }
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        this.updateTimestampMap(msgAlgorithm);
        if (MainSimulator.isAMDLSDistributedDebug){
            System.out.println("A_"+this.id+" recieve message from  A_"+msgAlgorithm.getSenderId().getId1());
        }

        updateNColor( msgAlgorithm);
        boolean ans = addMsgToFuture(msgAlgorithm);
        //!haveAllColors()&&

        if (!ans){
            NodeId sender = msgAlgorithm.getSenderId();
            int currentCounterInContext = this.counters.get(sender);
            int msgCounter = ((MsgAMDLSKAware) msgAlgorithm).getCounter();

            if (currentCounterInContext + 1 == msgCounter) {
                updateMsgInContextValueAssignment(msgAlgorithm);
                updateCounterFromMsg(sender, msgCounter);

            } else {
                this.future.add((MsgAMDLSKAware) msgAlgorithm);
            }
        }
        return true;
    }

    protected void updateCounterFromMsg(NodeId sender, int msgCounter) {

        super.updateCounterFromMsg(sender, msgCounter); // update the memory
        this.updateMyViewOnTheWorld(sender, msgCounter);
    }

    private void updateMyViewOnTheWorld(NodeId sender, int msgCounter) {
        Map<NodeId, Integer> map = this.timestampMap.get(this.nodeId);
        map.put(sender, msgCounter);
    }





    private Map<NodeId, Integer> getWhatOtherThinkOfMeSmallerColor(Map<NodeId, Integer> whatOtherThinkOfMe) {
        Map<NodeId, Integer> ans = new HashMap<NodeId, Integer>();
        for (NodeId nodeId : whatOtherThinkOfMe.keySet()) {
            int colorOfNodeId = this.neighborColors.get(nodeId);
            if (colorOfNodeId < this.myColor) {
                ans.put(nodeId, whatOtherThinkOfMe.get(nodeId));
            }
        }
        return ans;
    }

    private List<NodeId> getSmallerColorNotAware(int k, Map<NodeId, Integer> whatOtherThinkOfMe) {
        List<NodeId> ans = new ArrayList<NodeId>();
        Map<NodeId, Integer> whatOtherThinkOfMeSmallerColor = getWhatOtherThinkOfMeSmallerColor(whatOtherThinkOfMe);
        for (NodeId nodeId : whatOtherThinkOfMeSmallerColor.keySet()) {
            ans.add(nodeId);
            k = k - 1;
            if (k == 0) {
                return ans;
            }
        }
        return ans;
    }

    private Map<NodeId, Integer> whatOtherThinkOfMeLargerColor(Map<NodeId, Integer> whatOtherThinkOfMe) {
        Map<NodeId, Integer> ans = new HashMap<NodeId, Integer>();
        for (NodeId nodeId : whatOtherThinkOfMe.keySet()) {
            int colorOfNodeId = this.neighborColors.get(nodeId);
            if (colorOfNodeId > this.myColor) {
                ans.put(nodeId, whatOtherThinkOfMe.get(nodeId));
            }
        }
        return ans;
    }

    private List<NodeId> getLargerColorNotAware(int k, Map<NodeId, Integer> whatOtherThinkOfMe) {
        List<NodeId> ans = new ArrayList<NodeId>();
        Map<NodeId, Integer> whatOtherThinkOfMeLargerColor = whatOtherThinkOfMeLargerColor(whatOtherThinkOfMe);
        for (NodeId nodeId : whatOtherThinkOfMeLargerColor.keySet()) {
            ans.add(nodeId);
            k = k - 1;
            if (k == 0) {
                return ans;
            }
        }
        return ans;
    }


    private List<NodeId> getNeighborsToSendAfterColorStage() {
        List<NodeId> neighborsToSend = new ArrayList<NodeId>();

        int k = k_public;
        Map<NodeId, Integer> whatOtherThinkOfMe = getWhatOtherThinkOfMe();

        while (k > 0) {
            List<NodeId> temp = this.getSmallerColorNotAware(k, whatOtherThinkOfMe);
            neighborsToSend.addAll(temp);
            k = k - temp.size();

            if (k > 0) {
                temp = this.getLargerColorNotAware(k, whatOtherThinkOfMe);
                neighborsToSend.addAll(temp);
                k = k - temp.size();
            }
        }
        return neighborsToSend;
    }

    @Override
    protected void sendAMDLSmsgs() {

        List<NodeId> neighborsToSend = getNeighborsToSendAfterColorStage();
        List<Msg> msgsToOutbox = new ArrayList<Msg>();
        int rndK = this.r.nextInt(k_public);

        for (int i = 0; i < neighborsToSend.size(); i++) {
            NodeId recieverNodeId = neighborsToSend.get(i);
            MsgAMDLSKAware mva = new MsgAMDLSKAware(this.nodeId, recieverNodeId, this.valueAssignment, this.timeStampCounter,
                    this.time, this.myCounter, this.timestampMap);
            if (rndK == i) {
                if (MainSimulator.isAMDLSDistributedDebug) {
                    System.out.println("A_" + this.id + " sent message to A_" + recieverNodeId.getId1() + " color");
                }
                mva.changeToNoLoss();
            }
            msgsToOutbox.add(mva);
        }


        this.outbox.insert(msgsToOutbox);
    }




    private Map<NodeId, Integer> getWhatOtherThinkOfMe() {
        Map<NodeId, Integer> ans = new HashMap<NodeId, Integer>();
        for (NodeId nodeViewMaps : this.timestampMap.keySet()) {
            if (this.neighborsConstraint.keySet().contains(nodeViewMaps)) {
                Map<NodeId, Integer> theMap = this.timestampMap.get(nodeViewMaps);
                try {
                    int theCounterItThinks = theMap.get(this.nodeId);
                    ans.put(nodeViewMaps, theCounterItThinks);
                }catch (NullPointerException e){
                    System.out.println("say whaaat");
                }
            }
        }

        for (NodeId nodeId: this.neighborsConstraint.keySet()){
            if (!ans.keySet().contains(nodeId)){
                ans.put(nodeId,0);
            }
        }
        return ans;
    }

    private List<NodeId> getNeighborsToSendColorStage() {
        int k = k_public;

        List<NodeId> neighborsToSend = new ArrayList<NodeId>();
        Map<NodeId, Integer> whatOtherThinkOfMe = getWhatOtherThinkOfMe();

        while (k > 0) {
            //List<NodeId> temp = this.getAllNeighborsWithoutColorsSmallerIndex(k);
            List<NodeId> temp = this.getSmallerIndexNotAware(k, whatOtherThinkOfMe);
            neighborsToSend.addAll(temp);
            k = k - temp.size();

            if (k > 0) {
                temp = this.getLargerIndexNotAware(k, whatOtherThinkOfMe);
                neighborsToSend.addAll(temp);
                k = k - temp.size();
            }
        }

        if (neighborsToSend.size() != k_public) {
            throw new RuntimeException("logical bug size should be k");
        }
        return neighborsToSend;
    }

    private List<NodeId> getLargerIndexNotAware(int k, Map<NodeId, Integer> whatOtherThinkOfMe) {
        List<NodeId> ans = new ArrayList<NodeId>();
        for (NodeId nodeId : whatOtherThinkOfMe.keySet()) {
            if (nodeId.getId1() > this.id) {
                ans.add(nodeId);
                k = k - 1;
                if (k == 0) {
                    return ans;
                }
            }
        }
        return ans;
    }

    private List<NodeId> getSmallerIndexNotAware(int k, Map<NodeId, Integer> whatOtherThinkOfMe) {
        List<NodeId> ans = new ArrayList<NodeId>();
        for (NodeId nodeId : whatOtherThinkOfMe.keySet()) {
            if (nodeId.getId1() < this.id) {
                ans.add(nodeId);
                k = k - 1;
                if (k == 0) {
                    return ans;
                }
            }
        }
        return ans;
    }

    @Override
    protected void sendAMDLSColorMsgs() {
        this.counter_for_debug = 1 + this.counter_for_debug;

        List<NodeId> neighborsToSend = getNeighborsToSendColorStage();
        List<Msg> msgsToOutbox = new ArrayList<Msg>();
        int rndK = this.r.nextInt(k_public);

        for (int i = 0; i < neighborsToSend.size(); i++) {
            NodeId recieverNodeId = neighborsToSend.get(i);


            MsgAMDLSColorKAware mva = new MsgAMDLSColorKAware(this.nodeId, recieverNodeId, this.valueAssignment,
                    this.timeStampCounter, this.time, this.myCounter, this.timestampMap, this.myColor);
            if (rndK == i) {
                if (MainSimulator.isAMDLSDistributedDebug) {
                    System.out.println("A_" + this.id + " sent message to A_" + recieverNodeId.getId1() + " color");
                }
                mva.changeToNoLoss();
            }
            msgsToOutbox.add(mva);
        }

        outbox.insert(msgsToOutbox);
    }









/*
    private List<NodeId> getNeighborsByIndex(int k) {
        List<NodeId> ans = new ArrayList<>();
        int counter = 0;
        for (NodeId nodeId : neighborsConstraint.keySet()) {
            ans.add(nodeId);
            counter = counter + 1;
            if (counter == k) {
                return ans;
            }

        }
        return ans;
    }
    */


    public boolean getDidComputeInThisIteration() {


        return canSetColorFlag || consistentFlag || AMDLS_V1.sendWhenMsgReceive;
    }
/*
    private List<NodeId> getAllNeighborsWithNoColorLargerIndex(int k) {
        List<NodeId> ans = new ArrayList<>();
        int counter = 0;
        for (NodeId nodeId : neighborsConstraint.keySet()) {
            Integer nColor = this.neighborColors.get(nodeId);

            if (nColor == null && this.nodeId.getId1() < nodeId.getId1()) {
                ans.add(nodeId);
                counter = counter + 1;
                if (counter == k) {
                    return ans;
                }
            }
        }
        return ans;
    }

 */

/*
    private List<NodeId> getAllNeighborsWithoutColorsSmallerIndex(int k) {
        List<NodeId> ans = new ArrayList<>();
        int counter = 0;
        for (NodeId nodeId : neighborsConstraint.keySet()) {
            Integer nColor = this.neighborColors.get(nodeId);

            if (nColor == null && this.nodeId.getId1() > nodeId.getId1()) {
                ans.add(nodeId);
                counter = counter + 1;
                if (counter == k) {
                    return ans;
                }
            }
        }
        return ans;
    }
    */
/*
    private List<NodeId> getAllInconsistentAbove(int k) {
        List<NodeId> ans = new ArrayList<>();
        int counter = 0;
        for (NodeId nodeId : above) {
            Integer nCounter = this.counters.get(nodeId);

            if (nCounter == myCounter) {
                ans.add(nodeId);
                counter = counter + 1;
                if (counter == k) {
                    return ans;
                }
            }
        }
        return ans;
    }
*/
/*
    private List<NodeId> getAllInconsistentLower(int k) {
        List<NodeId> ans = new ArrayList<>();
        int counter = 0;
        for (NodeId nodeId : below) {
            Integer nCounter = this.counters.get(nodeId);

            if (nCounter != myCounter) {
                ans.add(nodeId);
                counter = counter + 1;
                if (counter == k) {
                    return ans;
                }
            }
        }
        return ans;
    }


 */


    public void sendMsgs() {
        //boolean sendAllTheTime = AMDLS_V1.sendWhenMsgReceive && this.gotMsgFlag;
        boolean flag = false;
        flagToAvoidLoop = false;
        if (this.canSetColorFlag || AMDLS_V3.sendWhenMsgReceive) {
            sendAMDLSColorMsgs();
            flagToAvoidLoop = true;
            this.consistentFlag = false;
            this.canSetColorFlag = false;
            if (releaseFutureMsgs()) {
                reactionToAlgorithmicMsgs();
            }
            boolean aboveConsistent = isAboveConsistent();
            boolean belowConsistent = isBelowConsistent();
            if (aboveConsistent && belowConsistent && allNeighborsHaveColor()) {
                flag = true;
            } else {
                flag = false;
            }
        }
        if (flag || (consistentFlag && !canSetColorFlag) || AMDLS_V3.sendWhenMsgReceive) {
            if (flag) {
                decideAndChange();
                this.timeStampCounter = this.timeStampCounter + 1;
            }
            if (this.myColor != null && !flagToAvoidLoop) {
                sendAMDLSmsgs();
            }
        }

    }

    protected void updateNColor(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgAMDLSColor) {
            Integer colorN = ((MsgAMDLSColor) msgAlgorithm).getColor();
            if (colorN != null) {
                //super.updateNColor(msgAlgorithm);

            }
        }
    }


    protected boolean addMsgToFuture(MsgAlgorithm msgAlgorithm) {
        if ( (msgAlgorithm instanceof MsgAMDLSColorKAware)==false && this.myCounter<=1
                && !((MsgAMDLSKAware)msgAlgorithm).isFromFuture()) {

            future.add((MsgAMDLSKAware)msgAlgorithm);
            return true;
        }
        return false;
		/*
		if (!canSetColor() && this.isWaitingToSetColor && msgAlgorithm instanceof MsgAMDLSColor) {

		} */
    }

}
