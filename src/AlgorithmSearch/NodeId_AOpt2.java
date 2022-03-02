package AlgorithmSearch;

import AgentsAbstract.NodeId;

public class NodeId_AOpt2 extends NodeId{

	private Integer color;
	private Integer counter;
	private KOptInfo kOptInfo;
	private int numberOfFriendships;
	/*
	public NodeId_AOpt2(int id1) {
		super(id1);
	}
	*/
	public NodeId_AOpt2(NodeId nodeId) {
		super(nodeId.getId1(),false);
		color = null;
		counter = null;
		kOptInfo = null;
		numberOfFriendships=0;
	}
	public Integer getColor() {
		return this.color;
	}
	public void setColor(Integer color) {
		this.color = color;
	}
	public Integer getCounter() {
		return counter;
	}
	public void setCounter(Integer counter) {
		this.counter = counter;
	}
	public KOptInfo getkOptInfo() {
		return kOptInfo;
	}
	public void setkOptInfo(KOptInfo kOptInfo) {
		this.kOptInfo = kOptInfo;
	}
	public int getNumberOfFrienships() {
		return numberOfFriendships;
	}
	public void increaseFriendshipByOne() {
		numberOfFriendships = numberOfFriendships + 1;
	}

	

}
