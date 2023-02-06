package AlgorithmInference;

import AgentsAbstract.AgentVariableInference;
import AgentsAbstract.NodeId;
import Main.UnboundedBuffer;
import Messages.Msg;
import Messages.MsgAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class MaxSumVariableBen extends AgentVariableInference {
    private Map<NodeId, Map<Integer,Double>> localView;
    private Map<Integer,Double> currentContext;
    private Map<NodeId,Map<Integer,Double>> infoToSend;

    public MaxSumVariableBen(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        this.localView = new HashMap<NodeId,Map<Integer,Double>>();
        this.currentContext = new HashMap<Integer,Double>();
        for (int i = 0; i <D ; i++) {
            currentContext.put(i,0.0);
        }

        this.outbox = new UnboundedBuffer<Msg>();
        this.inbox = new UnboundedBuffer<Msg>();
        this.nodeId = new NodeId(id1);
    }



    @Override
    public String toString() {
        return "AgentVariable:"+this.nodeId;
    }

    @Override
    public void initialize() {

    }

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        NodeId sender = msgAlgorithm.getSenderId();
        Map<Integer,Double> infoFromMsg = (HashMap<Integer,Double>)msgAlgorithm.getContext();
        this.localView.put(sender,infoFromMsg);
        return true;
    }

    @Override
    public boolean getDidComputeInThisIteration() {
        return false;
    }

    @Override
    public boolean compute() {
        selectValueForVariable();
        createMsgs();


        return true;
    }

    private void selectValueForVariable() {
        for (int i = 0; i < this.domainSize; i++) {

        }

    }

    @Override
    public void sendMsgs() {

    }

    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

    }

    @Override
    public void changeReceiveFlagsToFalse() {

    }

    @Override
    public void updateAlgorithmHeader() {

    }

    @Override
    public void updateAlgorithmData() {

    }

    @Override
    public void updateAlgorithmName() {

    }

    @Override
    protected void resetAgentGivenParametersV3() {

    }
}
