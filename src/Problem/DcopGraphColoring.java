package Problem;
import java.util.Random;

import AgentsAbstract.AgentVariable;

public class DcopGraphColoring extends Dcop {

	
	private double p1;
	private Random randomP1;
	private int costLb,costUb;
	
	public DcopGraphColoring(int dcopId, int A, int D, int costLb, int costUb, double p1) {
		super(dcopId,A, D);
		this.p1 = p1;
		randomP1 = new Random(this.dcopId*10);
		this.costLb = costLb;
		this.costUb = costUb;
		updateNames();
	}
	
	public DcopGraphColoring(int dcopId,int A, double p1) {
		this(dcopId, A, 3, 10,20, p1);
	}
		
	@Override
	public void createNeighbors() {
		for (int i = 0; i < agentsVariables.length; i++) {
			for (int j = i + 1; j < agentsVariables.length; j++) {
				double rnd = randomP1.nextDouble();
				if (rnd < p1) {
					AgentVariable a1 = agentsVariables[i];
					AgentVariable a2 = agentsVariables[j];
					this.neighbors.add(new Neighbor( a1,  a2,  D,  costLb, costUb,  dcopId));
			
				} // if neighbors
			} // for j
		} // for i
		
	}

	@Override
	protected void setDcopName() {
		Dcop.dcopName = "Graph Coloring";
		
	}

	@Override
	protected void setDcopHeader() {
		Dcop.dcopHeader = "p"+","+"Domain Size";
		
	}

	@Override
	protected void setDcopParameters() {
		Dcop.dcopParameters = this.p1+","+this.D;
	}

	

	
	/*
	 * for (int i = 0; i < agentsF.length; i++) {
			for (int j = i + 1; j < agentsF.length; j++) {
				double p1Max = rP1.nextDouble();
				if (p1Max < currentP1) {
					AgentField af1 = agentsF[i];
					AgentField af2 = agentsF[j];
					this.neighbors.add(new Neighbors(af1, af2));
				} // if neighbors
			} // for j
		} // for i
	 */
}
