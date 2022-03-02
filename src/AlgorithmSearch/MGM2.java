package AlgorithmSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgLR;
import Messages.MsgMgm2Phase2FriendshipInformation;
import Messages.MsgMgm2Phase3FriendshipReplay;
import Messages.MsgMgm2Phase3LR;
import Messages.MsgMgm2Phase5IsBestLR;
import Messages.MsgReceive;
import Messages.MsgValueAssignmnet;

abstract public class MGM2 extends AgentVariableSearch {
	protected HashSet<MsgAlgorithm> future;

	public static int repsOfMsgSentInPhase3Phase4 = 1;
	public static double probToBeOfferPhase1 = 0.5;

	protected boolean didEarlyPhase1Flag;
	protected boolean flagIDidPhase4Already;

	protected boolean flagValueAssignmentAlready;
	protected boolean flagRecieveNegativaReplay;
	protected boolean flagComputeRecieveValueMsgsPhase1;
	protected boolean flagComputeFriendshipInformationPhase2;
	protected boolean flagComputeOfferAndNegativeReplayPhase3;
	protected boolean flagComputeOfferAndPositiveReplayPhase3;
	protected boolean flagComputeAllLRandWithPartnerPhase4;
	protected boolean flagComputeAllLRandWithNoPartnerPhase4;
	protected boolean flagComputePartnerLRIsBestLRPhase5;

	// ##---------Phase 1---------##
	protected Map<NodeId, Boolean> phase1RecieveBooleanValueAssignmnet;
	protected Random phase1RndNeighborSelection;
	protected Random phase1RndIsOfferGiver;
	protected boolean phase1BooleanIsOfferGiver;
	protected NodeId phase1NodeIdSelectedFriend;
	protected KOptInfo phase1MyOpt2Created;
	// ##---------Phase 2---------##
	protected Map<NodeId, Boolean> phase2RecieveBooleanFriendshipOffers;
	protected Map<NodeId, MsgReceive<KOptInfo>> phase2RecieveFriendshipOffers;
	protected Set<NodeId> phase2SetNodeIdsAskedMeForFriendship;
	protected Random phase2RndNeighborFromOffers;
	protected NodeId phase2NodeIdAcceptedFriend;
	protected int phase2IntMyLr;
	protected Find2Opt phase2Opt2Recieve;
	protected Integer phase2PotentialComputedValueAssignmnet;
	protected Integer phase2PotentialComputedValueAssignmnetOfFriend;
	// ##---------Phase 3---------##
	protected Map<NodeId, Boolean> phase3RecieveBooleanLR;
	protected Map<NodeId, MsgReceive<Integer>> phase3RecieveLR; // id, variable
	protected Map<NodeId, Boolean> phase3RecieveBooleanInfoReplayFromFriend;
	protected Map<NodeId, MsgReceive<Find2Opt>> phase3RecieveInfoReplayFromFriend;

	// ##---------Phase 4---------##
	protected boolean phase4IsBestLR;
	// ##---------Phase 5---------##
	protected Map<NodeId, MsgReceive<Boolean>> phase5RecieveIsPartnerBestLR;
	protected Map<NodeId, Boolean> phase5RecieveBooleanIsPartnerBestLR;

	public MGM2(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		resetAgentGivenParametersV3();
		updateAlgorithmHeader();
		updateAlgorithmData();
	}

	@Override
	protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm m) {
		NodeId sender = m.getSenderId();

		try {
			if (m instanceof MsgValueAssignmnet) {
				return neighborsValueAssignmnet.get(sender).getTimestamp();
			}
			if (m instanceof MsgMgm2Phase2FriendshipInformation) {
				return this.phase2RecieveFriendshipOffers.get(sender).getTimestamp();
			}
			if (m instanceof MsgMgm2Phase3FriendshipReplay) {
				return this.phase3RecieveInfoReplayFromFriend.get(sender).getTimestamp();
			}

			if (m instanceof MsgMgm2Phase3LR) {
				return phase3RecieveLR.get(sender).getTimestamp();
			}
			if (m instanceof MsgMgm2Phase5IsBestLR) {
				return phase5RecieveIsPartnerBestLR.get(sender).getTimestamp();
			}
		} catch (NullPointerException e) {
			return -1;
		}

		return -1;
	}

	@Override
	public void updateAlgorithmHeader() {
		AgentVariable.algorithmHeader = "";
	}

	@Override
	public void updateAlgorithmData() {
		AgentVariable.algorithmData = "";
	}

	@Override
	protected void resetAgentGivenParametersV3() {
		resetFields();
		resetAgentGivenParametersV4();
	}

	protected void resetFields() {

		flagComputeRecieveValueMsgsPhase1 = false;
		flagComputeFriendshipInformationPhase2 = false;
		flagComputeOfferAndNegativeReplayPhase3 = false;
		flagComputeOfferAndPositiveReplayPhase3 = false;
		flagComputeAllLRandWithPartnerPhase4 = false;
		flagComputeAllLRandWithNoPartnerPhase4 = false;
		flagComputePartnerLRIsBestLRPhase5 = false;
		didEarlyPhase1Flag = false;
		// ** ---phase 1--- **
		resetPhase1RecieveBooleanValueAssignmnet();
		phase1RndIsOfferGiver = new Random();
		phase1RndIsOfferGiver.setSeed((id + 1) * 17 + dcopId);
		phase1RndIsOfferGiver.nextDouble();
		phase1RndNeighborSelection = new Random((id + 1) * 132 + dcopId * 15);
		phase1RndIsOfferGiver.nextInt();

		phase1BooleanIsOfferGiver = false;
		phase1NodeIdSelectedFriend = null;
		phase1MyOpt2Created = null;
		// ** ---phase 2--- **

		phase2RndNeighborFromOffers = new Random(id * 145 + dcopId * 36);
		phase2RndNeighborFromOffers.nextInt();
		resetPhase2RecieveBooleanFriendshipOffers();
		resetPhase2RecieveFriendshipOffers();
		phase2RecieveFriendshipOffers = new HashMap<NodeId, MsgReceive<KOptInfo>>();

		phase2SetNodeIdsAskedMeForFriendship = new HashSet<NodeId>();
		phase2NodeIdAcceptedFriend = null;
		phase2IntMyLr = -1;
		phase2Opt2Recieve = null;
		phase2PotentialComputedValueAssignmnet = null;
		phase2PotentialComputedValueAssignmnetOfFriend = null;

		// ** ---phase 3--- **
		resetPhase3RecieveBooleanLR();
		resetPhase3RecieveLR();
		resetPhase3RecieveInfoReplayFromFriend();
		flagRecieveNegativaReplay = false;
		// ** ---phase 4--- **
		phase4IsBestLR = false;
		flagIDidPhase4Already = false;

		// ** ---phase 5--- **
		resetPhase5RecieveIsPartnerBestLR();

	}

	protected void resetPhases(boolean withPhase1) {
		if (withPhase1) {
			resetPhase1();
		}
		resetPhase2();
		resetPhase3();
		resetPhase4();
		resetPhase5();

		didEarlyPhase1Flag = false;

	}

	protected void resetPhase1() {
		// resetPhase1

		flagValueAssignmentAlready = false;
		phase1BooleanIsOfferGiver = false;
		phase1NodeIdSelectedFriend = null;
		phase1MyOpt2Created = null;
	}

	protected void resetPhase2() {
		// resetPhase2
		resetPhase2RecieveFriendshipOffers();
		resetPhase2RecieveBooleanFriendshipOffers();

		phase2SetNodeIdsAskedMeForFriendship = new HashSet<NodeId>();
		phase2NodeIdAcceptedFriend = null;
		phase2IntMyLr = -1;
		phase2Opt2Recieve = null;
		phase2PotentialComputedValueAssignmnet = null;
		phase2PotentialComputedValueAssignmnetOfFriend = null;
	}

	protected void resetPhase3() {
		// ** ---phase 3--- **
		resetPhase3RecieveLR();
		resetPhase3RecieveInfoReplayFromFriend();
		flagRecieveNegativaReplay = false;

	}

	protected void resetPhase4() {
		// ** ---phase 4--- **
		phase4IsBestLR = false;
		flagIDidPhase4Already = false;
	}

	protected void resetPhase5() {
		// ** ---phase 5--- **
		resetPhase5RecieveIsPartnerBestLR();
	}

	protected void resetPhase5RecieveIsPartnerBestLR() {
		phase5RecieveIsPartnerBestLR = new HashMap<NodeId, MsgReceive<Boolean>>();
		phase5RecieveBooleanIsPartnerBestLR = new HashMap<NodeId, Boolean>();
		for (NodeId nodeId : this.getNeigborSetId()) {
			phase5RecieveIsPartnerBestLR.put(nodeId, null);
			phase5RecieveBooleanIsPartnerBestLR.put(nodeId, false);
		}

	}

	protected void resetPhase3RecieveInfoReplayFromFriend() {
		phase3RecieveInfoReplayFromFriend = new HashMap<NodeId, MsgReceive<Find2Opt>>();
		phase3RecieveBooleanInfoReplayFromFriend = new HashMap<NodeId, Boolean>();
		for (NodeId nodeId : this.getNeigborSetId()) {
			phase3RecieveInfoReplayFromFriend.put(nodeId, null);
			phase3RecieveBooleanInfoReplayFromFriend.put(nodeId, false);
		}

	}

	protected void resetPhase1RecieveBooleanValueAssignmnet() {

		phase1RecieveBooleanValueAssignmnet = new HashMap<NodeId, Boolean>();
		for (NodeId nodeId : this.getNeigborSetId()) {
			phase1RecieveBooleanValueAssignmnet.put(nodeId, false);
		}

	}

	protected void resetPhase2RecieveFriendshipOffers() {
		phase2RecieveFriendshipOffers = new HashMap<NodeId, MsgReceive<KOptInfo>>();
		// phase2RecieveBooleanFriendshipOffers = new HashMap<NodeId, Boolean>();
		for (NodeId nodeId : this.getNeigborSetId()) {
			if (phase2RecieveBooleanFriendshipOffers.get(nodeId) == false) {
				phase2RecieveFriendshipOffers.put(nodeId, null);
			}
			// phase2RecieveBooleanFriendshipOffers.put(nodeId, false);
		}
	}

	protected void resetPhase2RecieveBooleanFriendshipOffers() {
		phase2RecieveBooleanFriendshipOffers = new HashMap<NodeId, Boolean>();
		for (NodeId nodeId : this.getNeigborSetId()) {
			phase2RecieveBooleanFriendshipOffers.put(nodeId, false);
		}
	}

	protected void resetPhase3RecieveLR() {
		phase3RecieveLR = new HashMap<NodeId, MsgReceive<Integer>>(); // id, variable
		// phase3RecieveBooleanLR = new HashMap<NodeId, Boolean>();
		for (NodeId nodeId : this.getNeigborSetId()) {
			phase3RecieveLR.put(nodeId, null);
			// phase3RecieveBooleanLR.put(nodeId, false);
		}
	}

	protected void resetPhase3RecieveBooleanLR() {
		// phase3RecieveLR = new HashMap<NodeId, MsgReceive<Integer>>(); // id, variable
		phase3RecieveBooleanLR = new HashMap<NodeId, Boolean>();
		for (NodeId nodeId : this.getNeigborSetId()) {
			// phase3RecieveLR.put(nodeId, null);
			phase3RecieveBooleanLR.put(nodeId, false);
		}
	}

	protected abstract void resetAgentGivenParametersV4();

	private KOptInfo makeMyKOptInfo() {
		// TODO Auto-generated method stub
		return new KOptInfo(this.valueAssignment, nodeId, neighborsConstraint, domainArray,
				this.neighborsValueAssignmnet);
	}

	// ------------------** Msg Value Process **------------------

	/**
	 * recieve msg with value
	 * 
	 * @param m
	 */
	protected void recieveMsgPhase1(MsgAlgorithm m) {
		if (m instanceof MsgValueAssignmnet) {
			updateMsgInContextValueAssignmnet(m);
			phase1RecieveBooleanValueAssignmnet.put(m.getSenderId(), true);
		} else {
			throw new RuntimeException("Wrong type msg");
		}
	}

	/**
	 * rnd if I offer a neighbor to be a friend create information to send my friend
	 * 
	 * @return
	 */
	// offer
	protected boolean computePhase1() {
		Collection<MsgAlgorithm> toRemove = new ArrayList<MsgAlgorithm>();
		for (MsgAlgorithm msg : future) {
			if (msg instanceof MsgMgm2Phase3FriendshipReplay) {
				toRemove.add(msg);
			}
		}
		this.future.remove(toRemove);
		if (MainSimulator.isMGM2Debug) {
			System.out.println(this + " started phase1-----------time " + time);
		}

		double rnd = phase1RndIsOfferGiver.nextDouble();
		if (rnd < probToBeOfferPhase1) {
			this.phase1BooleanIsOfferGiver = true;
			phase1MyOpt2Created = makeMyKOptInfo();
			int i = this.phase1RndNeighborSelection.nextInt(this.neighborSize());
			this.phase1NodeIdSelectedFriend = (NodeId) this.getNeigborSetId().toArray()[i];
		} else {
			this.phase1BooleanIsOfferGiver = false;
			this.phase1NodeIdSelectedFriend = null;
		}

		if (MainSimulator.isMGM2Debug) {
			if (!phase1BooleanIsOfferGiver) {
				System.out.println(this + " is not an offerer");
			} else {
				System.out.println(this + " is an offerer to A_" + phase1NodeIdSelectedFriend.getId1());
			}

		}
		return true;
	}

	/**
	 * if offer send selected neighbor friendship and null to all the rest if not
	 * offer send null to all neighbors
	 */

	protected void sendPhase1() {
		List<Msg> msgs = new ArrayList<Msg>();

		if (this.phase1BooleanIsOfferGiver) {
			for (NodeId recieverNodeId : neighborsConstraint.keySet()) {
				MsgMgm2Phase2FriendshipInformation mva = null;
				if (recieverNodeId.getId1() == phase1NodeIdSelectedFriend.getId1()) {
					mva = new MsgMgm2Phase2FriendshipInformation(this.nodeId, recieverNodeId, phase1MyOpt2Created,
							this.timeStampCounter, this.time);
				} else {
					mva = new MsgMgm2Phase2FriendshipInformation(this.nodeId, recieverNodeId, null,
							this.timeStampCounter, this.time);
				}
				msgs.add(mva);

			}

		} else {
			for (NodeId recieverNodeId : neighborsConstraint.keySet()) {

				MsgMgm2Phase2FriendshipInformation mva = new MsgMgm2Phase2FriendshipInformation(this.nodeId,
						recieverNodeId, null, this.timeStampCounter, this.time);

				msgs.add(mva);
			}
		}
		this.outbox.insert(msgs);

	}

	private void checkRightAddress(MsgAlgorithm m) {
		NodeId recieverId = m.getRecieverId();
		if (!recieverId.equals(this.nodeId)) {
			throw new RuntimeException("mailer sent wrong agent msg");
		}
	}

	// ------------------** phase 2 **------------------

	/**
	 * Receive information from friend, or null from non friends
	 * 
	 * @param m
	 */
	protected void recieveMsgFriendshipInformationPhase2(MsgAlgorithm m) {
		if (m instanceof MsgMgm2Phase2FriendshipInformation) {
			checkRightAddress(m);
			NodeId senderId = m.getSenderId();
			MsgReceive<KOptInfo> msgRecieve = new MsgReceive<KOptInfo>((KOptInfo) m.getContext(), m.getTimeStamp());
			this.phase2RecieveFriendshipOffers.put(senderId, msgRecieve);
			this.phase2RecieveBooleanFriendshipOffers.put(senderId, true);
		} else {
			throw new RuntimeException("Wrong type msg");
		}
	}

	/**
	 * if i did not offer in the previous phase, then I can select from friends that
	 * offered me friendship if did not offer: organize a set of all neighbors that
	 * offered me. if nobody offered then I am alone, so I can calculate Local
	 * reduction alone if atlist one did offer, select friend and calculate
	 * information together
	 * 
	 * @return
	 */

	protected boolean computeIfNotOfferPhase2() {
		if (MainSimulator.isMGM2Debug) {
			System.out.println(this + " is about to select an offer");
		}
		phase2SetNodeIdsAskedMeForFriendship();
		if (phase2SetNodeIdsAskedMeForFriendship.isEmpty()) {
			computeLikeMGM1();
		} else {
			phase2SetNodeIdsAskedMeForFriendship();
			if (!phase2SetNodeIdsAskedMeForFriendship.isEmpty())
				computeGotRequestFromFriendsPhase2(true);
		}
		return true;

	}

	protected void phase2SetNodeIdsAskedMeForFriendship() {

		phase2SetNodeIdsAskedMeForFriendship = new HashSet<NodeId>();
		for (Entry<NodeId, MsgReceive<KOptInfo>> e : phase2RecieveFriendshipOffers.entrySet()) {
			if (e.getValue().getContext() != null) {
				phase2SetNodeIdsAskedMeForFriendship.add(e.getKey());
			}
		}
	}

	protected void computeLikeMGM1() {
		int candidate = getCandidateToChange_A();
		// if (candidate != this.valueAssignment) {
		this.phase2PotentialComputedValueAssignmnet = candidate;
		this.phase2PotentialComputedValueAssignmnetOfFriend = null;
		this.phase2IntMyLr = findLr(candidate);
		// }
	}

	protected void computeGotRequestFromFriendsPhase2(boolean withRandomSelection) {

		if (withRandomSelection) {
			selectRandomFriendFromAllOffers();
		} else {

			this.phase2NodeIdAcceptedFriend = (NodeId) phase2SetNodeIdsAskedMeForFriendship.toArray()[0];
		}
		createInfromationObjectTogetherWithFriend();
		useInfromationObjectToUpdateFields();
	}

	private void selectRandomFriendFromAllOffers() {
		int i = this.phase2RndNeighborFromOffers.nextInt(phase2SetNodeIdsAskedMeForFriendship.size());
		this.phase2NodeIdAcceptedFriend = (NodeId) phase2SetNodeIdsAskedMeForFriendship.toArray()[i];

	}

	private void createInfromationObjectTogetherWithFriend() {
		MsgReceive<KOptInfo> msgRec = this.phase2RecieveFriendshipOffers.get(this.phase2NodeIdAcceptedFriend);
		KOptInfo infoOfMyFriendPhase2 = msgRec.getContext();
		this.phase2Opt2Recieve = new Find2Opt(makeMyKOptInfo(), infoOfMyFriendPhase2);
	}

	private void useInfromationObjectToUpdateFields() {
		this.atomicActionCounter = phase2Opt2Recieve.getAtomicActionCounter();
		this.phase2IntMyLr = phase2Opt2Recieve.getLR();
		this.phase2PotentialComputedValueAssignmnet = phase2Opt2Recieve.getValueAssignmnet1();
		this.phase2PotentialComputedValueAssignmnetOfFriend = phase2Opt2Recieve.getValueAssignmnet2();

		phase3RecieveBooleanLR.put(phase2NodeIdAcceptedFriend, true);
		this.phase3RecieveLR.remove(phase2NodeIdAcceptedFriend);

	}

	/**
	 * 
	 */
	protected void sendIfNotOfferPhase2() {
		if (!this.phase1BooleanIsOfferGiver) {
			phase2SetNodeIdsAskedMeForFriendship();
			if (phase2SetNodeIdsAskedMeForFriendship.isEmpty()) {
				sendSingleLRPhase2();
			} else {
				sendDoubleLRPhase2();
			}
		}
	}

	protected void sendDoubleLRPhase2() {
		if (MainSimulator.isMGM2Debug) {
			System.out.println(this + " is sending LRs msgs sendDouble time " + time);
		}

		List<Msg> msgs = new ArrayList<Msg>();

		for (NodeId recieverNodeId : neighborsConstraint.keySet()) {
			if (phase2SetNodeIdsAskedMeForFriendship.contains(recieverNodeId)) {

				if (this.phase2NodeIdAcceptedFriend.getId1() == recieverNodeId.getId1()) {
					MsgMgm2Phase3FriendshipReplay mlr = new MsgMgm2Phase3FriendshipReplay(this.nodeId, recieverNodeId,
							this.phase2Opt2Recieve, this.timeStampCounter, this.time);
					msgs.add(mlr);
				} else {
					MsgMgm2Phase3FriendshipReplay mlr = new MsgMgm2Phase3FriendshipReplay(this.nodeId, recieverNodeId,
							null, this.timeStampCounter, this.time);
					msgs.add(mlr);

					MsgMgm2Phase3LR mlr2 = new MsgMgm2Phase3LR(this.nodeId, recieverNodeId, this.phase2IntMyLr,
							this.timeStampCounter, this.time);
					msgs.add(mlr2);

				}

			} else {
				MsgMgm2Phase3LR mlr = new MsgMgm2Phase3LR(this.nodeId, recieverNodeId, this.phase2IntMyLr,
						this.timeStampCounter, this.time);
				msgs.add(mlr);

			}
		} // for
		this.outbox.insert(msgs);

	}

	protected void sendAllOffersThatIamAnOffererMySelfPhase2() {
		phase2SetNodeIdsAskedMeForFriendship();
		List<Msg> msgs = new ArrayList<Msg>();

		for (NodeId recieverNodeId : phase2SetNodeIdsAskedMeForFriendship) {
			MsgMgm2Phase3FriendshipReplay mlr = new MsgMgm2Phase3FriendshipReplay(this.nodeId, recieverNodeId, null,
					this.timeStampCounter, this.time);
			msgs.add(mlr);
		}
		this.outbox.insert(msgs);

	}

	/**
	 * send LR msg too everyone
	 */
	protected void sendSingleLRPhase2() {

		if (MainSimulator.isMGM2Debug) {
			System.out.println(this + " is sending LRs msgs sendSingle time " + time);
		}
		List<Msg> msgs = new ArrayList<Msg>();

		for (NodeId recieverNodeId : neighborsConstraint.keySet()) {
			MsgMgm2Phase3LR mlr = new MsgMgm2Phase3LR(this.nodeId, recieverNodeId, this.phase2IntMyLr,
					this.timeStampCounter, this.time);
			msgs.add(mlr);
		}
		this.outbox.insert(msgs);

	}

	// ------------------** phase 3 **------------------

	/**
	 * may recieve a msg with replay from a friend
	 * 
	 * @param m
	 */

	protected void recieveMsgPhase3(MsgAlgorithm m) {
		NodeId sender = m.getSenderId();

		if (m instanceof MsgMgm2Phase3FriendshipReplay) {
			recieveMsgFriendshipReplayPhase3(m);
			if (!this.phase1BooleanIsOfferGiver || sender.getId1() != this.phase1NodeIdSelectedFriend.getId1()) {
				throw new RuntimeException("was not suppose to get this msg");
			}
			return;
		}

		if (m instanceof MsgMgm2Phase3LR) {
			recieveMsgLRPhase3(m);
		}

		throw new RuntimeException("wrong type of msg sent in phase 3");
	}

	protected void recieveMsgFriendshipReplayPhase3(MsgAlgorithm m) {
		if (m.getContext() != null) {
			phase3RecieveLR.remove(m.getSenderId()); // id, variable
			phase3RecieveBooleanLR.put(m.getSenderId(), true);
		}
		MsgReceive<Find2Opt> msgRecieve = new MsgReceive<Find2Opt>((Find2Opt) m.getContext(), m.getTimeStamp());
		this.phase3RecieveInfoReplayFromFriend.put(m.getSenderId(), msgRecieve);
		this.phase3RecieveBooleanInfoReplayFromFriend.put(m.getSenderId(), true);
	}

	protected void recieveMsgLRPhase3(MsgAlgorithm m) {
		Integer context = (Integer) m.getContext();
		int timestamp = m.getTimeStamp();
		MsgReceive<Integer> msgReceive = new MsgReceive<Integer>(context, timestamp);
		phase3RecieveLR.put(m.getSenderId(), msgReceive); // id, variable

		phase3RecieveBooleanLR.put(m.getSenderId(), true);

	}

	protected boolean computeOfferAndPositiveReplayPhase3() {

		if (phase3RecieveInfoReplayFromFriend.get(this.phase1NodeIdSelectedFriend) != null) {
			MsgReceive<Find2Opt> msgRecieve = phase3RecieveInfoReplayFromFriend.get(this.phase1NodeIdSelectedFriend);
			this.phase2IntMyLr = msgRecieve.getContext().getLR();
			this.phase2PotentialComputedValueAssignmnet = msgRecieve.getContext().getValueAssignmnet2();
			this.phase2PotentialComputedValueAssignmnetOfFriend = msgRecieve.getContext().getValueAssignmnet1();
		} else {
			computeLikeMGM1();
		}

		return true;
	}

	protected void sendAllLrExceptForPositiveFriendPhase3() {
		if (MainSimulator.isMGM2Debug) {
			System.out.println(this + " is sending LRs msgs sendAllLrExceptForPositive time " + time);
		}
		List<Msg> msgs = new ArrayList<Msg>();

		for (NodeId recieverNodeId : neighborsConstraint.keySet()) {
			if (this.phase1NodeIdSelectedFriend == null || (this.phase1NodeIdSelectedFriend != null
					&& this.phase1NodeIdSelectedFriend.getId1() != recieverNodeId.getId1())) {

				if (this.phase1NodeIdSelectedFriend.getId1() != recieverNodeId.getId1()) {
					MsgMgm2Phase3LR mlr = new MsgMgm2Phase3LR(this.nodeId, recieverNodeId, this.phase2IntMyLr,
							this.timeStampCounter, this.time);
					msgs.add(mlr);

				}
			}
		}
		this.outbox.insert(msgs);
	}

	// is best gain

	// ------------------** phase 4 **------------------
	protected void recieveMsgPhase4(MsgAlgorithm m) {
		if (m instanceof MsgMgm2Phase3LR) {
			recieveMsgLRPhase3(m);
		}
	}

	protected boolean computeAllLRandWithPartnerAmIBestPhase4() {

		this.phase4IsBestLR = amIBestLR_phase4();
		return true;
	}

	protected void sendAllLRandWithPartnerAmIBestPhase4() {
		NodeId myPartner = whoIsMyPartnerPhase4();
		List<Msg> msgs = new ArrayList<Msg>();

		if (myPartner != null) {
			MsgMgm2Phase5IsBestLR m = new MsgMgm2Phase5IsBestLR(this.nodeId, myPartner, this.phase4IsBestLR,
					this.timeStampCounter, this.time);
			msgs.add(m);
		}
		this.outbox.insert(msgs);

	}

	protected NodeId whoIsMyPartnerPhase4() {
		if (phase1BooleanIsOfferGiver && phase1NodeIdSelectedFriend != null
				&& this.phase3RecieveInfoReplayFromFriend.get(phase1NodeIdSelectedFriend).getContext() != null) {
			return phase1NodeIdSelectedFriend;
		}
		if (!phase1BooleanIsOfferGiver && phase2NodeIdAcceptedFriend != null) {
			return phase2NodeIdAcceptedFriend;
		}
		return null;
	}

	protected boolean amIWithPartner() {
		boolean isOfferAndAccept = phase1BooleanIsOfferGiver && phase1NodeIdSelectedFriend != null
				&& phase3RecieveBooleanInfoReplayFromFriend.get(phase1NodeIdSelectedFriend)
				&& this.phase3RecieveInfoReplayFromFriend.get(phase1NodeIdSelectedFriend).getContext() != null;

		boolean isRecieverAndNoRequest = !phase1BooleanIsOfferGiver && phase2NodeIdAcceptedFriend != null;

		return isOfferAndAccept || isRecieverAndNoRequest;
	}

	protected boolean computeAllLRandWithNoPartnerAmIBestPhase4() {
		if (amIBestLR_phase4()) {
			this.valueAssignment = this.phase2PotentialComputedValueAssignmnet;
			if (MainSimulator.isMGM2Debug) {
				System.out.println(this + " changed value at time " + time);

			}
		}
		return true;
	}

	protected boolean amIBestLR_phase4() {

		for (Entry<NodeId, MsgReceive<Integer>> e : phase3RecieveLR.entrySet()) {
			// try {
			Integer lrOfNeighbors = e.getValue().getContext();
			if (lrOfNeighbors > this.phase2IntMyLr) {
				return false;
			}
			if (lrOfNeighbors == this.phase2IntMyLr) {
				if (e.getKey().getId1() < this.id) {
					return false;
				}
			}
			// }catch (NullPointerException e1) {
			// System.err.println("AHHHHHHHHHHHH");
			// }

		}
		return true;
	}

// ------------------** phase 5 **------------------

	protected void recieveMsgPhase5(MsgAlgorithm m) {

		if (m instanceof MsgMgm2Phase5IsBestLR) {
			MsgReceive<Boolean> msgRecieve = new MsgReceive<Boolean>((boolean) m.getContext(), m.getTimeStamp());
			NodeId sender = m.getSenderId();
			NodeId myPartner = whoIsMyPartnerPhase4();

			if (sender.getId1() != myPartner.getId1()) {
				throw new RuntimeException("my partner did not send me a msg");
			}
			phase5RecieveIsPartnerBestLR.put(sender, msgRecieve);
			phase5RecieveBooleanIsPartnerBestLR.put(sender, true);

		}

	}

	protected boolean computePartnerLRIsBestLRPhase5() {
		this.phase4IsBestLR = amIBestLR_phase4();
		boolean isPartnerBestLRPhase4 = false;
		isPartnerBestLRPhase4 = phase5RecieveIsPartnerBestLR.get(this.whoIsMyPartnerPhase4()).getContext();
		if (phase4IsBestLR && isPartnerBestLRPhase4) {
			if (MainSimulator.isMGM2Debug) {
				System.out.println(this + " changed value at time " + time);
			}
			this.valueAssignment = this.phase2PotentialComputedValueAssignmnet;
		}
		NodeId myPartner = whoIsMyPartnerPhase4();
		int timestampOfPartner = getTimestampOfPartner();
		if (timestampOfPartner < 0) {
			throw new RuntimeException("The condition above is not correct");
		}
		MsgReceive<Integer> msgRecieve = new MsgReceive<Integer>(phase2PotentialComputedValueAssignmnetOfFriend,
				timestampOfPartner);
		if (phase2PotentialComputedValueAssignmnet == null) {
			throw new NullPointerException("The condition above is not correct");
		}

		// phase1RecieveBooleanValueAssignmnet.put(myPartner, true);

		return true;
	}

	protected void doReactionAndSendPhase1() {

		boolean isUpdate = computePhase1();
		// if (isMsgGoingToBeSent(isUpdate)) {
		computationCounter = computationCounter + 1;
		this.timeStampCounter = this.timeStampCounter + 1;
		if (MainSimulator.isAtomicTime) {
			this.time = this.time + this.atomicActionCounter;
			this.atomicActionCounter = 0;
		} else {
			this.time = this.time + 1;
		}
		sendPhase1();
		// resetPhase1RecieveBooleanValueAssignmnet();
		// releaseFutureMsgs();

		// }

	}

	protected boolean allMapBooleanMapIsTrue(Map<NodeId, Boolean> input) {

		for (Boolean b : input.values()) {
			if (!b) {
				return false;
			}
		}
		return true;
	}

	private int getTimestampOfPartner() {
		if (phase1BooleanIsOfferGiver && phase3RecieveInfoReplayFromFriend.get(phase1NodeIdSelectedFriend) != null) {
			return phase3RecieveInfoReplayFromFriend.get(phase1NodeIdSelectedFriend).getTimestamp();
		}
		if (!phase1BooleanIsOfferGiver && phase2NodeIdAcceptedFriend != null) {
			return phase2RecieveFriendshipOffers.get(phase2NodeIdAcceptedFriend).getTimestamp();
		}
		return -1;
	}

	protected void sendValueAssignmnetMsgsExceptPartnerPhase5() {
		List<Msg> msgs = new ArrayList<Msg>();

		for (NodeId recieverNodeId : neighborsConstraint.keySet()) {

			MsgValueAssignmnet mva = new MsgValueAssignmnet(this.nodeId, recieverNodeId, this.valueAssignment,
					this.timeStampCounter, this.time);
			msgs.add(mva);
		}

		this.outbox.insert(msgs);
	}
}
