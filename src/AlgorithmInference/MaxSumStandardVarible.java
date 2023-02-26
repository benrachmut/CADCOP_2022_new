package AlgorithmInference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableInference;
import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.MsgAlgorithm;
import Messages.MsgAlgorithmFactor;
import Messages.MsgReceive;

public  class  MaxSumStandardVarible extends AgentVariableInference {

	///// ******* Variables ******* ////

	protected boolean iAmAsync = true;
	protected boolean receiveMessageFlag;
	protected double dampingFactor = 0.9;
	protected HashMap<NodeId, double[]> storedMessges;
	HashMap<NodeId, MsgAlgorithmFactor> messagesToBeSent;
	Random rand = new Random();
	protected int computationCounter;
	protected int numberofIterations = 1;

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Control Variables ******* ////

	boolean damping =false;
	boolean storedMessageOn = false;
	protected boolean print = false;
	protected boolean printValueAssignment = false;
	protected boolean dampingPrint = false;
	protected boolean dust = false;

	///// ******* Constructor ******* ////

	public MaxSumStandardVarible(int dcopId, int D, int id1) {

		super(dcopId, D, id1);
		this.storedMessges = new HashMap<NodeId, double[]>();
		this.messagesToBeSent = new HashMap<NodeId, MsgAlgorithmFactor>();
		this.receiveMessageFlag = false;
		this.computationCounter = 0;
		updateAlgorithmHeader();
		updateAlgorithmData();
		updateAlgorithmName();

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Methods to initialize a new run ******* ////

	// OmerP - To reset the agent if this is a new run.
	@Override
	public void resetAgentGivenParametersV3() {

		this.storedMessges.clear();
		this.messagesToBeSent.clear();
		resetAgentGivenParametersV4();
	}

	public void resetAgentGivenParametersV4() {
	}

	// OmerP - Will send new messages for each one of the neighbors upon the
	public void initialize() {

		produceEmptyMessage();
		this.computationCounter++;
		sendMsgs();

		// }

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Main Methods ******* ////

	// OmerP - new information has arrived and the variable node will update its
	// value assignment.
	public boolean compute() {

		if (receiveMessageFlag) {

			produceNewMessages();
			chooseValueLongAssignment();
			this.computationCounter++;

		}

		return true;

	}

	// OmerP - will loop over the neighbors and will send to each one of the
	// neighbors.
	@Override
	public void sendMsgs() {

		for (NodeId i : functionMsgs.keySet()) {
			
			//mailer.sendMsg(messagesToBeSent.get(i));

			if (print) {
				printSentdMessage(messagesToBeSent.get(i));
			}

			if (storedMessageOn) {

				storeNewMessage(i, messagesToBeSent.get(i).getContext());

			}

		}

		messagesToBeSent.clear();
		changeReceiveFlagsToFalse();
		if (print) {
			printFlag();
		}

	}

	// OmerP - when a message received will update the context and flag that a
	// message was received.
	@Override
	public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {

		MsgAlgorithmFactor msgAlgorithmFactor = (MsgAlgorithmFactor) msgAlgorithm;

		if (print) {
			printReceivedMessage(msgAlgorithmFactor);
		}

		double[] contextFix = (double[]) msgAlgorithmFactor.getContext(); // will cast the message object as a double[].

		MsgReceive<double[]> newMessageReceveid = new MsgReceive<double[]>(contextFix,
				msgAlgorithmFactor.getTimeStamp()); //

		functionMsgs.put(msgAlgorithmFactor.getSenderId(), newMessageReceveid);

		changeReceiveFlagsToTrue(msgAlgorithm);

		if (print) {
			printFlag();
		}
		return true;

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Produce Messages Methods ******* ////

	// OmerP - will produce new messages.
	protected void produceNewMessages() {

		for (NodeId i : functionMsgs.keySet()) {

			double[] sentTable = new double[this.domainSize];
			sentTable = produceMessage(i, sentTable); // For each specific neighbor, sum all messages excluding the
														// table of the receiving function node.
			//sentTable = subtractMinimumValueD(sentTable);
			MsgAlgorithmFactor newMsg;

			if (damping) {

				sentTable = damping(i, sentTable); // Will produce a damped message.
				storedMessges.put(i, sentTable); // Will store the new message

			}

			if (iAmAsync) {

				newMsg = new MsgAlgorithmFactor(this.getNodeId(), i, sentTable, this.computationCounter, this.time);
			}

			else {

				newMsg = new MsgAlgorithmFactor(this.getNodeId(), i, sentTable, 0, this.time);

			}

			messagesToBeSent.put(i, newMsg);

		}

	}

	// OmerP - will produce empty messages.
	protected void produceEmptyMessage() {

		for (NodeId i : functionMsgs.keySet()) { // Start loop over the neighbors.

			double[] sentTable = new double[this.domainSize];
			sentTable = produceEmptyTable(i, sentTable); // For each specific neighbor, produce an empty message.
			MsgAlgorithmFactor newMsg = new MsgAlgorithmFactor(this.getNodeId(), i, sentTable, 0, this.time); // Create
																												// new
																												// factor
																												// message.
			messagesToBeSent.put(i, newMsg); // Store the message in the message to by sent HashMap.

			if (damping) { // If damping is on it will store the new message so it could restore it for the
							// next message that will be created.

				storedMessges.put(i, newMsg.getContext());
				// printStoredMessage(newMsg);

			}

		}

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Dust Methods ******* ////

	// Add dust as double.
	protected double[] addDust(double[] table) {

		for (int i = 0; i < table.length; i++) {

			double randomDust = rand.nextDouble() / 1000;

			table[i] = table[i] + randomDust;

		}

		return table;

	}

	// Add dust as long.
	protected long[] addLongDust(long[] table) {

		for (int i = 0; i < table.length; i++) {

			table[i] = table[i] + rand.nextInt(9);

		}

		return table;

	}

	///// ******* Stored Message Methods ******* ////

	protected void storeNewMessage(NodeId nodeid, double[] table) {

		if (storedMessageOn) {

			storedMessges.put(nodeid, table);

		}

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Choose Value Assignment Method ******* ////

	// OmerP - Will choose the first value assignment.
	public void chooseFirstValueAssignment() {

		Random rnd = new Random();
		setValueAssignmnet(rnd.nextInt(this.domainSize));

	}

	// OmerP - Will update the value assignment.
	public void chooseValueAssignment() {

		double[] table = new double[this.domainSize]; // Will create a new table for the beliefs.
		double bestValueAssignment = Double.MAX_VALUE; // Best value that is initialized to a big number.
		int valueAssignment = 0; // What that will assigned.

		for (NodeId i : functionMsgs.keySet()) { // Create the belief of the agent.

			table = sumMessages(table, functionMsgs.get(i).getContext());

		}

		table = addDust(table);

		for (int i = 0; i < table.length; i++) { // OmerP - choose the best value assignment out of the table.

			if (table[i] < bestValueAssignment) {

				bestValueAssignment = table[i];
				valueAssignment = i;

			}

		}

		setValueAssignmnet(valueAssignment);

	}

	public void chooseValueLongAssignment() {

		double[] table = new double[this.domainSize];
		double bestValueAssignment = Double.MAX_VALUE; // Best value that is initialized to a big number.
		int valueAssignment = 0; // What that will assigned.

		for (NodeId i : functionMsgs.keySet()) { // Create the belief of the agent.

			table = sumMessageAsLong(table, functionMsgs.get(i).getContext());

		}

		for (int i = 0; i < table.length; i++) { // OmerP - choose the best value assignment out of the table.

			if (table[i] < bestValueAssignment) {

				bestValueAssignment = table[i];
				valueAssignment = i;

			}

		}

		setValueAssignmnet(valueAssignment);
		if (print) {
			printValueAssignment(valueAssignment, table);
		}

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Arithmetic Messages ******* ////

	// OmerP - produce an empty message for the first iteration.
	protected double[] produceEmptyTable(NodeId to, double[] table) {

		for (int i = 0; i < table.length; i++) {

			table[i] = 0;

		}

		return table;

	}

	// OmerP - produce message to a function node;
	protected double[] produceMessage(NodeId to, double[] table) {

		for (NodeId i : functionMsgs.keySet()) {

			if (!(i.compareTo(to) == 0)) {

				table = sumMessages(table, functionMsgs.get(i).getContext());
			}

		}

		return table;

	}

	// OmerP - Sum two tables of doubles.
	protected double[] sumMessages(double[] table1, double[] table2) {

		double[] sumTable = new double[table1.length];

		for (int i = 0; i < table1.length; i++) {

			sumTable[i] = table1[i] + table2[i];

		}

		return sumTable;

	}

	protected double[] sumMessageAsLong(double[] table1, double[] table2) {

		double[] sumTable = new double[table1.length];

		for (int i = 0; i < table1.length; i++) {

			sumTable[i] = table1[i] + table2[i];

		}

		return sumTable;

	}

	// OmerP - chooses the minimal value in the table and subtract it from each
	// value in the table with
	protected void substractMinimumValue(long[] table) {

		long alpha = Integer.MAX_VALUE; // Initialize alpha to be a big integer.

		for (long x : table) {

			alpha = Math.min(alpha, x);

		}

		for (int i = 0; i < table.length; i++) {

			table[i] = table[i] - alpha;

		}

	}

	// OmerP - chooses the minimal value in the table and subtract it from each
	// value in the table.
	protected double[] subtractMinimumValueD(double[] tableD) {

		double[] table = new double[this.domainSize];
		double alpha = Integer.MAX_VALUE; // Initialize alpha to be a big integer.

		for (double x : tableD) { // find minimum cell.
			alpha = Math.min(alpha, x);
		}

		for (int i = 0; i < tableD.length; i++) { // subtract minimum cell value, from all cells.
			table[i] = tableD[i] - alpha;
		}

		return table;

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Damping Methods ******* ////

	protected double[] damping(NodeId to, double[] table) {

		double[] dampedTable = new double[this.domainSize];
		dampedTable = dampedMessage(table, storedMessges.get(to));
		return dampedTable;

	}

	// OmerP - Multiplication of messages.
	protected double[] messageMultiplication(double[] table, double multiplicationFactor) {

		double[] multupliedTable = new double[this.domainSize];

		for (int i = 0; i < table.length; i++) {

			multupliedTable[i] = table[i] * multiplicationFactor;

		}

		return multupliedTable;

	}

	// OmerP - gets two double[] and calculate the damping vector.
	protected double[] dampedMessage(double[] currentMessage, double[] lastMessage) {

		double[] dampedMessage = new double[this.domainSize];

		double[] currentMessageAfterAlpha = messageMultiplication(currentMessage, (1 - dampingFactor)); // table after
																										// alpha.
		double[] lastMessageAfterAlpha = messageMultiplication(lastMessage, dampingFactor); // table after one minus
																							// alpha.

		for (int i = 0; i < this.domainSize; i++) {

			dampedMessage[i] = currentMessageAfterAlpha[i] + lastMessageAfterAlpha[i];

		}

		if (dampingPrint) {
			printDampedTable(lastMessageAfterAlpha, currentMessageAfterAlpha, dampedMessage);
		}

		return dampedMessage;

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Printing Data ******* ////

	@Override
	public void updateAlgorithmHeader() {

		AgentVariable.algorithmHeader = "Damping_Factor";

	}

	@Override
	public void updateAlgorithmData() {

		AgentVariable.algorithmData = this.dampingFactor + "";

	}

	@Override
	public void updateAlgorithmName() {
		MainSimulator.Algorithm algorithm = MainSimulator.algorithm ;
		/*
		MainSimulator.MaxSum_SY
		,MaxSum_split_SY, MaxSum_ASY, MaxSum_split_ASY;
	/*
	* 101 = MaxSum-SY; 102 =
			* MaxSum_split-SY; 103 = MaxSum-ASY; 104 = MaxSum_split-ASY;
	*/

		if (!damping && algorithm == MainSimulator.Algorithm.MaxSum_SY) {
			AgentVariable.AlgorithmName = "MaxSum_SY";
		}

		if (!damping && algorithm == MainSimulator.Algorithm.MaxSum_SY) {
			AgentVariable.AlgorithmName = "MaxSum_DMS_SY";
		}

		if (!damping && algorithm == MainSimulator.Algorithm.MaxSum_split_SY) {
			AgentVariable.AlgorithmName = "MaxSum_DMS_SCFG_SY";
		}


		if (!damping && algorithm == MainSimulator.Algorithm.MaxSum_ASY) {
			AgentVariable.AlgorithmName = "MaxSum_ASY";
		}

		if (!damping && algorithm == MainSimulator.Algorithm.MaxSum_ASY) {
			AgentVariable.AlgorithmName = "MaxSum_DMS_ASY";
		}

		if (!damping && algorithm == MainSimulator.Algorithm.MaxSum_split_ASY) {
			AgentVariable.AlgorithmName = "MaxSum_DMS_SCFG_ASY";
		}
		
		

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Flags Methods ******* ////

	@Override
	protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

		this.receiveMessageFlag = true;

	}

	@Override
	public void changeReceiveFlagsToFalse() {

		this.receiveMessageFlag = false;

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Getters ******* ////

	@Override
	protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm MsgAlgorithm) {

		int timestamp = functionMsgs.get(MsgAlgorithm.getSenderId()).getTimestamp(); // OmerP - will get the timestamp
																						// of the messages.

		return timestamp;

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Clear HashMap without loosing key ******* ////

	// OmerP - will clear the HashMap from values double.
	protected void clearHashMapDoubleValues(HashMap<NodeId, double[]> hashMapToClear) {

		for (NodeId i : hashMapToClear.keySet()) {

			hashMapToClear.put(i, null);

		}

	}

	// OmerP - will clear the HashMap from values double.
	protected void clearHashMapIntValues(HashMap<NodeId, Integer> hashMapToClear) {

		for (NodeId i : hashMapToClear.keySet()) {

			hashMapToClear.put(i, null);

		}

	}

	@Override
	public boolean getDidComputeInThisIteration() {
		// TODO Auto-generated method stub
		return receiveMessageFlag;
	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Print Messages ******* ////

	protected void printStoredMessage(MsgAlgorithmFactor msg) {

		System.out.println("VariableNode:(" + msg.getSenderId().getId1() + "," + msg.getSenderId().getId2()
				+ ") STORED a message for FunctionNode ("

				+ msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") with message context: "
				+ Arrays.toString(msg.getContext()) + ".\n");

	}

	protected void printSentdMessage(MsgAlgorithmFactor msg) {

		System.out.println("Computation Counter:(" + this.computationCounter + "), VariableNode:("
				+ msg.getSenderId().getId1() + "," + msg.getSenderId().getId2() + ") SENT a message for FunctionNode ("

				+ msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") with message context: "
				+ Arrays.toString(msg.getContext()) + " and timestamp:(" + msg.getTimeStamp() + ").\n");

	}

	protected void printReceivedMessage(MsgAlgorithmFactor msg) {

		System.out.println(
				"Computation Counter:(" + this.computationCounter + "), VariableNode:(" + msg.getRecieverId().getId1()
						+ "," + msg.getRecieverId().getId2() + ") RECEIVED a message from FunctionNode ("

						+ msg.getSenderId().getId1() + "," + msg.getSenderId().getId2() + ") with message context: "
						+ Arrays.toString(msg.getContext()) + " and timestamp:(" + msg.getTimeStamp() + ").\n");

	}

	protected void printValueAssignment(int valueAssignment, double[] belief) {

		System.out.println("Computation Counter:(" + this.computationCounter + ") ,VariableNode:("
				+ this.getNodeId().getId1() + "," + this.getNodeId().getId2() + ") belief is " + Arrays.toString(belief)
				+ ", and value assignment is:" + valueAssignment + ".\n");

	}

	protected void printValueAssignmentIteration(int valueAssignment, double[] belief) {

		System.out.println("Computation Counter:(" + this.numberofIterations + ") ,VariableNode:("
				+ this.getNodeId().getId1() + "," + this.getNodeId().getId2() + ") belief is " + Arrays.toString(belief)
				+ ", and value assignment is:" + valueAssignment + ".\n");

	}
	
	protected void printDampedTable(double[] oldTable, double[] newTable, double[] dampedTable) {

		System.out.println("VariableNode:(" + this.getNodeId().getId1() + "," + this.getNodeId().getId2() + ") "
				+ "old table: " + Arrays.toString(oldTable) + " new table: " + Arrays.toString(newTable)
				+ ", and damped table: " + Arrays.toString(dampedTable) + ".\n");

	}

	protected void printFlag() {

		if (this.receiveMessageFlag) {

			System.out.println("VariableNode:(" + this.getNodeId().getId1() + "," + this.getNodeId().getId2()
					+ "), Flag is UP.\n");

		} else {

			System.out.println("VariableNode:(" + this.getNodeId().getId1() + "," + this.getNodeId().getId2()
					+ "), Flag is DOWN.\n");

		}

	}

	

}
