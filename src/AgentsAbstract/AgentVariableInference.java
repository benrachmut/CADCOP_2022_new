package AgentsAbstract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import Main.MainSimulator;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgReceive;
import Messages.MsgsMailerTimeComparator;

public abstract class AgentVariableInference extends AgentVariable {

	protected SortedMap<NodeId, MsgReceive<double[]>> functionMsgs;
	protected Map<NodeId, AgentFunction> functionNodes;
	protected boolean flagOfInferenceForKey;

	public AgentVariableInference(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		this.functionMsgs = new TreeMap<NodeId, MsgReceive<double[]>>();
		this.functionNodes = new TreeMap<NodeId, AgentFunction>();
		flagOfInferenceForKey = false;
		this.nodeId = new NodeId(id1,true);

	}
	public void meetNeighbor(int neighborId, Integer[][] constraint) {
		this.neighborsConstraint.put(new NodeId(neighborId,true), constraint);
	}
	
	public int getFunctionMsgsSize() {

		return functionMsgs.size();

	}
	/*
	public Integer[][] getMatrixWithAgent(int i) {
		if (this.neighborsConstraint.containsKey(new NodeId(i))) {
			return this.neighborsConstraint.get(new NodeId(i));
		}
		return null;
	}
	*/
	@Override
	public void resetAgentGivenParametersV2() {
		this.functionMsgs = Agent.resetMapToValueNull(this.functionMsgs);
		resetAgentGivenParametersV3();
		flagOfInferenceForKey = false;
	}

	protected abstract void resetAgentGivenParametersV3();

	@Override
	public NodeId getNodeId() {
		// TODO Auto-generated method stub
		return this.nodeId;
	}

	// To add with Ben.
	public boolean checkIfNodeIsContained(NodeId nodeId) {

		if (functionMsgs.containsKey(nodeId)) {

			return true;

		}

		else {

			return false;

		}

	}

	///// ******* New methods ******* ////

	// OmerP - New meetFunction method.
	public void meetFunction(List<NodeId> nodes) {

		for (int i = 0; i < nodes.size(); i++) {

			this.functionMsgs.put(nodes.get(i), null);

		}

	}

	// OmerP - New meetFunction method.
	public void meetFunction(NodeId nodeId) {

		functionMsgs.put(nodeId, null);

	}

	// OmerP - Will return the all the nodes.
	public SortedMap<NodeId, MsgReceive<double[]>> getMyFunctionMessage() {

		return this.functionMsgs;

	}

	public void holdTheFunctionNode(AgentFunction af) {
		this.functionNodes.put(af.nodeId, af);
		af.informFunctionNodeAboutItsVariableNode(this);

	}

	public int getFunctionNodesSize() {
		// TODO Auto-generated method stub
		return this.functionNodes.size();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "A_" + this.nodeId;
	}

	// -----------------------------------------------------------------------------------------------------------//


	protected void updateAgentTime(List<? extends Msg> messages) {
		updateAgentTimeForInference(messages);
	}
	
	public long getIdleTime() {
		return this.timeObject.getIdleTime();
	}
	@Override
	public boolean reactionToAlgorithmicMsgs() {
		if (MainSimulator.isThreadDebug) {
			System.out.println(this + " react to msgs");
		}
		this.atomicActionCounter = 0;

		if (MainSimulator.isThreadDebug && this.id == 1 && this.time == 99) {
			System.out.println(this + " " + this.timeObject.getTimeOfObject());
		}
		if (getDidComputeInThisIteration()) {

			boolean isUpdate = compute();
			if (isMsgGoingToBeSent(isUpdate)) {
				if (MainSimulator.isMaxSumThreadDebug) {
					System.out.println(this + "time is " +  this.timeObject.getTimeOfObject() + " BEFORE because computation");
				}
				computationCounter = computationCounter + 1;
				this.timeStampCounter = this.timeStampCounter + 1;
				if (MainSimulator.isAtomicTime) {
					synchronized (timeObject) {
						long currentTime = this.timeObject.getTimeOfObject();
						long updatedTime = currentTime + this.atomicActionCounter;
						this.timeObject.setTimeOfObject(updatedTime);
					}
				
					this.atomicActionCounter = 0;

				} else {
					synchronized (timeObject) {
						long currentTime = this.timeObject.getTimeOfObject();
						long updatedTime = currentTime + 1;
						this.timeObject.setTimeOfObject(updatedTime);
					}
				}
				
				if (MainSimulator.isMaxSumThreadDebug) {
					System.out.println(this + "time is " +  this.timeObject.getTimeOfObject() + " After because computation");
				}
				if (MainSimulator.isThreadDebug) {
					System.out.println(this + " notify mailer");
				}
				this.sendMsgs();
				this.changeRecieveFlagsToFalse();
			}
			return isUpdate;

		}
		return false;
	}
	
	
	


	
	
	
	
	
	
	

}
