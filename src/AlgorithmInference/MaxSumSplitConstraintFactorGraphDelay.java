package AlgorithmInference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import AgentsAbstract.AgentFunction;
import AgentsAbstract.NodeId;

public class MaxSumSplitConstraintFactorGraphDelay extends MaxSumStandardFunctionDelay {

	///// ******* Variables ******* ////

	Random rnd;
	double SCFGRatio;
	MaxSumStandardFunctionDelay firstSplit, secondSplit;
	protected List<MaxSumStandardFunctionDelay> splitFunctionNodes;
	double[][] constraintsMatrixDouble;
	double[][] constraintMatrixBeta; 
	double[][] constraintMatrixOneMinusBeta; 
	
	// -----------------------------------------------------------------------------------------------------------//

	///// ******* Constructor ******* ////

	public MaxSumSplitConstraintFactorGraphDelay(int dcopId, int D, int id1, int id2, Integer[][] constraints) {
		super(dcopId, D, id1, id2, constraints);
		rnd = new Random(id1*100+id2*3+dcopId*17);
		rnd.nextDouble();
		this.SCFGRatio = randomSplitConstraintRatio(0.4); 
		createDoubleConstraintMatrix(constraints); 
		createSplitConstraintMatrices();  
		this.splitFunctionNodes = new ArrayList<MaxSumStandardFunctionDelay>();
		this.firstSplit = new MaxSumStandardFunctionDelay(dcopId, D, id1, id2, this.constraintMatrixBeta);
		this.secondSplit = new MaxSumStandardFunctionDelay(dcopId, D, id2, id1, this.constraintMatrixOneMinusBeta);
		splitFunctionNodes.add(this.firstSplit);
		splitFunctionNodes.add(this.secondSplit);
		updataNodes();
	

}
	
	

	///// ******* Initialize Methods ******* ////
	@Override
	public void meetVariables(NodeId VariableOneNodeId, NodeId VariableTwoNodeId) {
		
		for(int i = 0; i < splitFunctionNodes.size() ; i++) {
		
			splitFunctionNodes.get(i).meetVariables(VariableOneNodeId, VariableTwoNodeId);
			
		}
	
	}
	
	public double randomSplitConstraintRatio(double rangeMin) {
		
		double rangeMax = 1 - rangeMin;
		double splitConstraintRatio = 0; 
		splitConstraintRatio = rangeMin + (rangeMax - rangeMin) * rnd.nextDouble();
		return splitConstraintRatio;
		
	}
	
	///// ******* Initialize Split Constraint Factor Graph Methods ******* //// 

	//OmerP - Will add a new nodeId to the updated list. 
	public void updataNodes(){
		
		for(int i = 0 ; i < splitFunctionNodes.size() ; i++) {
			
			if(!nodes.contains(splitFunctionNodes.get(i).getNodeId()))
			
				updataNodes(splitFunctionNodes.get(i).getNodeId());
			
			}
		
	}
		
	//OmerP - Transition to constraint matrix with doubles.
	protected void createDoubleConstraintMatrix(Integer[][] constraints) {
		
		this.constraintsMatrixDouble = new double[this.domainSize][this.domainSize]; //Will create a new constraint table of doubles. 
		this.constraintsMatrixDouble = AgentFunction.turnIntegerToDoubleMatrix(constraints); //Transformation of the constraint table to double. 
		
	}
	
	//OmerP - Create two matrices with SCFGRatio.
	protected void createSplitConstraintMatrices() {
		
		this.constraintMatrixBeta = new double[this.domainSize][this.domainSize];
		this.constraintMatrixOneMinusBeta = new double[this.domainSize][this.domainSize];
		this.constraintMatrixBeta = MatrixMultiplication(this.constraintsMatrixDouble, SCFGRatio);
		this.constraintMatrixOneMinusBeta = MatrixMultiplication(this.constraintsMatrixDouble, 1-SCFGRatio);
		constraintMatrixOneMinusBeta = this.transposeConstraintMatrix(constraintMatrixOneMinusBeta);
		
		
	}
	
	// -----------------------------------------------------------------------------------------------------------//
	
	@Override
	public void resetAgentGivenParametersV4() {
		
		rnd = new Random(this.nodeId.getId1()*100+this.nodeId.getId2()*3+dcopId*17);
		rnd.nextDouble();
		
	}
	
	///// ******* Split Constraint Factor Graph Methods ******* ////

	// OmerP - Will generate split constraint factor graph.
	protected double[][] splitConstraintFactorGraph() {

		double[][] splitConstraintTable = this.constraintsMatrixDouble;

		if (nodeId.getId1() > nodeId.getId2()) {

			MatrixMultiplication(splitConstraintTable, SCFGRatio);

		}

		else {

			MatrixMultiplication(splitConstraintTable, 1 - SCFGRatio);

		}

		return splitConstraintTable;

	}

	// OmerP - Will generate split constraint factor graph and will return the
	// matrix.
	protected double[][] splitConstraintFactorGraph(double[][] matrixToSplit) {

		double[][] splitConstraintTable;

		if (nodeId.getId1() > nodeId.getId2()) {

			splitConstraintTable = MatrixMultiplication(matrixToSplit, SCFGRatio);

		}

		else {

			splitConstraintTable = MatrixMultiplication(matrixToSplit, 1 - SCFGRatio);

		}

		return splitConstraintTable;

	}

	//OmerP - Multiplication of table - FIXED.
	protected double[][] MatrixMultiplication(double[][] table, double multiplicationFactor) {
		
		double[][] tableDAfterMultiplication = new double[this.domainSize][this.domainSize]; 
		
		for(int i = 0 ; i < tableDAfterMultiplication.length ; i++) {
			
			for(int j = 0; j < tableDAfterMultiplication.length; j++) {
				
				tableDAfterMultiplication[i][j] = table[i][j]*multiplicationFactor; 
				
			}
			
		}
		
		return tableDAfterMultiplication; 
		
	}
	
	// -----------------------------------------------------------------------------------------------------------//
	
	///// ******* Getters ******* ////

	// OmerP - Will return the firstSplit MaxSumStandardFunction.
	public MaxSumStandardFunction getFirstSplit() {

		return firstSplit;

	}

	// OmerP - Will return the secondSplit MaxSumStandardFunction.
	public MaxSumStandardFunction getSecondSplit() {

		return secondSplit;

	}

	// OmerP - Will return the list of Max Sum Standard Function. 
	public List<MaxSumStandardFunctionDelay> getSplitFunctionNodes(){
		
		return splitFunctionNodes; 
		
	}
	
	
	
	
	

}
