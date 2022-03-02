package Comparators;

import java.util.Comparator;

import AlgorithmSearch.NodeId_AOpt2;

public class CompTopColorAndMinIndex implements Comparator<NodeId_AOpt2> {

	@Override
	public int compare(NodeId_AOpt2 o1, NodeId_AOpt2 o2) {
		if (o1.getColor()<o2.getColor()) {
			return 1;
		}
		if (o1.getColor()>o2.getColor()) {
			return -1;
		}else {
			if (o1.getId1()<o2.getId1()) {
				return 1;
			}
			if (o1.getId1()>o2.getId1()) {
				return -1;
			}
		}
		return 0;
	}

}
