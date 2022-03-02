package AlgorithmInference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgAlgorithmFactor;
import Messages.MsgReceive;

public class MaxSumStandardVariableDelay extends MaxSumStandardVarible {

	///// ******* Variables ******* ////

	protected HashMap<NodeId, MaxSumMemory> neighborsMemory;
	protected HashMap<NodeId, Boolean> messagesArrivedControl;
	protected int neighborsSize;
	protected int timeStampToLook;
	private boolean print = false;
	private boolean printOnlyValueAssignment = false;
	protected boolean damping = true;
	protected boolean canCompute = false;

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Constructor and initialize Methods******* ////

	public MaxSumStandardVariableDelay(int dcopId, int D, int id1) {
		super(dcopId, D, id1);
		this.neighborsMemory = new HashMap<NodeId, MaxSumMemory>();
		this.messagesArrivedControl = new HashMap<NodeId, Boolean>();
		this.neighborsSize = this.functionMsgs.size();
		this.timeStampToLook = 0;
		updateAlgorithmHeader();
		updateAlgorithmData();
		updateAlgorithmName();
	}

	@Override
	public void updateAlgorithmHeader() {
		AgentVariable.algorithmHeader = "Damping Factor";
	}

	@Override
	public void updateAlgorithmData() {
		AgentVariable.algorithmData = this.dampingFactor + "";
	}

	
	
	public void updateNodeId() {
		this.nodeId = new NodeId(id + 1, true);
	}

	// OmerP - A method to initialize the memory of the agent will add the
	// neighbors.
	public void initializeNeighborsMemory() {

		for (NodeId i : functionMsgs.keySet()) {

			MaxSumMemory newNeighborMemory = new MaxSumMemory(this.nodeId, i, this.domainSize);
			neighborsMemory.put(i, newNeighborMemory);

		}

	}

	///// ******* Main Methods ******* ////

	public void resetAgentGivenParametersV5() {
	}

	// OmerP - will loop over the neighbors and will send to each one of the
	// neighbors. //Need to modify !!!!!

	public void sendMsgsP() {

		for (NodeId i : messagesToBeSent.keySet()) {

			if (!functionNodes.containsKey(i)) { // If I as a variable node does not holds the the function node.
				//mailer.sendMsg(messagesToBeSent.get(i));
				if (print) {
					printSentdMessage(messagesToBeSent.get(i));
				}
			}
			/*
			 * else { //If I as a variable node holds the the function node.
			 * List<MsgAlgorithm> messages = new ArrayList<MsgAlgorithm>();
			 * messages.add(messagesToBeSent.get(i)); if (print)
			 * {printSentdMessage(messagesToBeSent.get(i));} MaxSumStandardFunction
			 * functionNode = (MaxSumStandardFunction) functionNodes.get(i);
			 * 
			 * if (Main.MainSimulator.isMaxSumThreadDebug) {
			 * 
			 * System.err.println(this+" thread send message to "+messages.get(0).
			 * getRecieverId()); }
			 * 
			 * functionNodes.get(i).receiveAlgorithmicMsgs(messages);
			 * //functionNode.updateMessageInContext(messagesToBeSent.get(i)); }
			 */

			this.computationCounter++;
			messagesToBeSent.clear(); // When finish sending all the messages will clear the messages to be sent.
			clearMemoryFromAllNeighbors(this.timeStampToLook);
		} // synch
	}

	/*
	 * @Override protected void produceNewMessages() {
	 * 
	 * for (NodeId i : neighborsMemory.keySet()) {
	 * 
	 * createNewMessageFromMemory(this.timeStampToLook, i);
	 * 
	 * }
	 * 
	 * }
	 */
	@Override
	public void chooseValueLongAssignment() {

		double[] table = new double[this.domainSize];
		double bestValueAssignment = Double.MAX_VALUE; // Best value that is initialized to a big number.
		int valueAssignment = 0; // What that will assigned.

		for (NodeId i : neighborsMemory.keySet()) {

			MaxSumMemory maxSumMemory = neighborsMemory.get(i);
			// double[] context = maxSumMemory.findContextByIteration(this.timeStampToLook);
			double[] context;
			// New...

			try {

				context = maxSumMemory.findContextByIteration(this.timeStampToLook);

			}

			catch (NullPointerException e) {

				context = produceEmptyMessageForNullPointerExeption();

			}

			table = sumMessageAsLong(table, context);

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

	///// ******* Methods for the Async Version ******* ////

	protected void produceNewMessageForAsyncVersion() {

		for (NodeId i : functionMsgs.keySet()) {

			double[] sentTable = new double[this.domainSize];
			sentTable = produceMessage(i, sentTable); // For each specific neighbor, sum all messages excluding the
														// table of the receiving function node.
			sentTable = subtractMinimumValueD(sentTable);
			MsgAlgorithmFactor newMsg;

			if (damping) {

				sentTable = damping(i, sentTable); // Will produce a damped message.
				storedMessges.put(i, sentTable); // Will store the new message

			}

			newMsg = new MsgAlgorithmFactor(this.getNodeId(), i, sentTable, this.computationCounter,
					this.timeObject.getTimeOfObject());
			messagesToBeSent.put(i, newMsg);
			if (print) {
				printPreparedMessage(newMsg);
			}

		}

	}

	public void chooseValueLongAssignmentForAsyncVersion() {

		double[] table = new double[this.domainSize];
		double bestValueAssignment = Double.MAX_VALUE; // Best value that is initialized to a big number.
		int valueAssignment = 0; // What that will assigned.

		for (NodeId i : functionMsgs.keySet()) { // Create the belief of the agent.

			double[] context = new double[this.domainSize];

			try {

				context = functionMsgs.get(i).getContext();

			}

			catch (NullPointerException e) {context = produceEmptyMessageForNullPointerExeption();}

			table = sumMessageAsLong(table, context);

		}

		for (int i = 0; i < table.length; i++) { // OmerP - choose the best value assignment out of the table.

			if (table[i] < bestValueAssignment) {

				bestValueAssignment = table[i];
				valueAssignment = i;

			}

		}

		setValueAssignmnet(valueAssignment);
		if (printOnlyValueAssignment) {
			printValueAssignmentIteration(valueAssignment, table);
		}
		numberofIterations++; 
	}

	protected double[] produceEmptyMessageForNullPointerExeption() {

		double[] emptyTable = new double[this.domainSize];

		for (int i = 0; i < emptyTable.length; i++) {

			emptyTable[i] = 0;

		}

		return emptyTable;

	}

	// OmerP - produce message to a function node;
	protected double[] produceMessage(NodeId to, double[] table) {

		for (NodeId i : functionMsgs.keySet()) {

			if (!(i.compareTo(to) == 0)) {

				double[] context = new double[this.domainSize];

				try {

					context = functionMsgs.get(i).getContext();

				}

				catch (NullPointerException e) {

					context = produceEmptyMessageForNullPointerExeption();

				}

				table = sumMessages(table, context);
			}

		}

		return table;

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* MaxSumMemory Methods ******* ////

	// OmerP - A method that will store new context in the memory according to the
	// iteration.
	protected void storeNewContextInMemory(MsgAlgorithmFactor receivedMessage) {

		MaxSumMemory maxSumMemory = neighborsMemory.get(receivedMessage.getSenderId());
		Integer timeStamp = receivedMessage.getTimeStamp();
		double[] context = receivedMessage.getContext();
		// Print what i got to store...
		maxSumMemory.putContextInMemory(timeStamp, context);

	}

	// OmerP - A method that will check if all the message from the same iteration
	// was received.
	protected boolean didRecieveAllMessagesPerIteration(int iterationToLook) {

		int counter = 0;

		for (NodeId i : neighborsMemory.keySet()) {

			MaxSumMemory maxSumMemory = neighborsMemory.get(i);
			if (maxSumMemory.checkIfInMemory(iterationToLook)) {

				counter++;

			}

		}

		if (counter == this.neighborsSize) {

			return true;

		}

		else {

			return false;

		}

	}

	// OmerP - When all messages with the same iteration have been received this
	// method will create new messages to send.
	/*
	 * protected void createNewMessageFromMemory(int iterationToLook, NodeId
	 * receiverNode) {
	 * 
	 * double[] sentTable = new double[this.domainSize]; // Create a new table for
	 * the new message.
	 * 
	 * for (NodeId i : neighborsMemory.keySet()) {
	 * 
	 * if (!(i.compareTo(receiverNode) == 0)) { // If i is not the same as the
	 * receiver node.
	 * 
	 * MaxSumMemory maxSumMemory = neighborsMemory.get(i); // Will get the relevant
	 * memory. // double[] context = maxSumMemory.getMemory().get(iterationToLook);
	 * //Will // return the double of the memory in the relevant iteration. double[]
	 * context; // New
	 * 
	 * try {
	 * 
	 * context = maxSumMemory.getMemory().get(iterationToLook);
	 * 
	 * }
	 * 
	 * catch (NullPointerException e) {
	 * 
	 * context = produceEmptyMessageForNullPointerExeption();
	 * 
	 * }
	 * 
	 * sentTable = sumMessages(sentTable, context); // Will sum the messages.
	 * 
	 * }
	 * 
	 * }
	 * 
	 * sentTable = subtractMinimumValueD(sentTable);
	 * 
	 * if (damping) {
	 * 
	 * sentTable = damping(receiverNode, sentTable); // Will produce a damped
	 * message. storedMessges.put(receiverNode, sentTable); // Will store the new
	 * message
	 * 
	 * }
	 * 
	 * MsgAlgorithmFactor newMsg = new MsgAlgorithmFactor(this.getNodeId(),
	 * receiverNode, sentTable, this.computationCounter, this.time);
	 * messagesToBeSent.put(receiverNode, newMsg);
	 * 
	 * }
	 */
	// OmerP - will produce empty messages.
	@Override
	protected void produceEmptyMessage() {

		for (NodeId i : neighborsMemory.keySet()) { // Looping over the neighborsMemory.

			double[] sentTable = new double[this.domainSize]; // Create a new table.
			sentTable = produceEmptyTable(i, sentTable); // For each specific neighbor, produce an empty message.
			MsgAlgorithmFactor newMsg = new MsgAlgorithmFactor(this.getNodeId(), i, sentTable, this.computationCounter,
					this.timeObject.getTimeOfObject()); // Create new factor message.
			messagesToBeSent.put(i, newMsg); // Store the message in the message to by sent HashMap.
			if (print) {
				printPreparedMessage(newMsg);
			}
			if (damping) { // If damping is on it will store the new message so it could restore it for the
							// next message that will be created.

				storedMessges.put(i, newMsg.getContext());

			}

		}

	}

	protected void clearMemoryFromAllNeighbors(int iterationToLook) {

		for (NodeId i : neighborsMemory.keySet()) {

			MaxSumMemory maxSumMemory = neighborsMemory.get(i); // Will get the relevant memory.
			maxSumMemory.deleteContextFromMemory(iterationToLook - 1); // After the message was summed it will delete
																		// the message from the memory.

		}

	}

	///// ******* Print Methods ******* ////

	protected void printMessageUsedForCalculations() {

	}

	public void printPreparedMessage(MsgAlgorithmFactor msg) {

		System.out.println(
				"Computation Counter:(" + this.computationCounter + "),VariableNode:(" + msg.getSenderId().getId1()
						+ "," + msg.getSenderId().getId2() + ") PREPERED a message for FunctionNode ("

						+ msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") with message context: "
						+ Arrays.toString(msg.getContext()) + " and timestamp:(" + msg.getTimeStamp() + ").\n");

	}

	///// ******* Flags Methods ******* ////

	// If this is a sync run check if the size of the messages that was received is
	// equal to the number of my neighbors.
	protected boolean checkIfReceivedAllMessages() {

		int numberOfReceivedMessages = this.messagesArrivedControl.size();
		int sizeOfMyNeigbors = this.functionMsgs.size();
		if (numberOfReceivedMessages == sizeOfMyNeigbors) {

			messagesArrivedControl.clear();
			return true;

		} else {

			return false;

		}

	}

	protected void printFlag() {

		System.out.println(
				"VariableNode:(" + this.getNodeId().getId1() + "," + this.getNodeId().getId2() + "), Flag is UP.\n");

	}

	// -----------------------------------------------------------------------------------------------------------//

	///// ******* New Methods for the new simulator ******* ////

	// OmerP - The new method to send messages.
	// OmerP - The new method to send messages.
	@Override
	public void sendMsgs() {

		List<Msg> msgsToInsertMsgMailerBox = new ArrayList<Msg>(); // Create a new list of msgs.
		// HashMap<NodeId, List<Msg>> msgsToInsertMsgToMyFunctionNodes = new
		// HashMap<NodeId, List<Msg>>();

		for (NodeId recieverNodeId : messagesToBeSent.keySet()) { // Loop over messages to be sent.

			double[] context = messagesToBeSent.get(recieverNodeId).getContext(); // Get the context.
			MsgAlgorithmFactor newMsg = new MsgAlgorithmFactor(this.getNodeId(), recieverNodeId, context,
					this.timeStampCounter, this.timeObject.getTimeOfObject());

			if (this.functionNodes.containsKey(recieverNodeId)) {
				newMsg.setWithDelayToFalse();
				/*
				 * if (!msgsToInsertMsgToMyFunctionNodes.containsKey(recieverNodeId)) {
				 * List<Msg> sentList = new ArrayList<Msg>();
				 * msgsToInsertMsgToMyFunctionNodes.put(recieverNodeId, sentList); }
				 * msgsToInsertMsgToMyFunctionNodes.get(recieverNodeId).add(newMsg);
				 */
			}

			msgsToInsertMsgMailerBox.add(newMsg); // Add the message to the message box.
		}
/*
		for (NodeId recieverNodeId : msgsToInsertMsgToMyFunctionNodes.keySet()) {

			List<Msg> sentList = msgsToInsertMsgToMyFunctionNodes.get(recieverNodeId);
			if (!sentList.isEmpty()) {
				this.functionNodes.get(recieverNodeId).getInbox().insert(sentList);
			}
		}
		*/

		if (!msgsToInsertMsgMailerBox.isEmpty()) {
			outbox.insert(msgsToInsertMsgMailerBox); // Send the messages.
		}
		messagesToBeSent.clear();
		//msgsToInsertMsgToMyFunctionNodes.clear();

		if (MainSimulator.isThreadDebug) {

			System.out.println(this + "send context value");

		}

	}

	// OmerP - Will return the get did compute in this iteration.
	@Override
	public boolean getDidComputeInThisIteration() {

		return canCompute;
	}

	// Decide if to raise the flag of the agent.
	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

		this.canCompute = true;

	}

	@Override
	public void changeRecieveFlagsToFalse() {

		this.canCompute = false;

	}

	// OmerP - when a message received will update the context and flag that a
	// message was received.
	@Override
	public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {

		MsgAlgorithmFactor msgAlgorithmFactor = (MsgAlgorithmFactor) msgAlgorithm;

		double[] contextFix = (double[]) msgAlgorithmFactor.getContext(); // will cast the message object as a double[].

		MsgReceive<double[]> newMessageReceveid = new MsgReceive<double[]>(contextFix,
				msgAlgorithmFactor.getTimeStamp());

		functionMsgs.put(msgAlgorithmFactor.getSenderId(), newMessageReceveid);

		messagesArrivedControl.put(msgAlgorithmFactor.getSenderId(), true);

		if (print) {
			printReceivedMessage(msgAlgorithmFactor);
		}

		return true;
	}

	// OmerP - new information has arrived and the variable node will update its
	// value assignment.
	@Override
	public boolean compute() {

		chooseValueLongAssignmentForAsyncVersion();
		produceNewMessageForAsyncVersion();
		this.timeStampToLook++;
		return true;
	}

	// OmerP - Will send new messages for each one of the neighbors upon the
	@Override
	public void initialize() {

		initializeNeighborsMemory();
		produceEmptyMessage();
		sendMsgs();

	}

	@Override
	public void resetAgentGivenParametersV4() {

		this.neighborsSize = this.functionMsgs.size();
		this.neighborsMemory.clear();
		this.timeStampToLook = 0;
		this.computationCounter = 0;
		resetAgentGivenParametersV5();

	}

	@Override
	protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm MsgAlgorithm) {

		int timestamp = functionMsgs.get(MsgAlgorithm.getSenderId()).getTimestamp(); // OmerP - will get the timestamp
																						// of the messages.
		return timestamp;

	}

}
