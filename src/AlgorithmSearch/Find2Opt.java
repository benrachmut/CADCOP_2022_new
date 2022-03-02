package AlgorithmSearch;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import AgentsAbstract.NodeId;
import Messages.MsgReceive;

public class Find2Opt {
//---------------
	private Integer currentLocalCost; // id, variable
	private Integer currentValueAssingment1;
	private Integer currentValueAssingment2;
//---------------
	private NodeId nodeId1;
	private NodeId nodeId2;
//---------------
	private TreeMap<NodeId, Integer[][]> neighborsConstraint1;
	private TreeMap<NodeId, Integer[][]> neighborsConstraint2;
//---------------
	private int[] domainArray1;
	private int[] domainArray2;
//---------------
	private SortedMap<NodeId, Integer> neighborsValueAssignmnet1; // id, variable
	private SortedMap<NodeId, Integer> neighborsValueAssignmnet2;

	private Integer foundValueAssignmnet1;
	private Integer foundValueAssignmnet2;
	private Integer bestCostFound;

	private int atomicActionCounter;
	private int lr;
	
	public Find2Opt(KOptInfo kOptInfo1, KOptInfo kOptInfo2) {
		super();
		this.currentValueAssingment1 = kOptInfo1.getValueAssingment();
		this.currentValueAssingment2 = kOptInfo2.getValueAssingment();
		this.nodeId1 = kOptInfo1.getNodeId();
		this.nodeId2 = kOptInfo2.getNodeId();
		this.neighborsConstraint1 = kOptInfo1.getNeighborsConstraint();
		this.neighborsConstraint2 = kOptInfo2.getNeighborsConstraint();
		this.domainArray1 = kOptInfo1.getDomainArray();	
		this.domainArray2 = kOptInfo2.getDomainArray();
		this.neighborsValueAssignmnet1 = copyNeighborValAss(kOptInfo1.getNeighborsValueAssignmnet());
		this.neighborsValueAssignmnet2 = copyNeighborValAss(kOptInfo2.getNeighborsValueAssignmnet());
		
		//----- CALCULATIONS -----
		this.atomicActionCounter = 0;
		this.foundValueAssignmnet1 = currentValueAssingment1;
		this.foundValueAssignmnet2 = currentValueAssingment2;
		this.bestCostFound = this.findCurrentLocalCost();
		this.currentLocalCost = this.bestCostFound; 
		this.findOpt2ValueAssignments();	
		this.lr = this.currentLocalCost - this.bestCostFound;
	}

	private void findOpt2ValueAssignments() {
		TreeMap<Integer, Integer> costPerDomainNoLinkOne = getCostPerDomainNoLinkOne(); // domain, cost without link
		TreeMap<Integer, Integer> costPerDomainNoLinkTwo = getCostPerDomainNoLinkTwo(); // domain, cost without link

		for (int i = 0; i < domainArray1.length; i++) {
			int d1 = domainArray1[i];
			int costD1 = costPerDomainNoLinkOne.get(d1);
			for (int j = 0; j < domainArray2.length; j++) {
				int d2 = domainArray2[j];
				
				if (!(d1==this.currentValueAssingment1 && d2 == this.currentValueAssingment2)) {
					int costD2 = costPerDomainNoLinkTwo.get(d2);
					Integer costOfLink = findCostOfLink(d1, d2);
					Integer cost = costD1 + costD2 + costOfLink;
					if (cost < bestCostFound) {
						this.foundValueAssignmnet1 = d1;
						this.foundValueAssignmnet2 = d2;
						this.bestCostFound = cost;
					}
				}
				
			}
		}
	}

	/**
	 * look for cost per domain, the cost does not include the constraints with
	 * partner
	 * 
	 * @return
	 */

	private TreeMap<Integer, Integer> getCostPerDomainNoLinkTwo() {
		TreeMap<Integer, Integer> costPerDomainNoLinkTwo = new TreeMap<Integer, Integer>(); // domain, cost

		for (int j = 0; j < domainArray2.length; j++) {
			int d2 = domainArray2[j];
			Integer currentCostTwo = findCostWithoutLink(d2, neighborsValueAssignmnet2, neighborsConstraint2, nodeId1);
			costPerDomainNoLinkTwo.put(d2, currentCostTwo);

		}
		return costPerDomainNoLinkTwo;
	}

	/**
	 * look for cost per domain, the cost does not include the constraints with
	 * partner
	 * @return map of domain and cost per domain
	 */
	private TreeMap<Integer, Integer> getCostPerDomainNoLinkOne() {
		TreeMap<Integer, Integer> costPerDomainNoLinkOne = new TreeMap<Integer, Integer>(); // domain, cost
		for (int i = 0; i < domainArray1.length; i++) {
			int d1 = domainArray1[i];
			Integer currentCostOne = findCostWithoutLink(d1, neighborsValueAssignmnet1, neighborsConstraint1, nodeId2);
			costPerDomainNoLinkOne.put(d1, currentCostOne);
		}
		return costPerDomainNoLinkOne;
	}
	/**
	 * find cost per input domain
	 * @return cost per domain
	 */
	public int findCostWithoutLink(int domain, SortedMap<NodeId, Integer> neighborsValueAssignmnet,
			TreeMap<NodeId, Integer[][]> neighborsConstraint, NodeId nodeIdLink) {
		int ans = 0;
		for (Entry<NodeId, Integer> e : neighborsValueAssignmnet.entrySet()) {
			if (e.getValue() != null) {
				if (!e.getKey().equals(nodeIdLink)) {
					int nValueAssignmnet = e.getValue();
					Integer[][] nConst = neighborsConstraint.get(e.getKey());
					ans += nConst[domain][nValueAssignmnet];
					atomicActionCounter = atomicActionCounter + 1;

				}
			}
		}
		return ans;
	}

	
	
	private Integer findCurrentLocalCost() {
		Integer currentCostOneNoLink = findCostWithoutLink(currentValueAssingment1, neighborsValueAssignmnet1,
				neighborsConstraint1, nodeId2);
		Integer currentCostTwoNoLink = findCostWithoutLink(currentValueAssingment2, neighborsValueAssignmnet2,
				neighborsConstraint2, nodeId1);
		Integer costOfLink = findCostOfLink(currentValueAssingment1, currentValueAssingment2);

		return currentCostOneNoLink + currentCostTwoNoLink + costOfLink;
	}

	private Integer findCostOfLink(Integer valueAssingment1, Integer valueAssingment2) {

		Integer[][] const1And2 = this.neighborsConstraint1.get(nodeId2);
		int ans12 = const1And2[valueAssingment1][valueAssingment2];

		Integer[][] const2And1 = this.neighborsConstraint2.get(nodeId1);
		int ans21 = const2And1[valueAssingment2][valueAssingment1];

		if (ans12 != ans21) {
			throw new RuntimeException("it should be equals");
		} else {
			this.atomicActionCounter = atomicActionCounter + 1;
			return ans12;
		}

	}


	private SortedMap<NodeId, Integer> copyNeighborValAss(SortedMap<NodeId, MsgReceive<Integer>> input) {
		SortedMap<NodeId, Integer> ans = new TreeMap<NodeId, Integer>();
		for (Entry<NodeId, MsgReceive<Integer>> e : input.entrySet()) {
			ans.put(e.getKey(), e.getValue().getContext());
		}
		return ans;
	}

	public int getAtomicActionCounter() {
		return this.atomicActionCounter;
	}

	public int getLR() {
		return this.lr;
	}

	public Integer getValueAssignmnet1() {
		return this.foundValueAssignmnet1;
	}
	
	public Integer getValueAssignmnet2() {
		return this.foundValueAssignmnet2;
	}

}
