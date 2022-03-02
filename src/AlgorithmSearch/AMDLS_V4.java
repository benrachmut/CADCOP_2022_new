package AlgorithmSearch;

import java.util.Random;

import Main.MainSimulator;
import Messages.MsgAMDLS;
import Messages.MsgAMDLSColor;
import Messages.MsgAlgorithm;

public class AMDLS_V4 extends AMDLS_V3{

	private boolean flag_recieveMsg;
	public AMDLS_V4(int dcopId, int D, int agentId) {
		super(dcopId, D, agentId);
		flag_recieveMsg = false;
	}

	@Override
	protected void resetAgentGivenParametersV3() {		
		super.resetAgentGivenParametersV3();
		flag_recieveMsg = false;
	}
	
	@Override
	public void initialize() {
		//System.out.println("aaaaaaaaa");
		this.isWithTimeStamp = false;
		if (canSetColorInitilize()) {
			chooseColor();
			if (MainSimulator.isAMDLSDistributedDebug) {
				System.out.println("A_"+this.id+" select initial color "+this.myColor);
			}
			sendAMDLSColorMsgs();
			isWaitingToSetColor = false;
		}else {
			this.myCounter = 0;
		}
	}
	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {
		super.changeRecieveFlagsToTrue(msgAlgorithm);	
		if (allWithColor()) {
			flag_recieveMsg = true;
		}
	}
	
	
	private boolean allWithColor() {
		for (Integer c : this.neighborColors.values()) {
			if (c==null) {
				return false;
			}
		}
		return true;
	}

	public void sendMsgs() {
		boolean sendAllTheTime = AMDLS_V1.sendWhenMsgReceive && this.gotMsgFlag;
		boolean flag = false;
		//if ( this.canSetColorFlag) {
			sendAMDLSColorMsgs();	
			this.consistentFlag = false;
			this.canSetColorFlag = false;
			if (releaseFutureMsgs()) {	
				reactionToAlgorithmicMsgs();
			}
			boolean aboveConsistent = isAboveConsistent();
			boolean belowConsistent = isBelowConsistent();
			if (aboveConsistent && belowConsistent && allNeighborsHaveColor()) {
				flag = true;
			} else {
				flag = false;
			}
		//}
		if (flag || (consistentFlag && !canSetColorFlag)) {
			if (flag) {
				decideAndChange();
				this.timeStampCounter = this.timeStampCounter+1;
			}
			sendAMDLSmsgs();
		} 
		
	}

	@Override
	public void changeRecieveFlagsToFalse() {
		flag_recieveMsg = false;
		this.consistentFlag = false;
		this.canSetColorFlag = false;
	}
	/*
	@Override
	protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
	
		if (msgAlgorithm instanceof MsgAMDLSColor) {
			Integer colorN = ((MsgAMDLSColor) msgAlgorithm).getColor();
			neighborColors.put(msgAlgorithm.getSenderId(), colorN);
			if (this.myColor != null) {
				if (this.myColor > colorN) {
					this.above.add(msgAlgorithm.getSenderId());
				} else {
					this.below.add(msgAlgorithm.getSenderId());
				}
			}
		}

		
		//!haveAllColors()&&
		if ( (this.neighborColors.get(msgAlgorithm.getSenderId())==null  && this.myCounter<=1 
				&& !((MsgAMDLS)msgAlgorithm).isFromFuture())) {
			MsgAMDLS m = new MsgAMDLS((MsgAMDLSColor) msgAlgorithm);
			
				future.add((MsgAMDLS)msgAlgorithm);
			}

		else {
			super.updateMessageInContext(msgAlgorithm);
		}
		return true;
	}
	*/

	
}
