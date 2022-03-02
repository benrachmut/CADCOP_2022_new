package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreatorDelaysPossion extends CreatorDelays {
	private int[] lambdas = {100,1000,2000,5000};//{100,200,300,400,500,600,700,800,900,1000,1100,1200,1300,1400,1500};
	@Override
	
	protected ProtocolDelay createDefultProtocol(double gamma) {
		return new ProtocolDelaysPossion(gamma);
	}

	@Override
	protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean isTimeStamp, double gamma) {
		List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
		for (int lambda : lambdas) {
			ans.add(new ProtocolDelaysPossion(isTimeStamp, gamma, lambda));
		} // sigma
		return ans;
	}

	@Override
	protected String header() {
		return "lambda";
	}
	@Override
	public String name() {
		return "Possion";
	}
}
