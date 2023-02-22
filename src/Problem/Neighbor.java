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
	public enum CostType{uniform,poisson,color}

	private int costParameter;
	private double p2;
	private Random randomP2, randomCost;
	private int costLb;
	private int costUb;
	private int lambda;
	private CostType myCostType;

	public Neighbor(AgentVariable a1, AgentVariable a2, int D, int costLb, int costUb, int dcopId, double p2) {
		super();
		this.myCostType = CostType.uniform;
		updateVariables(a1, a2, costLb, costUb, D);
		this.p2 = p2;
		this.randomP2 = new Random((dcopId+1) * 10 + (a1.getId()+1) * 100 + (a2.getId()+1) * 1000);
		this.randomCost = new Random((dcopId+1) * 100 + (a1.getId()+1) * 300 + (a2.getId()+1) * 1200);
		createConstraintsWithP2();
		neighborsMeetings();
	}

	public Neighbor(AgentVariable a1, AgentVariable a2, int D, int costLb, int costUb, int dcopId) {
		this.myCostType = CostType.color;
		updateVariables(a1, a2, costLb, costUb, D);
		this.randomCost = new Random((1+dcopId) * 100 + (1+a1.getId()) * 300 + (1+a2.getId()) * 1200);
		createConstraintsForEquality();
		neighborsMeetings();
	}

	public Neighbor(AgentVariable a1, AgentVariable a2, int D, int lambda, int dcopId, double p2) {
		this.myCostType = CostType.poisson;
		updateVariables(a1, a2, lambda, D);
		this.randomCost = new Random((1+dcopId) * 100 + (1+a1.getId()) * 300 + (1+a2.getId()) * 1200);
		//createConstraintsForEquality();
		this.p2 =p2;
		this.randomP2 = new Random((dcopId+1) * 10 + (a1.getId()+1) * 100 + (a2.getId()+1) * 1000);
		createConstraintsWithP2();

		neighborsMeetings();
	}



	/*
	// USED BY SIMULATOR FOR MAXSUM
	public Neighbor(AgentVariable a1, AgentVariable a2, int D, int costConstantForColor, int dcopId, boolean dummy) {
		updateVariables(a1, a2, 0, costConstantForColor, D);
		this.randomCost = new Random((1+dcopId) * 100 + (1+a1.getId()) * 300 + (1+a2.getId()) * 1200);
		createConstraintsForEquality();
		neighborsMeetings();
	}
	*/


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

	private void updateVariables(AgentVariable a1, AgentVariable a2, int lambda, int D) {
		this.a1 = a1;
		this.a2 = a2;
		this.lambda = lambda;
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
					int rndCost = 0;
					boolean flag = false;
					if (this.myCostType == CostType.uniform) {
						rndCost = costLb + randomCost.nextInt(costUb - costLb);
						flag = true;
					}

					if (this.myCostType == CostType.poisson){
						if (this.lambda <20) {
							rndCost = getPoisRandomNumber();
						}else{
							double Z = randomCost.nextGaussian();
							rndCost =(int) (Z*+Math.sqrt(lambda)+lambda);
						}
						flag = true;
					}
					if (!flag){
						throw new RuntimeException("must enter ifs");
					}
					constraints[i][j] = rndCost;
					constraintsTranspose[j][i] = rndCost;

				}
			}
		}
	}

	private int getPoisRandomNumber() {
		int r = 0;
		double a = randomCost.nextDouble();
		double p = Math.exp(-lambda);

		while (a > p) {
			r++;
			a = a - p;
			p = p * lambda / r;
		}
		return r;

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
