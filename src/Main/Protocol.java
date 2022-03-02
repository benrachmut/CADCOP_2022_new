package Main;

import Delays.ProtocolDelay;
import Down.ProtocolDown;

public class Protocol {
	private ProtocolDown down;
	private ProtocolDelay delay;

	public Protocol(ProtocolDelay delay, ProtocolDown down) {
		super();
		this.down = down;
		this.delay = delay;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Protocol) {
			Protocol other = (Protocol) obj;
			boolean sameDelay = this.delay.equals(other.getDelay());
			boolean sameDown = this.down.equals(other.getDown());
			return sameDelay && sameDown;
		}
		return false;

	}

	public ProtocolDown getDown() {
		return down;
	}

	public ProtocolDelay getDelay() {
		return this.delay;
	}

	public void setSeeds(int id) {
		this.delay.setSeeds(id);
		//this.down.setSeeds(id);
	}
}
