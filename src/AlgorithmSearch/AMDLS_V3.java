package AlgorithmSearch;

import java.util.Random;

import AgentsAbstract.AgentVariable;
import Main.MailerIterations;
import Main.MainSimulator;
import Messages.MsgAlgorithm;

public class AMDLS_V3 extends AMDLS_V2 {

	private static double stochasticInitial = 0;
	private static double stochasticDecision = 1;


	private Random stochasticDecisionRandom;

	public AMDLS_V3(int dcopId, int D, int agentId) {
		super(dcopId, D, agentId);
		Random r = new Random(this.dcopId * 10 + this.id * 100);
		//this.rndStochasticInitial = r.nextDouble();
		this.stochasticDecisionRandom = new Random(this.dcopId * 12 + this.id * 143);
		//firstFlag = false;
		this.valueAssignment = this.firstRandomVariable;

	}

	@Override
	protected void resetAgentGivenParametersV3() {
		super.resetAgentGivenParametersV3();
		Random r = new Random(this.dcopId * 10 + this.id * 100);
		//this.rndStochasticInitial = r.nextDouble();
		this.stochasticDecisionRandom = new Random(this.dcopId * 12 + this.id * 143);
		//firstFlag = false;
		this.valueAssignment = this.firstRandomVariable;

	}

	@Override
	public void updateAlgorithmName() {

		AgentVariable.AlgorithmName = "LAMDLS";
	}

	@Override
	public void initialize() {
		this.isWithTimeStamp = false;
		this.valueAssignment = this.firstRandomVariable;
		if (canSetColorInitilize()) {
			chooseColor();


			sendAMDLSColorMsgs();
			this.myCounter = 1;
			//firstFlag = true;
			isWaitingToSetColor = false;
		} else {
			this.valueAssignment = Integer.MIN_VALUE;
			this.myCounter = 0;
		}
	}

	public boolean compute() {
			boolean flag = false;
			if (canSetColorFlag) {
				chooseColor();
				setAboveAndBelow();
				flag = true;
			}

			if (flag || (consistentFlag && !canSetColorFlag)) {
				double rnd = this.stochasticDecisionRandom.nextDouble();
				if (rnd < stochasticDecision || flag) {
					decideAndChange();
				} else {
					myCounter = myCounter + 1;
				}

			}
		return true;
	}

	/*
	 * protected void sendMsgs() { boolean sendAllTheTime =
	 * AMDLS_V1.sendWhenMsgReceive && this.gotMsgFlag; boolean flag = false; if (
	 * this.canSetColorFlag) { sendAMDLSColorMsgs(); boolean aboveConsistent =
	 * isAboveConsistent(); boolean belowConsistent = isBelowConsistent(); if
	 * (aboveConsistent && belowConsistent && allNeighborsHaveColor()) { flag =
	 * true; } else { flag = false; } } if (sendAllTheTime || (this.consistentFlag
	 * && !canSetColorFlag) || (flag)) { if (flag) { decideAndChange(); }
	 * sendAMDLSmsgs(); }
	 * 
	 * }
	 */
	// done
	@Override
	public void updateAlgorithmHeader() {
		AgentVariable.algorithmHeader = "Structure Heuristic" + "," + "Message Frequency" + ',' + "Decision" + ","
				+ "Random Initial Probebility" + "," + "Stochastic Decision";
	}

	// done
	@Override
	public void updateAlgorithmData() {
		String heuristic = "";
		if (structureHeuristic == 1) {
			heuristic = "Index";
		}

		if (structureHeuristic == 2) {
			heuristic = "Max Neighbor";
		}

		if (structureHeuristic == 3) {
			heuristic = "Min Neighbor";
		}
		// -------------------------
		String freq = "";
		if (AMDLS_V1.sendWhenMsgReceive) {
			freq = "high";
		} else {
			freq = "low";
		}

		// -------------------------
		String t = "";
		if (AMDLS_V1.typeDecision == 'A' || AMDLS_V1.typeDecision == 'a') {
			t = "a";
		}

		if (AMDLS_V1.typeDecision == 'B' || AMDLS_V1.typeDecision == 'b') {
			t = "b";
		}

		if (AMDLS_V1.typeDecision == 'C' || AMDLS_V1.typeDecision == 'c') {
			t = "c";
		}

		// -------------------------

		AgentVariable.algorithmData = heuristic + "," + freq + "," + t + "," + stochasticInitial + ","
				+ stochasticDecision;
	}

}
