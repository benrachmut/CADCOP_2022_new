package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreatorDelaysNone extends CreatorDelays {

	@Override
	protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean isTimeStamp, double gamma) {
		List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
		// ----For el delay

		ans.add(new ProtocolDelayNone(gamma));

		return ans;
	}

	@Override
	protected String header() {
		return "";
	}

	@Override
	protected ProtocolDelay createDefultProtocol(double gamma) {
		return new ProtocolDelayNone(gamma);

	}

	@Override
	public String name() {
		return "None";
	}
}
