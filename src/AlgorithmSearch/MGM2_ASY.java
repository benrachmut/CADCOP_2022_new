package AlgorithmSearch;

import Messages.MsgAlgorithm;

public class MGM2_ASY extends MGM2{

	public MGM2_ASY(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void resetAgentGivenParametersV4() {

	}

	@Override
	public void updateAlgorithmName() {

	}


	@Override
	protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
		return false;
	}

	@Override
	public boolean getDidComputeInThisIteration() {
		return false;
	}

	@Override
	protected boolean compute() {
		return false;
	}

	@Override
	public void sendMsgs() {

	}

	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

	}

	@Override
	public void changeRecieveFlagsToFalse() {

	}
}
