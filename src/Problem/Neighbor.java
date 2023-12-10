package Problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import AgentsAbstract.AgentFunction;
import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AlgorithmInference.MaxSumStandardFunctionDelay;
import AlgorithmInference.MaxSumStandardVariableDelay;
import Main.MainSimulator;
import Main.MainSimulatorIterations;
import Main.MainSimulatorIterations.CostType;


public class Neighbor {
	private AgentVariable a1, a2;
	// private AgentFunction f;
	private Integer[][] constraints;
	private Integer[][] constraintsTranspose;
	public static boolean costDebug = true;




	private int costParameter;
	private double p2;
	private Random randomP2, randomCost;
	private int costLb;
	private int costUb;
	//private int lambda;

	private double initMean;
	private int moveMeanCounter;
	private double moveMeanDistance;
	private double sd;

	private CostType myCostType;




	public Neighbor(AgentVariable a1, AgentVariable a2, int D, int dcopId, double p2, CostType costType) {
		super();
		this.myCostType = costType;
		updateVariables(a1, a2, D);
		this.p2 = p2;
		this.randomP2 = new Random((dcopId+1) * 10 + (a1.getId()+1) * 100 + (a2.getId()+1) * 1000);
		this.randomCost = new Random((dcopId+1) * 100 + (a1.getId()+1) * 300 + (a2.getId()+1) * 1200);
		createConstraintsWithP2();
		neighborsMeetings();
	}

	@Override
	public String toString() {
		return "Neighbors: {"+this.a1.getNodeId()+","+this.a2.getNodeId()+"}";
	}

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




/*
	private void updateVariables(AgentVariable a1, AgentVariable a2, double initMean, int moveMeanCounter, double moveMeanDistance, double sd, int D) {
		this.a1 = a1;
		this.a2 = a2;
		this.initMean =initMean;
		this.moveMeanCounter =moveMeanCounter  ;
		this.moveMeanDistance =moveMeanDistance  ;
		this.sd = sd ;
		this.constraints = new Integer[D][D];
		this.constraintsTranspose = new Integer[D][D];
	}
*/

/*

	private void updateVariables(AgentVariable a1, AgentVariable a2, double initMean, int moveMeanCounter, double moveMeanDistance,  int D) {
		this.a1 = a1;
		this.a2 = a2;
		this.initMean =initMean;
		this.moveMeanCounter =moveMeanCounter  ;
		this.moveMeanDistance =moveMeanDistance  ;
		this.sd = Integer.MIN_VALUE;
		this.constraints = new Integer[D][D];
		this.constraintsTranspose = new Integer[D][D];
	}
*/
	/*
	// USED BY SIMULATOR FOR MAXSUM
	public Neighbor(AgentVariable a1, AgentVariable a2, int D, int costConstantForColor, int dcopId, boolean dummy) {
		updateVariables(a1, a2, 0, costConstantForColor, D);
		this.randomCost = new Random((1+dcopId) * 100 + (1+a1.getId()) * 300 + (1+a2.getId()) * 1200);
		createConstraintsForEquality();
		neighborsMeetings();
	}
	*/



	private void updateVariables(AgentVariable a1, AgentVariable a2,  int D) {
		this.a1 = a1;
		this.a2 = a2;
		this.constraints = new Integer[D][D];
		this.constraintsTranspose = new Integer[D][D];

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
/*
	private void updateVariables(AgentVariable a1, AgentVariable a2, int lambda, int D) {
		this.a1 = a1;
		this.a2 = a2;
		this.lambda = lambda;
		this.constraints = new Integer[D][D];
		this.constraintsTranspose = new Integer[D][D];
	}
*/
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
		List<Integer> preferences1 = a1.preferenceDomain;
		List<Integer> preferences2 = a2.preferenceDomain;

		for (int i = 0; i < constraints.length; i++) {
			for (int j = 0; j < constraints[i].length; j++) {

				double rndProb = randomP2.nextDouble();
				if (rndProb < p2) {
					int rndCost = 0;
					if (!MainSimulatorIterations.amIRunning) {
						rndCost = costLb + randomCost.nextInt(costUb - costLb);
					}
					if (MainSimulatorIterations.amIRunning) {

						if (this.myCostType == CostType.uniform_0_100) {
							rndCost = 0 + randomCost.nextInt(100 - 0);
						}

						if (this.myCostType == CostType.poisson_50) {
							rndCost = getRandomPoisson(50);
						}
						boolean isScheduleProblem = this.myCostType == CostType.softScheduleSd10Hill1 ||  this.myCostType == CostType.softScheduleSd10Hill3 ||
								this.myCostType == CostType.hardScheduleSd10Hill1||  this.myCostType == CostType.hardScheduleSd10Hill3;
						boolean isHardScheduleProblem = this.myCostType == CostType.hardScheduleSd10Hill1||  this.myCostType == CostType.hardScheduleSd10Hill3;
						if (isScheduleProblem ) {
							double sd = 10;

							rndCost = getRandomNormalScheduleCost(sd,preferences1,i,preferences2,j);
							if (isHardScheduleProblem && i!=j){
								rndCost = 200;
							}

						}





						//if (costDebug){
						//	System.out.println(rndCost);
						//}
						//}


					}
					constraints[i][j] = rndCost;
					constraintsTranspose[j][i] = rndCost;
				}
			}
		}
	}

	private int getRandomNormalScheduleCost(double sd, List<Integer> preferences1, int i, List<Integer> preferences2, int j) {
		int happy1 = howMuchHappy(preferences1,i);
		int happy2 = howMuchHappy(preferences2,j);
		double avg = happy1+happy2/2.0;
		double mu = Math.ceil(avg) ;
		double Z = randomCost.nextGaussian();
		int ans  = (int) (Z*sd+(mu*10));
		if(ans<0){
			ans = 0;
		}
		return ans;
	}

	private int howMuchHappy(List<Integer> preference, int numInDomain) {
		int closest = preference.get(0); // Assume the first element is the closest initially

		for (int num : preference) {
			if (Math.abs(num - numInDomain) < Math.abs(closest - numInDomain)) {
				closest = num;
			}
		}
		return Math.abs(closest - numInDomain);
	}


	private int getRandomPoisson(int lambda) {
		int rndCost = 0;
		if (lambda <20) {
			rndCost = getPoisRandomNumber(lambda);
		}else{
			double Z = randomCost.nextGaussian();
			rndCost =(int) (Z*Math.sqrt(lambda)+lambda);
		}
		return rndCost;
	}

	private int getPoisRandomNumber(int lambda) {
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
