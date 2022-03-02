package Delays;

import java.util.Random;

public class ProtocolDelaysDistancePoisson extends ProtocolDelayMatrix {
	private double multiplier;

	public ProtocolDelaysDistancePoisson(boolean isTimeStamp, double gamma, double multiplier) {
		super(true, isTimeStamp, gamma);
		this.multiplier = multiplier;
	}

	public ProtocolDelaysDistancePoisson(double gamma) {
		super(false, true, 0.0);
		multiplier = 0;
	}

	@Override
	protected Double createDelay(Random r, int i, int j) {
		double distance = this.matrix[i][j];
		double lambda = distance * multiplier;

		if (lambda == 0) {
			return 0.0;
		}

		double ans;
		if (lambda >= 100) {
			ans = getRandomBig(r, lambda);
		} else {
			ans = getRandom(r, lambda);
		}
		if (ans < 0) {
			return 0.0;
		} else {
			return ans;
		}
	}
	

	public double getRandom(Random r, double lambda) {
		double L = Math.exp(-lambda);
		int k = 0;
		double p = 1.0;
		do {
			k++;
			p = p * r.nextDouble();
		} while (p > L);

		return k - 1;
	}

	public double getRandomBig(Random rand, double lambda) {

		double ans = rand.nextGaussian() * Math.sqrt(lambda) + lambda;
		// System.out.println(ans);
		return ans;
	}

	@Override
	protected String getStringParamets() {
		return this.multiplier + "";
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDelay other) {
		if (other instanceof ProtocolDelaysDistancePoisson) {
			ProtocolDelaysDistancePoisson otherUniform = (ProtocolDelaysDistancePoisson) other;
			boolean sameUB = otherUniform.multiplier == this.multiplier;
			return sameUB;
		}
		return false;
	}

}
