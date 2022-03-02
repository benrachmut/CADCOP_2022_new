package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import Messages.MsgAlgorithm;

public class DSA_SDP_ASY extends DSA_SDP {

	public DSA_SDP_ASY(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		updateAlgorithmName();
	}
	
	@Override
	public void updateAlgorithmName() {
		AgentVariable.AlgorithmName = "DSA_SDP_ASY";
	}
	
	
	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
		this.canCompute = true;
	}
	

	@Override
	public void changeRecieveFlagsToFalse() {
		canCompute = false;
	}

	@Override
	protected void resetAgentGivenParametersV5() {
		// no special fields....
	}


}
