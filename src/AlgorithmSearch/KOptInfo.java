package AlgorithmSearch;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import AgentsAbstract.NodeId;
import Messages.MsgReceive;

public class KOptInfo {
	// ---------------
	private Integer valueAssingment;
	// ---------------
	private NodeId nodeId;
	// ---------------
	private TreeMap<NodeId, Integer[][]> neighborsConstraint;
	// ---------------
	private int[] domainArray;
	// ---------------
	private SortedMap<NodeId, MsgReceive<Integer>> neighborsValueAssignmnet; // id, variable

	// ------------ Constructor ------------
	public KOptInfo(Integer valueAssingment, NodeId nodeId, TreeMap<NodeId, Integer[][]> neighborsConstraint,
			int[] domainArray, SortedMap<NodeId, MsgReceive<Integer>> neighborsValueAssignmnet) {
		super();
		this.valueAssingment = valueAssingment;
		this.nodeId = nodeId;
		this.neighborsConstraint = neighborsConstraint;
		this.domainArray = domainArray;
		this.neighborsValueAssignmnet = createCopy(neighborsValueAssignmnet);
				
			
	}

	private SortedMap<NodeId, MsgReceive<Integer>> createCopy(
			SortedMap<NodeId, MsgReceive<Integer>> input) {
		SortedMap<NodeId, MsgReceive<Integer>> ans = new TreeMap<NodeId, MsgReceive<Integer>>();
		for (Entry<NodeId, MsgReceive<Integer>> e : input.entrySet()) {
			MsgReceive<Integer> msgR = new MsgReceive<Integer>(e.getValue().getContext(), e.getValue().getTimestamp());
			ans.put(e.getKey(), msgR);
		}
		return ans;
	}

	public Integer getValueAssingment() {
		return valueAssingment;
	}

	public NodeId getNodeId() {
		return nodeId;
	}

	public TreeMap<NodeId, Integer[][]> getNeighborsConstraint() {
		return neighborsConstraint;
	}

	public int[] getDomainArray() {
		return domainArray;
	}

	public SortedMap<NodeId, MsgReceive<Integer>> getNeighborsValueAssignmnet() {
		return neighborsValueAssignmnet;
	}

}
