package AlgorithmSearch;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import AgentsAbstract.AgentVariable;

import java.util.Random;
import java.util.SortedMap;

import Messages.MsgAlgorithm;

public abstract class DSA_SDP extends DSA_B {

	protected static int k = 40;
	protected static double p_a = 0.6, p_b = 0.1, p_c = 0.4, p_d = 0.8;
	protected Random rndQ;
	protected int kCounter;

	public DSA_SDP(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		this.rndQ = new Random(this.dcopId * 95 + (this.id) * 354);
		this.rndQ.nextDouble();

		this.kCounter = 0;

	}

	@Override
	protected boolean compute() {
		double ratio = calcRatio();

		boolean didChangToSecondBest = checkToChangeToSecondBest(ratio);

		if (!didChangToSecondBest) {
			return eximneStochasticSlopeChange(ratio);
		}else {
			return true;
		}
	}

	private boolean eximneStochasticSlopeChange(double ratio) {
		int candidate = getCandidateToChange_B();
		if (candidate == valueAssignment) {
			kCounter = kCounter + 1;
			return false;
		} else {
			stochastic = p_a + Math.min(p_b, ratio);
			return stochasticChange(candidate);
		}
		
	}

	private boolean checkToChangeToSecondBest(double ratio) {
		if (k == kCounter) {

			kCounter = 0;
			double q = calcQSdp(ratio);
			double rnd = rndQ.nextDouble();
			if (rnd < q) {
				this.valueAssignment = secondBest();
				return true;
			}

		}
		return false;
	}

	private int secondBest() {
		SortedMap<Integer, Integer> costPerDomain = getCostPerDomain();
		int current_cost = Collections.min(costPerDomain.values());
		int selectedValue = getSelectedValue(current_cost, costPerDomain);
		costPerDomain.remove(selectedValue);
		double new_cost = Collections.min(costPerDomain.values());
		int ans = -1;
		for (Entry<Integer, Integer> e : costPerDomain.entrySet()) {
			if (e.getValue() == new_cost) {
				ans = e.getKey();
			}
		}
		if (ans == -1) {
			throw new RuntimeException("did not find second best alternative");
		}
		return ans;
	}

	private double calcQSdp(double ratio) {

		if (ratio > 1) {
			return 0;
		} else {
			return Math.max(p_c, p_d - ratio);
		}
	}

	private double calcRatio() {
		SortedMap<Integer, Integer> costPerDomain = getCostPerDomain();

		int current_cost = costPerDomain.get(this.valueAssignment); 
		
		costPerDomain.remove(this.valueAssignment);
		
		int new_cost = Collections.min(costPerDomain.values());

		double ans = 0;
		try {
		 ans = Math.abs(current_cost - new_cost) / current_cost;
		}catch(ArithmeticException e){
			ans = 0;
		}
		return ans;

	}

	private int getSelectedValue(int current_cost, SortedMap<Integer, Integer> costPerDomain) {
		int selectedValue = -1;

		for (Entry<Integer, Integer> e : costPerDomain.entrySet()) {
			if (e.getValue() == current_cost && e.getKey() != this.valueAssignment) {
				selectedValue = e.getKey();
			}
		}
		if (selectedValue == -1) {
			selectedValue = this.valueAssignment;
		}
		return selectedValue;
	}

	
	@Override
	protected void resetAgentGivenParametersV4() {
		this.rndQ = new Random(this.dcopId * 95 + this.id * 354);
		this.rndQ.nextDouble();

		this.kCounter = 0;

		resetAgentGivenParametersV5();

	}

	protected abstract void resetAgentGivenParametersV5();


	
	@Override
	public void updateAlgorithmHeader() {
		AgentVariable.algorithmHeader = "";
	}

	@Override
	public void updateAlgorithmData() {
		AgentVariable.algorithmData = "";
	}

	

}
