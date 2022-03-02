package Formation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import AlgorithmSearch.AMDLS_V1;
import Main.MainSimulator;

public class ColorFormation extends Formation {

	private Map<AgentVariable, Integer> colorOfAgent;

	public ColorFormation(AgentVariable[] input_a) {
		super(input_a);
		colorOfAgent = new TreeMap<AgentVariable, Integer>();
		for (AgentVariable a : this.agents) {
			colorOfAgent.put(a, -1);
		}
	}

	@Override
	public void execute() {
		while (allMarked() == false) {
			List<AgentVariable> notColoredYet = getNotColoredYet();
			AgentVariable a = Collections.max(notColoredYet, new AgentNeighborComp());
			int colorForA = getColorForA(a);
			this.colorOfAgent.put(a, colorForA);
		}
	}

	private int getColorForA(AgentVariable a) {
		Set<AgentVariable> neighborsOfThatGotColoredAlready = getNeighborsOfThatGotColoredAlready(a);
		if (neighborsOfThatGotColoredAlready.isEmpty()) {
			return 1;
		}

		int colorTry = 0;
		boolean flag = false;

		while (flag == false) {
			colorTry = colorTry + 1;
			boolean foundEqualColor = false;
			for (AgentVariable n : neighborsOfThatGotColoredAlready) {
				if (colorTry == this.colorOfAgent.get(n)) {
					foundEqualColor = true;
				}
			}

			if (!foundEqualColor) {
				flag = true;
			}

		}
		return colorTry;
	}

	private Set<AgentVariable> getNeighborsOfThatGotColoredAlready(AgentVariable a) {
		Set<AgentVariable> ans = new HashSet<AgentVariable>();
		Set<NodeId> neightborsA = a.getNeigborSetId();
		for (NodeId nId : neightborsA) {

			for (Entry<AgentVariable, Integer> e : colorOfAgent.entrySet()) {
				if (e.getKey().getNodeId().equals(nId)) {
					if (e.getValue() != -1) {
						ans.add(e.getKey());
					}
				}
			}

		}
		return ans;
	}

	private List<AgentVariable> getNotColoredYet() {
		List<AgentVariable> ans = new ArrayList<AgentVariable>();
		for (Entry<AgentVariable, Integer> e : this.colorOfAgent.entrySet()) {
			if (e.getValue() == -1) {
				ans.add(e.getKey());
			}
		}
		return ans;
	}

	private boolean allMarked() {
		for (Integer i : colorOfAgent.values()) {
			if (i == -1) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void setAboveBelow(AgentVariable a, Set<NodeId> above, Set<NodeId> below) {
		int agentHeight = this.colorOfAgent.get(a);
		for (NodeId nId : a.getNeigborSetId()) {
			for (AgentVariable nAgent : agents) {
				if (nAgent.getNodeId().equals(nId)) {
					int neighborHeight = this.colorOfAgent.get(nAgent);
					if (agentHeight == neighborHeight) {
						throw new RuntimeException("something is wrong with coloring, should not happen");
					}
					if (agentHeight < neighborHeight) {
						below.add(nId);
					} else {
						above.add(nId);
					}
				}
			}
		}

	}
	
	@Override
	public void setAboveBelow() {
		for (AgentVariable a : agents) {
			Set<NodeId> below = new TreeSet<NodeId>();
			Set<NodeId> above = new TreeSet<NodeId>();

			int agentHeight = this.colorOfAgent.get(a);
			for (NodeId nId : a.getNeigborSetId()) {
				for (AgentVariable nAgent : agents) {
					if (nAgent.getNodeId().equals(nId)) {
						int neighborHeight = this.colorOfAgent.get(nAgent);
						if (agentHeight == neighborHeight) {
							throw new RuntimeException("something is wrong with coloring, should not happen");
						}
						if (agentHeight < neighborHeight) {
							below.add(nId);
						} else {
							above.add(nId);
						}
					}
				}
			}
			if (MainSimulator.agentType == 5) {
				((AMDLS_V1) a).setAbove(above);
				((AMDLS_V1) a).setBelow(below);
			}

		}

	}

}
