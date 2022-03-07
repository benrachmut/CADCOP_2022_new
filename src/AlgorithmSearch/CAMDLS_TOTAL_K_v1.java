package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.Msg;
import Messages.MsgAMDLS;
import Messages.MsgAMDLSColor;
import Messages.MsgAlgorithm;

import java.util.*;

import static Delays.ProtocolDelayWithK.k_public;

public class CAMDLS_TOTAL_K_v1 extends CAMDLS_NAIVE {

    private  int counter_for_debug;
    private boolean flagToAvoidLoop;

    Map<NodeId,Map<NodeId,Integer>> whatOthersThinkAboutAll;
    Map<NodeId,Integer> whatIKnowAboutOthers;


    public CAMDLS_TOTAL_K_v1(int dcopId, int D, int agentId) {
        super(dcopId, D, agentId);
        AMDLS_V1.sendWhenMsgReceive = true;
        this.counter_for_debug = 0;
        flagToAvoidLoop = false;
        resetMapsOfTimeStamp();
        whatIKnowAboutOthers =  new HashMap<NodeId,Integer>();

    }

    @Override
    public void resetAgentGivenParametersV3() {
        super.resetAgentGivenParametersV3();
        whatOthersThinkAboutAll = new HashMap<NodeId,Map<NodeId,Integer>>();
    }

    private void resetMapsOfTimeStamp() {
        whatOthersThinkAboutAll = new HashMap<NodeId,Map<NodeId,Integer>>();
        whatIKnowAboutOthers = new HashMap<NodeId,Integer>();
        for (NodeId nodeId :neighborsConstraint.keySet()) {
            whatIKnowAboutOthers.put(nodeId,-1);
        }

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


    @Override
    protected void sendAMDLSmsgs() {
        int k = k_public;
        List<Msg> msgsToOutbox = new ArrayList<Msg>();
        List<NodeId> neighborsToSend = new ArrayList<NodeId>();

        while (k > 0) {
            List<NodeId> temp = this.getAllInconsistentAbove(k);
            neighborsToSend.addAll(temp);
            k = k - temp.size();

            if (k > 0) {
                temp = this.getAllInconsistentLower(k);
                neighborsToSend.addAll(temp);
                k = k - temp.size();

            }
        }


        for (NodeId recieverNodeId : neighborsConstraint.keySet()) {
            int rndK = this.r.nextInt(k);
            for (int i = 0; i < k; i++) {
                MsgAMDLS mva = new MsgAMDLS(this.nodeId, recieverNodeId, this.valueAssignment, this.timeStampCounter,
                        this.time, this.myCounter);
                if (rndK == i) {
                    mva.changeToNoLoss();
                    if (MainSimulator.isAMDLSDistributedDebug){
                        System.out.println("A_"+this.id+" sent message to A_"+recieverNodeId.getId1()+" amdls");
                    }
                }
                msgsToOutbox.add(mva);
            }

        }
        this.outbox.insert(msgsToOutbox);
    }




    @Override
    protected void sendAMDLSColorMsgs() {
        this.counter_for_debug  = 1+this.counter_for_debug;
        int k = k_public;
        List<Msg> msgsToOutbox = new ArrayList<Msg>();
        List<NodeId> neighborsToSend = new ArrayList<NodeId>();
        boolean flag = false;
        while (k > 0) {
            List<NodeId> temp = this.getAllNeighborsWithoutColorsSmallerIndex(k);
            neighborsToSend.addAll(temp);
            k = k - temp.size();
            if (!temp.isEmpty()){
                flag = true;
            }

            if (k > 0) {
                temp = this.getAllNeighborsWithNoColorLargerIndex(k);
                neighborsToSend.addAll(temp);
                k = k - temp.size();
                if (!temp.isEmpty()) {
                    flag = true;
                }
            }
            if (!flag){
                temp = this.getNeighborsByIndex(k);
                neighborsToSend.addAll(temp);
                k = k - temp.size();
            }
        }
        if (neighborsToSend.size() != k_public) {
            throw new RuntimeException("logical bug size should be k");
        }


        int rndK = this.r.nextInt(k_public);

        for (int i = 0; i < neighborsToSend.size(); i++) {
            NodeId recieverNodeId = neighborsToSend.get(i);


            MsgAMDLSColor mva = new MsgAMDLSColor(this.nodeId, recieverNodeId, this.valueAssignment,
                    this.timeStampCounter, this.time, this.myCounter, this.myColor);
            if (rndK == i) {
                if (MainSimulator.isAMDLSDistributedDebug){
                    System.out.println("A_"+this.id+" sent message to A_"+recieverNodeId.getId1()+" color");
                }
                mva.changeToNoLoss();
            }
            msgsToOutbox.add(mva);
        }

        outbox.insert(msgsToOutbox);
    }

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

    public boolean getDidComputeInThisIteration() {


        return canSetColorFlag || consistentFlag || AMDLS_V1.sendWhenMsgReceive;
    }

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


    public void sendMsgs() {
        //boolean sendAllTheTime = AMDLS_V1.sendWhenMsgReceive && this.gotMsgFlag;
        boolean flag = false;
        flagToAvoidLoop = false;
        if ( this.canSetColorFlag || AMDLS_V3.sendWhenMsgReceive) {
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
        if (flag || (consistentFlag && !canSetColorFlag)  || AMDLS_V3.sendWhenMsgReceive) {
            if (flag) {
                decideAndChange();
                this.timeStampCounter = this.timeStampCounter+1;
            }
            if (this.myColor!=null && !flagToAvoidLoop) {
                sendAMDLSmsgs();
            }
        }

    }

    protected void updateNColor(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgAMDLSColor) {
            Integer colorN = ((MsgAMDLSColor) msgAlgorithm).getColor();
            if (colorN != null) {
                super.updateNColor(msgAlgorithm);

            }
        }
    }

}
