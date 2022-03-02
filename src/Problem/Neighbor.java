package Problem;

import java.util.Random;

import AgentsAbstract.AgentFunction;
import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AlgorithmInference.MaxSumStandardFunctionDelay;
import AlgorithmInference.MaxSumStandardVariableDelay;
import Main.MainSimulator;

public class Neighbor {
	private AgentVariable a1, a2;
	// private AgentFunction f;
	private Integer[][] constraints;
	private Integer[][] constraintsTranspose;

	private int costParameter;
	private double p2;
	private Random randomP2, randomCost;
	private int costLb;
	private int costUb;

	public Neighbor(AgentVariable a1, AgentVariable a2, int D, int costLb, int costUb, int dcopId, double p2) {
		super();
		updateVariables(a1, a2, costLb, costUb, D);
		this.p2 = p2;
		this.randomP2 = new Random((dcopId+1) * 10 + (a1.getId()+1) * 100 + (a2.getId()+1) * 1000);
		this.randomCost = new Random((dcopId+1) * 100 + (a1.getId()+1) * 300 + (a2.getId()+1) * 1200);
		createConstraintsWithP2();
		neighborsMeetings();
	}

	public Neighbor(AgentVariable a1, AgentVariable a2, int D, int costLb, int costUb, int dcopId) {
		updateVariables(a1, a2, costLb, costUb, D);
		this.randomCost = new Random((1+dcopId) * 100 + (1+a1.getId()) * 300 + (1+a2.getId()) * 1200);
		createConstraintsForEquality();
		neighborsMeetings();
	}

	private void updateVariables(AgentVariable a1, AgentVariable a2, int costLb, int costUb, int D) {
		this.a1 = a1;
		this.a2 = a2;
		this.costLb = costLb;
		this.costUb = costUb;

		this.constraints = new Integer[D][D];
		this.constraintsTranspose = new Integer[D][D];

	}

	public Integer[][] getConstraints() {
		return constraints;
	}

	public Integer[][] getConstraintsTranspose() {
		return constraintsTranspose;
	}

	private void updateVariables(AgentVariable a1, AgentVariable a2, int costParameter, int D) {
		this.a1 = a1;
		this.a2 = a2;
		this.costParameter = costParameter;
		this.constraints = new Integer[D][D];
		this.constraintsTranspose = new Integer[D][D];
	}

	private void createConstraintsForEquality() {
		for (int i = 0; i < constraints.length; i++) {
			for (int j = 0; j < constraints[i].length; j++) {
				if (j == i) {
					int rndCost = costLb + randomCost.nextInt(costUb - costLb);
					constraints[i][j] = rndCost;
					constraintsTranspose[j][i] = rndCost;
				} else {
					constraints[i][j] = 0;
					constraintsTranspose[j][i] = 0;

				}
			}
		}

	}

	private void createConstraintsWithP2() {
		for (int i = 0; i < constraints.length; i++) {
			for (int j = 0; j < constraints[i].length; j++) {
				double rndProb = randomP2.nextDouble();
				if (rndProb < p2) {
					int rndCost = costLb + randomCost.nextInt(costUb - costLb);
					constraints[i][j] = rndCost;
					constraintsTranspose[j][i] = rndCost;

				}
			}
		}
	}

	/**
	 * We acknowledge Omer's brilliance
	 * 
	 * @return
	 */
	public Integer getCurrentCost() {
		int i = a1.getValueAssignment();
		int j = a2.getValueAssignment();

		try {
			return this.constraints[i][j];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}

	}

	public void neighborsMeetings() {

		a1.meetNeighbor(a2.getId(), this.constraints);
		a2.meetNeighbor(a1.getId(), this.constraintsTranspose);
	}

	public AgentVariable getA1() {
		return a1;
	}

	public AgentVariable getA2() {
		return a2;
	}

	public Integer getCurrentAnytimeCost() {
		Integer i = ((AgentVariableSearch) a1).getValueAssignmentOfAnytime();
		Integer j = ((AgentVariableSearch) a2).getValueAssignmentOfAnytime();

		if (i == null || j == null) {
			return null;
		}
		return this.constraints[i][j];
	}

	public double distanceBetween() {
		
		return a1.getQuadraticDistanceTo(a2.getLocation());
	}

	
}
