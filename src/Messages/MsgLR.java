package Messages;

import AgentsAbstract.NodeId;

public class MsgLR extends MsgAlgorithm {
	public MsgLR(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
		super(sender, reciever, context, timeStamp, time);
		// TODO Auto-generated constructor stub
	}

	public MsgLR(MsgAlgorithm m) {
		this(m.getSenderId(), m.getRecieverId(), m.getContext(), m.getTimeStamp(), m.getTimeOfMsg());
	}
}
