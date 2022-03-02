package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreatorDelaysExponential extends CreatorDelays {

	private double[] lambdas = {100,250,500,750,1000,1250,1500};
	
	@Override
	protected ProtocolDelay createDefultProtocol(double gamma) {
		return new ProtocolDelayExponential(gamma);
	}

	@Override
	protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean isTimeStamp, double gamma) {
		List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
		for (double lambda : lambdas) {
			ans.add(new ProtocolDelayExponential(isTimeStamp, gamma, lambda));
		} // sigma
		return ans;
	}

	@Override
	protected String header() {
		return "lambda";
	}

	@Override
	public String name() {
		return "Exponential";
	}
	
	



}
