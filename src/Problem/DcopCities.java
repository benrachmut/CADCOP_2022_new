package Problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.management.RuntimeErrorException;
import javax.swing.text.StyledEditorKit.ForegroundAction;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentVariable;
import AgentsAbstract.Location;
import AgentsAbstract.LocationRandomUniform;
import AgentsAbstract.NodeId;
import Main.MainSimulator;

public class DcopCities extends Dcop {
	private Map<AgentVariable, List<AgentVariable>> citiesAllocation;// agent of represents center, other agents close
																		// to
																		// the city
	protected double[][] agentsQuadraticDistance;
	private List<AgentVariable> agentsWithoutLocations;

	private int minCost, maxCost;
	private int numberOfCities;
	private double sdSquareFromCity;
	private double exponentForNeighborCitizens;


	private double dcopCityP2;
	private Random randomNotHub;
	private Map<AgentVariable, Double> probsPerAgent;

	public DcopCities(int dcopId, int A, int D, int numberOfCities, double sdSquareFromCity, int minCost, int maxCost,
			double dcopCityP2, double exponentForNeighborCitizens) {
		super(dcopId, A, D);
		this.exponentForNeighborCitizens = exponentForNeighborCitizens;
		this.dcopCityP2 = dcopCityP2;
		this.minCost = minCost;
		this.maxCost = maxCost;
		randomNotHub = new Random(dcopId * 174);
		randomNotHub.nextDouble();
		this.numberOfCities = numberOfCities;
		this.sdSquareFromCity = sdSquareFromCity;
		agentsWithoutLocations = new ArrayList<AgentVariable>();
		for (AgentVariable a : agentsVariables) {
			agentsWithoutLocations.add(a);
		}
		agentsQuadraticDistance = new double[A][A];

		List<Location> citiesCenterLocations = createLocationToCities();
		List<AgentVariable> agentMayers = selectAgentsToBeCenter();

		citiesAllocation = setMayersLocationAndInitiateCityMap(citiesCenterLocations, agentMayers);
		allocateAgentsToCities();
		giveCitizensLocations();
		checkIfCityAllocationIsValid(citiesCenterLocations, agentMayers);
		calculateQuadarticDistance();

		
	}

	

	private void calculateQuadarticDistance() {
		try {
			for (int i = 0; i < agentsVariables.length; i++) {
				for (int j = 0; j < agentsVariables.length; j++) {
					agentsQuadraticDistance[i][j] = agentsVariables[i]
							.getQuadraticDistanceTo(agentsVariables[j].getLocation());
				}
			}
		} catch (NullPointerException e) {
			System.err.println("There is not location to the agents");
		}
	}

	

	private void checkIfCityAllocationIsValid(List<Location> citiesCenterLocations, List<AgentVariable> agentMayers) {
		if (!agentsWithoutLocations.isEmpty()) {
			throw new RuntimeException("not all variable agents have location");
		}
		if (citiesCenterLocations.size() != agentMayers.size()) {
			throw new RuntimeException("should be citiesCenterLocations.size()==agentMayers.size()");
		}
	}

	private void giveCitizensLocations() {
		for (Entry<AgentVariable, List<AgentVariable>> e : citiesAllocation.entrySet()) {
			Location mayerLocation = e.getKey().getLocation();
			List<AgentVariable> citizens = e.getValue();
			for (AgentVariable citizen : citizens) {
				citizen.setLocationCloseToCity(mayerLocation, this.dcopId, sdSquareFromCity);
				agentsWithoutLocations.remove(citizen);
			}
		}

	}

	private void allocateAgentsToCities() {
		List<AgentVariable> keysAsArray = new ArrayList<AgentVariable>(citiesAllocation.keySet());
		for (AgentVariable a : agentsWithoutLocations) {
			Random r = new Random((dcopId + 1) * 125 + (a.getId() + 1) * 17);
			r.nextInt();
			List<AgentVariable> cityCitizens = citiesAllocation.get(keysAsArray.get(r.nextInt(keysAsArray.size())));
			cityCitizens.add(a);
		}

	}

	private Map<AgentVariable, List<AgentVariable>> setMayersLocationAndInitiateCityMap(
			List<Location> citiesCenterLocations, List<AgentVariable> agentMayers) {
		Map<AgentVariable, List<AgentVariable>> ans = new HashMap<AgentVariable, List<AgentVariable>>();
		for (int i = 0; i < citiesCenterLocations.size(); i++) {
			AgentVariable mayer = agentMayers.get(i);
			mayer.setLocation(citiesCenterLocations.get(i));
			ans.put(agentMayers.get(i), new ArrayList<AgentVariable>());
		}
		return ans;
	}

	public List<AgentVariable> selectAgentsToBeCenter() {
		Random rand = new Random((this.dcopId + 1) * 175);

		// create a temporary list for storing
		// selected element
		List<AgentVariable> agentMayers = new ArrayList<AgentVariable>();
		for (int i = 0; i < this.numberOfCities; i++) {
			// take a random index between 0 to size
			// of given List
		//	try {
			int randomIndex = rand.nextInt(agentsWithoutLocations.size());
			
			// add element in temporary list
			agentMayers.add(agentsWithoutLocations.get(randomIndex));
			// Remove selected element from orginal list
			agentsWithoutLocations.remove(randomIndex);
		//	}catch(Exception e) {
		//		System.out.println();
		//	}
		}
		return agentMayers;

	}

	private List<Location> createLocationToCities() {
		List<Location> ans = new ArrayList<Location>();
		for (int i = 0; i < MainSimulator.numberOfCities; i++) {
			ans.add(new LocationRandomUniform(dcopId, i));
		}
		return ans;
	}

	@Override
	protected void setDcopName() {
		Dcop.dcopName = "LocationBase";


	}

	@Override
	protected void setDcopHeader() {
		Dcop.dcopHeader = "numberOfCities"+","+","+"sdSquareFromCity"+","+"exponentForNeighborCitizens";
	}

	@Override
	protected void setDcopParameters() {
		Dcop.dcopParameters = this.numberOfCities+","+this.sdSquareFromCity+","+this.exponentForNeighborCitizens;

	}

	@Override
	public void createNeighbors() {

		mayersNeighborsWithOneAnother();
		//printNeighborsCoordinates();
		otherCitizensNeighborsSelection();
		//printNeighborsCoordinates();
		//System.out.println();
		if (MainSimulator.isDcopCityDebug) { 
			// printLocations(); //
			//printProbForHist();
			
			//printLocationOfMayers();
			//printLocationOfCiviliens();

			//printNeighborsCoordinates();
			//printAvgDistances();
			
		}
		//System.out.println();
	}

	
/*
	private void printLocationOfCiviliens() {
		System.out.println("Location civil");
		for (List<AgentVariable> l : this.citiesAllocation.values()) {
			for (AgentVariable a : l) {
				System.out.println(a.getLocation());
			}
		}
		
	}
*/
	/*
	private void printLocationOfMayers() {
		System.out.println("Location mayers");
		for (AgentVariable a : this.citiesAllocation.keySet()) {
			System.out.println(a.getLocation());
		}
		
	}
*/
	/*
	private void printProbForHist() {
		for (Double d : probsPerAgent.values()) {
			System.out.print(d+",");
		}
		
		System.out.println();
	}
*/
	
	
	private void printAvgDistances() {
		double sum = 0; 
		for (Neighbor n : this.neighbors) {
			sum = sum+ n.distanceBetween();
		}
		System.out.println(sum/this.neighbors.size());
		System.out.println();		
	}

	private void otherCitizensNeighborsSelection() {

		List<AgentVariable> allCitizens = new ArrayList<AgentVariable>();

		for (Entry<AgentVariable, List<AgentVariable>> e : this.citiesAllocation.entrySet()) {
			allCitizens.addAll(e.getValue());
			allCitizens.add(e.getKey());
		}
		
		for (AgentVariable a1 : allCitizens) {
			
			Map<AgentVariable, Double> ans = new HashMap<AgentVariable, Double>();
			List<AgentVariable> allExceptAAndNeigbors = getAllExceptAAndNeigbors(a1, allCitizens);
			double maxDistance = getMaxDistanceOfA(a1);

			for (AgentVariable a2 : allExceptAAndNeigbors) {
				
				double distanceToA1 = a1.getQuadraticDistanceTo(a2.getLocation());
				double P = Math.pow(1 - (distanceToA1 / maxDistance), this.exponentForNeighborCitizens);
				
				//if (a1.getId() == 0 && MainSimulator.isDcopCityDebug) {
					//System.out.println(P);
				//}
				
				double p = randomNotHub.nextDouble();
				p = randomNotHub.nextDouble();
				
				if (p<P) {
					this.neighbors.add(new Neighbor(a1, a2, D, minCost, maxCost, dcopId, dcopCityP2));
				}
				//ans.put(a2, p);
				// System.out.print(p + ",");
			}
			/*
			int counter = 0;
			while (counter < this.neighborsOfNonMayers){// && a1.getNeigborSetId().size() < this.neighborsOfNonMayers) {
				//System.out.println(a1.getNeigborSetId());
				this.probsPerAgent = getProbsPerAgent(a1, allCitizens);
				Map<AgentVariable, Double> cumulativeProb = getCumulativeProb(probsPerAgent);
				double d = randomNotHub.nextDouble();

				for (Entry<AgentVariable, Double> e : cumulativeProb.entrySet()) {
					if (d < e.getValue()) {
						AgentVariable a2 = e.getKey();
						this.neighbors.add(new Neighbor(a1, a2, D, minCost, maxCost, dcopId, dcopCityP2));
						counter = counter + 1;
						break;
					}
				}
			}
			*/
		}
	}

	/*
	private Map<AgentVariable, Double> getCumulativeProb(Map<AgentVariable, Double> probsPerAgent) {
		probsPerAgent = DcopScaleFreeNetwork.sortByValue(probsPerAgent);
		Map<AgentVariable, Double> ans = new HashMap<AgentVariable, Double>();
		

		double sum = getSomeOfSet(probsPerAgent.values());
		double cumelative = 0;
		for (Entry<AgentVariable, Double> e : probsPerAgent.entrySet()) {
			AgentVariable n = e.getKey();
			double currentD = e.getValue();
			cumelative += currentD;
			ans.put(n, cumelative / sum);
		}
		ans = DcopScaleFreeNetwork.sortByValue(ans);

		return ans;
	}
*/
	/*
	private double getSomeOfSet(Collection<Double> values) {
		double sum = 0;
		for (Double d : values) {
			sum += d;
		}
		return sum;
	}
*/
	/*
	private Map<AgentVariable, Double> getProbsPerAgent(AgentVariable a1, List<AgentVariable> allCitizents) {
		Map<AgentVariable, Double> ans = new HashMap<AgentVariable, Double>();
		List<AgentVariable> allExceptAAndNeigbors = getAllExceptAAndNeigbors(a1, allCitizents);

		double maxDistance = getMaxDistanceOfA(a1);

		for (AgentVariable a2 : allExceptAAndNeigbors) {

			double distanceToA1 = a1.getQuadraticDistanceTo(a2.getLocation());
			double p = Math.pow(1 - (distanceToA1 / maxDistance), this.exponentForNeighborCitizens);

			ans.put(a2, p);
			// System.out.print(p + ",");
		}

		return ans;
	}
*/
	private List<AgentVariable> getAllExceptAAndNeigbors(AgentVariable a1, List<AgentVariable> allCitizents) {
		List<AgentVariable> ans = new ArrayList<AgentVariable>(allCitizents);
		ans.remove(a1);

		for (NodeId nd : a1.getNeigborSetId()) {
			AgentVariable neighbor = getAgentGivenNodeId(nd);
			ans.remove(neighbor);
		}
		return ans;
	}

	private AgentVariable getAgentGivenNodeId(NodeId nd) {
		for (AgentVariable a : this.agentsVariables) {
			if (a.getNodeId().equals(nd)) {
				return a;
			}
		}
		return null;
	}

	private double getMaxDistanceOfA(AgentVariable a1) {
		double[] distanceToAll = agentsQuadraticDistance[a1.getId()];
		Collection<Double> c = new ArrayList<Double>();
		for (double d : distanceToAll) {
			c.add(d);
		}
		return Collections.max(c);
	}

	private void mayersNeighborsWithOneAnother() {
		Set<AgentVariable> mayersSet = this.citiesAllocation.keySet();
		List<AgentVariable> mayersList = new ArrayList<AgentVariable>(mayersSet);
		for (int i = 0; i < mayersList.size(); i++) {
			AgentVariable a1 = mayersList.get(i);
			for (int j = i + 1; j < mayersList.size(); j++) {
				AgentVariable a2 = mayersList.get(j);
				this.neighbors.add(new Neighbor(a1, a2, D, minCost, maxCost, this.dcopId, this.dcopCityP2));
			}
		}

	}

	private void printNeighborsCoordinates() {
		for (Neighbor n : this.neighbors) {
			AgentVariable a1 = n.getA1();
			AgentVariable a2 = n.getA2();

			System.out.println(a1.getLocation());
			System.out.println(a2.getLocation());
			System.out.println();
		}
		System.out.println();

	}



	public double[][] getDistancesMatrix() {
		// TODO Auto-generated method stub
		return this.agentsQuadraticDistance;
	}
}
