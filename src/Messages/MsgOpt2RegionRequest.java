package Messages;

import AgentsAbstract.NodeId;

abstract public class MsgOpt2RegionRequest extends MsgAlgorithm {
	public MsgOpt2RegionRequest(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
		super(sender, reciever, context, timeStamp, time);
		// TODO Auto-generated constructor stub
	}
}
