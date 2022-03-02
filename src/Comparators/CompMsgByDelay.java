package Comparators;

import java.util.Comparator;

import Messages.Msg;

public class CompMsgByDelay implements Comparator<Msg> {

	@Override
	public int compare(Msg o1, Msg o2) {

		if (o1.getDelayOfMsg() > o2.getDelayOfMsg()) {
			return 1;
		}
		if (o1.getDelayOfMsg() < o2.getDelayOfMsg()) {
			return -1;
		}
		return 0;
	}

}
