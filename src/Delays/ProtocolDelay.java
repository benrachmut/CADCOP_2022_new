package Delays;
import java.util.Random;

public abstract class ProtocolDelay {
	
	protected boolean imperfectCommunicationScenario;
	protected boolean isTimeStamp;
	protected double gamma;
	protected Random rndGammaAlgorthmic, rndGammaAnytime;
	protected Random rndDelayAlgorthmic, rndDelayAnytime;

	

	public ProtocolDelay(boolean imperfectCommunicationScenario, boolean isTimeStamp, double gamma) {
		
		this.imperfectCommunicationScenario = imperfectCommunicationScenario;
		this.isTimeStamp = isTimeStamp;
		this.gamma = gamma;
	}
	
	
	public Double createDelay(boolean isAlgorithmicMsg) {
		Random whichRandom;
		if (isAlgorithmicMsg) {
			whichRandom = rndGammaAlgorthmic;
		}else {
			whichRandom = rndGammaAnytime;
		}
		double rnd = whichRandom.nextDouble();
		if (rnd<gamma) {
			return null;
		}
		else {
			
			Random whichRandomDelay;
			if (isAlgorithmicMsg) {
				whichRandomDelay =  rndDelayAlgorthmic;
			}else {
				whichRandomDelay = rndDelayAnytime;
			}

			return createDelay(whichRandomDelay);
		}
	}
	protected abstract Double createDelay(Random r);

	public void setSeeds(int dcopId) {
		this.rndGammaAlgorthmic = new Random(dcopId*145);
		this.rndGammaAnytime = new Random(dcopId*321);
		this.rndDelayAlgorthmic = new Random(dcopId*632);
		this.rndDelayAnytime = new Random(dcopId*873);
		
	}

	public boolean isWithTimeStamp() {
		// TODO Auto-generated method stub
		return isTimeStamp;
	}

	@Override
	public String toString() {
		String pc;

		if (imperfectCommunicationScenario) {
			pc = "Imperfect Communication";
		}else {
			pc = "Perfect Communication";
		}
		
		
		String timeS;
		if (this.isTimeStamp) {
			timeS = "w Timestamp";
		}else {
			timeS = "w/o Timestamp";
		}
		
		return pc+","+timeS+","+this.gamma+","+getStringParamets();
	}


	protected abstract String getStringParamets();
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProtocolDelay) {
			ProtocolDelay other = (ProtocolDelay)obj;
			
			 boolean sameImperfectCommunicationScenario  = this.imperfectCommunicationScenario == other.getIsImperfectCommunication();
			 if (this.imperfectCommunicationScenario == false && sameImperfectCommunicationScenario) {
				return true;
			}
			 boolean sameIsTimeStamp = this.isTimeStamp == other.getIsTimeStamp();
			 boolean sameGamma= this.gamma == other.getGamma();
			 boolean sameOthers = checkSpecificEquals(other);
			
			
			return  sameImperfectCommunicationScenario && sameIsTimeStamp && sameGamma && sameOthers;
		}
		return false;
	}


	protected abstract boolean checkSpecificEquals(ProtocolDelay other);


	public double getGamma() {
		// TODO Auto-generated method stub
		return this.gamma;
	}
	


	private boolean getIsTimeStamp() {
		// TODO Auto-generated method stub
		return this.isTimeStamp;
	}


	private boolean getIsImperfectCommunication() {
		// TODO Auto-generated method stub
		return this.imperfectCommunicationScenario;
	}


	

	

}
