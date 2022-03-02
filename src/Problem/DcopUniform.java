package Problem;
import java.util.Random;

import AgentsAbstract.AgentVariable;

public class DcopUniform extends Dcop {

	private Random randomP1;
	private double p1;
	private double p2;
	private int costLb;
	private int costUb;

	public DcopUniform(int dcopId, int A, int D, int costLb,int costUb, double p1, double p2) {
		super(dcopId,A, D);
		this.randomP1 = new Random(this.dcopId * 10);
		this.p1 = p1;
		this.p2 = p2;
		this.D = D;
		this.costLb = costLb;
		this.costUb = costUb;
		updateNames();
	}
	/*
	public DcopUniform(int dcopId,int A, double p1, double p2) {
		this(dcopId,A, 10, 100, p1, p2);

	}
	*/

	@Override
	public void createNeighbors() {
		for (int i = 0; i < agentsVariables.length; i++) {
			for (int j = i + 1; j < agentsVariables.length; j++) {
				double rnd = randomP1.nextDouble();
				if (rnd < p1) {
					AgentVariable a1 = agentsVariables[i];
					AgentVariable a2 = agentsVariables[j];
					
					this.neighbors.add(new Neighbor(a1, a2, D, costLb,costUb, dcopId, p2));
			
				} // if neighbors
			} // for j
		} // for i
	}
	
	@Override
	protected void setDcopName() {
		Dcop.dcopName = "Random Uniform "+p1;

		
		
	}

	@Override
	protected void setDcopHeader() {
		Dcop.dcopHeader = "p1"+","+"p2"+","+"Domain Size";
		
	}

	@Override
	protected void setDcopParameters() {
		Dcop.dcopParameters = this.p1+","+this.p2+","+this.D;
	}

	

}
