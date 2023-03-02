package Main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentFunction;
import AgentsAbstract.NodeId;
import Delays.ProtocolDelayMatrix;
import Delays.ProtocolDelayMessageAmount;
import Delays.ProtocolDelayWithK;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgsAgentTimeComparator;
import Problem.Dcop;

public class MailerThread extends Mailer implements Runnable {
	private long time;
	private Collection<Thread> agentsThreads;
	// private boolean clockUpdatedFromMsgPlacedInBoxFlag;

	public MailerThread(Protocol protocol, long terminationTime, Dcop dcop, int dcopId) {
		super(protocol, terminationTime, dcop, dcopId);
		time = 0;
		this.agentsThreads = new HashSet<Thread>();

	}

	@Override
	public void execute() {
		System.out.println("wont be used");
	}

	@Override
	public void run() {
		/*
		if (MainSimulator.isAMDLSDistributedDebug) {
			for (Agent a : dcop.getAllAgents()) {
				System.out.println((AMDLS_V1)a+" neighbors: "+((AMDLS_V1)a).getNeigborSetId());
			}
		}
		*/
		
		createData(this.time);
	
		List<Msg> msgsFromInbox = inbox.extract();
		
		if (MainSimulator.isThreadDebug) {
			System.out.println("mailer msgs extract from inbox: " + msgsFromInbox);
		}
		
		placeMsgsFromInboxInMessageBox(msgsFromInbox);
		
		shouldUpdateClockBecuaseNoMsgsRecieved();
		
		List<Msg> msgToSend = this.handleDelay();
		agentsRecieveMsgs(msgToSend);
		msgsFromInbox = new ArrayList<Msg>();
		
		while (this.time < this.terminationTime) {
			
			
			createData(this.time);

			while (inbox.isEmpty()) {
				if (areAllIdle() && inbox.isEmpty() && !this.messageBox.isEmpty()) {
					shouldUpdateClockBecuaseNoMsgsRecieved();
					msgToSend = this.handleDelay();
					agentsRecieveMsgs(msgToSend);

				}
			}
		
			

		if (MainSimulator.isThreadDebug) {
			System.out.println("mailer goes to sleep");
		}

		msgsFromInbox = inbox.extract();
		placeMsgsFromInboxInMessageBox(msgsFromInbox);
		
		
		if (MainSimulator.isThreadDebug) {
			System.out.println("mailer wakes up");

		}

		msgToSend = this.handleDelay();
		boolean flag = false;

		
		
		if (MainSimulator.isThreadDebug) {
			System.out.println("mailer handleDelay");
			System.out.println("msgToSend:"+msgToSend);
		}
		
		
		if (this.protocol.getDelay().getGamma()>0 && !(this.protocol.getDelay() instanceof ProtocolDelayWithK)) {
			if (msgToSend.isEmpty() && areAllIdle()) {
				this.time = this.terminationTime-1;
				createData(this.time);
				this.time = this.terminationTime;

			}
		}
		
		
		
		
		agentsRecieveMsgs(msgToSend);
		if (MainSimulator.isThreadDebug) {
			System.out.println("mailer agentsRecieveMsgs");
		}
		

	}

	killAgents();

	}

	/**
	 * give msg delay and treat mailer clock
	 * 
	 * @param msgsFromInbox
	 */
	protected void placeMsgsFromInboxInMessageBox(List<Msg> msgsFromInbox) {
		this.algorithmMsgsCounterDeliver = this.algorithmMsgsCounterDeliver+ msgsFromInbox.size();
		for (Msg m : msgsFromInbox) {
			boolean flag = false;
			updateMailerClockUponMsgRecieved(m);
			boolean isMsgAlgorithm = m instanceof MsgAlgorithm;
			boolean isLoss = m.getIsLoss();
			if (m.isWithDelay()) {
				int d=-1;
				if (this.protocol.getDelay() instanceof ProtocolDelayMatrix) {
					int[] indexes = getSenderAndRecieverId1(m);
					d =	createDelay(isMsgAlgorithm,indexes[0],indexes[1],isLoss);
				}
				else if(this.protocol.getDelay() instanceof ProtocolDelayMessageAmount){
					d = createDelay(isMsgAlgorithm,this.messageBox.size(),isLoss);
					//System.out.println(d);
				}
				else {
				 d = createDelay(isMsgAlgorithm,isLoss);
				}
				if (d == -1) {
					flag = true;
				}
				
				m.setTimeOfMsg(d);
			}
			if (!flag) {
				changeMsgsCounter(m);
				this.messageBox.add(m);
			}
		}
	}

	
	

	private int[] getSenderAndRecieverId1(Msg m) {
		NodeId senderNodeId = m.getSenderId();
		int i = getProparIndex(senderNodeId);
		NodeId recieverNodeId = m.getRecieverId();
		int j = getProparIndex(recieverNodeId);

		int[]ans = {i,j};
		return ans;
	}

	private int getProparIndex(NodeId nodeId) {
		if (nodeId.getId2()!=0) {
			nodeId = getAgentVariableHoldingNodeId(nodeId);
		}
		if (nodeId.isPlusOne()) {
			return nodeId.getId1()-1;
		}
		return nodeId.getId1();
	}

	private NodeId getAgentVariableHoldingNodeId(NodeId functionNodeId) {
		AgentFunction af = dcop.getFunctionNodes(functionNodeId);
		
		return af.getAgentVariableInference().getNodeId();
	}

	private boolean mailerHasMsgsToSend() {
		Msg minTimeMsg = Collections.min(messageBox, new MsgsAgentTimeComparator());
		long minTime = minTimeMsg.getTimeOfMsg();

		if (minTime <= this.time) {
			return true;
		}
		return false;
	}

	private boolean areAllIdle() {
		for (Agent a : dcop.getAllAgents()) {
			if (!a.getIsIdle()) {
				return false;
			}
		}
		return true;
	}

	protected void updateMailerClockUponMsgRecieved(Msg msg) {
		// for (Msg msg : msgToSend) {
		long timeMsg = msg.getTimeOfMsg();
		if (this.time <= timeMsg) {
			this.time = timeMsg;
		}
		// }

	}

	private void shouldUpdateClockBecuaseNoMsgsRecieved() {

		Msg<?> minTimeMsg = Collections.min(messageBox, new MsgsAgentTimeComparator());
		long minTime = minTimeMsg.getTimeOfMsg();
		// int oldTime = time;

		if (minTime > this.time) {
			this.time = minTime;
		}

	}
	/*
	 * @Override public void sendMsg(Msg m) { super.sendMsg(m);
	 * updateMailerClockUponMsgRecieved(m); try { int t = m.getAgentTime(); int d =
	 * m.getDelay(); int timeToSendByMailer = t + d;
	 * m.setAgentTime(timeToSendByMailer); m.setMailerTime(this.time); } catch
	 * (NullPointerException e) { m.setAgentTime(m.getAgentTime());
	 * m.setMailerTime(this.time); }
	 * 
	 * }
	 */

	private void killAgents() {
		for (UnboundedBuffer<Msg> ubb : outboxes.values()) {
			ubb.removeAllMsgs();
			ubb.insert(null);
		}

	}

	@Override
	protected List<Msg> handleDelay() {
		List<Msg> toSend = new ArrayList<Msg>();
		for (Msg msg : messageBox) {
			if (msg.getTimeOfMsg() <= this.time) {
				toSend.add(msg);
			}
		}
		this.messageBox.removeAll(toSend);
		if (MainSimulator.isThreadDebug) {
			System.out.println("mailer: "+toSend);
		}
		return toSend;
	}

	@Override
	public void setMailerName() {
		Mailer.mailerName = "Thread";
	}

}
