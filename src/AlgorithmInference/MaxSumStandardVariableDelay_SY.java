package AlgorithmInference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import AgentsAbstract.AgentVariable;
import Messages.MsgAlgorithm;
import Messages.MsgReceive;

public class MaxSumStandardVariableDelay_SY extends MaxSumStandardVariableDelay{
	
	private Collection<MsgAlgorithm> future;

	///// ******* Constructor and initialize Methods******* ////
	
	public MaxSumStandardVariableDelay_SY(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		this.future = new ArrayList<MsgAlgorithm>();

	}
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Initialize Methods ******* ////

	// OmerP - Will send new messages for each one of the neighbors upon the
	@Override
	public void initialize() {

		initializeNeighborsMemory();
		produceEmptyMessage();
		sendMsgs();

	}
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Send Message methods ******* ////

	@Override
	public void sendMsgs() {
		super.sendMsgs();
		releaseFutureMsgs();

	}
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Receive Message Methods ******* ////

	@Override
	public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {

		if (this.timeStampCounter == msgAlgorithm.getTimeStamp()) {
			super.updateMessageInContext(msgAlgorithm);
		} else {
			this.future.add(msgAlgorithm);
		}
		return true;
	}
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Handle Flags Methods ******* ////
	
	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
		
		for (MsgReceive<double[]> m : this.functionMsgs.values()) {
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

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Compute ******* ////

	// OmerP - new information has arrived and the variable node will update its
	// value assignment.
	@Override
	public boolean compute() {

		chooseValueLongAssignmentForAsyncVersion();
		produceNewMessageForAsyncVersion();
		this.timeStampToLook++;
		return true;
	}
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Reset Agent Methods ******* ////

	public void resetAgentGivenParametersV5() {
		this.future = new ArrayList<MsgAlgorithm>();
	}
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Documentation Methods ******* ////

	
}
