package Delays;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;

public class ProtocolDelaysPossion extends ProtocolDelay {

	private int lambda;


	public ProtocolDelaysPossion(boolean isTimeStamp, double gamma, int lambda) {
		super(true, isTimeStamp, gamma);
		this.lambda = lambda;
	}

	public ProtocolDelaysPossion() {
		super(false, true, 0.0);
		this.lambda = 0;
	}

	public ProtocolDelaysPossion(double gamma) {
		super(false, false, gamma);
		this.lambda = 0;
	}

	@Override
	protected Double createDelay(Random r) {

		if (this.lambda == 0) {
			return 0.0;
		}

	
		double ans ;
		if (this.lambda>=100) {
			ans= getRandomBig(r);
		}else {
			ans= getRandom(r);
		}
		if (ans < 0) {
			return 0.0;
		} else {
			return ans;
		}
	}

	public double getRandomBig(Random rand) {
		
		double ans = rand.nextGaussian() * Math.sqrt(this.lambda) + this.lambda;
		//System.out.println(ans);
		return ans ;

		//Also, bear in mind that the Poisson distribution P(λ) for large λ can be approximated 
		//very well by the normal distribution N(λ, sqrt(λ)).
		
		
		
		/*
		BigDecimal a = new BigDecimal(rand.nextDouble());
		BigDecimal L = new BigDecimal(Math.exp(1));
		BigDecimal one = new BigDecimal(1);
		L = one.divide(L.pow(lambda), MathContext.DECIMAL64);

		int k = 0;

		BigDecimal p = new BigDecimal(1);

		do {
			k++;
			p = p.multiply(new BigDecimal(rand.nextDouble()));
		} while (p.compareTo(L) == 1);
		System.out.println((k));

		return k - 1;
		*/
	}

	public double getRandom(Random r) {
		double L = Math.exp(-lambda);
		int k = 0;
		double p = 1.0;
		do {
			k++;
			p = p * r.nextDouble();
		} while (p > L);

		return k - 1;
	}



	@Override
	protected String getStringParameters() {
		// TODO Auto-generated method stub
		return this.lambda + "";
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDelay other) {
		if (other instanceof ProtocolDelaysPossion) {
			ProtocolDelaysPossion otherUniform = (ProtocolDelaysPossion) other;
			boolean sameUB = otherUniform.getLambda() == this.lambda;
			return sameUB;
		}
		return false;
	}

	private double getLambda() {
		return this.lambda;
	}

}
