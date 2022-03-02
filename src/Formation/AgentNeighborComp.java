package Formation;

import java.util.Comparator;

import AgentsAbstract.AgentVariable;

public class AgentNeighborComp implements Comparator<AgentVariable> {

	@Override
	public int compare(AgentVariable o1, AgentVariable o2) {
		// TODO Auto-generated method stub
		return o1.getNeigborSetId().size()-o2.getNeigborSetId().size();
	}

}
