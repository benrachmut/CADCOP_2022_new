package AgentsAbstract;

import java.util.Random;

import Main.MainSimulator;

public class LocationRandomNormal extends Location {
	
	private double sdSquare;
	private Location cityLocation; 

	
	public LocationRandomNormal(int dcop_id, int agentId,Location cityLocation,double sd) {
		super(dcop_id,agentId);
		this.sdSquare = sd;
		this.cityLocation = cityLocation;
		generateRandomXY();
		
	}

	@Override
	protected void generateRandomXY() {
		double xMu = cityLocation.x;
		double yMu = cityLocation.y;
		r.nextGaussian();
		this.x = r.nextGaussian() * this.sdSquare + xMu;
		this.y = r.nextGaussian() * this.sdSquare + yMu;
	}
	

}
