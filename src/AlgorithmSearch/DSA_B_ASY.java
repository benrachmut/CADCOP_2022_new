package AlgorithmSearch;

import java.util.Random;


import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Messages.MsgAlgorithm;
import Messages.MsgValueAssignmnet;

public class DSA_B_ASY extends DSA_B {


	public DSA_B_ASY(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		updateAlgorithmName();

	}
	

	

	@Override
	public void updateAlgorithmName() {
		AgentVariable.AlgorithmName = "DSA_ASY";
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
	protected void resetAgentGivenParametersV4() {
		// no special fields....
	}



}
