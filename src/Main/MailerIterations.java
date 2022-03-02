package Main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentFunction;
import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableInference;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.Context;
import AgentsAbstract.NodeId;
import AlgorithmSearch.AMDLS_V1;
import AlgorithmSearch.AMDLS_V2;
import AlgorithmSearch.DSA_B_SY;
import AlgorithmSearch.MGM;
import Comparators.CompMsgByDelay;
import Data.Data;
import Delays.ProtocolDelay;
import Down.ProtocolDown;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgAnyTime;
import Problem.Dcop;

public class MailerIterations extends Mailer {

	public static int m_iteration;

	public MailerIterations(Protocol protocol, Long terminationTime, Dcop dcop, int i) {
		super(protocol, terminationTime, dcop,i);
	}

	@Override
	public void execute() {
		for (int iteration = 0; iteration < this.terminationTime; iteration++) {
			
			System.out.println("#############ITERATION="+iteration+"#############");
			if (iteration == 28) {
				System.out.println("ahhhhhhhhhhh");
			}
			m_iteration = iteration;
			createData(iteration);
			List<Msg> msgsFromInbox = new ArrayList<Msg>();
			if (!inbox.isEmpty()) {
				msgsFromInbox = inbox.extract();
			}
			placeMsgsFromInboxInMessageBox(msgsFromInbox);			
			List<Msg> msgToSend = this.handleDelay();
			agentsRecieveMsgs(msgToSend);
			extractFromInbox();
			
		}
	}

	private void extractFromInbox() {
		for (Agent agent : dcop.getAllAgents()) {
			agent.extractFromInboxUsedByMailerIteration();
		}
		
	}

	private void printContexts() {
		for (AgentVariable a : this.dcop.getVariableAgents()) {
			AgentVariableSearch as = (AgentVariableSearch) a;
			Context c = as.createMyContext();
			System.out.println(c);
		}

	}

	private void printHeaderForDegbugMgm() {
		String ans = "iteration" + ",";
		for (AgentVariable a : dcop.getVariableAgents()) {
			ans = ans + "a" + a.getId() + " VA" + ",";
		}
		for (AgentVariable a : dcop.getVariableAgents()) {
			ans = ans + "a" + a.getId() + " LR" + ",";
		}

		System.out.println(ans);

	}

	private void printForDegbugMgm(int iteration) {
		String ans = iteration + ",";
		for (AgentVariable a : dcop.getVariableAgents()) {
			ans = ans + a.getValueAssignment() + ",";
		}
		for (AgentVariable a : dcop.getVariableAgents()) {
			ans = ans + ((MGM) a).getLR() + ",";
		}
		System.out.println(ans);
	}

	private void printHeaderForDebugDSA_SY() {
		String ans = "Iteration,Global_Cost,";
		for (int i = 0; i < dcop.getVariableAgents().length; i++) {
			AgentVariable av = dcop.getVariableAgents()[i];
			if (av instanceof DSA_B_SY) {
				int currentId = av.getId();
				ans = ans + currentId + "_rnd" + "," + currentId + "_value,";
				for (NodeId nNodeId : av.getNeigborSetId()) {
					int n = nNodeId.getId1();
					ans = ans + currentId + "_view_on_" + n + "," + currentId + "_timestamp_on_" + n + ",";
				}
			}
		}
		System.out.println(ans);

	}

	private void printForDebugDSA_SY(int iteration) {
		String ans = iteration + "," + this.dataMap.get(iteration).getGlobalCost() + ",";

		for (int i = 0; i < dcop.getVariableAgents().length; i++) {
			AgentVariable av = dcop.getVariableAgents()[i];
			if (av instanceof DSA_B_SY) {
				DSA_B_SY a = (DSA_B_SY) av;
				ans = ans + a.getStringForDebug();
			} else {
				System.out.println("should not use printForDebugDSA_SY");
				throw new RuntimeException();
			}
		}
		System.out.println(ans);
	}

	private void agentsReactToMsgs(int iteration) {

		for (Agent agent : dcop.getAllAgents()) {

			if (iteration == 0) {
				//agent.resetAgent();
			//	agent.initialize(); // abstract method in agents
			} else {
				agent.reactionToAlgorithmicMsgs();
				agentsCommunicateThierAction(agent);	
			}
			debugMethodsAfterComputationPerAgent(iteration,agent);
		}
		sendAnytimeMsgs();
		debugMethodsAfterComputations(iteration);
	}

	private void debugMethodsAfterComputationPerAgent(int iteration, Agent agent) {
		if (MainSimulator.isAMDLSDistributedDebug && iteration == 4000) {
			((AMDLS_V1) agent).printAMDLSstatus();
		}
		
	}

	private void sendAnytimeMsgs() {
		if (MainSimulator.isAnytime) {
			for (AgentVariable a : dcop.getVariableAgents()) {
				if (a instanceof AgentVariableSearch) {
					((AgentVariableSearch) a).sendAnytimeMsgs();
				}
			}
		}
		
	}

	private void agentsCommunicateThierAction(Agent agent) {
		if (agent.getDidComputeInThisIteration()) {
			agent.sendMsgs();
			agent.changeRecieveFlagsToFalse();
		}
	}

	private void debugMethodsAfterComputations(int iteration) {
		if (MainSimulator.isAMDLSDistributedDebug && iteration == 4000) {
			System.out.println();
		}
		

	}

	private boolean didAgentRecieveAnytimeMsgInThisIteration(Agent agent) {
		if (this.recieversAnyTimeMsgs.containsKey(agent.getNodeId())) {
			return true;
		}
		return false;
	}

	private boolean didAgentRecieveAlgorithmicMsgInThisIteration(Agent agent) {
		if (this.recieversAlgortihmicMsgs.containsKey(agent.getNodeId())) {
			return true;
		}

		return false;
	}

	public synchronized List<Msg> handleDelay() {
		Collections.sort(this.messageBox, new CompMsgByDelay());
		List<Msg> msgToSend = new ArrayList<Msg>();
		Iterator it = this.messageBox.iterator();

		while (it.hasNext()) {
			Msg msg = (Msg) it.next();
			if (msg.getDelayOfMsg() <= 0) {
				msgToSend.add(msg);
				it.remove();
			} else {
				msg.setDelayOfMsg(msg.getDelayOfMsg() - 1);
			}
		}
		return msgToSend;
	}

	@Override
	public void setMailerName() {
		Mailer.mailerName = "Iteration";

	}

	@Override
	protected void placeMsgsFromInboxInMessageBox(List<Msg> msgsFromInbox) {
		for (Msg m : msgsFromInbox) {
			boolean flag = false;
			if (m.isWithDelay()) {
				int d = createDelay(m instanceof MsgAlgorithm);
				if (d == -1) {
					flag = true;
				}
				m.setDelayOfMsg(d);
			}
			if (!flag) {
				changeMsgsCounter(m);
				this.messageBox.add(m);
			}
		}
		
	}

}
