package AlgorithmSearch;

import AgentsAbstract.AgentVariableSearch;
import Messages.MsgAlgorithm;

public class MonoToken2Opt extends AgentVariableSearch {

    private TokenTree treeToken;
    private boolean isTreeComplete;

    public MonoToken2Opt(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
    }

    @Override
    public void initialize() {
        super.initialize();
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
    protected void resetAgentGivenParametersV3() {

    }
}
