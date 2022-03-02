package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.NodeId;
import AlgorithmSearch.CAMDLS_NAIVE;
import Messages.Msg;
import Messages.MsgAMDLS;
import Messages.MsgAMDLSColor;

import java.util.*;

import static Delays.ProtocolDelayWithK.k_public;

public class CAMDLS_V2 extends CAMDLS_NAIVE {

    public CAMDLS_V2(int dcopId, int D, int agentId) {
        super(dcopId, D, agentId);
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

    @Override
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
}
