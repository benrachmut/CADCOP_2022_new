package Down;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import Delays.ProtocolDelay;

public abstract class CreatorDown {

	protected boolean[] imperfectCommunicationScenario= {false,true};

	public  List<ProtocolDown> createProtocolDowns() {
		List<ProtocolDown> ans = new ArrayList<ProtocolDown>();
		for (boolean b : imperfectCommunicationScenario) {
			if (b == false) {
				ans.add(createDefultProtocol());
			}
			else {
				ans.addAll(createCombinationsDown());	
			}
		}
		return ans;
	}
	
	public String getHeader() {
		return "";
	}


	protected abstract String header();
		
	protected abstract Collection<? extends ProtocolDown> createCombinationsDown();

	
	protected abstract ProtocolDown createDefultProtocol();
}