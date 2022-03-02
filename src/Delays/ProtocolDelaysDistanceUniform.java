package Delays;

import java.util.Random;

public class ProtocolDelaysDistanceUniform  extends ProtocolDelayMatrix {
	private double multiplier;

	public ProtocolDelaysDistanceUniform(boolean isTimeStamp, double gamma, double multiplier) {
		super(true, isTimeStamp, 0.0);
		this.multiplier = multiplier;
	}

	public ProtocolDelaysDistanceUniform(double gamma) {
		super(false, true, 0.0);
		multiplier = 0;
	}

	@Override
	protected Double createDelay(Random r, int i, int j) {
		double distance = this.matrix[i][j];
		double ub = distance * multiplier;
		return r.nextDouble()*ub;
	}
	

	@Override
	protected String getStringParamets() {
		return this.multiplier + "";
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDelay other) {
		if (other instanceof ProtocolDelaysDistanceUniform) {
			ProtocolDelaysDistanceUniform otherUniform = (ProtocolDelaysDistanceUniform) other;
			boolean sameUB = otherUniform.multiplier == this.multiplier;
			return sameUB;
		}
		return false;
	}
}
