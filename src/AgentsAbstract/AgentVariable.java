package AgentsAbstract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import Main.MainSimulator;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgAnyTime;
import Messages.MsgAnyTimeDown;
import Messages.MsgAnyTimeUp;
import Messages.MsgReceive;

public abstract class AgentVariable extends Agent {

	public static String AlgorithmName;

	public static String algorithmHeader;
	public static String algorithmData;

	protected int valueAssignment;
	private Double valueAssignmentChangeCounter;
	protected int firstRandomVariable;
	protected TreeMap<NodeId, Integer[][]> neighborsConstraint; // id and matrix of constraints
	protected int[] domainArray;

	protected Location location;
	//x_coordinates;
	//protected double y_coordinates;

	// ----------**Formations**----------
	// -----*DFS*-----
	protected NodeId dfsFather;
	protected Set<NodeId> dfsSons;



	public AgentVariable(int dcopId, int D, int id1) {
		super(dcopId, D);
		
		neighborsConstraint = new TreeMap<NodeId, Integer[][]>();
		this.id = id1;
		this.domainArray = new int[domainSize];
		createDomainArray();
		Random r = new Random((132 * (id1+1)) + 100 * (dcopId+1));
		firstRandomVariable = r.nextInt(D);
		// resetAgent();
		valueAssignmentChangeCounter = 0.0;
		this.dfsSons = new HashSet<NodeId>();

	}
	
	

	@Override
	public String toString() {
		return "A_" + this.id;
	}

	@Override
	public void resetAgentGivenParameters() {
		valueAssignment = firstRandomVariable;
		valueAssignmentChangeCounter = 0.0;


		resetAgentGivenParametersV2();
	}

	/**
	 * update the Algorithm Header string
	 */
	public abstract void updateAlgorithmHeader();

	/**
	 * update the Algorithm Data string
	 */
	public abstract void updateAlgorithmData();

	/**
	 * update the Algorithm Name string
	 */
	public abstract void updateAlgorithmName();

	protected boolean setValueAssignmnet(int input) {
		if (this.valueAssignment != input) {
			this.valueAssignmentChangeCounter++;
			this.valueAssignment = input;
			return true;
		}
		return false;
	}

	public int getValueAssignment() {
		return this.valueAssignment;
	}

	private void createDomainArray() {
		for (int domainValue = 0; domainValue < domainSize; domainValue++) {
			domainArray[domainValue] = domainValue;
		}
	}

	public void meetNeighbor(int neighborId, Integer[][] constraint) {
		this.neighborsConstraint.put(new NodeId(neighborId,false), constraint);
	}

	public int neighborSize() {
		return this.neighborsConstraint.size();
	}

	protected abstract void resetAgentGivenParametersV2();

	

	

	/*
	 * public synchronized void reactionToAlgorithmicMsgs() {
	 * 
	 * if (getDidComputeInThisIteration()) {
	 * 
	 * boolean isUpdate = compute(); if (isMsgGoingToBeSent(isUpdate)) {
	 * 
	 * computationCounter = computationCounter + 1; this.timeStampCounter =
	 * this.timeStampCounter + 1; this.time = this.time + 1;
	 * 
	 * if (MainSimulator.isWhatAgentDebug && this.id ==1) {
	 * System.out.println("before send msgs"); }
	 * 
	 * sendMsgs();
	 * 
	 * if (MainSimulator.isWhatAgentDebug && this.id ==1) {
	 * System.out.println("changing to false"); } changeRecieveFlagsToFalse(); } } }
	 */

	

	/*
	 * @Override public void run() { initialize(); while (terminationCondition()) {
	 * synchronized (this) { while (msgBoxAlgorithmic.isEmpty() &&
	 * msgBoxAnytime.isEmpty()) { try { wait(); } catch (InterruptedException e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); } } if
	 * (msgBoxAlgorithmic.isEmpty() == false) { reactionToAlgorithmicMsgs(); } if
	 * (msgBoxAnytime.isEmpty() == false) { reactionToAnytimeMsgs(); }
	 * 
	 * } // synch }
	 * 
	 * }
	 * 
	 */



	// ------------- **TREE METHODS**-------------

	// ------------- **GENERAL USE**-------------

	public Set<NodeId> getNeigborSetId() {
		return this.neighborsConstraint.keySet();
	}

	public Double getChangeValueAssignmentCounter() {
		// TODO Auto-generated method stub
		return this.computationCounter;
	}
/*
	public Integer[][] getMatrixWithAgent(int i) {
		if (this.neighborsConstraint.containsKey(new NodeId(i))) {
			return this.neighborsConstraint.get(new NodeId(i));
		}
		return null;
	}
*/
	public int getTimestamp() {
		return this.timeStampCounter;
	}

	public void setDfsFather(NodeId input) {
		this.dfsFather = input;

	}

	public void addDfsSon(NodeId input) {
		this.dfsSons.add(input);
	}

	public NodeId getDfsFather() {
		// TODO Auto-generated method stub
		return this.dfsFather;
	}

	public double getIfColor() {
		// TODO Auto-generated method stub
		return 1.0;
	}

	public Integer getColorNumber() {
		return 1;
	}
/*
	public Location getLocation() {
		return this.x_coordinates;
	}
*/
	public double getQuadraticDistanceTo(Location otherLocation) {
		return this.location.getQuadraticDistanceTo(otherLocation);
	}
	public Location getLocation() {
		// TODO Auto-generated method stub
		return this.location;
	}
	@Override
	public int hashCode() {
		return this.id;
	}

	public void setLocation(Location myLocation) {
		this.location = myLocation; 
		
	}



	public void setLocationCloseToCity(Location mayerLocation, int dcopId, double sdSquareFromCity) {
		this.location = new LocationRandomNormal(dcopId, this.id, mayerLocation, sdSquareFromCity);
		
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return ((AgentVariable)obj).getId()==this.id;
	}

	

}
