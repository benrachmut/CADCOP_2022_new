package Messages;

import java.util.Comparator;

public class MsgsAgentTimeComparator implements Comparator<Msg> {

	@Override
	public int compare(Msg o1, Msg o2) {
		
		if (o1.getTimeOfMsg() == o2.getTimeOfMsg()) {
			return 0;
		}
		
		long max= Long.max(o1.getTimeOfMsg(), o2.getTimeOfMsg());
		
		if (max == o1.getTimeOfMsg()) {
			return 1;
		}
		if (max == o2.getTimeOfMsg()) {
			return -1;
		}else {
			throw new RuntimeException("will never print this");
		}
		
		
	
	}

	
}
