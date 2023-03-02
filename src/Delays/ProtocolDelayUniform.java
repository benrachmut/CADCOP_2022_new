package Delays;

import java.util.Random;

public class ProtocolDelayUniform extends ProtocolDelay {

	private double ub;
	
	public ProtocolDelayUniform( boolean isTimeStamp, double gamma, double ub) {
		super(true, isTimeStamp,gamma);

		this.ub = ub;
		
	}


	
	public ProtocolDelayUniform(double gamma) {
		super(false, false,gamma);
		ub = 0;
	}
	@Override
	protected Double createDelay(Random r) {
		return r.nextDouble()*ub;
	}

	
	
	protected String getStringParameters() {
		return this.ub+"";
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDelay other) {
		if (other instanceof ProtocolDelayUniform) {
			ProtocolDelayUniform otherUniform = (ProtocolDelayUniform)other;
			boolean sameUB = otherUniform.getUb() == this.ub;
			return sameUB;
		}
		return false;
	}



	public double getUb() {
		// TODO Auto-generated method stub
		return this.ub;
	}


	

}
