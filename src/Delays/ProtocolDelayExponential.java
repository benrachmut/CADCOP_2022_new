package Delays;

import java.util.Random;

public class ProtocolDelayExponential extends ProtocolDelay {

	private double lambda;

	public ProtocolDelayExponential(boolean isTimeStamp, double gamma, double lambda) {
		super(true, isTimeStamp, gamma);
		this.lambda = lambda;
	}

	public ProtocolDelayExponential() {
		super(false, true, 0.0);
		this.lambda = 0;
	}

	public ProtocolDelayExponential(double gamma) {
		super(false, true, gamma);
		this.lambda = 0;
	}

	@Override
	protected Double createDelay(Random r) {

		if (this.lambda == 0) {
			return 0.0;
		}

	

		double ans = getRandomExponential(r);
		if (ans < 0) {
			return 0.0;
		} else {
			return ans;
		}
	}

     public double getRandomExponential(Random r) {
         return -(Math.log(r.nextDouble()) / lambda);
     }

	

	@Override
	protected String getStringParamets() {
		// TODO Auto-generated method stub
		return this.lambda+"";
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDelay other) {
		if (other instanceof ProtocolDelayExponential) {
			ProtocolDelayExponential otherUniform = (ProtocolDelayExponential)other;
			boolean sameUB = otherUniform.getLambda() == this.lambda;
			return sameUB;
		}
		return false;
	}

	private double getLambda() {
		return this.lambda;
	}


	
	
	
}
