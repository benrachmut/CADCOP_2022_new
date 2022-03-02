package Messages;

import AgentsAbstract.NodeId;

public class MsgAlgorithm extends Msg{

	
	public MsgAlgorithm(NodeId sender, NodeId reciever, Object context, int timeStamp, long time) {
		super(sender, reciever, context, timeStamp,time);
		// TODO Auto-generated constructor stub
	}
	

	public void setContext(Object input) {
		this.context = input;
	}



	




	
}
