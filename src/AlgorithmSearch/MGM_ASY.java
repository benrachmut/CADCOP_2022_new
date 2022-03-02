package AlgorithmSearch;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Messages.MsgAlgorithm;
import Messages.MsgLR;
import Messages.MsgReceive;
import Messages.MsgValueAssignmnet;

public class MGM_ASY extends MGM {

	public MGM_ASY(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		updateAlgorithmName();

	}

	@Override
	protected void resetAgentGivenParametersV4() {
		// no specific fields
	}

	@Override
	public void updateAlgorithmName() {
		AgentVariable.AlgorithmName = "MGM_ASY";
	}

	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
		if (msgAlgorithm instanceof MsgValueAssignmnet) {
			computeLr = true;
		}
		if (msgAlgorithm instanceof MsgLR) {
			computeVA = true;
		} 
		
	}

	@Override
	public void changeRecieveFlagsToFalse() {
		computeLr = false;
		computeVA = false;
	}
}
