package Messages;

import AgentsAbstract.NodeId;

public class MsgAnyTimeDown extends MsgAnyTime {

	public MsgAnyTimeDown(NodeId sender, NodeId reciever, Object context, int timeStamp,
			long time) {
		super(sender, reciever, context, timeStamp, time);
	}



}
