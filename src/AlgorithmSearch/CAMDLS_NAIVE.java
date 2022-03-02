package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.NodeId;
import Messages.Msg;
import Messages.MsgAMDLS;
import Messages.MsgAMDLSColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static Delays.ProtocolDelayWithK.k_public;

public class CAMDLS_NAIVE extends  AMDLS_V3{
    private Random r;

    public CAMDLS_NAIVE(int dcopId, int D, int agentId) {
        super(dcopId, D, agentId);
        this.isWithTimeStamp=true;
        r = new Random(dcopId*1000+agentId*10);

    }

    @Override
    public void resetAgentGivenParametersV3() {
        super.resetAgentGivenParametersV3();
        r = new Random(dcopId*1000+this.id*10);
    }

    @Override
    public void updateAlgorithmName() {
        AgentVariable.AlgorithmName = "CAMDLS Naive";
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
                if (rndK==i){
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
        List<Msg>msgsToOutbox = new ArrayList<Msg>();
            for (NodeId recieverNodeId : neighborsConstraint.keySet()) {
                int rndK = this.r.nextInt(k);
                for (int i = 0; i < k; i++) {
                    MsgAMDLSColor mva = new MsgAMDLSColor(this.nodeId, recieverNodeId, this.valueAssignment,
                            this.timeStampCounter, this.time, this.myCounter, this.myColor);
                    if (rndK==i){
                        mva.changeToNoLoss();
                    }


                msgsToOutbox.add(mva);

            }
        }
        outbox.insert(msgsToOutbox);
    }


}
