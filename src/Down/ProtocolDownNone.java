package Down;


public class ProtocolDownNone extends ProtocolDown{


	@Override
	public void setSeeds(int seed) {

	}

	@Override
	protected String getStringParamets() {
		return null;
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDown other) {
		return false;
	}
}
