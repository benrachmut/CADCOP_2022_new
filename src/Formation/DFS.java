package Formation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AlgorithmSearch.AMDLS_V1;
import Main.MainSimulator;

public class DFS extends Formation {

	protected Map<NodeId, Integer> numberInTree;
	protected Map<AgentVariable, Boolean> visited;

	public DFS(AgentVariable[] input_a) {
		super(input_a);
		this.visited = initColorMap();

		numberInTree = new TreeMap<NodeId, Integer>();
		for (AgentVariable a : this.agents) {
			numberInTree.put(a.getNodeId(), -1);
		}
	}

	private Map<AgentVariable, Boolean> initColorMap() {
		Map<AgentVariable, Boolean> ans = new HashMap<AgentVariable, Boolean>();
		for (AgentVariable agentField : agents) {
			ans.put(agentField, false);
		}

		return ans;
	}

	// -----------set above and below------------

	public void setAboveBelow(AgentVariable a, Set<NodeId> above, Set<NodeId> below) {

		int aLevel = this.numberInTree.get(a.getNodeId());

		Set<NodeId> nIds = a.getNeigborSetId();

		for (NodeId nodeId : nIds) {
			int levelOfN = this.numberInTree.get(nodeId);

			if (levelOfN < aLevel) {
				above.add(nodeId);
			}
			if (levelOfN > aLevel) {
				below.add(nodeId);
			}
			if (levelOfN == aLevel) {
				throw new RuntimeException("something is wrong with psaudo tree");
			}
		}
		

	}
	public void setAboveBelow() {
		for (AgentVariable a : agents) {
			int aLevel = this.numberInTree.get(a.getNodeId());
			Set<NodeId> above = new TreeSet<NodeId>();
			Set<NodeId> below = new TreeSet<NodeId>();
			Set<NodeId> nIds = a.getNeigborSetId();
			
			for (NodeId nodeId : nIds) {
				int levelOfN = this.numberInTree.get(nodeId);
			
				if (levelOfN<aLevel) {
					above.add(nodeId);
				}
				if (levelOfN>aLevel) {
					below.add(nodeId);
				}
				if (levelOfN==aLevel) {
					throw new RuntimeException("something is wrong with psaudo tree");
				}
			}
			if (MainSimulator.agentType==5) {
				((AMDLS_V1)a).setBelow(below);
				((AMDLS_V1)a).setAbove(above);
			}
			
	
			

		}
		

	}
	
	private Collection<AgentVariable> getFathers() {
		Collection<AgentVariable> ans = new ArrayList<AgentVariable>();
		for (AgentVariable a : this.agents) {
			if (a.getDfsFather() == null) {
				ans.add(a);
			}
		}
		return ans;
	}

	// -----------execute------------

	@Override
	public void execute() {
		while (someOneIsNotColored()) {
			AgentVariable notVisited = findFirstNotVisited();
			execute(notVisited);
		}
	}

	private void execute(AgentVariable currentA) {
		if (currentA.getDfsFather() == null) {
			this.numberInTree.put(currentA.getNodeId(), 0);
		} else {
			AgentVariable father = getAgentByNodeId(currentA.getDfsFather());
			int level = this.numberInTree.get(father.getNodeId()) + 1;
			this.numberInTree.put(currentA.getNodeId(), level);
		}
		this.visited.put(currentA, true);
		List<AgentVariable> sons = getSons(currentA);
		for (AgentVariable agentFieldSon : sons) {
			if (!visited.get(agentFieldSon)) {
				agentFieldSon.setDfsFather(currentA.getNodeId());
				currentA.addDfsSon(agentFieldSon.getNodeId());
				execute(agentFieldSon);
			}
		}
	}

	private List<AgentVariable> getSons(AgentVariable currntA) {
		Set<NodeId> nSetId = currntA.getNeigborSetId();
		List<AgentVariable> sons = getNeighborsOfAgents(nSetId);
		Collections.sort(sons, new AgentNeighborComp());
		//Collections.reverse(sons);
		return sons;
	}

	private List<AgentVariable> getNeighborsOfAgents(Set<NodeId> nSetId) {
		List<AgentVariable> ans = new ArrayList<AgentVariable>();
		for (NodeId nId : nSetId) {
			for (AgentVariable agentVariable : this.agents) {
				if (agentVariable.getNodeId().getId1() == nId.getId1()) {
					ans.add(agentVariable);
				}
			}
		}

		return ans;
	}

	private AgentVariable findFirstNotVisited() {
		Collection<AgentVariable> notColored = getNotColoredYet();
		return Collections.min(notColored, new AgentNeighborComp());
	}

	private Collection<AgentVariable> getNotColoredYet() {
		Collection<AgentVariable> notColored = new HashSet<AgentVariable>();
		for (Entry<AgentVariable, Boolean> e : visited.entrySet()) {
			if (e.getValue() == false) {
				notColored.add(e.getKey());
			}
		}
		return notColored;
	}

	private boolean someOneIsNotColored() {
		Collection<Boolean> colors = this.visited.values();
		for (Boolean c : colors) {
			if (!c) {
				return true;
			}
		}
		return false;
	}

}
