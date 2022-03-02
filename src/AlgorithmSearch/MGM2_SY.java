package AlgorithmSearch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.MsgAlgorithm;
import Messages.MsgMgm2Phase2FriendshipInformation;
import Messages.MsgMgm2Phase3FriendshipReplay;
import Messages.MsgMgm2Phase3LR;
import Messages.MsgMgm2Phase5IsBestLR;
import Messages.MsgReceive;
import Messages.MsgValueAssignmnet;

public class MGM2_SY extends MGM2 {
	protected boolean waitingForValueMsgs;
	protected boolean waitingForFirstFriendshipInformation;
	protected boolean waitingForOfferInformation;
	protected boolean waitingForAllLR;
	protected boolean waitingForPartnerIsBestLR;
	private boolean fromFuture;

	public MGM2_SY(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		resetAgentGivenParametersV4();
		updateAlgorithmName();
	}

	@Override
	protected boolean updateMessageInContext(MsgAlgorithm m) {
		if (m instanceof MsgValueAssignmnet) {
			if (waitingForValueMsgs && !waitingForPartnerIsBestLR) {
				recieveMsgPhase1(m);
				return true;
			} else if (!fromFuture) {

				future.add(m);
			}
			return false;
		}
		if (m instanceof MsgMgm2Phase2FriendshipInformation) {
			if (waitingForFirstFriendshipInformation) {
				recieveMsgFriendshipInformationPhase2(m);
				return true;
			} else if (!fromFuture) {
				boolean doesMsgExists = doesMsgExists(m, this.phase2RecieveFriendshipOffers);
				if (!doesMsgExists) {
					future.add(m);
				}
			}
			return false;
		}

		if (m instanceof MsgMgm2Phase3FriendshipReplay ) {
			if (waitingForOfferInformation) {
				recieveMsgFriendshipReplayPhase3(m);
				return true;
			} else if (!fromFuture) {
				boolean doesMsgExists = doesMsgExists(m, this.phase3RecieveInfoReplayFromFriend);
				if (!doesMsgExists) {
					if (this.phase1BooleanIsOfferGiver) {
						future.add(m);
					}
				}
			}
			return false;
		}

		// || m instanceof MsgMgm2Phase3FriendshipReplay
		//
		if (m instanceof MsgMgm2Phase3LR) {

			if (waitingForAllLR) {

				recieveMsgLRPhase3(m);
				
				return true;

				// if (m instanceof MsgMgm2Phase3FriendshipReplay) {
				// recieveMsgFriendshipReplayPhase3(m);
				// }
			} else if (!fromFuture) {
				boolean doesMsgExists = doesMsgExists(m, this.phase3RecieveLR);
				if (!doesMsgExists) {
					future.add(m);
				}
			}
			return false;

		}

		if (m instanceof MsgMgm2Phase5IsBestLR) {
			
			if (waitingForPartnerIsBestLR ) {
				recieveMsgPhase5(m);
				return true;
			} else if (!fromFuture) {
				
				boolean doesMsgExists = doesMsgExists(m, this.phase5RecieveIsPartnerBestLR);
				if (!doesMsgExists) {
					future.add(m);
				}
			}
			

			
			return false;
		}

		return true;
	}

	private static <T> boolean doesMsgExists(MsgAlgorithm m, Map<NodeId, MsgReceive<T>> map) {
		try {
			MsgReceive<?> msgR = map.get(m.getSenderId());
			if (msgR.getTimestamp() == m.getTimeStamp()) {
				return true;
			}
			return false;
		} catch (NullPointerException e) {
			return false;
		}
	}

	@Override
	protected void resetAgentGivenParametersV4() {
		this.future = new HashSet<MsgAlgorithm>();
		this.isWithTimeStamp = false;
		waitingForValueMsgs = true;
		waitingForFirstFriendshipInformation = false;
		waitingForOfferInformation = false;
		waitingForAllLR = false;
		waitingForPartnerIsBestLR = false;
		flagValueAssignmentAlready = false;

	}

	@Override
	public void updateAlgorithmName() {
		AgentVariable.AlgorithmName = "MGM2_SY";
	}

	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm m) {

		if (this.waitingForValueMsgs  && (m instanceof MsgValueAssignmnet
				&& allMapBooleanMapIsTrue(this.phase1RecieveBooleanValueAssignmnet))) {
			
			resetPhase1RecieveBooleanValueAssignmnet();
			
			this.flagComputeRecieveValueMsgsPhase1 = true;

			if (MainSimulator.isMGM2Debug) {
				System.out.println(this + " recieve all values " + " time " + this.time);
			}
			/*
			Iterator <MsgAlgorithm>it = future.iterator();
			while (it.hasNext()) {
				MsgAlgorithm msgAlgorithm =  it.next();
				if (msgAlgorithm instanceof MsgMgm2Phase5IsBestLR) {
					it.remove();
				}
			}
			*/
		}

		if (this.waitingForFirstFriendshipInformation && m instanceof MsgMgm2Phase2FriendshipInformation
				&& allMapBooleanMapIsTrue(this.phase2RecieveBooleanFriendshipOffers)) {

			this.flagComputeFriendshipInformationPhase2 = true;
			if (MainSimulator.isMGM2Debug) {
				System.out.println(this + " time " + this.time + " recieve all FriendshipOffers");
			}

			if (this.neighborSize() == 1 && !this.phase1BooleanIsOfferGiver) {
				phase2SetNodeIdsAskedMeForFriendship();
				if (!phase2SetNodeIdsAskedMeForFriendship.isEmpty())
					computeGotRequestFromFriendsPhase2(false);
			}
			resetPhase2RecieveBooleanFriendshipOffers();
		}

		if (this.waitingForOfferInformation && m instanceof MsgMgm2Phase3FriendshipReplay
				&& gaveOfferAndRecieveNegetiveReplay(m)) {

			this.flagComputeOfferAndNegativeReplayPhase3 = true;
			if (MainSimulator.isMGM2Debug) {
				System.out.println(this + " gave Offer And Recieve Negetive Replay from: A_" + m.getSenderId().getId1()
						+ " at time " + time);
			}

		}

		if (this.waitingForOfferInformation && m instanceof MsgMgm2Phase3FriendshipReplay
				&& gaveOfferAndRecievePostivieReplay(m)) {
			this.flagComputeOfferAndPositiveReplayPhase3 = true;
			this.phase3RecieveBooleanLR.put(whoIsMyPartnerPhase4(), true);
		
			if (MainSimulator.isMGM2Debug) {
				System.out.println(this + " gave Offer And Recieve positive Replay from: A_" + m.getSenderId().getId1()
						+ " at time " + this.time);
			}

		}

		if (((this.waitingForAllLR && m instanceof MsgMgm2Phase3LR) || this.neighborSize() == 1 )
				
				
				&& allMapBooleanMapIsTrue(phase3RecieveBooleanLR)) {

			if (amIWithPartner()) {
				this.flagComputeAllLRandWithPartnerPhase4 = true;
				if (MainSimulator.isMGM2Debug) {
					System.out.println(this + " with partner and recieve all LR msgs, time " + this.time);
				}
			} else {
				this.flagComputeAllLRandWithNoPartnerPhase4 = true;
				if (MainSimulator.isMGM2Debug) {
					System.out.println(this + " without partner and recieve all LR msgs");
				}
			}
			resetPhase3RecieveBooleanLR();


		}

		if (waitingForPartnerIsBestLR && m instanceof MsgMgm2Phase5IsBestLR) {

			waitingForValueMsgs = true;
			waitingForPartnerIsBestLR = false;

			this.flagComputePartnerLRIsBestLRPhase5 = true;

			if (MainSimulator.isMGM2Debug) {
				System.out.println(this + " recieve (" + m.getContext() + ") from A_" + m.getSenderId().getId1()
						+ " at time " + this.time);
			}

			resetPhase3RecieveBooleanLR();
		}

	}

	private boolean gaveOfferAndRecieveNegetiveReplay(MsgAlgorithm m) {
		return this.phase1BooleanIsOfferGiver && m.getSenderId().getId1() == this.phase1NodeIdSelectedFriend.getId1()
				&& phase3RecieveInfoReplayFromFriend.get(this.phase1NodeIdSelectedFriend).getContext() == null;
	}

	private boolean gaveOfferAndRecievePostivieReplay(MsgAlgorithm m) {
		return this.phase1BooleanIsOfferGiver && m.getSenderId().getId1() == this.phase1NodeIdSelectedFriend.getId1()
				&& phase3RecieveInfoReplayFromFriend.get(this.phase1NodeIdSelectedFriend).getContext() != null;
	}

	@Override
	protected boolean compute() {
		if (this.flagComputeRecieveValueMsgsPhase1 && flagComputePartnerLRIsBestLRPhase5) {
			computePartnerLRIsBestLRPhase5();
			computePhase1();
		} else if (this.flagComputeRecieveValueMsgsPhase1 && flagComputeAllLRandWithNoPartnerPhase4) {
			computeAllLRandWithNoPartnerAmIBestPhase4();
			computePhase1();
		}else {
			if (this.flagComputeRecieveValueMsgsPhase1) {
				computePhase1();
			}
			if (this.flagComputeFriendshipInformationPhase2) {

				if (this.phase1BooleanIsOfferGiver == false) {
					phase2SetNodeIdsAskedMeForFriendship();
					if (phase2SetNodeIdsAskedMeForFriendship.isEmpty()) {
						computeLikeMGM1();
					} else {
						if (this.neighborSize() > 1) {
							computeGotRequestFromFriendsPhase2(true);
						}

					}
				}
			}

			if (flagComputeOfferAndNegativeReplayPhase3) {
				computeLikeMGM1();
			}

			if (flagComputeOfferAndPositiveReplayPhase3) {

				computeOfferAndPositiveReplayPhase3();
			}

			if (this.flagComputeAllLRandWithPartnerPhase4) {
				computeAllLRandWithPartnerAmIBestPhase4();
			}

			if (this.flagComputeAllLRandWithNoPartnerPhase4) {
				computeAllLRandWithNoPartnerAmIBestPhase4();
			}
			if (flagComputePartnerLRIsBestLRPhase5) {
				computePartnerLRIsBestLRPhase5();
			}
		}
		return true;
	}

	@Override
	public void sendMsgs() {
		
		if (this.flagComputeRecieveValueMsgsPhase1 && flagComputeAllLRandWithNoPartnerPhase4) {
			waitingForAllLR = false;
			// waitingForPartnerIsBestLR = true;
			waitingForValueMsgs = true;
			sendFlagComputeAllLRandWithNoPartnerPhase4();

			sendPhase1();
			resetPhase1RecieveBooleanValueAssignmnet();
			waitingForValueMsgs = false;
			waitingForFirstFriendshipInformation = true;
		} else {

			if (this.flagComputeRecieveValueMsgsPhase1) {
				sendPhase1();
				resetPhase1RecieveBooleanValueAssignmnet();
				waitingForValueMsgs = false;
				waitingForFirstFriendshipInformation = true;
			}

			if (this.flagComputeFriendshipInformationPhase2) {
				if (this.phase1BooleanIsOfferGiver) {
					sendAllOffersThatIamAnOffererMySelfPhase2();
				}

				if (this.phase1BooleanIsOfferGiver == false) {
					phase2SetNodeIdsAskedMeForFriendship();
					if (phase2SetNodeIdsAskedMeForFriendship.isEmpty()) {
						sendSingleLRPhase2();
					} else {
						sendDoubleLRPhase2();
					}
				}

				waitingForFirstFriendshipInformation = false;
				if (this.phase1BooleanIsOfferGiver) {
					waitingForOfferInformation = true;
				} else {
					waitingForAllLR = true;
				}

			}

			if (flagComputeOfferAndNegativeReplayPhase3) {
				sendSingleLRPhase2();
				waitingForOfferInformation = false;
				waitingForAllLR = true;
				// resetPhase3RecieveInfoReplayFromFriend();
			}

			if (flagComputeOfferAndPositiveReplayPhase3) {
				sendAllLrExceptForPositiveFriendPhase3();
				waitingForOfferInformation = false;
				waitingForAllLR = true;
				// resetPhase3RecieveInfoReplayFromFriend();
			}
			if (this.flagComputeAllLRandWithPartnerPhase4) {

				waitingForAllLR = false;
				waitingForPartnerIsBestLR = true;
				
				
				sendAllLRandWithPartnerAmIBestPhase4();
				if (MainSimulator.isMGM2Debug) {
					System.out.println(this + " sent if it is best (" + this.phase4IsBestLR + ") to A_"
							+ whoIsMyPartnerPhase4() + " at time: " + this.time);
				}
			}

			if (this.flagComputeAllLRandWithNoPartnerPhase4) {
				waitingForAllLR = false;
				// waitingForPartnerIsBestLR = true;
				waitingForValueMsgs = true;
				
				sendFlagComputeAllLRandWithNoPartnerPhase4();
				
			}
			if (flagComputePartnerLRIsBestLRPhase5) {
				waitingForValueMsgs = true;
				waitingForPartnerIsBestLR = false;
				sendFlagComputePartnerLRIsBestLRPhase5();

			}
		}
		if (!fromFuture || getDidComputeInThisIteration()) {
			releaseFutureMsgs();
		}
	}

	private void sendFlagComputePartnerLRIsBestLRPhase5() {
		/*
		if (allMapBooleanMapIsTrue(this.phase1RecieveBooleanValueAssignmnet)) {
			resetPhase1();
			doReactionAndSendPhase1();
			didEarlyPhase1Flag = true;
			resetPhase1RecieveBooleanValueAssignmnet();
			waitingForValueMsgs = false;
			waitingForFirstFriendshipInformation = true;
		}
*/
		if (!didEarlyPhase1Flag) {
			if (!this.phase1BooleanIsOfferGiver) {
				resetPhase1();
			}
		}
		flagValueAssignmentAlready = false;
		resetPhase2();
		resetPhase3();
		resetPhase4();
		resetPhase5();

		sendValueAssignmnetMsgs();

		didEarlyPhase1Flag = false;

		if (MainSimulator.isMGM2Debug) {
			System.out.println(this + " sent values with partner");
		}

	}

	private void sendFlagComputeAllLRandWithNoPartnerPhase4() {
		sendValueAssignmnetMsgs();
		resetPhases(!didEarlyPhase1Flag);

	
		if (MainSimulator.isMGM2Debug) {
			System.out.println(this + " sent values and lonely, time " + this.time);
		}
	}

	private void releaseFutureMsgs() {
		
		this.fromFuture = true;
		HashSet<MsgAlgorithm> toRemove = new HashSet<MsgAlgorithm>();
		boolean flag = false;
		
		for (MsgAlgorithm m : this.future) {
			
			flag = shouldChangeFutureFlag(m);
			if (flag) {
				toRemove.add(m);
			}
		}
		changeRecieveFlagsToFalse();

		for (MsgAlgorithm m : toRemove) {
		
			updateMessageInContextAndTreatFlag(m);
			reactionToAlgorithmicMsgs();
			sendMsgs();
		}
		changeRecieveFlagsToFalse();

		this.future.removeAll(toRemove);
		this.fromFuture = false;

		/*
		 * boolean flag = false; if (m instanceof MsgValueAssignmnet &&
		 * (flagComputePartnerLRIsBestLRPhase5 ||
		 * flagComputeAllLRandWithNoPartnerPhase4)) { flag = true; }
		 * 
		 * if (m instanceof MsgMgm2Phase2FriendshipInformation &&
		 * flagComputeRecieveValueMsgsPhase1) { flag = true; }
		 * 
		 * if (m instanceof MsgMgm2Phase3FriendshipReplay &&
		 * flagComputeFriendshipInformationPhase2) { flag = true; }
		 * 
		 * if (m instanceof MsgMgm2Phase3LR && (flagComputeOfferAndNegativeReplayPhase3
		 * || flagComputeOfferAndPositiveReplayPhase3 ||
		 * flagComputeFriendshipInformationPhase2)) { flag = true; }
		 * 
		 * if ((m instanceof MsgMgm2Phase5IsBestLR &&
		 * flagComputeAllLRandWithPartnerPhase4)) { flag = true; } boolean anotherFlag =
		 * false;
		 * 
		 * if (flag) {
		 * 
		 * boolean isupdated = updateMessageInContext(m); if (isupdated) {
		 * changeRecieveFlagsToTrue(m); if (m instanceof MsgValueAssignmnet &&
		 * this.flagComputeRecieveValueMsgsPhase1) { anotherFlag = true;
		 * this.flagComputeAllLRandWithNoPartnerPhase4 = false;
		 * this.flagComputePartnerLRIsBestLRPhase5 = false; }
		 * 
		 * if (m instanceof MsgMgm2Phase2FriendshipInformation &&
		 * this.flagComputeFriendshipInformationPhase2) { anotherFlag = true; // if
		 * (!waitingForValueMsgs) { this.flagComputeRecieveValueMsgsPhase1 = false; // }
		 * }
		 * 
		 * if (m instanceof MsgMgm2Phase3FriendshipReplay &&
		 * (this.flagComputeOfferAndPositiveReplayPhase3 ||
		 * flagComputeOfferAndNegativeReplayPhase3)) { anotherFlag = true; if
		 * (!waitingForFirstFriendshipInformation) {
		 * flagComputeFriendshipInformationPhase2 = false; } }
		 * 
		 * if (m instanceof MsgMgm2Phase3LR &&
		 * (this.flagComputeFriendshipInformationPhase2 &&
		 * (this.flagComputeOfferAndNegativeReplayPhase3 ||
		 * this.flagComputeOfferAndPositiveReplayPhase3)) && !waitingForAllLR) {
		 * anotherFlag = true; if (!waitingForOfferInformation) {
		 * flagComputeFriendshipInformationPhase2 = false; //
		 * flagComputeOfferAndPositiveReplayPhase3=false; //
		 * flagComputeOfferAndNegativeReplayPhase3=false; }
		 * 
		 * }
		 * 
		 * else if (m instanceof MsgMgm2Phase3LR &&
		 * (this.flagComputeAllLRandWithNoPartnerPhase4 ||
		 * flagComputeAllLRandWithPartnerPhase4)) { anotherFlag = true;
		 * flagComputeOfferAndPositiveReplayPhase3 = false;
		 * flagComputeOfferAndNegativeReplayPhase3 = false;
		 * flagComputeFriendshipInformationPhase2 = false;
		 * 
		 * }
		 * 
		 * if (m instanceof MsgMgm2Phase5IsBestLR &&
		 * this.flagComputePartnerLRIsBestLRPhase5) { anotherFlag = true;
		 * flagComputeAllLRandWithNoPartnerPhase4 = false;
		 * flagComputeAllLRandWithPartnerPhase4 = false;
		 * flagComputeOfferAndPositiveReplayPhase3 = false;
		 * flagComputeOfferAndNegativeReplayPhase3 = false;
		 * flagComputeFriendshipInformationPhase2 = false;
		 * 
		 * } if (anotherFlag) { toRemove.add(m); } else { toRelease.add(m); } }
		 * 
		 * }
		 * 
		 * }
		 * 
		 * if (!toRemove.isEmpty()) { reactionToAlgorithmicMsgs(); ArrayList<Boolean>
		 * afterBools = createFlagsArray(); boolean areArraysSimilar =
		 * areArraysSimilar(beforeBools, afterBools); if (!areArraysSimilar) {
		 * sendMsgs(); } changeRecieveFlagsToFalse(); }
		 * 
		 * this.future.removeAll(toRemove); if (!toRelease.isEmpty())
		 * 
		 * { changeRecieveFlagsToFalse(); reactionToAlgorithmicMsgs(); }
		 * this.future.removeAll(toRelease); this.fromFuture = false;
		 */

	}

	private boolean shouldChangeFutureFlag(MsgAlgorithm m) {
		if (m instanceof MsgValueAssignmnet && waitingForValueMsgs) {
			return true;
		}
		if (m instanceof MsgMgm2Phase2FriendshipInformation && waitingForFirstFriendshipInformation) {
			return true;
		}
		if (m instanceof MsgMgm2Phase3FriendshipReplay && waitingForOfferInformation) {
			return true;
		}
		if (m instanceof MsgMgm2Phase3LR && waitingForAllLR) {
			return true;
		}
		if (m instanceof MsgMgm2Phase5IsBestLR && waitingForPartnerIsBestLR) {
			return true;
		}
		return false;
	}

	private boolean areArraysSimilar(ArrayList<Boolean> beforeBools, ArrayList<Boolean> afterBools) {
		if (beforeBools.size() != afterBools.size()) {
			return false;
		}
		for (int i = 0; i < beforeBools.size(); i++) {
			boolean b1 = beforeBools.get(i);
			boolean b2 = afterBools.get(i);
			if (b1 != b2) {
				return false;
			}
		}
		return true;
	}

	private ArrayList<Boolean> createFlagsArray() {
		ArrayList<Boolean> ans = new ArrayList<Boolean>();
		ans.add(flagComputeRecieveValueMsgsPhase1);
		ans.add(flagComputeFriendshipInformationPhase2);
		ans.add(flagComputeOfferAndNegativeReplayPhase3);
		ans.add(flagComputeOfferAndPositiveReplayPhase3);
		ans.add(flagComputeAllLRandWithPartnerPhase4);
		ans.add(flagComputeAllLRandWithNoPartnerPhase4);
		ans.add(flagComputePartnerLRIsBestLRPhase5);

		return ans;
	}

	@Override
	public void changeRecieveFlagsToFalse() {
		flagComputeRecieveValueMsgsPhase1 = false;
		flagComputeFriendshipInformationPhase2 = false;
		flagComputeOfferAndNegativeReplayPhase3 = false;
		flagComputeOfferAndPositiveReplayPhase3 = false;
		flagComputeAllLRandWithPartnerPhase4 = false;
		flagComputeAllLRandWithNoPartnerPhase4 = false;
		flagComputePartnerLRIsBestLRPhase5 = false;
	}

	@Override
	public boolean getDidComputeInThisIteration() {
		return flagComputeRecieveValueMsgsPhase1 || flagComputeFriendshipInformationPhase2
				|| flagComputeOfferAndNegativeReplayPhase3 || flagComputeOfferAndPositiveReplayPhase3
				|| flagComputeAllLRandWithPartnerPhase4 || flagComputeAllLRandWithNoPartnerPhase4
				|| flagComputePartnerLRIsBestLRPhase5;
	}

}
