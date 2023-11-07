package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import Messages.MsgAlgorithm;

public class MonoToken2Opt extends AgentVariableSearch {

    private TokenTree tokenTree;
    private boolean isTreeComplete;

    public MonoToken2Opt(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        updateAlgorithmHeader();
        updateAlgorithmData();
        updateAlgorithmName();
        resetAgentGivenParametersV3();
    }


    @Override
    protected void resetAgentGivenParametersV3() {
        isTreeComplete = false;
        tokenTree = null;

    }

    @Override
    public void initialize() {
        if (this.nodeId.getId1() == 0) {
            TokenTree tr = new TokenTree(this.nodeId);
        }
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
        AgentVariable.algorithmHeader = "";
    }

    @Override
    public void updateAlgorithmData() {
        AgentVariable.algorithmData = "";
    }

    @Override
    public void updateAlgorithmName() {
        AgentVariable.AlgorithmName = "TokenBase2Opt";
    }

}
