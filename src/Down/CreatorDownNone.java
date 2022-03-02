package Down;

import java.util.Collection;
import java.util.HashSet;



public class CreatorDownNone  extends CreatorDown {

	@Override
	protected ProtocolDown createDefultProtocol(){
		// TODO Auto-generated method stub
		return new ProtocolDownNone();
	}

	@Override
	protected String header() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	protected Collection<? extends ProtocolDown> createCombinationsDown() {
		// TODO Auto-generated method stub
		return new HashSet<ProtocolDown>();
	}


}
