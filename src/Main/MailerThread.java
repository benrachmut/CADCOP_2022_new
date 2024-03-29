package Main;

import java.util.*;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentFunction;
import AgentsAbstract.NodeId;
import Delays.ProtocolDelayMatrix;
import Delays.ProtocolDelayMessageAmount;
import Delays.ProtocolDelayWithK;
import Messages.*;
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

			while (inbox.isEmpty() ) {
				//System.out.println("2");
				//sleepForLittle();
				sleepForLittle();

				if ( areAllIdle() && inbox.isEmpty() && !this.messageBox.isEmpty() ) {
						sleepForLittle();
					if (  areAllIdle() &&  inbox.isEmpty() && !this.messageBox.isEmpty()) {
						sleepForLittle();

						shouldUpdateClockBecuaseNoMsgsRecieved();
						msgToSend = this.handleDelay();
						agentsRecieveMsgs(msgToSend);
						//sleepForLittle();
						//System.out.println("1");
					}
					}

			}



			if (MainSimulator.isThreadDebug) {
			System.out.println("mailer goes to sleep");
		}

			//sleepForLittle();

		msgsFromInbox = inbox.extract();
		placeMsgsFromInboxInMessageBox(msgsFromInbox);


		if (MainSimulator.isThreadDebug) {
			System.out.println("mailer wakes up");

		}
			//sleepForLittle();

		msgToSend = this.handleDelay();
		if (MainSimulator.isThreadDebug) {
			System.out.println("mailer handleDelay");
			System.out.println("msgToSend:" + msgToSend);
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

	private Msg hasTimerIsOverMsgIn(List<Msg> msgToSend) {
		for (Msg m:msgToSend) {
			if (m instanceof MsgDALOSelfTimer){
				return m;
			}
		}
	return null;
	}

	private void sleepForLittle() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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


			if (m instanceof MsgDALOSpamToCloseTimer){
				lookForTheTimerMsg(m);
			}


			else if (m.isWithDelay()) {

				if(m instanceof MsgDALOSelfTimer) {
				lookForTheTimerMsg(m);
				}
				int d=-1;
				if (this.protocol.getDelay() instanceof ProtocolDelayMatrix) {
					int[] indexes = getSenderAndRecieverId1(m);
					d =	createDelay(isMsgAlgorithm,indexes[0],indexes[1],isLoss);
				}
				else if(this.protocol.getDelay() instanceof ProtocolDelayMessageAmount){

					if (m.getManualDelay()==0) {
						d = createDelay(isMsgAlgorithm, this.messageBox.size(), isLoss);
					}else{
						d  = m.getManualDelay();
					}
					//System.out.println(d);
				}
				else {
					if (m.getManualDelay()==0) {
						d = createDelay(isMsgAlgorithm, isLoss);
					}else{
					d  = m.getManualDelay();
				}
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

	private void lookForTheTimerMsg(Msg msgInput) {
		Set<Msg> toRemove = new HashSet<Msg>();
		for (Msg m :this.messageBox) {
			if (m instanceof MsgDALOSelfTimer && m.getSenderId().equals(msgInput.getSenderId())){
				toRemove.add(m);
			}
		}
		this.messageBox.removeAll(toRemove);

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
