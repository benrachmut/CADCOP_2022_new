package Down;

import java.util.Collection;

public class CreatorDownPoission extends CreatorDown{
	private int[] lambdas = {600,1500};//{100,200,300,400,500,600,700,800,900,1000,1100,1200,1300,1400,1500};

	@Override
	protected String header() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Collection<? extends ProtocolDown> createCombinationsDown() {
		for (boolean b : imperfectCommunicationScenario) {
			
		}
		return null;
	}

	@Override
	protected ProtocolDown createDefultProtocol() {
		// TODO Auto-generated method stub
		return new ProtocolDownPoission();
	}

}
