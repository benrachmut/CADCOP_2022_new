package AgentsAbstract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Comparators.ContextSimilarityComparator;

import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import Main.MailerIterations;
import Main.MainSimulator;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgAnyTime;
import Messages.MsgAnyTimeDown;
import Messages.MsgAnyTimeDownTopFound;
import Messages.MsgAnyTimeUp;
import Messages.MsgReceive;
import Messages.MsgValueAssignmnet;

public abstract class AgentVariableSearch extends AgentVariable {

	protected SortedMap<NodeId, MsgReceive<Integer>> neighborsValueAssignmnet; // id, variable

	// ------Anytime----
	protected NodeId anytimeFather;
	protected Set<NodeId> anytimeSons;
	private Set<NodeId> belowAnytime;
	protected List<Context> anytimeUpToSend;
	// protected Set<Context> anytimeUpToSendPast;

//	protected Set<Context> anytimeUpToSendPast;

	// protected List<Context> anytimeDownToSend;
	private Integer anytimeValueAssignmnet;
	private Context anytimeBestContext;
	private Map<Integer, List<Context>> contextInMemory;
	private Random randContext;
	private Context bestContexFound;
	private boolean hasAnytimeNews;

	private int topAnytimeCounter;

	// private Set<Context> fullContextFound;
	// private Set<Context> newFullContextFoundToSend;

	private Set<CombinedContextCollection> combinedContextCollection;

	private Object toDelete;

	public AgentVariableSearch(int dcopId, int D, int id1) {
		super(dcopId, D, id1);

		this.time = 1;
		this.nodeId = new NodeId(id1,false);

		this.neighborsValueAssignmnet = new TreeMap<NodeId, MsgReceive<Integer>>();
		anytimeUpToSend = new ArrayList<Context>();
		// anytimeUpToSendPast = new HashSet<Context>();
		// fullContextFound = new HashSet<Context>();
		// newFullContextFoundToSend = new HashSet<Context>();
		bestContexFound = null;
		anytimeValueAssignmnet = null;
		anytimeBestContext = null;
		hasAnytimeNews = false;
		contextInMemory = new TreeMap<Integer, List<Context>>();
		randContext = new Random(this.id * 10 + dcopId * 153);
		topAnytimeCounter = 0;
		if (MainSimulator.isAnytime) {
			// this.isWithTimeStamp = true;
		}
	}

	@Override
	public long getIdleTime() {
		// TODO Auto-generated method stub
		return this.idealTime;
	}
	@Override
	public void initialize() {
		this.sendValueAssignmnetMsgs();

		if (MainSimulator.isAnytime) {
			if (this.neighborsValueAssignmnet.isEmpty()) {
				this.bestContexFound = createMyContext();
				this.anytimeValueAssignmnet = this.bestContexFound.getValueAssignmentPerAgent(this.id);
			}
		}
	}

	@Override
	public void meetNeighbor(int neighborId, Integer[][] constraint) {
		super.meetNeighbor(neighborId, constraint);
		this.neighborsValueAssignmnet.put(new NodeId(neighborId,false), null);
	}

	@Override
	public void resetAgentGivenParametersV2() {
		this.neighborsValueAssignmnet = Agent
				.<NodeId, MsgReceive<Integer>>resetMapToValueNull(this.neighborsValueAssignmnet);
		anytimeValueAssignmnet = null;
		if (combinedContextCollection != null) {
			for (CombinedContextCollection ccc : combinedContextCollection) {
				ccc.restart();
			}
		}

		for (List<Context> l : contextInMemory.values()) {
			l = new ArrayList<Context>();
		}

		topAnytimeCounter = 0;
		// fullContextFound = new HashSet<Context>();
		// newFullContextFoundToSend = new HashSet<Context>();
		anytimeUpToSend = new ArrayList<Context>();
		// anytimeUpToSendPast = new HashSet<Context>();
		bestContexFound = null;
		anytimeBestContext = null;
		randContext = new Random(this.id * 10 + dcopId * 153);
		hasAnytimeNews = false;

		resetAgentGivenParametersV3();
	}

	protected abstract void resetAgentGivenParametersV3();

	public double getCostPov() {
		return getCostPerInput(this.valueAssignment);
	}

	public int getCostPerInput(int input) {
		int ans = 0;
		for (Entry<NodeId, MsgReceive<Integer>> e : this.neighborsValueAssignmnet.entrySet()) {
			if (e.getValue() != null) {
				int nValueAssignmnet = e.getValue().getContext();
				Integer[][] nConst = this.neighborsConstraint.get(e.getKey());
				try {
					ans += nConst[input][nValueAssignmnet];
				} catch (Exception e12) {
					return -1;

				}
				atomicActionCounter = atomicActionCounter + 1;
			}
		}
		return ans;
	}

	public Integer getValueAssignmentOfAnytime() {
		return this.anytimeValueAssignmnet;
	}

	protected SortedMap<Integer, Integer> getCostPerDomain() {
		SortedMap<Integer, Integer> ans = new TreeMap<Integer, Integer>();

		for (int domainCandidate : domainArray) {
			int sumCostPerAgent = this.getCostPerInput(domainCandidate);
			ans.put(domainCandidate, sumCostPerAgent);

		}
		return ans;
	}

	protected int getCandidateToChange_B() {
		SortedMap<Integer, Integer> costPerDomain = this.getCostPerDomain();
		int minCost = Collections.min(costPerDomain.values());
		try {
			Integer costOfCurrentValue = costPerDomain.get(this.valueAssignment);
			if (minCost <= costOfCurrentValue && costOfCurrentValue != 0) {
				SortedSet<Integer> alternatives = getAlternativeCandidate(minCost, costPerDomain);
				if (alternatives.isEmpty()) {
					return this.valueAssignment;
				}
				return alternatives.first();
			}
		} catch (NullPointerException e) {
			SortedSet<Integer> alternatives = getAlternativeCandidate(minCost, costPerDomain);
			if (alternatives.isEmpty()) {
				return this.valueAssignment;
			}
			return alternatives.first();
		}
		/*
		 * Integer costOfCurrentValue = costPerDomain.get(this.valueAssignment); if
		 * (costOfCurrentValue == null || minCost <= costOfCurrentValue &&
		 * costOfCurrentValue != 0) { SortedSet<Integer> alternatives =
		 * getAlternativeCandidate(minCost, costPerDomain); if (alternatives.isEmpty())
		 * { return this.valueAssignment; } return alternatives.first(); }
		 */
		return this.valueAssignment;

	}

	protected int getCandidateToChange_A() {
		SortedMap<Integer, Integer> costPerDomain = this.getCostPerDomain(); // atomic time change
		int minCost = Collections.min(costPerDomain.values());
		try {
			Integer costOfCurrentValue = costPerDomain.get(this.valueAssignment);
			if (minCost < costOfCurrentValue) {
				SortedSet<Integer> alternatives = getAlternativeCandidate(minCost, costPerDomain);
				if (alternatives.isEmpty()) {
					return this.valueAssignment;
				}
				return alternatives.first();
			}
		} catch (NullPointerException e) {
			SortedSet<Integer> alternatives = getAlternativeCandidate(minCost, costPerDomain);
			if (alternatives.isEmpty()) {
				return this.valueAssignment;
			}
			return alternatives.first();
		}

		return this.valueAssignment;

	}

	protected int getCandidateToChange_C() {
		SortedMap<Integer, Integer> costPerDomain = this.getCostPerDomain();
		int minCost = Collections.min(costPerDomain.values());
		try {
			Integer costOfCurrentValue = costPerDomain.get(this.valueAssignment);
			if (minCost <= costOfCurrentValue) {
				SortedSet<Integer> alternatives = getAlternativeCandidate(minCost, costPerDomain);
				if (alternatives.isEmpty()) {
					return this.valueAssignment;
				}
				return alternatives.first();
			}
		} catch (NullPointerException e) {
			SortedSet<Integer> alternatives = getAlternativeCandidate(minCost, costPerDomain);
			if (alternatives.isEmpty()) {
				return this.valueAssignment;
			}
			return alternatives.first();
		}
		/*
		 * Integer costOfCurrentValue = costPerDomain.get(this.valueAssignment); if
		 * (costOfCurrentValue == null || minCost <= costOfCurrentValue) {
		 * SortedSet<Integer> alternatives = getAlternativeCandidate(minCost,
		 * costPerDomain); if (alternatives.isEmpty()) { return this.valueAssignment; }
		 * return alternatives.first(); }
		 */
		return this.valueAssignment;

	}

	private SortedSet<Integer> getAlternativeCandidate(int minCost, SortedMap<Integer, Integer> costPerDomain) {
		SortedSet<Integer> ans = new TreeSet<Integer>();
		for (Entry<Integer, Integer> e : costPerDomain.entrySet()) {
			int cost = e.getValue();
			int valueAssignmnet = e.getKey();
			if (cost == minCost) {
				ans.add(valueAssignmnet);
			}
		}
		if (ans.contains(this.valueAssignment)) {
			ans.remove(this.valueAssignment);
		}

		return ans;
	}

	protected void updateMsgInContextValueAssignmnet(MsgAlgorithm msgAlgorithm) {
		Integer context = (Integer) msgAlgorithm.getContext();
		int timestamp = msgAlgorithm.getTimeStamp();
		MsgReceive<Integer> msgReceive = new MsgReceive<Integer>(context, timestamp);
		this.neighborsValueAssignmnet.put(msgAlgorithm.getSenderId(), msgReceive);
	}

	protected int getTimestampOfValueAssignmnets(MsgAlgorithm msgAlgorithm) {
		NodeId senderNodeId = msgAlgorithm.getSenderId();
		MsgReceive<Integer> msgReceive = this.neighborsValueAssignmnet.get(senderNodeId);
		if (msgReceive == null) {
			return -1;
		}
		return msgReceive.getTimestamp();
	}

	protected void sendValueAssignmnetMsgs() {
		List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
		for (NodeId recieverNodeId : neighborsConstraint.keySet()) {
			MsgValueAssignmnet mva = new MsgValueAssignmnet(this.nodeId, recieverNodeId, this.valueAssignment,
					this.timeStampCounter, this.time);
			msgsToInsertMsgBox.add(mva);
		}

		outbox.insert(msgsToInsertMsgBox);
		if (MainSimulator.isThreadDebug) {
			System.out.println(this + " send msg value");
		}

	}

	@Override
	public boolean reactionToAlgorithmicMsgs() {
		Context context_j = createMyContext();
		boolean isValueAssignmnetChange = super.reactionToAlgorithmicMsgs();
		if (MainSimulator.isAnytime) {
			if (isValueAssignmnetChange || context_j == null) {
				Context context_i = createMyContext();
				if (context_i != null) {
					if (this.anytimeSons.size() == 0) {
						// if (anytimeUpToSendPast.add(context_i)) {
						anytimeUpToSend.add(context_i);
						// }
					} else {
						placeContextInMemory(context_i, this.id);
					}
				}
			}

		}
		return isValueAssignmnetChange;

	}

	public void receiveAlgorithmicMsgs(List<? extends MsgAlgorithm> messages) {
		Context context_i_beforeMsgUpdate = null;

		if (MainSimulator.isAnytime) {
			if (!messages.isEmpty()) {
				context_i_beforeMsgUpdate = createMyContext();
			}
		}

		super.receiveAlgorithmicMsgs(messages);

		if (MainSimulator.isAnytime) {
			if (!messages.isEmpty()) {
				Context context_i_AfterMsgUpdate = null;
				context_i_AfterMsgUpdate = createMyContext();
				if (context_i_AfterMsgUpdate != null) {

					boolean firstCond = (context_i_AfterMsgUpdate != null && context_i_beforeMsgUpdate == null);

					if (firstCond || !context_i_beforeMsgUpdate.isSameValueAssignmnets(context_i_AfterMsgUpdate)) {
						if (this.anytimeSons.size() == 0) {
							// if (anytimeUpToSendPast.add(context_i_AfterMsgUpdate)) {
							anytimeUpToSend.add(context_i_AfterMsgUpdate);
							// }

						} else {
							placeContextInMemory(context_i_AfterMsgUpdate, this.id);
						}

					}
				}

			}
		}

	}

	public Context createMyContext() {

		TreeMap<Integer, Integer> m = new TreeMap<Integer, Integer>();
		try {
			for (Entry<NodeId, MsgReceive<Integer>> e : this.neighborsValueAssignmnet.entrySet()) {
				m.put(e.getKey().getId1(), e.getValue().getContext());
			}

			int myCost = this.getCostPerDomain().get(this.valueAssignment);

			return new Context(m, this.id, this.valueAssignment, myCost);
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	protected void handleMsgs(List<Msg> messages) {
		List<MsgAnyTime> anytimeMsgs = extractAnytimeMsgs(messages);
		if (!anytimeMsgs.isEmpty()) {
			recieveAnyTimeMsgs(anytimeMsgs);
		}
		List<MsgAlgorithm> algorithmicMsgs = extractAlgorithmicMsgs(messages);
		if (!algorithmicMsgs.isEmpty()) {
			receiveAlgorithmicMsgs(algorithmicMsgs);
			reactionToAlgorithmicMsgs();
		}
		sendAnytimeMsgs();
	}

	protected List<MsgAnyTime> extractAnytimeMsgs(List<Msg> messages) {
		List<MsgAnyTime> ans = new ArrayList<MsgAnyTime>();
		for (Msg msg : messages) {
			if (msg instanceof MsgAnyTime) {
				ans.add((MsgAnyTime) msg);
			}
		}
		return ans;
	}
	/*
	 * @Override public void run() {
	 * 
	 * while (true) {
	 * 
	 * isIdle = true; List<Msg> messages = this.inbox.extract(); isIdle = false;
	 * 
	 * if (messages == null) { break; } List<MsgAlgorithm> algorithmicMsgs =
	 * extractAlgorithmicMsgs(messages); receiveAlgorithmicMsgs(algorithmicMsgs); if
	 * (MainSimulator.isAnytime) { List<MsgAnyTime> anytimeMsgs = extractAnytimeMsgs
	 * (messages); recieveAnyTimeMsgs(anytimeMsgs); } reactionToAlgorithmicMsgs();
	 * if (MainSimulator.isAnytime) { sendAnytimeMsgs(); }
	 * 
	 * } if (MainSimulator.isThreadDebug) { System.err.println(this+" is dead"); } }
	 */

	public void recieveAnyTimeMsgs(List<? extends MsgAnyTime> messages) {

		for (MsgAnyTime msgAnyTime : messages) {
			if (msgAnyTime instanceof MsgAnyTimeUp) {
				placeContextInMemory((Context) msgAnyTime.getContext(), msgAnyTime.getSenderId().getId1());
			}

			if (msgAnyTime instanceof MsgAnyTimeDown) {
				MsgAnyTimeDown down = (MsgAnyTimeDown) msgAnyTime;
				Context c = (Context) down.getContext();
				if (this.bestContexFound == null) {
					this.bestContexFound = c;
					this.anytimeValueAssignmnet = c.getValueAssignmentPerAgent(this.id);
					this.hasAnytimeNews = true;
				} else {
					int costOfContextRecived = c.getTotalCost();
					int costOfBestContextFound = bestContexFound.getTotalCost();
					if (costOfContextRecived < costOfBestContextFound) {
						bestContexFound = c;
						this.anytimeValueAssignmnet = c.getValueAssignmentPerAgent(this.id);
						this.hasAnytimeNews = true;
					}
				}
			}

			if (msgAnyTime instanceof MsgAnyTimeDownTopFound) {
				Context contextFromMsg = (Context) msgAnyTime.getContext();
				// this.fullContextFound.add(contextFromMsg);
				Map<Integer, List<Context>> toDelete = new HashMap<Integer, List<Context>>();
				for (Entry<Integer, List<Context>> e : contextInMemory.entrySet()) {
					List<Context> l = new ArrayList<Context>();
					for (Context c : e.getValue()) {
						// increaseTime(c, contextFromMsg);
						if (c.isConsistentWith(contextFromMsg)) {
							if (!toDelete.containsKey(e.getKey())) {
								toDelete.put(e.getKey(), new ArrayList<Context>());
							}
							toDelete.get(e.getKey()).add(contextFromMsg);
						}
					}
				}

				for (Entry<Integer, List<Context>> e : contextInMemory.entrySet()) {
					e.getValue().removeAll(toDelete.get(e.getKey()));
				}
			}

		}

		updateAgentTime(messages);

	}

	private void placeContextInMemory(Context input, Integer creator) {
		if (contextHasNulls()) {
			return;
		}
		if (creator != this.id) { // it is not an c_i context
			tryToCombineFromAnytimeMsgUp(input, creator);

		} else {
			for (Entry<Integer, List<Context>> e : this.contextInMemory.entrySet()) {
				if (e.getKey() != creator) {
					for (Context context : e.getValue()) {
						attemptToCombineAndHandleAfterICreated(context, input, e.getKey());
					}
				}
			}
		}

		if (!isContextInMemory(creator, input)) {
			this.contextInMemory.get(creator).add(input);
		}

		removeGivenHeuristic(creator, MainSimulator.anytimeMemoryLimitedSize);
	}

	private void attemptToCombineAndHandleAfterICreated(Context context, Context input, int iCreatedWith) {

		Set<Context> ans = new HashSet<Context>();
		increaseTime(context, input);
		Context combined = context.combineWith(input);
		if (combined != null && isValidWithBest(combined)) {

			if (this.anytimeSons.size() == 1) {
				combinedAndNoSons(input, combined);
			} else {
				for (Integer idOther : contextInMemory.keySet()) {
					if (idOther != iCreatedWith && idOther != id) {
						for (Context c : contextInMemory.get(idOther)) {
							increaseTime(combined, c);
							Context combinedOther1 = c.combineWith(combined);
							if (combinedOther1 != null) {
								if (this.anytimeSons.size() == 2) {
									anytimeUpToSend.add(combinedOther1);
								}
								if (this.anytimeSons.size() > 2) {
									throw new RuntimeException("more then 3");
								}
							}
						}
					}
				}
			}
		}

	}

	private void tryToCombineFromAnytimeMsgUp(Context input, int creator) {
		for (Context context : this.contextInMemory.get(this.id)) {
			increaseTime(context, input);
			Context combined = context.combineWith(input);
			if (combined != null) {
				if (isValidWithBest(combined)) {
					if (this.anytimeSons.size() == 1) {
						combinedAndNoSons(input, combined);
					} else {
						saveWithReleventCombined(combined, creator);
					}
				}
			}
		}
	}

	private boolean isValidWithBest(Context combined) {
		if (this.bestContexFound == null) {
			return true;
		} else {
			if (combined.getTotalCost() < this.bestContexFound.getTotalCost()) {
				return true;
			} else {
				return false;
			}
		}
	}

	private void saveWithReleventCombined(Context combined, int creator) {
		for (CombinedContextCollection ccc : this.combinedContextCollection) {
			if (ccc.isIdInCCC(creator)) {
				ccc.addContext(combined);
			}
			for (Context contextToCombine : ccc.getContexts()) {
				Context anotherCombined = contextToCombine.combineWith(combined);
				increaseTime(contextToCombine, combined);
				if (anotherCombined != null && isValidWithBest(combined)) {
					Set<Integer> ids = ccc.getIds();
					ids.add(creator);
					if (this.isAnytimeTop()) {
						handleContextAdditionForTreeTop(anotherCombined);
					} else {
						// if (anytimeUpToSendPast.add(anotherCombined)) {
						anytimeUpToSend.add(anotherCombined);
						// }
					}
				}
			}
		}
	}

	private boolean isContextInMemory(Integer creator, Context input) {
		for (Context cInMemory : this.contextInMemory.get(creator)) {
			if (cInMemory.isSameValueAssignmnets(input) && cInMemory.sameCosts(input)) {
				return true;
			}
		}
		return false;
	}

	private void combinedAndNoSons(Context input, Context combined) {
		if (!this.isAnytimeTop()) {
			// if (anytimeUpToSendPast.add(combined)) {
			anytimeUpToSend.add(combined);
			// }
		} else {
			handleContextAdditionForTreeTop(combined);
		}
	}

	private boolean lessNullsInContext(Context context, int i) {
		int numOfNulls = context.numberOfNulls();

		return numOfNulls < i;
	}

	private void handleContextAdditionForTreeTop(Context context) {

		int costOfCandidateContext = context.getTotalCost();

		if (bestContexFound == null) {
			System.out.println(MailerIterations.m_iteration + ": bestContexFound: " + costOfCandidateContext);
			bestContexFound = context;
			this.topAnytimeCounter = this.topAnytimeCounter + 1;
			this.anytimeValueAssignmnet = context.getValueAssignmentPerAgent(this.id);
			this.hasAnytimeNews = true;
		} else {
			int costOfBestContext = this.bestContexFound.getTotalCost();
			this.topAnytimeCounter = this.topAnytimeCounter + 1;
			if (MainSimulator.isAnytimeDebug) {
				System.out.println(MailerIterations.m_iteration + ": costOfBestContext: " + costOfBestContext
						+ ", costOfCandidateContext: " + costOfCandidateContext);
			}

			if (costOfBestContext > costOfCandidateContext) {
				bestContexFound = context;
				this.hasAnytimeNews = true;
				this.anytimeValueAssignmnet = context.getValueAssignmentPerAgent(this.id);
			}

		}
	}

	public boolean isAnytimeTop() {
		return this.anytimeFather == null;
	}

	private boolean contextIncludesCostOfAllBelowAndMe(Context context) {
		boolean isIIncluded = context.getCost(this.id) != null;
		if (!isIIncluded) {
			return false;
		}
		return isAllBelowMeIncluded(context);
	}

	private boolean isAllBelowMeIncluded(Context context) {
		for (NodeId nodeId : this.belowAnytime) {
			if (context.getCost(nodeId.getId1()) == null) {
				return false;
			}
		}
		return true;
	}

	private void removeGivenHeuristic(Integer creator, int anytimeMemoryLimitedSize) {
		int memoryHeurstic = MainSimulator.anytimeMemoryHuerstic;

		if (memoryHeurstic != 1 && getMemorySize() > anytimeMemoryLimitedSize) {

			// 1 = no memoryLimit, 2=MSC, 3=Fifo, 4=Random
			Context c = null;
			List<Context> creatorMemory = this.contextInMemory.get(creator);
			if (memoryHeurstic == 2) {
				creatorMemory.remove(Collections.min(this.contextInMemory.get(creator),
						new ContextSimilarityComparator(this.createMyContext())));
			}
			if (memoryHeurstic == 3) {
				c = creatorMemory.remove(0);
			}
			if (memoryHeurstic == 4) {
				int rndIndex = randContext.nextInt(creatorMemory.size());
				creatorMemory.remove(rndIndex);
			}
		}
	}

	private int getMemorySize() {
		int ans = 0;
		for (List<Context> l : this.contextInMemory.values()) {
			ans = ans + l.size();
		}
		return ans;
	}

	/*
	 * private void getConextToAdd(Context input, Set<Context> toAdd, Integer
	 * creator) { Set<Context> toDelete = new HashSet<Context>(); for
	 * (Entry<Integer, List<Context>> e : this.contextInMemory.entrySet()) { if
	 * (e.getKey() != creator) { for (Context context : e.getValue()) {
	 * increaseTime(context, input); Context combined = context.combineWith(input);
	 * if (combined != null) { if (!isConsistentWithTop(context)) { if
	 * (!isContextInCollection(combined, contextInMemory, this.id)) {
	 * toAdd.add(combined); } } } } } }
	 * 
	 * Set<Context> setAll = new HashSet<Context>(this.contextInMemory); boolean
	 * isAdded = setAll.add(input); if (isAdded) { toAdd.add(input); } /* if
	 * (MainSimulator.deleteAfterCombine) {
	 * this.contextInMemory.removeAll(toDelete); }
	 */
	// }

	private void increaseTime(Context context, Context input) {
		if (MainSimulator.isAtomicTime) {
			this.time = this.time + context.getContextSize() * input.getContextSize() / MainSimulator.div;

		} else {
			this.time = this.time + 1;
		}

	}
	/*
	 * private boolean isConsistentWithTop(Context context) { if (bestContexFound ==
	 * null) { return false; } //else { /* for (Context c : fullContextFound) { if
	 * (c.isConsistentWith(context)) { return true; } } if
	 * (context.isConsistentWith(bestContexFound)) { return true; } return false; }
	 */
	// }

	private boolean contextHasNulls() {
		for (MsgReceive<Integer> m : neighborsValueAssignmnet.values()) {
			if (m == null) {
				return true;
			}
		}
		return false;
	}

	private Context selectContextAndToRemove(int limitedMemorySize, List<Context> cToAddTo) {

		int anytimeMemoryHuerstic = MainSimulator.anytimeMemoryHuerstic;
		// 1 = no memoryLimit, 2=MSC, 3=Fifo, 4=Random
		Context c = null;
		if (anytimeMemoryHuerstic == 2) {
			c = Collections.min(cToAddTo, new ContextSimilarityComparator(this.createMyContext()));
		}
		if (anytimeMemoryHuerstic == 3) {
			c = cToAddTo.get(0);
		}
		if (anytimeMemoryHuerstic == 4) {
			int rndIndex = randContext.nextInt(cToAddTo.size());
			c = cToAddTo.get(rndIndex);
		}
		cToAddTo.remove(c);
		return c;

	}

	public void sendAnytimeMsgs() {
		List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
		for (Context c : anytimeUpToSend) {
			Msg m = new MsgAnyTimeUp(this.nodeId, this.anytimeFather, c, this.timeStampCounter, this.time);
			msgsToInsertMsgBox.add(m);
		}

		this.anytimeUpToSend = new ArrayList<Context>();

		if (hasAnytimeNews) {
			for (NodeId son : this.anytimeSons) {
				Msg m = new MsgAnyTimeDown(this.nodeId, son, this.bestContexFound, this.timeStampCounter, this.time);
				msgsToInsertMsgBox.add(m);
			}
		}

		outbox.insert(msgsToInsertMsgBox);

		hasAnytimeNews = false;

	}

	private Set<CombinedContextCollection> createCombinedContextCollections(List<NodeId> forCombinations) {
		Set<CombinedContextCollection> ans = new HashSet<CombinedContextCollection>();
		ArrayList<boolean[]> combsBoll = getBoolArr(forCombinations.size());
		for (boolean[] bs : combsBoll) {
			Set<Integer> ids = new HashSet<Integer>();
			for (int i = 0; i < bs.length; i++) {
				if (bs[i]) {
					ids.add(forCombinations.get(i).getId1());
				}
			}
			if (ids.size() != forCombinations.size() && !ids.isEmpty()) {
				ans.add(new CombinedContextCollection(ids));
			}
		}
		return ans;
	}

	static public ArrayList<boolean[]> getBoolArr(int length) {
		int numOptions = 1 << length;
		ArrayList<boolean[]> finalArray = new ArrayList<boolean[]>();
		for (int o = 0; o < numOptions; o++) {
			boolean[] newArr = new boolean[length];
			for (int l = 0; l < length; l++) {
				int val = (1 << l) & o;
				newArr[l] = val > 0;
			}
			finalArray.add(newArr);
		}
		return finalArray;
	}

	public Double getCostOfBestContext() {
		if (bestContexFound == null) {
			return null;
		} else {
			double ans = this.bestContexFound.getTotalCost();
			return ans;
		}
	}

	public int getCounterOfContext() {
		return this.topAnytimeCounter;
	}

	public NodeId getAnytimeFather() {
		// TODO Auto-generated method stub
		return this.anytimeFather;
	}

	public Set<NodeId> getAnytimeSons() {
		return this.anytimeSons;
	}

	public Set<NodeId> getBelowAnytime() {
		// TODO Auto-generated method stub
		return this.belowAnytime;
	}

	public int getAnytimeUpToSendSize() {
		// TODO Auto-generated method stub
		return this.anytimeUpToSend.size();
	}

	/*
	 * @Override protected synchronized void waitUntilMsgsRecieved() { if
	 * (getDidComputeInThisIteration() == false) { waitingMethodology(); if
	 * (stopThreadCondition == true) { return; } }
	 * 
	 * if (this.getDidComputeInThisIteration()) { this.reactionToAlgorithmicMsgs();
	 * this.sendMsgs(); this.changeRecieveFlagsToFalse(); } if
	 * (MainSimulator.isAnytime) { this.sendAnytimeMsgs(); } }
	 * 
	 */
	public void setAnytimeFather(NodeId father) {
		this.anytimeFather = father;
	}

	public void setAnytimeSons(Set<NodeId> sons) {
		this.anytimeSons = sons;
		if (!this.anytimeSons.isEmpty()) {
			Iterator<NodeId> it = anytimeSons.iterator();
			this.contextInMemory.put(it.next().getId1(), new ArrayList<Context>());
			this.contextInMemory.put(this.id, new ArrayList<Context>());
		}

	}

	public void setAnytimeBelow(Set<NodeId> below) {
		this.belowAnytime = below;
	}

	public void turnDFStoAnytimeStructure(Set<NodeId> below) {
		this.belowAnytime = below;
		this.anytimeSons = dfsSons;
		if (dfsSons.size() > 1) {
			List<NodeId> forCombinations = new ArrayList<NodeId>();
			for (NodeId nodeId : dfsSons) {
				forCombinations.add(nodeId);
			}
			this.combinedContextCollection = createCombinedContextCollections(forCombinations);
			for (NodeId nodeId : dfsSons) {
				this.contextInMemory.put(nodeId.getId1(), new ArrayList<Context>());
				this.contextInMemory.put(this.id, new ArrayList<Context>());
			}
		} else {
			if (dfsSons.size() == 1) {
				Iterator<NodeId> it = dfsSons.iterator();
				this.contextInMemory.put(it.next().getId1(), new ArrayList<Context>());
				this.contextInMemory.put(this.id, new ArrayList<Context>());
			}
			this.combinedContextCollection = null;
		}
		this.anytimeFather = dfsFather;
	}

	public Set<NodeId> getDFSSons() {
		return dfsSons;

	}

	/**
	 * used by MGM
	 * 
	 * @param candidate
	 * @return
	 */
	protected int findLr(int candidate) {
		SortedMap<Integer, Integer> costPerDomain = this.getCostPerDomain();
		int costOfCandidate = costPerDomain.get(candidate);
		int costOfCurrentValueAssignment = costPerDomain.get(this.valueAssignment);
		return costOfCurrentValueAssignment - costOfCandidate;
	}

}