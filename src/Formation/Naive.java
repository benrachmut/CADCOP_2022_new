package Formation;

import java.util.Set;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.NodeId;

public class Naive extends Formation {
	private AgentVariable[] input;
	public Naive(AgentVariable[] input_a) {
		super(input_a);
		this.input = input_a;
	}

	@Override
	public void setAboveBelow() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAboveBelow(AgentVariable a, Set<NodeId> above, Set<NodeId> below) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute() {
		for (int i = 0; i < input.length; i++) {
			
		}
		
	}

}
