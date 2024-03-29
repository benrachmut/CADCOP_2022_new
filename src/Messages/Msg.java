package Messages;

import AgentsAbstract.NodeId;

public class Msg<InfoType> {

	private NodeId sender;
	private NodeId reciever;
	protected Object context;
	protected long timeOfMsg;
	private int timestamp;
	
	boolean withDelay;
	private int delayOfMsg;
	private boolean isLoss;
	private  int manualDelay;

	//private Integer delay;
	
	//private int mailerTime;
	public Msg(NodeId sender, NodeId reciever, Object context, int timeStamp, long agentTime) {
		super();
		this.sender = sender;
		this.reciever = reciever;
		this.context = context;
		this.timestamp = timeStamp;
		this.timeOfMsg = agentTime;
		this.withDelay = true;
		this.manualDelay = 0;
		this.delayOfMsg=0;
		this.isLoss = true;
	}

	public boolean getIsLoss(){
		return this.isLoss;
	}
	public void changeToNoLoss(){
		this.isLoss=false;
	}
	public void setWithDelayToFalse() {
		this.withDelay = false;
	}
	protected void setManualDelay(int delayOfMsg){
		this.manualDelay = delayOfMsg;

	}

	public int getManualDelay(){
		return this.manualDelay;
	}

	/*
	public void setMailerTime(int mailerTime) {
		this.mailerTime = mailerTime;
	}
	public int getMailerTime() {
		return this.mailerTime;
	}

*/


/*
	public void setDelay(Integer d) {
		this.delay = d;
		
	}
	*/
	public Integer getTimeStamp() {
		// TODO Auto-generated method stub
		return this.timestamp;
	}


	public NodeId getRecieverId() {
		return reciever;
	}
	
	public NodeId getSenderId() {
		return sender;
	}
	/*
	public Integer getDelay() {
		return this.delay;
	}
*/
	public Object getContext() {
		return context;
	}
	

	public long getTimeOfMsg() {
		return this.timeOfMsg;
	}

	public void setTimeOfMsg(int delay) {
		this.timeOfMsg = timeOfMsg+delay;	
	}
	
	public void setDelayOfMsg(int delay) {
		this.delayOfMsg = delay;
	}

	public int getDelayOfMsg() {
		return this.delayOfMsg;
	}
	@Override
	public String toString() {
	return "from "+this.sender.getId1()+" to "+ this.reciever.getId1()+ " time "+this.timeOfMsg;
	}


	public boolean isWithDelay() {
		return this.withDelay;
	}

	

	
	
	
}
