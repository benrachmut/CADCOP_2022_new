package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import Delays.ProtocolDelay;
import Main.MainSimulator;

/**
 * class is used for creating different combinations of delay protocols needs to
 * correspond with Protocol delay class
 * 
 * @return
 */
public abstract class CreatorDelays {

	protected boolean[] imperfectCommunicationScenario = { true };
	protected boolean[] isTimeStamps = { false};
	protected double[] gammas = {0.5,1};//{0,0.05,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,0.95};

	/**
	 * creates combinations if assuming imperfect communications, each class with
	 * its Appropriate createCombinationsDelay method.
	 * 
	 * if perfectCommunications = false each extends class needs to return its
	 * default constructor
	 * 
	 * @return
	 */
	public List<ProtocolDelay> createProtocolDelays() {
		List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
		if (MainSimulator.delayType == 0) {
			for (double gamma : gammas) {
				ans.add(createDefultProtocol(gamma));
			}
		} else {

			for (boolean perfectP : imperfectCommunicationScenario) {
				if (perfectP == false) {
					for (double gamma : gammas) {

						ans.add(createDefultProtocol(gamma));
					}
				} else {
					for (boolean isTimeStamp : isTimeStamps) {
						for (double gamma : gammas) {
							Collection<? extends ProtocolDelay> toAdd = createCombinationsDelay(isTimeStamp, gamma);
							if (toAdd != null) {
								ans.addAll(toAdd);
							}
						}
					}
				}
			}
		}
		return ans;
	}

	protected abstract ProtocolDelay createDefultProtocol(double gamma);

	protected abstract Collection<? extends ProtocolDelay> createCombinationsDelay(boolean isTimeStamp, double gamma);

	public String getHeader() {
		return "Perfect Communication,Timestamp Use,Message Lost Prob," + header();
	}

	protected abstract String header();

	public abstract String name();

}
