package Down;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import AgentsAbstract.Agent;
import Delays.ProtocolDelay;
import Main.MainSimulator;

public abstract class ProtocolDown {

	protected int[][] counterRowSmall;
	protected int[][] counterRowLarge;

	
	public ProtocolDown() {
		int A = MainSimulator.A;
		counterRowSmall = new int[A][A];
		counterRowLarge = new int[A][A];
	}
	
	abstract public void setSeeds(int seed);


	public String toString() {
		return  getStringParamets();
	}

	protected abstract String getStringParamets();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProtocolDown) {
			ProtocolDown other = (ProtocolDown) obj;
			return checkSpecificEquals(other);
		}
		return false;
	}

	protected abstract boolean checkSpecificEquals(ProtocolDown other);




}
