package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreatorMissingMsgDistance extends CreatorDelaysMatrixDependenet{


 
	@Override
	protected ProtocolDelay createDefultProtocol(double gamma) {
		 return new ProtocolMissingMsgDistance(0);
	}

	@Override
	protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean isTimeStamp, double gamma) {
		List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
		ans.add(new ProtocolMissingMsgDistance());
		return ans;
	}

	@Override
	protected String header() {
		return "smallP";
	}

	@Override
	public String name() {
		return "Missing_Msg_Distance";
	}
}
