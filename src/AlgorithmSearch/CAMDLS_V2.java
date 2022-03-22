package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AlgorithmSearch.CAMDLS_NAIVE;
import Messages.Msg;
import Messages.MsgAMDLS;
import Messages.MsgAMDLSColor;
import Messages.MsgAlgorithm;

import java.util.*;

import static Delays.ProtocolDelayWithK.k_public;

public class CAMDLS_V2 extends AgentVariableSearch {
    protected TreeMap<NodeId, Integer> neighborColors;
    protected Integer myColor;

    public CAMDLS_V2(int dcopId, int D, int agentId) {
        super(dcopId, D, agentId);
        neighborColors = new TreeMap<NodeId, Integer>();
        myColor = null;
    }

    @Override
    protected void resetAgentGivenParametersV3() {
        myColor = null;
        resetNeighborColors();
    }
    private void resetNeighborColors() {
        neighborColors = new TreeMap<NodeId, Integer>();
        for (NodeId nodeId : this.neighborsConstraint.keySet()) {
            neighborColors.put(nodeId, null);
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


    protected void sendAMDLSmsgs() {
        int k = k_public;
        List<Msg> msgsToOutbox = new ArrayList<Msg>();
        for (NodeId recieverNodeId : neighborsConstraint.keySet()) {
            int rndK = this.r.nextInt(k);
            for (int i = 0; i < k; i++) {
                MsgAMDLS mva = new MsgAMDLS(this.nodeId, recieverNodeId, this.valueAssignment, this.timeStampCounter,
                        this.time, this.myCounter);
                if (rndK == i) {
                    mva.changeToNoLoss();
                }
                msgsToOutbox.add(mva);
            }

        }
        this.outbox.insert(msgsToOutbox);
    }


    protected void sendAMDLSColorMsgs() {
        int k = k_public;
        List<Msg> msgsToOutbox = new ArrayList<Msg>();
        List<NodeId> neighborsToSend = new ArrayList<NodeId>();
        while (k > 0) {
            List<NodeId> temp = this.getAllNeighborsWithoutColorsSmallerIndex(k);
            neighborsToSend.addAll(temp);
            k = k - temp.size();

            if (k > 0) {
                temp = this.getAllNeighborsWithNoColorLargerIndex(k);
                neighborsToSend.addAll(temp);
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
                mva.changeToNoLoss();
            }
            msgsToOutbox.add(mva);
        }

        outbox.insert(msgsToOutbox);
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

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }

    @Override
    protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        return false;
    }

    @Override
    public boolean getDidComputeInThisIteration() {
        return false;
    }

    @Override
    protected boolean compute() {
        return false;
    }

    @Override
    public void sendMsgs() {

    }

    @Override
    protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

    }

    @Override
    public void changeRecieveFlagsToFalse() {

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
        updateMyCounter();
		/*
		if (MainSimulator.is2OptDebug|| MainSimulator.isAMDLSDistributedDebug) {
			System.out.println("A_"+this.id+" color: "+this.myColor);
		}
		*/

    }

    protected void updateMyCounter(){
        this.myCounter = 1;
    }
    @Override
    public void initialize() {
        this.isWithTimeStamp = false;
        if (determineByIndexInit()) {
            chooseColor();


            sendAMDLSColorMsgs();
            this.myCounter = 1;
            //firstFlag = true;
            isWaitingToSetColor = false;
        } else {
            this.valueAssignment = Integer.MIN_VALUE;
            this.myCounter = 0;
        }
    }
}
