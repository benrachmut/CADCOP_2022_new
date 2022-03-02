package AlgorithmSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import AgentsAbstract.AgentVariable;
import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.MsgAlgorithm;
import Messages.MsgReceive;

public class DSA_B_SY extends DSA_B {
	private Collection<MsgAlgorithm> future;

	public DSA_B_SY(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		resetAgentGivenParametersV4();
		updateAlgorithmName();

	}

	@Override
	protected void resetAgentGivenParametersV4() {
		// resetNeighborRecieveInThisIteration();
		this.isWithTimeStamp = false;
		this.future = new ArrayList<MsgAlgorithm>();
	}

	@Override
	public void updateAlgorithmName() {
		AgentVariable.AlgorithmName = "DSA_SY";
	}

	@Override
	protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {

		if (this.timeStampCounter == msgAlgorithm.getTimeStamp()) {
			super.updateMessageInContext(msgAlgorithm);
		} else {
			this.future.add(msgAlgorithm);
		}
		return true;
	}

	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

		for (MsgReceive<Integer> m : this.neighborsValueAssignmnet.values()) {
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
	public void changeRecieveFlagsToFalse() {
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

	public String getStringForDebug() {
		String ans = "";
		ans = ans + this.rndForDebug + ",";
		ans = ans + this.valueAssignment + ",";

		for (Entry<NodeId, MsgReceive<Integer>> e : this.neighborsValueAssignmnet.entrySet()) {
			int context;
			int timeStamp;
			MsgReceive<Integer> mva = e.getValue();
			if (mva == null) {
				context = -1;
				timeStamp = -1;
			} else {
				Object context1 = e.getValue().getContext();
				context = (int) context1;
				timeStamp = e.getValue().getTimestamp();
			}
			ans = ans + context + "," + timeStamp + ",";
		}
		return ans;
	}

}
