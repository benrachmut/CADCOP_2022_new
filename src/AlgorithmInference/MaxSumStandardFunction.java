package AlgorithmInference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentFunction;
import AgentsAbstract.AgentVariableInference;
import AgentsAbstract.NodeId;
import Messages.MsgAlgorithm;
import Messages.MsgAlgorithmFactor;
import Messages.MsgReceive;

public class MaxSumStandardFunction extends AgentFunction {

	///// ******* Variables ******* ////

	protected boolean receiveMessageFlag;
	protected boolean iAmAsync = true; 
	HashMap<NodeId, double[]> storedMessgesTable = new HashMap<NodeId, double[]>();
	HashMap<NodeId, MsgAlgorithmFactor> messagesToBeSent = new HashMap<NodeId, MsgAlgorithmFactor>(); 
	HashMap<NodeId, double[][]> neighborsConstraintMatrix = new HashMap<NodeId, double[][]>(); 
	protected boolean printConstraints = false; 
	protected int computationCounter;
	private Random r;

	//-----------------------------------------------------------------------------------------------------------//

	///// ******* Control Variables ******* ////
	
	boolean storedMessageOn = false;
	boolean print = false; 
	 
	///// ******* Constructors and Initialization Methods ******* ////

	//OmerP - Constructor for regular factor graph. 
	public MaxSumStandardFunction(int dcopId, int D, int id1, int id2, Integer[][] constraints) {
		
		super(dcopId, D, id1, id2);
		this.r = new Random();
		initializeNeighborsConstraintMatrix(id1, id2, constraints);
		updataNodes(getNodeId());
		this.receiveMessageFlag = false;
		this.computationCounter = 0; 
		
	}
	
	//OmerP - Constructor for Split Constraint Factor Graph. 
	public MaxSumStandardFunction(int dcopId, int D, int id1, int id2, double[][] constraints) {
		
		super(dcopId, D, id1, id2);
		this.r = new Random();
		NodeId av1 = new NodeId(id1, true);
		NodeId av2 = new NodeId(id2, true);
		neighborsConstraintMatrix.put(av1, constraints);
		neighborsConstraintMatrix.put(av2, transposeConstraintMatrix(constraints));
		addDust();
		updataNodes(getNodeId());
		this.receiveMessageFlag = false;
		this.computationCounter = 0;
		
	}
	
	//Will Initialize the constraint matrix. 
	protected void initializeNeighborsConstraintMatrix(int id1, int id2 , Integer[][] constraints) {
		
		NodeId av1 = new NodeId(id1, true);
		NodeId av2 = new NodeId(id2, true);
		neighborsConstraintMatrix.put(av1, turnIntegerToDoubleMatrix(constraints));
		neighborsConstraintMatrix.put(av2, transposeConstraintMatrix(turnIntegerToDoubleMatrix(constraints)));
		addDust();
		if(printConstraints) {
			printConstraints(av1, neighborsConstraintMatrix.get(av1));
			printConstraints(av2, neighborsConstraintMatrix.get(av2));
		}
		
	}
		
	protected void addDust() {

		double rangeMin = 0;
		double rangeMax = 1;

		for (NodeId nodeIdConstraint : neighborsConstraintMatrix.keySet()) {

			double[][] constraintMatrix = neighborsConstraintMatrix.get(nodeIdConstraint);

			for (int i = 0; i < constraintMatrix.length; i++) {

				for (int j = 0; j < constraintMatrix.length; j++) {

					double randonValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
					randonValue = randonValue / 10000;
					constraintMatrix[i][j] = constraintMatrix[i][j] + randonValue;

				}

			}

		}

	}
	
	//-----------------------------------------------------------------------------------------------------------//

	///// ******* Main Methods ******* ////

	/**
	* OmerP - will send new messages for each one of the neighbors upon the initiation of the algorithm (iteration = 0) - Checked !!!
	 */
	public void initialize() {
			
	}
		
	//OmerP - function node don't need to update anything so he will return false - Checked !!!
	@Override
	protected boolean compute() {
		
		if(receiveMessageFlag) {
			
			produceNewMessages(); 
			this.computationCounter++; 
			
			return true; 
			
		}
		
		return false; 
		
	}
	
	//OmerP - To reset the agent if this is a new run - Need to check during run. 
	@Override
	public void resetAgentGivenParametersV2() {	
		this.storedMessgesTable.clear();
		messagesToBeSent.clear();
		resetAgentGivenParametersV3();
		
	}
	
	public void resetAgentGivenParametersV3() {}
	
	//OmerP - produce new messages - Checked !!!
	protected void produceNewMessages() {
		
		for(NodeId i: variableMsgs.keySet()) {
			
			double[] sentTable = new double[this.domainSize];
			sentTable = produceFunctionMessage(i);
			MsgAlgorithmFactor newMsg;
			
			if(iAmAsync) { 
				
				 newMsg = new MsgAlgorithmFactor(this.getNodeId(), i, sentTable, this.computationCounter, this.time);
			}
			
			else {
				 newMsg = new MsgAlgorithmFactor(this.getNodeId(), i, sentTable, 0, this.time);
			}
			
			messagesToBeSent.put(i, newMsg);
			//printStoredMessage(newMsg);
			
		}
			
	}
	
	//OmerP - will loop over the neighbors and will send to each one of the a message - Need to check during run. 
	@Override
	public void sendMsgs() {
		
		for(NodeId i: messagesToBeSent.keySet()) {
			
			//mailer.sendMsg(messagesToBeSent.get(i));
			
			if(print) {printSentMessage(messagesToBeSent.get(i));}
			
			if(storedMessageOn) {
				
				storedMessgesTable.put(i, messagesToBeSent.get(i).getContext());
				
			}
				
		}
		
		changeRecieveFlagsToFalse();
		if(print) {printFlag();}
		messagesToBeSent.clear();
		
		
	}
	
	@Override
	protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
		
		int timestamp = variableMsgs.get(msgAlgorithm.getSenderId()).getTimestamp(); //OmerP - will get the timestamp of the messages. 
		
		return timestamp; 
		
		

	}

	//OmerP - will get the message and update context in HashMap - Need to check during run. 
	@Override
	protected boolean  updateMessageInContext(MsgAlgorithm msgAlgorithm) {
		
		MsgAlgorithmFactor msgAlgorithmFactor = (MsgAlgorithmFactor) msgAlgorithm;

		if(print) {printReceivedMessage(msgAlgorithmFactor);}
		
		double[] contextFix = (double[]) msgAlgorithmFactor.getContext(); //will cast the message object as a double[].
		
		MsgReceive<double[]> newMessageReceveid = new MsgReceive<double[]>(contextFix, msgAlgorithmFactor.getTimeStamp()); //
		
		variableMsgs.put(msgAlgorithmFactor.getSenderId(), newMessageReceveid);
		
		changeRecieveFlagsToTrue(msgAlgorithm);
		
		if(print) {printFlag();}
		return true;

	}
	
	//-----------------------------------------------------------------------------------------------------------//
	
	///// ******* ConstraintMatrix ******* //// 
			
	//OmerP - Will initialize a matrix to zero.  
	protected void initializeMatrixToZero(double[][] matrix) {
		
		for(int i = 0; i < matrix.length ; i++) {
			
			for(int j = 0; j < matrix.length ; j++) {
				
				matrix[i][j] = 0; 
				
			}
			
			
		}
		
	}
	
	//-----------------------------------------------------------------------------------------------------------//

    ///// ******* Arithmetic Methods ******* ////
	
	//OmerP - Multiplication of table - FIXED.
	protected long[][] tableMultiplication(long[][] table, long multiplicationFactor) {
		
		long[][] tableDAfterMultiplication = new long[this.domainSize][this.domainSize]; 
		
		for(int i = 0 ; i < tableDAfterMultiplication.length ; i++) {
			
			for(int j = 0; j < tableDAfterMultiplication.length; j++) {
				
				tableDAfterMultiplication[i][j] = table[i][j]*multiplicationFactor; 
				
			}
			
		}
		
		return tableDAfterMultiplication; 
		
	}
	
	//OmerP - Get the best value of the matrix - FIXED.
	protected double[] getBestValueTable(double[][] constraintMatrix) {
		
		double[] table = new double[this.domainSize];
		
		for(int i = 0 ; i < constraintMatrix.length ; i++) {
			
			double bestCurrentValue = Double.MAX_VALUE; 

			for(int j = 0 ; j < constraintMatrix.length ; j++) {
				
				increaseAtomicCounter();
				
				if(constraintMatrix[i][j] < bestCurrentValue) {
					
					bestCurrentValue = constraintMatrix[i][j]; 
					
				}
								
			}

			table[i] = bestCurrentValue; 
			
		}
		
		return table; 
		
	}
	
	//OmerP - Add an table to the matrix - FIXED. 
	protected double[][] addTableToMatrix(double[] receivedTable, double[][] constraintMatrix){
		
		double[][] returenedMatrix = new double[this.domainSize][this.domainSize];
		
		
		for(int i = 0; i < this.domainSize ; i++) {
			
			for(int j = 0; j < this.domainSize ; j++) {
				
				returenedMatrix[i][j] = constraintMatrix[i][j] + receivedTable[j];
				
			}
			
		}
	
		return returenedMatrix;
		
	}
	
	//-----------------------------------------------------------------------------------------------------------//

    ///// ******* Send Messages ******* ////
	
	//OmerP - Will produce function message
	protected double[] produceFunctionMessage(NodeId to) {
		
		double[] sentMessage = new double[this.domainSize]; //The message that will be sent.
		double[][] constraintMatrix = neighborsConstraintMatrix.get(to);
				
		if(getOtherNodeIdMessage(to) != null) { //If the message that the other variable node sent is not Null.
			
			constraintMatrix = addTableToMatrix(getOtherNodeIdMessage(to), constraintMatrix); //Will add the second variable node message to the constraint matrix.
			
		}
		
		sentMessage = getBestValueTable(constraintMatrix);  //The best value will be chosen out of the matrix. 
		return sentMessage; //Sending the message. 
		
	}
				
	//-----------------------------------------------------------------------------------------------------------//

	///// ******* Stored Message Methods ******* ////

	protected void storeNewMessage(NodeId nodeId, double[] table) {
		
		if(storedMessageOn) {
			
			storedMessgesTable.put(nodeId, table);
			
		}
		
	}
		
	protected boolean areDifferentMessages(NodeId to, double[] table) {
		
		double[] lastStroedMessage = getLastSavedMessage(to);
		
		for(int i = 0 ; i < table.length ; i ++) {
			
			if(table[i] != lastStroedMessage[i]) {
				
				return true;
				
			}
			
		}
		
		return false; 
		
	}
	
	//OmerP - Loops over the messagesSent map and return the tableD that was saved. 
	protected double[] getLastSavedMessage(NodeId recevier) {
		
		for(NodeId i: variableMsgs.keySet()) {
			
			if(i.compareTo(recevier) == 0) {
				
				return variableMsgs.get(i).getContext(); 
				
			}
			
		}
		
		double[] emptyMessage = new double[this.domainSize];
	
		return emptyMessage;
		
	}
	
	//-----------------------------------------------------------------------------------------------------------//
	
    ///// ******* Getters ******* ////
	
	//OmerP - will return the message that is from the other variable node. 
	protected double[] getOtherNodeIdMessage(NodeId to) {
		
		
		if(variableMsgs.size() == 1) { //Check the size of the neighbors, if it is 1, it is not full yet and will return null. 
			
			return null; 
			
		}
			
		NodeId[] temp = new NodeId[2]; //Create an array of NodeId in the size of 2. 
		
		variableMsgs.keySet().toArray(temp); 
				
		if(to.equals(temp[0])) {
						
			return variableMsgs.get(temp[1]).getContext(); //Will return the message that received from the other variable node that was received. 
		}
		
		else {
			
			return variableMsgs.get(temp[0]).getContext(); //Will return the message that received from the other variable node that was received. 
			
		}
		
	}

	//OmerP - Parameter that are needed to be initialized every time a new problem is initialized. 
	@Override
	protected void resetAgentGivenParameters() {

		for(NodeId i : variableMsgs.keySet()) {
			
			variableMsgs.replace(i, variableMsgs.get(i), null); //OmerP - will put null instead of the value that was stored. 
			
		}
		
		for(NodeId i : storedMessgesTable.keySet()) {
			
			storedMessgesTable.replace(i, storedMessgesTable.get(i), null); //OmerP - will put null instead of the value that was stored. 
			
		}
		
		resetAgentGivenParametersV2();
	}
	
    ///// ******* Flags Methods ******* ////

	//OmerP - Flag that should be down after the all the messages were sent. 
	@Override
	public void changeRecieveFlagsToFalse() {
		
		this.receiveMessageFlag = false;
		
	}

	//OmerP - Flag that should be raised when a message was received. 
	@Override
	protected void changeRecieveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

		this.receiveMessageFlag = true;
		
	}

	@Override
	public boolean getDidComputeInThisIteration() {

		return receiveMessageFlag;
	}

	//OmerP - will clear the HashMap from values double. 
	protected void clearHashMapIntValues(HashMap<NodeId, Integer> hashMapToClear) {
		
		for(NodeId i: hashMapToClear.keySet()) {
			
			hashMapToClear.put(i, null);
			
		}
		
	}
	
	public HashMap<NodeId, double[][]> getNeighborsConstraintMatrix() {
		
		return this.neighborsConstraintMatrix;
		
	}

	//-----------------------------------------------------------------------------------------------------------//

	///// ******* Tests and Prints Methods ******* ////

	protected void printStoredMessage(MsgAlgorithmFactor msg) {
		
		System.out.println("FunctionNode:(" + msg.getSenderId().getId1() + "," + msg.getSenderId().getId2() + ") STORED a message for VariableNode ("
				
				+ msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") with message context: " + Arrays.toString(msg.getContext()) + ".\n");
		
	}
	
	protected void printSentMessage(MsgAlgorithmFactor msg) {
		
		System.out.println("Computation Counter:(" + this.computationCounter + "),FunctionNode:(" + msg.getSenderId().getId1() + "," + msg.getSenderId().getId2() + ") SENT a message for VariableNode ("
				
				+ msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") with message context: " + Arrays.toString(msg.getContext()) + " and timestamp:(" + msg.getTimeStamp() + ").\n");
		
	}
	
	protected void printReceivedMessage(MsgAlgorithmFactor msg) {
		
		System.out.println("Computation Counter:(" + this.computationCounter + "), FunctionNode:(" + msg.getRecieverId().getId1() + "," + msg.getRecieverId().getId2() + ") RECEIVED a message from VariableNode ("
				
				+ msg.getSenderId().getId1() + "," + msg.getSenderId().getId2() + ") with message context: " + Arrays.toString(msg.getContext()) + " and timestamp:(" + msg.getTimeStamp() + ").\n");
		
	}
	
	//OmerP - A method to check that the creation of the transpose matrix went correctly. 
	protected void checkIfTransposedCorrectly(double[][] constraint , double[][] constraintTranspose) {
		
		if(constraint.length != constraintTranspose.length) {
			
			System.out.println("Constraint matrix and constraint transpose are not in the same size.\n");
			
		}
		
		
		for(int i = 0; i < constraint.length ; i++) {
			
			for(int j = 0; j < constraint.length ; j++) {
				
				if(Double.compare(constraint[i][j], constraintTranspose[j][i]) != 0) {
					
					System.out.println("Bug in transposing matrix");
					break;
				
				}
	
			}
			
		}
		
		System.out.println("Constraint:" + Arrays.deepToString(constraint) + ".\n");
		System.out.println("Transpose Constraint:" + Arrays.deepToString(constraintTranspose) + ".\n");
		System.out.println("Transpose is OK.\n");
		
		
	}
	
	protected void printConstraints(NodeId av, double[][] constraints) {
		
		System.out.println("Function node (" + this.getNodeId().getId1() +"," + this.getNodeId().getId2() + ") "
				+ "constraint matrix with (" + av.getId1() + "," + av.getId2() + ") is "+ Arrays.deepToString(constraints) + ".\n");

		
	}
	
	protected void printFlag() {
			
			if(this.receiveMessageFlag) {
				
				System.out.println("FunctionNode:(" + this.getNodeId().getId1() + "," + this.getNodeId().getId2() + "), Flag is UP.\n");
				
			}else {
				
				System.out.println("FunctionNode:(" + this.getNodeId().getId1() + "," + this.getNodeId().getId2() + "), Flag is DOWN.\n");
				
			}
			
		}
	
	protected void printIncreaseInAtomicCounter() {
		
		System.out.println("FunctionNode:(" + this.getNodeId().getId1() + "," + this.getNodeId().getId2() + ") increased its atomic operation to(" + this.atomicActionCounter + ").\n");
		
	}
	
	//-----------------------------------------------------------------------------------------------------------//

    ///// ******* Atomic Counter - NCLO ******* ////
	
	protected void increaseAtomicCounter() {
		
		this.atomicActionCounter = atomicActionCounter + 1; 
		if(print) {printIncreaseInAtomicCounter();}
		
		
	}

	
	// -----------------------------------------------------------------------------------------------------------//
	
	

	
}
