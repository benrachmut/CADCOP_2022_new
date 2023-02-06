package Problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentFunction;
import AgentsAbstract.AgentVariable;

import AgentsAbstract.AgentVariableInference;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;

import AlgorithmInference.*;
//import AlgorithmInference.MaxSumSplitConstraintFactorGraphSync;
//import AlgorithmInference.MaxSumStandardFunctionSync;
//import AlgorithmInference.MaxSumStandardVaribleSync;

import AlgorithmSearch.*;

import Formation.ColorFormation;
import Formation.DFS;
import Formation.Formation;
import Main.Mailer;
import Main.MainSimulator;
import Main.MainSimulatorIterations;
import Main.UnboundedBuffer;
import Messages.Msg;

public abstract class Dcop {

	// ------- ** for graph use **------

	protected AgentVariable[] agentsVariables;
	protected List<Neighbor> neighbors;
	protected Neighbor[][] neighborsMatrix;
	protected int D;
	public List<Thread> agentsThreads;

	// ------- ** for factor graph use **------
	protected List<AgentFunction> agentFunctions;
	protected List<Agent> agentsAll;

	protected int dcopId;

	public static String dcopName;
	public static String dcopHeader;
	public static String dcopParameters;
//	protected List<AgentFunction> functionNodes;

	public Dcop(int dcopId, int A, int D) {

		this.D = D;
		this.agentFunctions = new ArrayList<AgentFunction>();
		this.dcopId = dcopId;
		agentsVariables = new AgentVariable[A];
		this.agentsAll = new ArrayList<Agent>();

		createVariableAgents();
		neighbors = new ArrayList<Neighbor>();
		neighborsMatrix = new Neighbor[A][A];
		updateNames();

		/*
		 * if(MainSimulator.isLocationDebug) { Location cityLocation =
		 * randomlySelectAgentCity(agentId); a.updateLocationGivenCity(cityLocation);
		 * printCityLoctions(); printAgentsLocation(); }
		 */
	}



	protected void updateNames() {
		setDcopName();
		setDcopHeader();
		setDcopParameters();
	}

	protected abstract void setDcopName();

	protected abstract void setDcopHeader();

	protected abstract void setDcopParameters();

	public void dcopMeetsMailer(Mailer mailer) {
		UnboundedBuffer<Msg> msgsFromAgentsToMailer = new UnboundedBuffer<Msg>();
		mailer.setInbox(msgsFromAgentsToMailer);
		for (Agent a : agentsAll) {
			UnboundedBuffer<Msg> msgsFromMailerToSpecificAgent = new UnboundedBuffer<Msg>();
			mailer.meetAgent(msgsFromMailerToSpecificAgent, a.getNodeId());
			a.meetMailer(msgsFromMailerToSpecificAgent, msgsFromAgentsToMailer, mailer);
			// > msgsFromMeToMailer, UnboundedBuffer<Msg> msgsFromMailerToMe
		}
	}

	public int computeGlobalCost() {
		int ans = 0;
		for (Neighbor n : neighbors) {
			ans += n.getCurrentCost();
		}
		return ans;
	}

	public abstract void createNeighbors();

	private void createVariableAgents() {
		for (int agentId = 0; agentId < agentsVariables.length; agentId++) {
			agentsVariables[agentId] = createAgentInstance(agentId);
			AgentVariable a = agentsVariables[agentId];

			this.agentsAll.add(a);
		}

	}
/*
	private Location randomlySelectAgentCity(int agentId) {

		List<Integer> keysAsArray = new ArrayList<Integer>(this.citiesLocation.keySet());
		Random r = new Random((agentId+1) * 178 + (this.dcopId+1) * 817);
		r.nextInt();
		Location cityLocation = citesLocation.get(keysAsArray.get(r.nextInt(keysAsArray.size())));
		return cityLocation;
	}
*/
	/*
	public double[][] getAgentsQuadraticDistance() {
		double[][] ans = new double[agentsQuadraticDistance.length][agentsQuadraticDistance[0].length];
		for (int i = 0; i < ans.length; i++) {
			for (int j = 0; j < ans[i].length; j++) {
				ans[i][j] = new Double(agentsQuadraticDistance[i][j]);
			}
		}
		return ans;
	}
*/
	private AgentVariable createAgentInstance(int agentId) {
		AgentVariable ans = null;
		int agentType = MainSimulator.agentType;

		if (agentType == 1) {
			ans = new DSA_B_ASY(dcopId, D, agentId);
		}
		if (agentType == 2) {
			ans = new DSA_B_SY(dcopId, D, agentId);
		}
		if (agentType == 3) {
			ans = new MGM_ASY(dcopId, D, agentId);
		}
		if (agentType == 4) {
			ans = new MGM_SY(dcopId, D, agentId);
		}

		// if (agentType == 5) {
		// ans = new AMDLS_V1(dcopId, D, agentId);
		// }

		if (agentType == 7) {
			ans = new AMDLS_V3(dcopId, D, agentId);
		}

		if (agentType == 8) {
			ans = new DSA_SDP_ASY(dcopId, D, agentId);
		}
		if (agentType == 9) {
			ans = new DSA_SDP_SY(dcopId, D, agentId);
		}
		if (agentType == 10) {
			ans = new MGM2_ASY(dcopId, D, agentId);
		}
		if (agentType == 11) {
			ans = new MGM2_SY(dcopId, D, agentId);
		}
		if (agentType == 12) {
			ans = new CAMDLS_NAIVE(dcopId, D, agentId);
		}
		if (agentType == 13) {
			ans = new CAMDLS_V2(dcopId, D, agentId);
		}

		if (agentType == 14) {
			ans = new MonotonicStochasticOrderSearch(dcopId, D, agentId);
		}


		if (agentType == 15){
			ans = new MonotonicStochastic2CoordinationV3(dcopId, D, agentId);
		}

		if(agentType == 16){
			ans = new MonotonicStochastic2CoordinationV3(dcopId, D, agentId);
		}

		if (agentType == 101) {
			ans = new MaxSumStandardVariableDelay_SY(dcopId, D, agentId); // Sync version without memory.
		}

		if (agentType == 102) {

			ans = new MaxSumStandardVariableDelay_SY(dcopId, D, agentId); // Sync Split version without memory.

		}

		if (agentType == 103) { // To add.

			ans = new MaxSumStandardVariableDelay(dcopId, D, agentId); // Sync and Async with memory.

		}

		if (agentType == 104) { // To add.

			ans = new MaxSumStandardVariableDelay(dcopId, D, agentId); // Sync Split version without memory.


		}

		//------------------------
		if (MainSimulatorIterations.amIRunning){
			if (MainSimulatorIterations.algorithm == MainSimulatorIterations.Algorithm.maxsum){
				ans = new MaxSumVariableBen(dcopId, D, agentId+1);
			}
		}

		return ans;
	}

	private void createFormations() {

		Formation[] formations = { new ColorFormation(agentsVariables), new DFS(agentsVariables) };
		for (Formation f : formations) {
			f.execute();
		}

		handleFormationForAMDLS(formations);
		handleFormationForSearchAnytime(formations);

		if (MainSimulator.isAnytime && MainSimulator.isAnytimeDebug) {
			formationPrinting();
			System.out.println();
		}
	}

	private void formationPrinting() {
		System.out.println("--------Agents neighbors-------");

		for (AgentVariable a : this.agentsVariables) {
			System.out.println("**" + a + "**");
			System.out.println(a.getNeigborSetId());

		}

		System.out.println("--------Agents Anytime Formation-------");
		for (AgentVariable a : this.agentsVariables) {
			AgentVariableSearch as = (AgentVariableSearch) a;
			System.out.println("**" + as + "**");
			if (as.getAnytimeFather() == null) {
				System.out.println("father: is TOP");
			} else {
				System.out.println("father: " + as.getAnytimeFather());
			}
			System.out.println("sons: " + as.getAnytimeSons());
		}

		System.out.println("--------Agents Anytime Formation Below-------");
		for (AgentVariable a : this.agentsVariables) {
			AgentVariableSearch as = (AgentVariableSearch) a;
			System.out.println("**" + as + "**");
			System.out.println("below agents:" + as.getBelowAnytime());
		}
	}

	private void handleFormationForSearchAnytime(Formation[] formations) {
		if (MainSimulator.isAnytime && this.isSearchAlgorithm()) {

			for (AgentVariable a : agentsVariables) {
				Set<NodeId> above = new HashSet<NodeId>();
				Set<NodeId> below = new HashSet<NodeId>();
				if (MainSimulator.anytimeFormation == 1) {
					formations[1].setAboveBelow(a, above, below);
					((AgentVariableSearch) a).turnDFStoAnytimeStructure(below);
				}
				if (MainSimulator.anytimeFormation == 3) {
					doNaiveFormation();

				}
			}
		}
	}

	private void doNaiveFormation() {
		ArrayList<AgentVariableSearch> agentsSearch = new ArrayList<AgentVariableSearch>();
		for (AgentVariable ttt : agentsVariables) {
			if (ttt.getNeigborSetId().size() != 0) {
				if (ttt instanceof AgentVariableSearch) {
					agentsSearch.add((AgentVariableSearch) ttt);
				}
			}
		}

		AgentVariableSearch[] aArray = new AgentVariableSearch[agentsSearch.size()];
		int counter = 0;
		for (AgentVariableSearch ttt : agentsSearch) {
			aArray[counter] = ttt;
			counter = counter + 1;
		}

		for (int i = 0; i < aArray.length; i++) {
			boolean flag = false;
			Set<NodeId> sons = new HashSet<NodeId>();
			AgentVariableSearch ttt = aArray[i];
			if (i == 0) {
				flag = true;
				sons.add(aArray[i + 1].getNodeId());
				ttt.setAnytimeSons(sons);
				ttt.setAnytimeBelow(getBelowAgents(i, aArray));
			}
			if (i == aArray.length - 1) {
				flag = true;

				ttt.setAnytimeFather(aArray[i - 1].getNodeId());
				ttt.setAnytimeBelow(new HashSet<NodeId>());
				ttt.setAnytimeBelow(sons);
				ttt.setAnytimeSons(sons);

			}

			if (!flag) {
				sons.add(aArray[i + 1].getNodeId());
				ttt.setAnytimeSons(sons);
				ttt.setAnytimeFather(aArray[i - 1].getNodeId());
				ttt.setAnytimeBelow(getBelowAgents(i, aArray));
			}
		}
	}

	private Set<NodeId> getBelowAgents(int i, AgentVariableSearch[] aArray) {
		Set<NodeId> ans = new HashSet<NodeId>();
		for (int j = 0; j < aArray.length; j++) {
			if (j > i) {
				ans.add(aArray[j].getNodeId());
			}
		}
		return ans;
	}

	private void handleFormationForAMDLS(Formation[] formations) {

		if (MainSimulator.agentType == 5) {
			if (AMDLS_V1.structureColor) {
				formations[0].setAboveBelow();
			} else {
				formations[1].setAboveBelow();
			}

		}

	}

	public Dcop initiate() {
		createNeighbors();
		//calculateQuadarticDistance();
		createFormations();

		if (isInferenceAgent()) {
			createFactorGraphCombined();
		}
		return this;
	}



	private void createFactorGraphCombined() {

		int agentType = MainSimulator.agentType;
		//Collections.shuffle(this.neighbors, new Random(this.dcopId));
		for (Neighbor n : neighbors) {

			AgentVariableInference av1 = (AgentVariableInference) n.getA1();
			AgentVariableInference av2 = (AgentVariableInference) n.getA2();

			Integer[][] constraints = n.getConstraints();

			AgentFunction af = null;

			if (MainSimulatorIterations.amIRunning) {
				if (MainSimulatorIterations.algorithm == MainSimulatorIterations.Algorithm.maxsum) {
					af = new MaxSumFunctionBen(dcopId, D, av1.getId(), av2.getId(), constraints);

					av1.meetFunctionV2(af.getNodeId());
					av2.meetFunctionV2(af.getNodeId());

					this.agentFunctions.add(af);
					this.agentsAll.add(af);
				}

			} else {

				if (agentType == 101) {
					af = new MaxSumStandardFunctionDelay_SY(dcopId, D, av1.getId(), av2.getId(), constraints);
					this.agentFunctions.add(af);
					this.agentsAll.add(af);
					av1.meetFunction(af.getMyNodes());
					av2.meetFunction(af.getMyNodes());
					af.meetVariables(av1.getNodeId(), av2.getNodeId());

					AgentVariableInference agentVariableThatWillHoldFunction = whichAgentShouldHoldFunctionNode(av1, av2);
					agentVariableThatWillHoldFunction.holdTheFunctionNode(af);

				}

				if (agentType == 102) {

					af = new MaxSumSplitConstraintFactorGraphDelay_SY(dcopId, D, av1.getId(), av2.getId(),
							constraints); // Will create a new MaxSumSplitConstraintFactorGraphSync
					MaxSumSplitConstraintFactorGraphDelay_SY splitConstraintAgent = (MaxSumSplitConstraintFactorGraphDelay_SY) af;

					List<MaxSumStandardFunctionDelay_SY> splitList = splitConstraintAgent.getSplitFunctionNodes();

					for (int i = 0; i < splitConstraintAgent.getSplitFunctionNodes().size(); i++) {
						this.agentFunctions.add(splitList.get(i));
						this.agentsAll.add(splitList.get(i));
					}

					av1.meetFunction(af.getMyNodes());
					av2.meetFunction(af.getMyNodes());
					af.meetVariables(av1.getNodeId(), av2.getNodeId());

					MaxSumStandardFunction af1 = splitConstraintAgent.getFirstSplit();
					av1.holdTheFunctionNode(af1);
					// af1.variableNodeThatHoldsMe(av1);

					MaxSumStandardFunction af2 = splitConstraintAgent.getSecondSplit();
					av2.holdTheFunctionNode(af2);
					// af2.variableNodeThatHoldsMe(av2);

				}

				if (agentType == 103) {
					af = new MaxSumStandardFunctionDelay(dcopId, D, av1.getId(), av2.getId(), constraints);
					this.agentFunctions.add(af);
					this.agentsAll.add(af);
					av1.meetFunction(af.getMyNodes());
					av2.meetFunction(af.getMyNodes());
					af.meetVariables(av1.getNodeId(), av2.getNodeId());

					AgentVariableInference agentVariableThatWillHoldFunction = whichAgentShouldHoldFunctionNode(av1, av2);

					agentVariableThatWillHoldFunction.holdTheFunctionNode(af);
					// af.variableNodeThatHoldsMe(agentVariableThatWillHoldFunction);
				}

				if (agentType == 104) {

					af = new MaxSumSplitConstraintFactorGraphDelay(dcopId, D, av1.getId(), av2.getId(),
							constraints); // Will create a new MaxSumSplitConstraintFactorGraphSync
					MaxSumSplitConstraintFactorGraphDelay splitConstraintAgent = (MaxSumSplitConstraintFactorGraphDelay) af;

					List<MaxSumStandardFunctionDelay> splitList = splitConstraintAgent.getSplitFunctionNodes();

					for (int i = 0; i < splitConstraintAgent.getSplitFunctionNodes().size(); i++) {
						this.agentFunctions.add(splitList.get(i));
						this.agentsAll.add(splitList.get(i));
					}

					av1.meetFunction(af.getMyNodes());
					av2.meetFunction(af.getMyNodes());
					af.meetVariables(av1.getNodeId(), av2.getNodeId());

					MaxSumStandardFunction af1 = splitConstraintAgent.getFirstSplit();
					av1.holdTheFunctionNode(af1);
					// af1.variableNodeThatHoldsMe(av1);

					MaxSumStandardFunction af2 = splitConstraintAgent.getSecondSplit();
					av2.holdTheFunctionNode(af2);
					// af2.variableNodeThatHoldsMe(av2);

				}

			}
		}

		if (MainSimulator.isFactorGraphDebug) {
			pringAgentAll();
		}

	}

	private AgentVariableInference whichAgentShouldHoldFunctionNode(AgentVariableInference av1,
			AgentVariableInference av2) {

		int av1Functions = av1.getFunctionNodesSize(); // Get the size of function in av1.
		int av2Functions = av2.getFunctionNodesSize(); // Get the size of function in av2.

		if (av1Functions < av2Functions) { // If the number of function nodes of av1 is smaller than av2 -> will add to
											// av1.
			return av1;
		}
		if (av1Functions > av2Functions) { // If the number of function nodes of av2 is smaller than av1 -> will add to
											// // av2.
			return av2;
		}

		else { // If equal will determine based on the id number.

			int neigborsOfav1 = av1.neighborSize();
			int neigborsOfav2 = av2.neighborSize();

			if (neigborsOfav1 < neigborsOfav2) {
				return av1;
			}

			if (neigborsOfav1 > neigborsOfav2) {
				return av2;
			}

			else {

				if (av1.getNodeId().getId1() < av2.getNodeId().getId1()) { // Will compare the id of each agent.
					return av1;

				}

				else {
					return av2;

				}
			}
		}

	}

	private void createFactorGraph() {

		int agentType = MainSimulator.agentType;

		for (Neighbor n : neighbors) {

			AgentVariableInference av1 = (AgentVariableInference) n.getA1();
			AgentVariableInference av2 = (AgentVariableInference) n.getA2();

			Integer[][] constraints = n.getConstraints();

			AgentFunction af = null;

			if (agentType == 100 || agentType == 101 || agentType == 103) {

				if (agentType == 100) {

					af = new MaxSumStandardFunction(dcopId, D, av1.getId(), av2.getId(), constraints);

				}
/*
				if (agentType == 101) {

					af = new MaxSumStandardFunctionSync(dcopId, D, av1.getId(), av2.getId(), constraints);

				}
*/


				if (agentType == 103) {

					af = new MaxSumStandardFunctionDelay(dcopId, D, av1.getId(), av2.getId(), constraints);

				}

				this.agentFunctions.add(af);
				this.agentsAll.add(af);
				av1.meetFunction(af.getMyNodes());
				av2.meetFunction(af.getMyNodes());
				af.meetVariables(av1.getNodeId(), av2.getNodeId());

			}

			if (agentType == 102 || agentType == 104) {

				int avSplitOne = av1.getId() + 1;
				int avSplitTwo = av2.getId() + 1;
/*
				if (agentType == 102) {

					af = new MaxSumSplitConstraintFactorGraphSync(dcopId, D, av1.getId(), av2.getId(), constraints); // Will
																														// create
																														// a
																														// new
																														// MaxSumSplitConstraintFactorGraphSync
					MaxSumSplitConstraintFactorGraphSync splitConstraintAgent = (MaxSumSplitConstraintFactorGraphSync) af; // Casting
																															// af
																															// as
																															// MaxSumSplitConstraintFactorGraphSync
					List<MaxSumStandardFunctionSync> splitList = splitConstraintAgent.getSplitFunctionNodes(); // Get
																												// the
																												// list
																												// of
																												// the
																												// function
																												// agents.
					for (int i = 0; i < splitConstraintAgent.getSplitFunctionNodes().size(); i++) { // Looping over the
																									// list and adds
																									// each function
																									// agent on the list
																									// to agentFunction
																									// and agentsAll.

						this.agentFunctions.add(splitList.get(i));
						this.agentsAll.add(splitList.get(i));

					}

				}
*/
				if (agentType == 104) {

					af = new MaxSumSplitConstraintFactorGraphDelay(dcopId, D, av1.getId() + 1, av2.getId() + 1,
							constraints); // Will create a new MaxSumSplitConstraintFactorGraphSync
					MaxSumSplitConstraintFactorGraphDelay splitConstraintAgent = (MaxSumSplitConstraintFactorGraphDelay) af; // Casting
																																// af
																																// as
																																// MaxSumSplitConstraintFactorGraphSync
					List<MaxSumStandardFunctionDelay> splitList = splitConstraintAgent.getSplitFunctionNodes(); // Get
																												// the
																												// list
																												// of
																												// the
																												// function
																												// agents.
					for (int i = 0; i < splitConstraintAgent.getSplitFunctionNodes().size(); i++) { // Looping over the
																									// list and adds
																									// each function
																									// agent on the list
																									// to agentFunction
																									// and agentsAll.

						this.agentFunctions.add(splitList.get(i));
						this.agentsAll.add(splitList.get(i));

					}

				}

				av1.meetFunction(af.getMyNodes());
				av2.meetFunction(af.getMyNodes());
				af.meetVariables(av1.getNodeId(), av2.getNodeId());

			}

		}

		if (MainSimulator.isFactorGraphDebug) {
			pringAgentAll();
		}

	}
	/*
	 * public List<Agent> getAgents() { return agentsAll; }
	 */

	public int getId() {
		return this.dcopId;
	}

	public List<Neighbor> getNeighbors() {
		return this.neighbors;
	}

	public AgentVariable[] getVariableAgents() {
		return agentsVariables;
	}

	public boolean isInferenceAgent() {
		return (this.agentsVariables[0] instanceof AgentVariableInference);
	}

	public boolean isSearchAlgorithm() {
		// TODO Auto-generated method stub
		return (this.agentsVariables[0] instanceof AgentVariableSearch);
	}

	///// ******* Debug methods ******* ////
	///// ******* Debug methods ******* ////

	// OmerP - To print all the neighbors list.
	public void printAllNeighbors() {

		for (int i = 0; i < neighbors.size(); i++) {

			System.out.println("AgentVariable:(" + neighbors.get(i).getA1().getNodeId().getId1() + ","
					+ neighbors.get(i).getA1().getNodeId().getId2() + ") is constraind with AgentVariable:("
					+ neighbors.get(i).getA2().getNodeId().getId1() + ","
					+ neighbors.get(i).getA2().getNodeId().getId2() + ").\n");

		}

	}

	// OmerP - Print when variable and function were connected.
	public void printVariablesandFunctionConstraints(AgentVariableInference av1, AgentVariableInference av2,
			AgentFunction af) {

		System.out.println("Agent Variable Inference (" + av1.getNodeId().getId1() + "," + av1.getNodeId().getId2()
				+ ") and Agent Variable Inference (" + av2.getNodeId().getId1() + "," + av2.getNodeId().getId2()
				+ ") are connected to Agent Function (" + af.getNodeId().getId1() + "," + af.getNodeId().getId2()
				+ ").\n");

	}

	// OmerP - This method aims to check if for all function node there are two
	// variable nodes at variable message size.
	public void binaryDebug() {

		boolean okMessage = true;

		for (int i = 0; i < agentFunctions.size(); i++) {

			if ((MainSimulator.agentType == 7) || (MainSimulator.agentType == 8)) {

				if (agentFunctions.get(i).getVariableMsgsSize() != 2) {

					System.out.println("Severe error !!! Agent Function (" + agentFunctions.get(i).getNodeId().getId1()
							+ "," + agentFunctions.get(i).getNodeId().getId2() + ") has "
							+ agentFunctions.get(i).getVariableMsgsSize() + " neighbors.\n");
					okMessage = false;

				}

			}
/*
			if (MainSimulator.agentType == 9) {

				MaxSumSplitConstraintFactorGraphSync maxSplitFunctionNode = (MaxSumSplitConstraintFactorGraphSync) agentFunctions
						.get(i); // Casting the get access to getSplitFunctionNodes method.

				for (int j = 0; j < maxSplitFunctionNode.getSplitFunctionNodes().size(); j++) {

					if (maxSplitFunctionNode.getSplitFunctionNodes().get(j).getVariableMsgsSize() != 2) {

						System.out.println(
								"Severe error !!! Agent Function (" + agentFunctions.get(i).getNodeId().getId1() + ","
										+ agentFunctions.get(i).getNodeId().getId2() + ") has "
										+ agentFunctions.get(i).getVariableMsgsSize() + " neighbors.\n");
						okMessage = false;

					}

				}

			}
*/
		}

		if (okMessage) {

			System.out.println("Factor Graph Check: All Constraints Are Binary.\n");

		}

	}

	// OmerP - This method aims to check if for all variable nodes that they have
	// the number of function nodes as the number of neighbors.
	public void functionDebug() {

		boolean okMessage = true;
		int[] checkArray = new int[neighbors.size()];

		for (int i = 0; i < neighbors.size(); i++) { // OmerP - will initialize the checkArray.

			checkArray[neighbors.get(i).getA1().getNodeId().getId1()]++;
			checkArray[neighbors.get(i).getA2().getNodeId().getId1()]++;

		}

		for (int i = 0; i < checkArray.length; i++) {

			AgentVariableInference agentVariable = (AgentVariableInference) agentsVariables[i];
			int toCheck = agentVariable.getFunctionMsgsSize();

			if (checkArray[i] != toCheck) {

				System.out.println(
						"Severe error !!! The number of neighbors of variable agent " + i + " that was initializaed is"
								+ checkArray[i] + "while the number of initializaed agents are " + toCheck + ".\n");
				okMessage = false;

			}

		}

		if (okMessage) {

			System.out.println("Factor Graph Check: All Variable Agent Initializaed Correctly.");

		}

	}

	// OmerP - Will check the each variable node has the number of function nodes as
	// its neighbors.
	/*
	public void variableNodeDebug() {

		for (Neighbor n : neighbors) {

			AgentVariableInference av1 = (AgentVariableInference) n.getA1();
			AgentVariableInference av2 = (AgentVariableInference) n.getA2();

			if (MainSimulator.agentType == 7) {

				NodeId nodeToCheck = new NodeId(av1.getNodeId().getId1(), av2.getNodeId().getId1());

				if (av1.checkIfNodeIsContained(nodeToCheck)) {

					printNodeConnection(nodeToCheck, av1, true);

				} else {

					printNodeConnection(nodeToCheck, av1, false);

				}

				if (av2.checkIfNodeIsContained(nodeToCheck)) {

					printNodeConnection(nodeToCheck, av2, true);

				} else {

					printNodeConnection(nodeToCheck, av2, false);
				}

			}

			if (MainSimulator.agentType == 8) {

				NodeId nodeToCheckOne = new NodeId(av1.getNodeId().getId1(), av2.getNodeId().getId1());
				NodeId nodeToCheckTwo = new NodeId(av2.getNodeId().getId1(), av1.getNodeId().getId1());

				if (av1.checkIfNodeIsContained(nodeToCheckOne)) {

					printNodeConnection(nodeToCheckOne, av1, true);

				} else {

					printNodeConnection(nodeToCheckOne, av1, false);

				}

				if (av1.checkIfNodeIsContained(nodeToCheckTwo)) {

					printNodeConnection(nodeToCheckTwo, av1, true);

				} else {

					printNodeConnection(nodeToCheckTwo, av1, false);

				}

				if (av2.checkIfNodeIsContained(nodeToCheckOne)) {

					printNodeConnection(nodeToCheckOne, av2, true);

				} else {

					printNodeConnection(nodeToCheckOne, av2, false);

				}

				if (av2.checkIfNodeIsContained(nodeToCheckTwo)) {

					printNodeConnection(nodeToCheckTwo, av2, true);

				} else {

					printNodeConnection(nodeToCheckTwo, av2, false);

				}

			}

		}

	}
*/
	// OmerP - Will check the each function node has the number of variable nodes as
	// its neighbors.
	/*

	public void functionNodeDebug() {

		for (int i = 0; i < agentFunctions.size(); i++) {

			NodeId nodeToCheckOne = new NodeId(agentFunctions.get(i).getNodeId().getId1(), 0);
			NodeId nodeToCheckTwo = new NodeId(agentFunctions.get(i).getNodeId().getId2(), 0);

			if (MainSimulator.agentType == 7) {

				if (agentFunctions.get(i).checkIfNodeIsContained(nodeToCheckOne)) {

					printNodeConnection(nodeToCheckOne, agentFunctions.get(i), true);

				} else {

					printNodeConnection(nodeToCheckOne, agentFunctions.get(i), false);

				}

				if (agentFunctions.get(i).checkIfNodeIsContained(nodeToCheckTwo)) {

					printNodeConnection(nodeToCheckTwo, agentFunctions.get(i), true);

				} else {

					printNodeConnection(nodeToCheckTwo, agentFunctions.get(i), false);

				}

			}

			if (MainSimulator.agentType == 8) {

				MaxSumSplitConstraintFactorGraphSync maxSplitFunctionNode = (MaxSumSplitConstraintFactorGraphSync) agentFunctions
						.get(i); // Casting the get access to getSplitFunctionNodes method.

				for (int j = 0; j < maxSplitFunctionNode.getSplitFunctionNodes().size(); j++) {

					if (maxSplitFunctionNode.getSplitFunctionNodes().get(j).checkIfNodeIsContained(nodeToCheckOne)) {

						System.out.println("NodeId:(" + nodeToCheckOne.getId1() + "," + nodeToCheckOne.getId2()
								+ ") is in Agent Function Inference ("
								+ maxSplitFunctionNode.getSplitFunctionNodes().get(j).getNodeId().getId1() + ","
								+ maxSplitFunctionNode.getSplitFunctionNodes().get(j).getNodeId().getId2()
								+ ") list.\n");

					} else {

						System.out.println("NodeId:(" + nodeToCheckOne.getId1() + "," + nodeToCheckOne.getId2()
								+ ") is NOT Agent Function Inference ("
								+ maxSplitFunctionNode.getSplitFunctionNodes().get(j).getNodeId().getId1() + ","
								+ maxSplitFunctionNode.getSplitFunctionNodes().get(j).getNodeId().getId2()
								+ ") list.\n");

					}

					if (maxSplitFunctionNode.getSplitFunctionNodes().get(j).checkIfNodeIsContained(nodeToCheckTwo)) {

						System.out.println("NodeId:(" + nodeToCheckTwo.getId1() + "," + nodeToCheckTwo.getId2()
								+ ") is in Agent Function Inference ("
								+ maxSplitFunctionNode.getSplitFunctionNodes().get(j).getNodeId().getId1() + ","
								+ maxSplitFunctionNode.getSplitFunctionNodes().get(j).getNodeId().getId2()
								+ ") list.\n");

					} else {

						System.out.println("NodeId:(" + nodeToCheckTwo.getId1() + "," + nodeToCheckTwo.getId2()
								+ ") is NOT Agent Function Inference ("
								+ maxSplitFunctionNode.getSplitFunctionNodes().get(j).getNodeId().getId1() + ","
								+ maxSplitFunctionNode.getSplitFunctionNodes().get(j).getNodeId().getId2()
								+ ") list.\n");

					}

				}
			}

		}

		// -----------------------------------------------------------------------------------------------------------//
	}
*/
	// OmerP - Method for debug split constraint factor graph connections
/*
	public void printSplitFunctionNode(AgentFunction agentFunction) {

		MaxSumSplitConstraintFactorGraphSync af = (MaxSumSplitConstraintFactorGraphSync) agentFunction;

		for (int i = 0; i < af.getSplitFunctionNodes().size(); i++) {

			for (int j = 0; j < af.getMyNodes().size(); j++) {

				System.out.println("Agent Function:(" + af.getSplitFunctionNodes().get(i).getNodeId().getId1() + ","
						+ +af.getSplitFunctionNodes().get(i).getNodeId().getId2() + "), is connected to:("
						+ af.getMyNodes().get(j).getId1() + "," + af.getMyNodes().get(j).getId2() + ").\n");

			}

		}

	}
*/
	// OmerP - Will print all the agents and the constraints
	public void pringAgentAll() {

		for (int i = 0; i < this.agentsAll.size(); i++) {

			if (this.agentsAll.get(i) instanceof MaxSumStandardFunction) {

				MaxSumStandardFunction af = (MaxSumStandardFunction) this.agentsAll.get(i);
				Set<NodeId> vnSet = af.getVariableMsgs().keySet();
				for (NodeId k : vnSet) {

					System.out.println("Agent function:(" + af.getNodeId().getId1() + "," + af.getNodeId().getId2()
							+ ") " + "is constrained with agent variable:(" + k.getId1() + "," + k.getId2()
							+ ") and constraints:" + Arrays.deepToString(af.getNeighborsConstraintMatrix().get(k))
							+ ".\n");

				}
			}

			if (this.agentsAll.get(i) instanceof AgentVariable) {

				AgentVariableInference av = (AgentVariableInference) this.agentsAll.get(i);
				Set<NodeId> fnSet = av.getMyFunctionMessage().keySet();
				for (NodeId k : fnSet) {

					System.out.println("Agent variable:(" + av.getNodeId().getId1() + "," + av.getNodeId().getId2()
							+ ") " + "is constrained with agent function :(" + k.getId1() + "," + k.getId2() + ")\n");

				}

			}

		}

	}

	///// ******* Print methods ******* ////

	public void printNodeConnection(NodeId nodeId, AgentFunction agentFunction, boolean check) {

		if (check) {

			System.out.println("NodeId:(" + nodeId.getId1() + "," + nodeId.getId2()
					+ ") is in Agent Function Inference (" + agentFunction.getNodeId().getId1() + ","
					+ agentFunction.getNodeId().getId2() + ") list.\n");

		}

		if (!check) {

			System.out.println("NodeId:(" + nodeId.getId1() + "," + nodeId.getId2()
					+ ") is NOT in Agent Function Inference (" + agentFunction.getNodeId().getId1() + ","
					+ agentFunction.getNodeId().getId2() + ") list.\n");

		}

	}

	public void printNodeConnection(NodeId nodeId, AgentVariableInference agentVariable, boolean check) {

		if (check) {

			System.out.println("NodeId:(" + nodeId.getId1() + "," + nodeId.getId2()
					+ ") is in Agent Function Inference (" + agentVariable.getNodeId().getId1() + ","
					+ agentVariable.getNodeId().getId2() + ") list.\n");

		}

		if (!check) {

			System.out.println("NodeId:(" + nodeId.getId1() + "," + nodeId.getId2()
					+ ") is NOT in Agent Function Inference (" + agentVariable.getNodeId().getId1() + ","
					+ agentVariable.getNodeId().getId2() + ") list.\n");

		}

	}

	public AgentVariable getVariableAgents(int i) {
		for (AgentVariable av : agentsVariables) {
			if (av.getId() == i) {
				return av;
			}
		}
		throw new RuntimeException();
	}

	public List<Thread> getAgentThreads() {
		return agentsThreads;
	}

	public void initilizeAndStartRunAgents() {
		agentsThreads = new ArrayList<>();

		for (Agent a : agentsAll) {
			a.resetAgent();
			a.initialize();
			if (MainSimulator.isThreadMailer) {
				agentsThreads.add(new Thread(a));
			}
		}
		if (MainSimulator.isThreadMailer) {
			for (Thread thread : agentsThreads) {
				thread.start();
			}
		}

	}

	public List<Agent> getAllAgents() {
		return agentsAll;
	}



	public AgentFunction getFunctionNodes(NodeId functionNodeId) {
		for (AgentFunction af : agentFunctions) {
			if (af.getNodeId().equals(functionNodeId)) {
				return af;
			}
		}
		throw new RuntimeException("there is no such functionNodeId");
	}

}