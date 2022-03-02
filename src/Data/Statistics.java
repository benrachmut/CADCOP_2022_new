package Data;

import java.util.Collection;
import java.util.List;

public class Statistics {

	public static Double mean(Collection<Double> list) {
		Double sum = sum(list);
		Double size = sizeWithoutNull(list);	
		if (sum == null) {
			return null;
		}
		return sum/size;
	}

	private static Double sizeWithoutNull(Collection<Double> list) {
		Double counter = 0.0;
		for (Double d : list) {
			if (d!= null) {
				counter = counter+1.0;
			}
		}
		return counter;
	}

	public static Double sum(Collection<Double> list) {
		Double sum = 0.0;
		for (Double d : list) {
			if (d!= null) {
				sum+=d;
			}
		}
		return sum;
	}
}
