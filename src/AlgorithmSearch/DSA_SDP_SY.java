package AlgorithmSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import AgentsAbstract.AgentVariable;
import Messages.MsgAlgorithm;
import Messages.MsgReceive;

public class DSA_SDP_SY extends DSA_SDP {

	private List<MsgAlgorithm> future;

	public DSA_SDP_SY(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		updateAlgorithmName();
		resetAgentGivenParametersV5();
	}
	
	@Override
	protected void resetAgentGivenParametersV5() {
		this.isWithTimeStamp = false;
		this.future = new ArrayList<MsgAlgorithm>();
	}

	@Override
	public void updateAlgorithmName() {
		AgentVariable.AlgorithmName = "DSA_SDP_SY";
	}

	@Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {

		
		if (this.timeStampCounter == msgAlgorithm.getTimeStamp()) {
			
			super.updateMessageInContext(msgAlgorithm);
		} else {
			
			this.future.add(msgAlgorithm);
		}
		return true;

	}

	@Override
	protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
		for (MsgReceive<Integer> m : this.neighborsValueAssignment.values()) {
			int msgTimestamp = 0;
			if (m == null) {
				return;
			} else {
				msgTimestamp = m.getTimestamp();
			}
			if (msgTimestamp != this.timeStampCounter) {
				return;
			}
		}
		canCompute = true;
	}

	@Override
	public void changeReceiveFlagsToFalse() {
			canCompute = false;
		
	}

	@Override
	public void sendMsgs() {
			sendValueAssignmnetMsgs();
			releaseFutureMsgs();	
	}
	private void releaseFutureMsgs() {
		Collection<MsgAlgorithm> toRelease = new HashSet<MsgAlgorithm>();
		for (MsgAlgorithm m : this.future) {
			if (m.getTimeStamp() == this.timeStampCounter) {
				toRelease.add(m);
				updateMessageInContext(m);

			}
		}
		this.future.removeAll(toRelease);
	}
}
