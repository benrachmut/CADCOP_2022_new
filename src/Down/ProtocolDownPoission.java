package Down;

public class ProtocolDownPoission extends ProtocolDown {

	private int lambda;
	
	public ProtocolDownPoission() {
		super();
		this.lambda=0;
	}

	public ProtocolDownPoission(int lambda) {
		super();
		if (lambda>100) {
			throw new RuntimeException("lambda is too big");
		}
		this.lambda=lambda;

	}

	@Override
	public void setSeeds(int seed) {
		
	}

	@Override
	protected String getStringParamets() {
		return this.lambda+"";
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDown other) {
		if (other instanceof ProtocolDownPoission) {
			return this.lambda == ((ProtocolDownPoission)other).lambda;
		}
		return false;
	}
	

}
