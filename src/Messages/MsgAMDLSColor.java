package Messages;

import AgentsAbstract.NodeId;

public class MsgAMDLSColor extends MsgAMDLS {
	private Integer color;
	public MsgAMDLSColor(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int counter, Integer color) {
		super(sender, reciever, context, timeStamp, time, counter);
		this.color = color;
	}
	public Integer getColor() {
		// TODO Auto-generated method stub
		return this.color;
	}
	
	

}
