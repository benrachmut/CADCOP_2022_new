package Main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Comparators.CompAgentVariableByNeighborSize;
import Data.Data;
import Delays.ProtocolDelayMatrix;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgAnyTime;
import Problem.Dcop;
import Problem.DcopCities;

public abstract class Mailer {
	protected Protocol protocol;
	protected List<Msg> messageBox;
	protected Dcop dcop;
	protected long terminationTime;
	protected SortedMap<Long, Data> dataMap;
	private Double algorithmMsgsCounterArrive;
	protected Double algorithmMsgsCounterDeliver;

	private Double anytimeMsgsCounter;
	protected Map<NodeId, List<MsgAlgorithm>> recieversAlgortihmicMsgs;
	protected Map<NodeId, List<MsgAnyTime>> recieversAnyTimeMsgs;

	public static String mailerName;
	
	
	protected UnboundedBuffer<Msg> inbox;
	protected Map<NodeId,UnboundedBuffer<Msg>> outboxes;

	public Mailer(Protocol protocol, long terminationTime, Dcop dcop,int dcopId) {
		super();
		this.dcop = dcop;
		this.protocol = protocol;

		algorithmMsgsCounterArrive = 0.0;
		algorithmMsgsCounterDeliver = 0.0;
		this.anytimeMsgsCounter = 0.0;
		this.protocol.setSeeds(dcopId);
		this.messageBox = new ArrayList<Msg>();
		this.terminationTime = terminationTime;
		this.dataMap = new TreeMap<Long, Data>();
		this.outboxes = new HashMap <NodeId,UnboundedBuffer<Msg>> ();
		if (protocol.getDelay() instanceof ProtocolDelayMatrix) {
			double [][] matrix = null;
			if (dcop instanceof DcopCities) {
				matrix = ((DcopCities)dcop).getDistancesMatrix();	
			}
			((ProtocolDelayMatrix)protocol.getDelay()).setMatix(matrix);
		}
		setMailerName();

	}

	abstract public void setMailerName();
	abstract protected void placeMsgsFromInboxInMessageBox(List<Msg> msgsFromInbox);
	public Data getDataPerIteration(long i) {
		if (dataMap.containsKey(i)) {
			return this.dataMap.get(i);
		} else {
			if (MainSimulator.isAtomicTime) {
				TreeMap<Long, Data> limitedData = getLimitedData(i);
				Long maxInt = limitedData.lastKey();//Collections.max(limitedData.keySet());
				Data ans = limitedData.get(maxInt);
				/*
				limitedData.remove(maxInt);
				for (Integer toMoveFromData : limitedData.keySet()) {
					dataMap.remove(toMoveFromData);
				}
				*/
				return ans; 

			} else {
				while (true) {
					i = i - 1;
					if (dataMap.containsKey(i)) {
						return this.dataMap.get(i);
					}
				}
			}
		}
	}

	private TreeMap<Long, Data> getLimitedData(long i) {
		TreeMap<Long, Data> ans = new TreeMap<Long, Data>();
		for (Entry<Long, Data> e : this.dataMap.entrySet()) {
			if (e.getKey() <= i) {
				ans.put(e.getKey(),e.getValue());
			}else {
				break;
			}
		}
		return ans;
	}

	/**
	 * used by agents when creating messages
	 * 
	 * @param m
	 */
	/*
	public synchronized void sendMsg(Msg m) {

		changeMsgsCounter(m);
		int d = createDelay(m instanceof MsgAlgorithm);
		if (d != -1) {
			m.setDelay(d);
			this.messageBox.add(m);
			
			if (MainSimulator.isMaxSumThreadDebug) {
				System.err.println(m + "entered mailBox at mailer");
			}
		}

		if (MainSimulator.isCommunicationDebug) {
			System.out.println(m);
		}
		
		
	}
	*/
/*
	public void sendMsgWitoutDelay(MsgAlgorithm m) {
		m.setDelay(0);
		this.messageBox.add(m);
	}
	*/

	protected void changeMsgsCounter(Msg m) {
		if (m instanceof MsgAlgorithm) {
			this.algorithmMsgsCounterArrive++;

		}
		if (m instanceof MsgAnyTime) {
			this.anytimeMsgsCounter++;
		}
	}

	protected int createDelay(boolean isAlgorithmicMsg, boolean isLoss) {
		Double d = this.protocol.getDelay().createDelay(isAlgorithmicMsg,isLoss);
		if (d == null) {
			return -1;
		}
		double dd = d;
		int ans = (int) dd;
		return ans;
	}
	
	protected int createDelay(boolean isAlgorithmicMsg, int i, int j,boolean isLoss) {
		Double d = ((ProtocolDelayMatrix)this.protocol.getDelay()).createDelay(isAlgorithmicMsg,i,j,isLoss);
		if (MainSimulator.isDcopCityDebug&&((i == 0 && j == 31) ||(i == 31 && j == 0))) {
			System.out.println(d);
		}
		
		if (d == null) {
			return -1;
		}
		double dd = d;
		int ans = (int) dd;
		return ans;
	}

	public abstract void execute();

	protected abstract List<Msg> handleDelay();

	/**
	 * called from main each time a mailer and dcop meet prior to execution, thus
	 * the mailer updates its fields
	 * 
	 * @param dcopId
	 * @param agents
	 */
	
	public void mailerMeetsDcop(int dcopId) {
		this.messageBox = new ArrayList<Msg>();
		//this.dcop = dcop;
		boolean isWithTimeStamp = this.protocol.getDelay().isWithTimeStamp();
		this.protocol.getDelay().setSeeds(dcopId);
	}

	@Override
	public String toString() {
		return protocol.getDelay().toString() + "," + protocol.getDown();
	}

	public boolean isWithTimeStamp() {
		return this.protocol.getDelay().isWithTimeStamp();
	}

	// public abstract void execute();

	// -------------** agentsRecieveMsgs methods**-------------
	/**
	 * @param msgToSend created by handle msgs
	 */
	protected void agentsRecieveMsgs(List<Msg> msgToSend) {
		Map<NodeId, List<Msg>>MsgsByReceiverNodeId = getRecieversByNodeId(msgToSend);
		for (Entry<NodeId, List<Msg>> e : MsgsByReceiverNodeId.entrySet()) {
			NodeId receiverId = e.getKey();
			List<Msg> msgsForAgnet = e.getValue();
			UnboundedBuffer<Msg> nodeIdInbox = this.outboxes.get(receiverId);
			nodeIdInbox.insert(msgsForAgnet);

		}
		
		
		/*
		List<MsgAnyTime> msgsAnyTime = new ArrayList<MsgAnyTime>();
		List<MsgAlgorithm> msgsAlgorithm = new ArrayList<MsgAlgorithm>();
		iterateOverMsgToSend(msgToSend, msgsAnyTime, msgsAlgorithm);
		handleMsgAlgorithm(msgsAlgorithm);
		if (MainSimulator.isAnytime) {
			handleMsgAnytime(msgsAnyTime);
		}
		
		*/
	}

	/**
	 * sort messages by type, anytime and algorithm
	 * 
	 * @param msgToSend
	 * @param msgsAnyTime
	 * @param msgsAlgorithm
	 */
	private void iterateOverMsgToSend(List<Msg> msgToSend, List<MsgAnyTime> msgsAnyTime,
			List<MsgAlgorithm> msgsAlgorithm) {
		for (Msg msg : msgToSend) {
			if (msg instanceof MsgAnyTime) {
				msgsAnyTime.add((MsgAnyTime) msg);
			} else {
				msgsAlgorithm.add((MsgAlgorithm) msg);
			}
		}
	}

	// -------------** handleMsgAlgorithm methods**-------------

	/**
	 * handle messages differently if factor graph or not because of node id
	 * 
	 * @param msgsAlgorithm
	 */
	/*
	private void handleMsgAlgorithm(List<MsgAlgorithm> msgsAlgorithm) {
		this.recieversAlgortihmicMsgs = getRecieversByNodeIdAlgorithmic(msgsAlgorithm);
		for (Entry<NodeId, List<MsgAlgorithm>> e : recieversAlgortihmicMsgs.entrySet()) {
			NodeId recieverId = e.getKey();
			List<MsgAlgorithm> msgsForAnAgnet = e.getValue();
			Agent recieverAgent = getAgentByNodeId(recieverId);
			if (recieverAgent == null) {
				System.err.println("from mailer: something is wrong with finding the recieverAgent");
			}
			
			if (Main.MainSimulator.isMaxSumThreadDebug) {
				
				Collection<String> senders = new ArrayList<String>();
				for (MsgAlgorithm m : msgsForAnAgnet) {
					senders.add(m.getSenderId().toString());
				}
				System.err.println("mailer thread send message to {"+msgsForAnAgnet.get(0).getRecieverId()+"}"+" from {"+senders+"}");
			}
			
			if (Main.MainSimulator.isMaxSumThreadDebug) {
				System.err.println("");
			}
			recieverAgent.receiveAlgorithmicMsgs(msgsForAnAgnet);
		}

	}
	*/




	/**
	 * get agent from dcop given its NodeId
	 * 
	 * @param recieverId
	 * @return
	 */
	
	private Agent getAgentByNodeId(NodeId recieverId) {
		for (Agent a : dcop.getAllAgents()) {
			NodeId aId = a.getNodeId();
			if (aId == null) {
				System.err.println("from mailer: you didnt make sure that the agents are in factor graph");
			}
			if (aId.equals(recieverId)) {
				return a;
			}
		}
		return null;
	}
	

	/**
	 * create map of messages were the key is receiver NodeId and key is list of
	 * messages destine to it
	 * 
	 * @param msgsAlgorithm
	 * @return
	 */

	private Map<NodeId, List<Msg>> getRecieversByNodeId(List<Msg> msgsAlgorithm) {
		Map<NodeId, List<Msg>> ans = new HashMap<NodeId, List<Msg>>();
		for (Msg msg : msgsAlgorithm) {
			NodeId reciever = msg.getRecieverId();
			if (!ans.containsKey(reciever)) {
				ans.put(reciever, new ArrayList<Msg>());
			}
			ans.get(reciever).add(msg);
		}

		return ans;
	}

	private Map<NodeId, List<MsgAnyTime>> getRecieversByNodeIdAnyTime(List<MsgAnyTime> msgsAnyTime) {
		Map<NodeId, List<MsgAnyTime>> ans = new HashMap<NodeId, List<MsgAnyTime>>();
		for (MsgAnyTime msg : msgsAnyTime) {
			NodeId reciever = msg.getRecieverId();
			if (!ans.containsKey(reciever)) {
				ans.put(reciever, new ArrayList<MsgAnyTime>());
			}
			ans.get(reciever).add(msg);
		}

		return ans;
	}

	protected boolean isAnytimeUpToSend() {
		for (AgentVariable a : this.dcop.getVariableAgents()) {
			AgentVariableSearch as = (AgentVariableSearch) a;
			if (as.getAnytimeUpToSendSize() != 0) {
				return true;
			}
		}
		return false;
	}


	public Double getAlgorithmMsgsCounterArrive() {
		return this.algorithmMsgsCounterArrive;
	}
	public Double getAlgorithmMsgsCounterDeliver() {
		return this.algorithmMsgsCounterDeliver;
	}

	public Double getAnytimeMsgsCounter() {
		return this.anytimeMsgsCounter;
	}

	protected void createData(long i) {
		dataMap.put(i, new Data(i, this.dcop, this));
	}

	public Double getLastGlobalCost() {
		try {
			Long lastTime = dataMap.lastKey();
			Data d = dataMap.get(lastTime);
			return d.getGlobalCost();
		} catch (Exception e) {
			return 0.0;
		}
	}

	public Double getLastGlobalAnytimeCost() {
		try {
			Long lastTime = dataMap.lastKey();
			Data d = dataMap.get(lastTime);
			return d.getGlobalAnytimeCost();
		} catch (Exception e) {
			return 0.0;
		}
	}

	public Long getFirstKeyInData() {
		return this.dataMap.firstKey();
	}

	public Dcop getDcop() {
		return this.dcop;
	}


	public boolean isMaxOfItsNeighbors(AgentVariable a) {
		SortedSet<AgentVariable> agentsLocal = getSortedSetOfAgentInput(a);
		agentsLocal.add(a);
		return agentsLocal.last().getId() == a.getId();
	}

	public boolean isMinOfItsNeighbors(AgentVariable a) {
		SortedSet<AgentVariable> agentsLocal = getSortedSetOfAgentInput(a);
		agentsLocal.add(a);
		return agentsLocal.first().getId() == a.getId();
	}

	private SortedSet<AgentVariable> getSortedSetOfAgentInput(AgentVariable a) {
		SortedSet<AgentVariable> agentsLocal = new TreeSet<AgentVariable>(new CompAgentVariableByNeighborSize());

		Set<NodeId> nSet = a.getNeigborSetId();
		for (NodeId nodeId : nSet) {
			Agent aN = getAgentByNodeId(nodeId);
			if (aN instanceof AgentVariable) {
				AgentVariable agN = (AgentVariable) aN;
				agentsLocal.add(agN);
			} else {
				throw new RuntimeException("Should not use this method, only for agent variables");
			}
		}
		return agentsLocal;
	}

	public Set<NodeId> getNeighborsWithMoreNeighborsThenMe(AgentVariable a) {

		Set<NodeId> ans = new HashSet<NodeId>();

		SortedSet<AgentVariable> agentsLocal = getSortedSetOfAgentInput(a);
		// Iterator<AgentVariable> it = agentsLocal.iterator();
		CompAgentVariableByNeighborSize c = new CompAgentVariableByNeighborSize();

		for (AgentVariable n : agentsLocal) {
			int c_number = c.compare(a, n);
			if (c_number < 0) {
				ans.add(n.getNodeId());
			}
			if (c_number > 0) {
				// will stay out
			}
			if (c_number == 0) {
				throw new RuntimeException("The comparator should not have equal values");
			}
		}
		return ans;
	}

	public Set<NodeId> getNeighborsWithLessNeighborsThenMe(AgentVariable a) {
		Set<NodeId> ans = new HashSet<NodeId>();

		SortedSet<AgentVariable> agentsLocal = getSortedSetOfAgentInput(a);
		Iterator<AgentVariable> it = agentsLocal.iterator();
		Comparator c = new CompAgentVariableByNeighborSize();

		for (AgentVariable n : agentsLocal) {
			int c_number = c.compare(a, n);
			if (c_number < 0) {
				// will stay out
			}
			if (c_number > 0) {
				ans.add(n.getNodeId());
			}
			if (c_number == 0) {
				throw new RuntimeException("The comparator should not have equal values");
			}
		}
		return ans;
	}

	public Data getLastData() {
		Long lastKay = this.dataMap.lastKey();
		Data ans = this.dataMap.get(lastKay);
		return ans;
	}

	public void meetAgent(UnboundedBuffer<Msg> msgsFromMailerToSpecificAgent,
			NodeId nodeId) {
		this.outboxes.put(nodeId,msgsFromMailerToSpecificAgent);
	}

	public void setInbox(UnboundedBuffer<Msg> msgsFromAgentsToMailer) {
		this.inbox = msgsFromAgentsToMailer;
	}

	public SortedMap<Long, Data> getAllDataMap() {
		return this.dataMap;
		
	}



}