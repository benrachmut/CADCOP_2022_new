package Messages;

import AgentsAbstract.NodeId;

public abstract class MsgAnyTime extends Msg<Integer> {

	public MsgAnyTime(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
		super(sender, reciever, context, timeStamp,  time);
		// TODO Auto-generated constructor stub
	}


}
