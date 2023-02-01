package Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.Context;
import AgentsAbstract.NodeId;
import AlgorithmSearch.AMDLS_V1;
import AlgorithmSearch.DSA_B_SY;
import AlgorithmSearch.MGM;
import Comparators.CompMsgByDelay;
import Messages.Msg;
import Messages.MsgAlgorithm;
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




	private void agentsReactToMsgs(int iteration) {

		for (Agent agent : dcop.getAllAgents()) {

			if (iteration == 0) {
				//agent.resetAgent();
				agent.initialize(); // abstract method in agents
			} else {
				agent.reactionToAlgorithmicMsgs();
				agentsCommunicateThierAction(agent);	
			}
			//debugMethodsAfterComputationPerAgent(iteration,agent);
		}
		//sendAnytimeMsgs();
		//debugMethodsAfterComputations(iteration);
	}



	private void agentsCommunicateThierAction(Agent agent) {
		if (agent.getDidComputeInThisIteration()) {
			agent.sendMsgs();
			agent.changeReceiveFlagsToFalse();
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
				int d = createDelay(m instanceof MsgAlgorithm,m.getIsLoss());
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
