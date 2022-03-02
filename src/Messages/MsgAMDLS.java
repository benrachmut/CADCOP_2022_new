package Messages;

import AgentsAbstract.NodeId;

public class MsgAMDLS extends MsgValueAssignmnet {

	private int counter;
	private boolean fromFuture;
	public MsgAMDLS(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int counter) {
		super(sender, reciever, context, timeStamp, time);
		this.counter = counter;
		fromFuture = false;
	}
	
	public MsgAMDLS(MsgAMDLSColor m) {
		super(m.getSenderId(), m.getRecieverId(), m.getContext(), m.getTimeStamp(),m.getTimeOfMsg());
		this.counter = m.getCounter();
		fromFuture = false;

	}

	public int getCounter() {
		return this.counter;
	}

	public boolean isFromFuture() {
		return fromFuture;
	}
	
	public void setFromFutureToTrue() {
		 fromFuture = true;
	}

}
