package Messages;

import AgentsAbstract.NodeId;

import java.util.Map;

public class MsgValueAssignmnet extends MsgAlgorithm {

	public MsgValueAssignmnet(NodeId sender, NodeId reciever, Object context, int timeStamp,long time) {
		super(sender, reciever, context, timeStamp,time);
	}

	public MsgValueAssignmnet(MsgAlgorithm m) {
	
		this(m.getSenderId(), m.getRecieverId(), m.getContext(), m.getTimeStamp(), m.getTimeOfMsg());
		/*
		if (this.getSenderId().getId1() == 2) {
			System.out.println("from MsgValueAssignmnet");
		}
		*/
	}

	@Override
	public String toString() {
		return this.getRecieverId()+" received Value from "+this.getSenderId();
	}


}
