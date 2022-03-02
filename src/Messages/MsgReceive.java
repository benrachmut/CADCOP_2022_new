package Messages;

public class MsgReceive<Context> {

	private Context context;
	private Integer timestamp;
	
	public MsgReceive(Context context, Integer timeStamp) {
		super();
		this.context = context;
		this.timestamp = timeStamp;
	}
	
	public Context getContext() {
		return this.context;
	}
	public Integer getTimestamp() {
		return this.timestamp;
	}
	
	
}
