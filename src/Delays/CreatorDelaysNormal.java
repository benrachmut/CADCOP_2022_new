package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import Delays.ProtocolDelay;



public class CreatorDelaysNormal extends CreatorDelays{

	
	private  double[] sigmas = {10};//{ 5,10,25,50 };
	private  double[] mus = {5,10,25,50,100,200 }; // 
	
	
	
	
	@Override
	protected ProtocolDelay createDefultProtocol(double gamma) {
		return new ProtocolDelayNormal(gamma);
	}
	
	@Override
	protected  Collection<? extends ProtocolDelay> createCombinationsDelay(boolean timestampBoolean, double gamma) {
		List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
		// ----For el delay

			for (double sigma : sigmas) {
				for (double mu : mus) {
					ans.add(new ProtocolDelayNormal(timestampBoolean, gamma, sigma, mu));
				} // mu
			} // sigma
		return ans;
	}
	
	protected String header() {
		return "Sigma,Mu"; 
		
	}

	@Override
	public String name() {
		return "Normal";
	}
}
