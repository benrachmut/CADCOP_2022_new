package AgentsAbstract;

import java.util.Random;

public class LocationRandomUniform extends Location {

	
	public LocationRandomUniform(int dcop_id, int agentId) {
		super(dcop_id,agentId);
		generateRandomXY();
	}

	@Override
	protected void generateRandomXY() {
		r.nextDouble();
		this.x=r.nextDouble();
		this.y=r.nextDouble();
	}
 
	
}
