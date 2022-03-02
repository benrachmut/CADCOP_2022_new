package Comparators;

import java.util.Comparator;

import AgentsAbstract.AgentVariable;

public class CompAgentVariableByNeighborSize implements Comparator<AgentVariable> {

	@Override
	public int compare(AgentVariable o1, AgentVariable o2) {
		int delta = o1.neighborSize() - o2.neighborSize();
		if (delta != 0) {
			return delta;
		} else {
			return  o2.getId()-o1.getId() ;
		}
	}

}
