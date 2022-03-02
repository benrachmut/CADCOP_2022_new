package Delays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ProtocolMissingMsgDistance extends ProtocolDelayMatrix {

	public ProtocolMissingMsgDistance(double gamma) {
		super(false, true, 0.0);
	}

	
	public ProtocolMissingMsgDistance() {
		super(true, true, 0.0);
	}
	
	
	@Override
	protected Double createDelay(Random r, int i, int j) {
		
		if (this.imperfectCommunicationScenario) {
			double distance = this.matrix[i][j];
			double largestDistance = getLargestDistance(i, j);
			double largeP = distance / largestDistance;
			if (r.nextDouble() < largeP) {
				return 0.0;
			} else {
				return null;
			}
		}else {
			return 0.0;
		}
		
	}

	private double getLargestDistance(int i, int j) {
		double largestJ = largestDistanceJ(j);
		double largestI = largestDistanceI(i);
		return Math.max(largestI, largestJ);
	}

	private double largestDistanceI(int i) {
		List<Double> l = new ArrayList<Double>();
		for (int j = 0; j < matrix[0].length; j++) {
			l.add(matrix[i][j]);
		}
		return Collections.max(l);
	}

	private double largestDistanceJ(int j) {
		List<Double> l = new ArrayList<Double>();
		for (int i = 0; i < matrix.length; i++) {
			l.add(matrix[i][j]);
		}
		return Collections.max(l);
	}

	@Override
	protected String getStringParamets() {
		return  "";
	}

	@Override
	protected boolean checkSpecificEquals(ProtocolDelay other) {
		return true;
	}
}
