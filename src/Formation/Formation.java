package Formation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.NodeId;

public abstract class Formation {
	protected Collection<AgentVariable> agents;

	public Formation( AgentVariable[] input_a) {
		this.agents = createAfList(input_a);
	}
	public abstract void   setAboveBelow();

	public abstract void setAboveBelow(AgentVariable a, Set<NodeId> above, Set<NodeId> below);
	
	public abstract void execute();

	protected AgentVariable getAgentByNodeId(NodeId nodeId ) {
		for (AgentVariable a : agents) {
			if (a.getNodeId().getId1() == nodeId.getId1()) {
				return a;
			}
		}
		return null;
	}
	
	
	private Collection<AgentVariable> createAfList(AgentVariable[] aFieldInput) {
		Collection<AgentVariable> ans = new ArrayList<AgentVariable>();

		for (AgentVariable temp : aFieldInput) {
			ans.add(temp);
		}
		return ans;
	}
	
}
