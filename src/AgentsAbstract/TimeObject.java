package AgentsAbstract;

import java.util.Vector;

public class TimeObject {

	private long timeOfObject;
	private long idleTime;

	public TimeObject(long timeOfObject) {
		this.timeOfObject = timeOfObject;
		this.idleTime = 0;
	}

	public synchronized long getTimeOfObject() {
		return timeOfObject;
	}

	
	public synchronized void addIdealTime(long addToIdeal) {
		this.idleTime = this.idleTime+addToIdeal;

	}
	public synchronized void setTimeOfObject(long timeOfObject) {
	
		this.timeOfObject = timeOfObject;
	}

	public synchronized void addToTime(long atomicActionCounter) {
		this.timeOfObject += atomicActionCounter;
	}
	
	public synchronized long getIdleTime() {
		return this.idleTime;
	}
	
	
	
}
