package AlgorithmSearch;

import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AgentsAbstract.SelfCounterable;
import Main.MainSimulator;
import Messages.MsgAlgorithm;
import Messages.MsgPT2opt;

import java.util.*;

public class MonoSequencePT2opt  extends AgentVariableSearch implements SelfCounterable {

    enum Status {
        idle,
        joinPseudoTree,
        consistent,
        replyToOffer,
        receiveReply
    }
    private Status myStatus;
    private int selfCounter;
    private MsgPT2opt msgPT2opt;
    private int pseudoTreeIndex;
    private Map<NodeId,Integer> largerPseudoTreeIndexNeighbor;
    private Map<NodeId,Integer> smallerPseudoTreeIndexNeighbor;

    @Override
    public void initialize() {
        selfCounter = 1;
        if (this.nodeId.getId1() == 0 && !this.neighborsConstraint.keySet().isEmpty()){
            pseudoTreeIndex = 0;
            Map <NodeId,Integer > nodeIdsInTree = new HashMap<NodeId,Integer>();
            nodeIdsInTree.put(this.nodeId, 0);
            NodeId minId = Collections.min(this.neighborsConstraint.keySet());
            nodeIdsInTree.put(minId,1);
            this.largerPseudoTreeIndexNeighbor.put(minId,1);
            msgPT2opt = new MsgPT2opt(this.nodeId,minId,nodeIdsInTree,this.timeStampCounter,this.time);
            //if (MainSimulator.)
        }else {
            new RuntimeException(this+" has no neighbors");
        }

        myStatus = Status.receiveReply;
        sendMsgs();

    }

    @Override
    protected void resetAgentGivenParametersV3() {

    }

    public MonoSequencePT2opt(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
    }

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        return false;
    }

    @Override
    public boolean getDidComputeInThisIteration() {
        return false;
    }

    @Override
    public boolean compute() {
        return false;
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
    public int getSelfCounterable() {
        return selfCounter;
    }
}
