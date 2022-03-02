package AlgorithmInference;

import java.util.Arrays;
import java.util.HashMap;

import AgentsAbstract.NodeId;


public class MaxSumMemory {
	
	///// ******* Variables ******* ////

	protected HashMap<Integer, double[]> memory; 
	protected NodeId nodeId; 
	protected NodeId neighbor; 
	protected int domainSize; 
	protected boolean print = false; 
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Constructor ******* ////

	public MaxSumMemory(NodeId nodeid, NodeId neighbor, int domainSize) {
			
			this.nodeId = nodeid; 
			this.neighbor = neighbor;
			this.memory = new HashMap<Integer, double[]>();
			this.domainSize = domainSize;
			
	}
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Methods ******* ////

	//OmerP - Store a context in the memory. 
	protected void putContextInMemory(Integer iteration, double[] context) {
				
		memory.put(iteration, context);
		if(print) {printStoredMessage(iteration, context);}
				
	}
	
	//OmerP - Find a context according to an iteration. 
	protected double[] findContextByIteration(int iteration) {
		
		double[] context = new double[this.domainSize]; 
		
		try {
			
			context = memory.get(iteration);
			
			} catch (Exception e) {
				
				System.out.println("Error - NodeId:(" + this.nodeId.getId1() + "," + this.nodeId.getId2() + "), did not found message iteration"
						+ "(" + iteration + ") from (" + this.neighbor.getId1() + "," + this.neighbor.getId2() + ").\n");
				
			}
		
		return context;
		
		
	}
	
	//OmerP - Will remove a mapping from the memory. 
	protected void deleteContextFromMemory(int iteration) {
		
		if(checkIfInMemory(iteration)) {
			
			memory.remove(iteration);
		}
		
		
	}
	
	//OmerP - Check if the memory contain this context. 
	protected boolean checkIfInMemory(Integer iteration) {
		
		if(memory.containsKey(iteration)) {
			
			//System.out.println("Error - NodeId:(" + this.nodeId.getId1() + "," + this.nodeId.getId2() + "), allready has a mapping of this message.\n");
			return true;
		}
		
		return false;
	}
	
	///// ******* Getters ******* ////

	//OmerP - Return the entire memory. 
	public HashMap<Integer, double[]> getMemory(){
		
		return this.memory;
		
	}
	
	//OmerP  - Return the nodeId of the memory of the agent. 
	public NodeId getMemoryNodeId() {
		
		return this.nodeId;
		
	}
	
	protected void printStoredMessage(Integer iteration, double[] context) {
		
		String nodeOne = "VariableNode"; 
		String nodeTwo = "FunctionNode"; 
		
		if(this.nodeId.getId2() > 0) {
			
			nodeOne = "FunctionNode"; 
			nodeTwo = "VariableNode";
			
		}
	
		System.out.println("" + nodeOne + ":(" + this.nodeId.getId1() + "," + this.nodeId.getId2() + ") RECIEVED from "
				+  nodeTwo + " (" + this.neighbor.getId1() + "," + this.neighbor.getId2() + ") a message, timestamp:(" + iteration + "),"
						+ " context " + Arrays.toString(context) + ".\n");
		
	}
	
	// -----------------------------------------------------------------------------------------------------------//

}
