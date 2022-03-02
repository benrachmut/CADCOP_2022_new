package AlgorithmSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;


import AgentsAbstract.AgentVariable;
import AgentsAbstract.NodeId;
import Messages.MsgAlgorithm;
import Messages.MsgLR;
import Messages.MsgReceive;
import Messages.MsgValueAssignmnet;

public class MGM_SY extends MGM {
	private Collection<MsgAlgorithm> future;
	// private SortedMap<NodeId, Boolean> isFromNeighborLr;
	// private SortedMap<NodeId, Boolean> isFromNeighborValueAssignmnent;
	// private int currentIteration;
	// private Collection<MsgLR> futureMsgs;
	private boolean currentPhaseWaitForVA;

	public MGM_SY(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		this.isWithTimeStamp = false;
		resetAgentGivenParametersV4();
		updateAlgorithmName();
	}

	@Override
	protected void resetAgentGivenParametersV4() {
		this.isWithTimeStamp = false;
		currentPhaseWaitForVA = true;
		this.future = new ArrayList<MsgAlgorithm>();
		// this.currentIteration = 0;
		// futureMsgs = new ArrayList<MsgLR>();
	}
	/*
	 * private void resetFromNeighborValueAssignmnent() {
	 * this.isFromNeighborValueAssignmnent = new TreeMap<NodeId, Boolean>(); for
	 * (NodeId nodeId : this.neighborsConstraint.keySet()) {
	 * this.isFromNeighborValueAssignmnent.put(nodeId, false); }
	 * 
	 * }
	 * 
	 * private void resetFromNeighborLr() { this.isFromNeighborLr = new
	 * TreeMap<NodeId, Boolean>(); for (NodeId nodeId :
	 * this.neighborsConstraint.keySet()) { this.isFromNeighborLr.put(nodeId,
	 * false); } }
	 */

	@Override
	public void updateAlgorithmName() {
		AgentVariable.AlgorithmName = "MGM_SY";

	}

	@Override
	protected boolean updateMessageInContext(MsgAlgorithm m) {
		if (this.timeStampCounter == m.getTimeStamp()) {
			super.updateMessageInContext(m);
		} else {
			this.future.add(m);
		}
		return true;
		/*
		 * boolean shouldUpdateInContext1 = (m instanceof MsgLR &&
		 * this.currentPhaseWaitForVA == false); boolean shouldUpdateInContext2 = (m
		 * instanceof MsgValueAssignmnet && this.currentPhaseWaitForVA == true); boolean
		 * shouldUpdateInContext = shouldUpdateInContext1 || shouldUpdateInContext2;
		 * 
		 * if (shouldUpdateInContext) { super.updateMessageInContext(m); }else {
		 * MsgAlgorithm copiedM = null; if (m instanceof MsgLR) { copiedM = new
		 * MsgLR(m); }else { copiedM = new MsgValueAssignmnet(m);
		 * 
		 * } mailer.sendMsgWitoutDelay(copiedM); m.setContext(null); }
		 */

	}

	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
		if (currentPhaseWaitForVA) {
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
			computeLr = true;
		} else {
			for (MsgReceive<Integer> m : this.neighborsLR.values()) {
				int msgTimestamp = 1;
				if (m == null) {
					return;
				} else {
					msgTimestamp = m.getTimestamp();
				}
				if (msgTimestamp != this.timeStampCounter) {
					return;
				}
			}
			computeVA = true;
		}

		/*
		 * if (msgAlgorithm.getContext() != null) { NodeId sender =
		 * msgAlgorithm.getSenderId(); int msgTimestamp = msgAlgorithm.getTimeStamp();
		 * 
		 * if (msgAlgorithm instanceof MsgLR) { this.isFromNeighborLr.put(sender, true);
		 * checkIfCanComputeVA(); }
		 * 
		 * if (msgAlgorithm instanceof MsgValueAssignmnet) {
		 * this.isFromNeighborValueAssignmnent.put(sender, true); checkIfCanComputeLR();
		 * } }
		 * 
		 */

	}

	@Override
	public void changeRecieveFlagsToFalse() {
		if (this.computeLr) {
			this.computeLr = false;
			currentPhaseWaitForVA = false;
		}
		if (this.computeVA) {
			this.computeVA = false;
			currentPhaseWaitForVA = true;
		}

	}

	@Override
	public void sendMsgs() {

		super.sendMsgs();

		if (computeLr || computeVA) {
			releaseFutureMsgs();
		}
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
