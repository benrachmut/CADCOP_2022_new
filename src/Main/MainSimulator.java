package Main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.util.Map.Entry;

import AgentsAbstract.Agent;
import AgentsAbstract.AgentVariable;
import Data.Data;
import Delays.*;
import Down.CreatorDown;
import Down.CreatorDownNone;
import Down.CreatorDownPoission;
import Down.ProtocolDown;
import Problem.*;

public class MainSimulator {

	// --------------------------------**Experiment Repetitions**
	public static int div = 1;
	public static int delta = 100;
	public static int start =0;
	public static int end = start+delta;
	public static int end_temp = start; // DO NOT CHANGE
	public static long termination = 100000;//8000000 30000007;
	public static int howManyIterationForCalculation =10; //10000
    private static int everyHowManyExcel = 100;
	// ------------------------------**PROBLEM MAGNITUDE**
	public static int A = 10; // amount of agents
	private static int D = 10;
	// ------------------------------**Algorithm Selection**
	public enum Algorithm {
		DSA_ASY, DSA_SY, MGM_ASY, MGM_SY, LAMDLS, DSA_SDP_ASY, DSA_SDP_SY, MGM2_ASY, MGM2_SY,
		CAMDLS_NAIVE, CAMDLS_V2,MGM2_SY_V2,
		MonoStochasticOrderSearch,
		MonoDeterministicNo2Opt,
		LAMDLS2,
		MonoToken2Opt,
		DALO2,
		MaxSum_SY,MaxSum_split_SY, MaxSum_ASY, MaxSum_split_ASY, LAMDLS3;
	}
	public static Algorithm algorithm = Algorithm.DALO2;
	// ------------------------------*Communication Selection**
	public enum DelayType {
		none, normal, uniform, Exponential ,Poisson,
		distancePois ,distanceUniform ,distanceMissingMsg , DelayWithK, amountMsgInSystemLinear
	}
	public static DelayType myDelayType = DelayType.uniform;

	// ------------------------------*DCOP type**
	public enum DcopType {
		RandomUniform, GraphColoring, ScaleFreeNetwork, SolarSystem
	}
	public static DcopType myDcopType = DcopType.RandomUniform;

	// 1 = Random uniform
	public static double dcopUniformP1 = 0.5;//0.5
	public static double dcopUniformP2 = 1;// Probability for two values in domain between neighbors to have constraints
	public static int costLbUniform = 1;
	public static int costUbUniform = 100;
	// 2 = Graph Coloring
	public static double dcopGraphColoringP1 = 0.05;// Probability for agents to have constraints
	public static int costLbColor = 10;
	public static int costUbColor = 100;
	// 3 = scale free
	public static int dcopScaleHubs = 10; // number of agents with central weight
	public static int dcopScaleNeighbors = 3; // number of neighbors (not including policy of hubs
	public static double dcopScaleP2 = 1;// Probability for two values in domain between neighbors to have constraints
	public static int costLbScale = 1;
	public static int costUbScale = 100;
	// 4 = cities
	public static int numberOfCities = 5;
	public static double sdSquareFromCity = 0.05;
	public static int minCostCity = 1;
	public static int maxCostCity = 100;
	public static double dcopCityP2 = 1;// Probability for two values in domain between neighbors to have constraints
	public static double exponentForNeighborCitizens = 3;





	public static boolean isCAMDLS_V2 = false;
	public static boolean isThreadDebug = false;
	public static boolean isAMDLSDistributedDebug = false;
	public static boolean isAnytimeThreadDebug = false;
	public static boolean isAnytimeDebug = false;
	public static boolean isFactorGraphDebug = false;
	public static boolean isMGM2Debug = false;
	public static boolean isMaxSumThreadDebug = false;
	public static boolean is2OptDebug = false;
	public static boolean isDcopCityDebug = false;
	public static boolean isIdealTimeDebug = false;
	public static boolean isMSOSDebug = false;
	public static boolean isColorMS2SDebug= false;
	public static boolean isMS2SDebug= false;
	public static boolean isAnotherColorDebug= false;
	public static boolean isMDC2CDebug =false;
	public static boolean  isMonoStochComputationDebug=false;
	public static boolean  isMGM2v2Debug=false;
	public static boolean  isLAMDLSDebug=false;
	public static boolean isLAMDLS3Debug=false;;
	public static boolean isDalo2Debug= true;



	public static CreatorDelays creatorDelay;
	// ------------------------------**For Data
	public static List<Mailer> mailerAll = new ArrayList<Mailer>();
	public static Map<Protocol, List<Mailer>> mailersByProtocol = new HashMap<Protocol, List<Mailer>>();
	// ------------------------------**Algorithmic relevance under imperfect
	// communication**
	// true = send only if change, false = send regardless if change took place
	public static boolean sendOnlyIfChange = false;
	// ------------------------------**Implementation**
	public static boolean isThreadMailer = true; // determines the mailers type
	public static boolean isAtomicTime = true;
	// public static int dividAtomicTime = 1;
	public static int multiplicationTime = 1;// 2;
	;// 10000;//100000; // sparse = 100,dense=100
	private static Double[] convergeEximne = { };

	// ------------------------------**any time**
	public static boolean isAnytime = false;
	// 1 = DFS; 2 = BFS
	public static int anytimeFormation = 1;
	//	public static boolean deleteAfterCombine = false;
	// 1 = no memoryLimit, 2=MSC, 3=Fifo, 4=Random
	public static int anytimeMemoryHuerstic = 2;
	public static int anytimeMemoryLimitedSize = 100;
	/*
	 * delayTypes: 0 = non
	 */
	public static int downType = 0;
	public static CreatorDown creatorDown;

	public static String protocolDelayHeader = "";
	public static String protocolDownHeader = "";
	public static String mailerHeader = "";

	public static String header = "";
	public static Collection<String> meanLineInExcel = new ArrayList<String>();
	public static Collection<String> lastLineInExcel = new ArrayList<String>();
	public static Collection<String> convergeLineInExcel = new ArrayList<String>();

	public static String fileName = "";

	public static void main(String[] args) {

		if (myDcopType == DcopType.SolarSystem && !((myDelayType == DelayType.distancePois)||(myDelayType == DelayType.distanceUniform)||(myDelayType == DelayType.distanceMissingMsg))) {
			throw new RuntimeException("if dcopBenchMark is city then delay type must be distance base distance");
		}

		if (isAtomicTime && isThreadMailer) {
			termination = termination * multiplicationTime;
		}

		if (!isThreadMailer) {
			isAtomicTime = false;
		}
		Dcop[] dcops = generateDcops();
		List<Protocol> protocols = createProtocols();
		runDcops(dcops, protocols);
		createData();
	}

	private static void createExcel(Collection<String> lines) {
		BufferedWriter out = null;
		try {
			FileWriter s = new FileWriter(fileName + ".csv");
			out = new BufferedWriter(s);
			out.write(header);
			out.newLine();

			for (String o : lines) {
				out.write(o);
				out.newLine();
			}

			out.close();
		} catch (Exception e) {
			System.err.println("Couldn't open the file");
		}

	}

	private static void createData() {
		createMeanData();
		createLastData();
		//createConvergeData();

	}

	private static void createConvergeData() {
		createFileName("Converge");
		createHeaderConverge();
		createConverge();
		createExcel(convergeLineInExcel);
	}

	private static void createHeaderConverge() {

		header = "DCOP";

		header = header + "," + protocolDelayHeader + "," + "Algorithm" + "," + AgentVariable.algorithmHeader + ","
				+ "Global Cost" + "," + "Anytime Cost";

		header = "run#" + "," + header + "," + "converge bound" + "," + "converge NCLO Global Cost" + ","
				+ "converge NCLO Anytime";

	}

	private static void createConverge() {
		// header = header + "," + protocolDelayHeader + "," + "Algorithm" + "," +
		// AgentVariable.algorithmHeader + ",";

		String dcopString = Dcop.dcopName;
		String algoString = AgentVariable.AlgorithmName + "," + AgentVariable.algorithmData;
		for (Entry<Protocol, List<Mailer>> e : mailersByProtocol.entrySet()) {
			String protocolString = e.getKey().getDelay().toString();
			List<Mailer> mailers = e.getValue();
			for (Mailer mailer : mailers) {
				SortedMap<Long, Data> dataOfMailer = mailer.getAllDataMap();
				//Long convergeTime = mailer.getConvergeTime();
				//for (Double convergeLimit : convergeEximne) {
					//Long ncloGlobal = Data.convergeTime(datasOfMailer, convergeLimit, true);
					//Long ncloAnytime = Data.convergeTime(datasOfMailer, convergeLimit, false);
					int dcopId = mailer.dcop.getId();
					String line = dcopId + "," + dcopString + "," + protocolString + "," + algoString + ","
							+ mailer.getLastGlobalCost() + "," + mailer.getLastGlobalAnytimeCost() + ",";
					convergeLineInExcel.add(line);
				//}
			}

			/*
			 * SortedMap<Integer, Data> mapLastDataPerDcop =
			 * getMapLastDataPerDcop(e.getValue()); String anytimeInfoString =
			 * getAnytimeString(); for (Entry<Integer, Data> e1 :
			 * mapLastDataPerDcop.entrySet()) { String tempAns = dcopString + "," +
			 * protocolString + "," + algoString + "," + e1.getValue(); if (isAnytime) {
			 * tempAns = tempAns + "," + anytimeInfoString; }
			 * convergeLineInExcel.add(e1.getKey() + "," + tempAns); }
			 */
		}

	}

	private static void createLastData() {
		createFileName("Last");
		createHeader(true, "run#");
		createLast();
		createExcel(lastLineInExcel);
	}

	private static void createLast() {
		String dcopString = Dcop.dcopName;
		String algoString = AgentVariable.AlgorithmName + "," + AgentVariable.algorithmData;
		for (Entry<Protocol, List<Mailer>> e : mailersByProtocol.entrySet()) {
			String protocolString = e.getKey().getDelay().toString();
			SortedMap<Integer, Data> mapLastDataPerDcop = getMapLastDataPerDcop(e.getValue());
			Map<Integer,Long> mailerAndConvergeMap = createConvergeDataPerMailer(e);
			
			String anytimeInfoString = getAnytimeString();
			for (Entry<Integer, Data> e1 : mapLastDataPerDcop.entrySet()) {
				String tempAns = dcopString + "," + protocolString + "," + algoString + "," + e1.getValue()+","+mailerAndConvergeMap.get(e1.getKey());
				if (isAnytime) {
					tempAns = tempAns + "," + anytimeInfoString;
				}
				lastLineInExcel.add(e1.getKey() + "," + tempAns);
			}
		}

	}

	private static Map<Integer,Long> createConvergeDataPerMailer(Entry<Protocol, List<Mailer>> e) {
		Map<Integer,Long> ans = new HashMap<Integer,Long>();
		for (Mailer m:e.getValue()){
			double lastGlobalCost = getLastGlobalCost(m);
			List<Long> timesSorted = getTimesSorted(lastGlobalCost,m);

			for (int i = 0; i < timesSorted.size(); i++) {
				Long maxTime = timesSorted.get(i);
				Data data = m.getDataPerIteration(maxTime);
				Double gcData = data.getGlobalCost();
				if (gcData!=lastGlobalCost){
					ans.put(m.dcop.getId(),maxTime);
					break;
				}
			}
		}
		return ans;

	}

	private static List<Long> getTimesSorted(double lastGlobalCost, Mailer m) {
		SortedMap<Long, Data> dataMap = m.getAllDataMap();
		List<Long> times = new ArrayList<Long>(dataMap.keySet());
		Collections.sort(times);
		Collections.reverse(times);
		return times;
	}

	private static double getLastGlobalCost(Mailer m) {
		SortedMap<Long, Data> dataMap = m.getAllDataMap();
		Long maxIter = Collections.max(dataMap.keySet());
		Data lastData = m.getDataPerIteration(maxIter);
		return   lastData.getGlobalCost();
	}


	private static void createMeanData() {
		createFileName("Mean");
		createHeader(false, "");
		createMeansByProtocol();
		createExcel(meanLineInExcel);
	}

	private static void createFileName(String fileType) {


		String ans = "Algorithm_" + AgentVariable.AlgorithmName;

		if (!AgentVariable.algorithmData.equals("")) {
			ans = ans + "(" + AgentVariable.algorithmData + "),";
		} else {
			ans = ans + ",";
		}
		ans = ans + "DCOP_" + Dcop.dcopName + ",";
		ans = ans + "Mailer_" + Mailer.mailerName + ",";
		ans = ans + "A_" + A + ",";
		ans = ans + "SReps_" + (start) + ",";
		ans = ans + "EReps_" + (end_temp) + ",";
		ans = ans + "Time_" + (termination);

		if (isAnytime) {
			ans = ans + "," + "Heurstic_" + (anytimeMemoryHuerstic);
			ans = ans + "," + "del_" + ("true");

			if (anytimeMemoryHuerstic != 1) {
				ans = ans + "," + "size_" + (anytimeMemoryLimitedSize);
			}

		}
		fileName = fileType + ",delay_" + creatorDelay.name() + "," + ans;
	}

	private static void createMeansByProtocol() {
		String dcopString = Dcop.dcopName;
		// String dcopString = "";

		String algoString = AgentVariable.AlgorithmName + "," + AgentVariable.algorithmData;
		for (Entry<Protocol, List<Mailer>> e : mailersByProtocol.entrySet()) {
			String protocolString = e.getKey().getDelay().toString();
			SortedMap<Long, List<Data>> mapBeforeCalcMean = getMeanMapBeforeAvg(e.getValue());
			SortedMap<Long, Data> meanMap = createMeanMap(mapBeforeCalcMean);
			System.out.println(e.getKey());

			String anytimeInfoString = getAnytimeString();
			for (Entry<Long, Data> e1 : meanMap.entrySet()) {
				String tempAns = dcopString + "," + protocolString + "," + algoString + "," + e1.getValue();
				if (isAnytime) {
					tempAns = tempAns + "," + anytimeInfoString;
				}
				meanLineInExcel.add(tempAns);

			}
		}

	}

	private static String getAnytimeString() {
		if (isAnytime) {
			String formation = "";
			if (anytimeFormation == 1) {
				formation = "DFS";
			}
			String heuristic = "";

			if (anytimeMemoryHuerstic == 1) {
				heuristic = "No Memory Limitation";
			}

			if (anytimeMemoryHuerstic == 2) {
				heuristic = "MSC";
			}

			if (anytimeMemoryHuerstic == 3) {
				heuristic = "FIFO";
			}
			if (anytimeMemoryHuerstic == 3) {
				heuristic = "Random";
			}

			String MemorySize = Integer.toString(anytimeMemoryLimitedSize);
			String isWithDelCombine = "";
			/*
			 * if (deleteAfterCombine) { isWithDelCombine = "Delete Combined"; } else {
			 * isWithDelCombine = "Delete Combined - not";
			 *
			 * }
			 */
			return formation + "," + heuristic + "," + MemorySize + "," + isWithDelCombine;
		}
		return "";
	}

	private static SortedMap<Long, Data> createMeanMap(SortedMap<Long, List<Data>> input) {
		SortedMap<Long, Data> ans = new TreeMap<Long, Data>();
		for (Entry<Long, List<Data>> e : input.entrySet()) {
			ans.put(e.getKey(), new Data(e));
		}
		return ans;
	}

	private static SortedMap<Integer, Data> getMapLastDataPerDcop(List<Mailer> mailersPerProtocol) {
		SortedMap<Integer, Data> ans = new TreeMap<Integer, Data>();

		for (Mailer mailer : mailersPerProtocol) {
			Data lastData = mailer.getLastData();
			int dcopId = mailer.getDcop().getId();
			ans.put(dcopId, lastData);
		}

		return ans;
	}

	private static SortedMap<Long, List<Data>> getMeanMapBeforeAvg(List<Mailer> mailers) {
		SortedMap<Long, List<Data>> ans = new TreeMap<Long, List<Data>>();

		Long firstMax = getFirstMax(mailers);

		if (MainSimulator.isAtomicTime) {
			for (Long i = firstMax; i < termination; i = i + howManyIterationForCalculation) {
				List<Data> listPerIteration = new ArrayList<Data>();
				for (Mailer mailer : mailers) {
					listPerIteration.add(mailer.getDataPerIteration(i));
				}
				ans.put(i, listPerIteration);
			}
		} else {
			for (Long i = firstMax; i < termination; i++) {
				List<Data> listPerIteration = new ArrayList<Data>();
				for (Mailer mailer : mailers) {
					listPerIteration.add(mailer.getDataPerIteration(i));
				}
				ans.put(i, listPerIteration);
			}
		}

		return ans;
	}

	private static Long getFirstMax(List<Mailer> mailers) {
		List<Long> firsts = new ArrayList<Long>();
		for (Mailer m : mailers) {
			firsts.add(m.getFirstKeyInData());
		}
		return Collections.min(firsts);
	}

	// ------------ 1. DCOP CREATION------------
	private static Dcop[] generateDcops() {
		Dcop[] ans = new Dcop[end - start];
		for (int i = 0; i < end - start; i++) {
			int dcopId = i + start;
			ans[i] = createDcop(dcopId).initiate();
		}
		return ans;
	}

	private static Dcop createDcop(int dcopId) {
		Dcop ans = null;
		// use default Domain contractors

		if (myDcopType == DcopType.RandomUniform) {
			ans = new DcopUniform(dcopId, A, D, costLbUniform, costUbUniform, dcopUniformP1, dcopUniformP2);
		}

		if (myDcopType == DcopType.GraphColoring) {
			ans = new DcopGraphColoring(dcopId, A, 3, costLbColor, costUbColor, dcopGraphColoringP1);
		}

		if (myDcopType == DcopType.ScaleFreeNetwork) {
			ans = new DcopScaleFreeNetwork(dcopId, A, D, costLbScale, costUbScale, dcopScaleHubs, dcopScaleNeighbors,
					dcopScaleP2);
		}

		if (myDcopType == DcopType.SolarSystem) {
			ans = new DcopCities(dcopId, A, D, numberOfCities, sdSquareFromCity, minCostCity, maxCostCity, dcopCityP2,
					exponentForNeighborCitizens);
		}


		return ans;
	}

	// ------------ 2. PROTOCOL CREATION------------
	private static List<Protocol> createProtocols() {
		creatorDelay = getCreatorDelays();
		protocolDelayHeader = creatorDelay.getHeader();
		List<ProtocolDelay> delays = creatorDelay.createProtocolDelays();

		creatorDown = getCreatorDowns();
		protocolDownHeader = creatorDown.getHeader();
		List<ProtocolDown> downs = creatorDown.createProtocolDowns();

		List<Protocol> ans = new ArrayList<Protocol>();

		for (ProtocolDelay delay : delays) {
			for (ProtocolDown down : downs) {
				Protocol p = new Protocol(delay, down);
				ans.add(p);
			}
		}
		for (Protocol protocol : ans) {
			mailersByProtocol.put(protocol, new ArrayList<Mailer>());
		}
		return ans;
	}

	private static CreatorDown getCreatorDowns() {
		CreatorDown ans = null;

		if (downType == 0) {
			ans = new CreatorDownNone();
		}

		if (downType == 1) {
			ans = new CreatorDownPoission();
		}

		return ans;
	}

	private static CreatorDelays getCreatorDelays() {
		CreatorDelays ans = null;

		if (myDelayType == DelayType.amountMsgInSystemLinear){
			ans = new CreatorDelaysMsgInSystemLinear();
		}

		if (myDelayType == DelayType.none) {
			ans = new CreatorDelaysNone();
		}

		if (myDelayType == DelayType.normal) {
			ans = new CreatorDelaysNormal();
		}

		if (myDelayType == DelayType.uniform) {
			ans = new CreatorDelaysUniform();
		}

		if (myDelayType == DelayType.Exponential) {
			ans = new CreatorDelaysExponential();
		}

		if (myDelayType == DelayType.Poisson) {
			ans = new CreatorDelaysPossion();
		}

		if (myDelayType == DelayType.distancePois) {
			ans = new CreatorDelaysDistancePoisson();
		}
		if (myDelayType == DelayType.distanceUniform) {
			ans = new CreatorDelaysDistanceUniform();
		}
		if (myDelayType == DelayType.distanceMissingMsg) {
			ans = new CreatorMissingMsgDistance();
		}

		if (myDelayType == DelayType.DelayWithK) {
			ans = new CreatorDelaysWithKLoss();

		}
		return ans;
	}

	// ------------ 3. Run the DCOPs------------

	private static void runDcops(Dcop[] dcops, List<Protocol> protocols) {
		for (Dcop dcop : dcops) {
			int protocolCounter = -1;
			for (Protocol protocol : protocols) {
				protocolCounter += 1;
				ProtocolDelay pd = protocol.getDelay();

				if (pd instanceof ProtocolDelayWithK){
					((ProtocolDelayWithK)pd).updatePublicK();
				}
				Mailer mailer = getMailer(protocol, dcop);
				dcop.dcopMeetsMailer(mailer);
				mailer.mailerMeetsDcop(dcop.getId());
				if (isDalo2Debug) {
					printNeighbors(dcop);
				}
				dcop.initilizeAndStartRunAgents();
				infromAllAgentsUponTimeStamp(protocol, dcop.getAllAgents());
				if (isThreadMailer) {
					executeThreadMailer(mailer, dcop);
				} else {
					mailer.execute();
				}
				addMailerToDataFrames(protocol, mailer);
				System.out.println(
						"Algo: " + AgentVariable.AlgorithmName + "; Finish DCOP: " + dcop.getId() + " " + Dcop.dcopName
								+ "; " + " ; SCORE: " + mailer.getDataPerIteration(termination - 1).getGlobalCost()
								+ "; protocol " + protocol.getDelay());
			}
			System.out.println("----------------------------");
			end_temp = dcop.getId();

			if (end_temp % everyHowManyExcel == 0 && end_temp != 0) {
				createData();
				meanLineInExcel = new ArrayList<String>();
				lastLineInExcel = new ArrayList<String>();
			}

		}

	}

	private static void printNeighbors(Dcop dcop) {
		for (Neighbor n:
			 dcop.getNeighbors()) {
			System.out.println(n);

		}
	}

	private static void infromAllAgentsUponTimeStamp(Protocol protocol, List<Agent> agents) {
		boolean isWithTimeStamp = protocol.getDelay().isWithTimeStamp();
		for (Agent a : agents) {
			a.setIsWithTimeStamp(isWithTimeStamp);
		}

	}

	private static void executeThreadMailer(Mailer mailer, Dcop dcop) {
		Thread t = new Thread((MailerThread) mailer);
		t.start();
		try {
			for (Thread threadAgent : dcop.getAgentThreads()) {
				threadAgent.join();
			}
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private static void addMailerToDataFrames(Protocol protocol, Mailer mailer) {
		mailerAll.add(mailer);
		mailersByProtocol.get(protocol).add(mailer);

	}

	private static Mailer getMailer(Protocol protocol, Dcop dcop) {
		Mailer ans;
		if (isThreadMailer) {
			ans = new MailerThread(protocol, termination, dcop, dcop.getId());
		} else {
			ans = new MailerIterations(protocol, termination, dcop, dcop.getId());
		}

		return ans;
	}

	// ------------ 4. DATA------------

	private static void createHeader(boolean b, String addition) {

		header = "DCOP";
		header = header + "," + protocolDelayHeader + "," + "Algorithm" + "," + AgentVariable.algorithmHeader + ",";
		header = header + Data.header()+",converge (NCLO)";
		if (isAnytime) {
			header = header + "," + "Formation" + "," + "Heuristic" + "," + "Memory Size" + ","
					+ "Delete After Combine";
		}
		if (b) {
			header = addition + "," + header;
		}

	}

	// ------------ 5. Debug------------

	private static void printProblemCreationDebug(Dcop[] dcops) {
		printAmountOfNeighbors(dcops);
		// printConstraintMatrixs(dcops);

	}

	private static void print2DArray(Integer[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print("[" + matrix[i][j] + "]");
			}
			System.out.println();
		}
	}

	private static void printAmountOfNeighbors(Dcop[] dcops) {
		for (Dcop dcop : dcops) {
			System.out.println("-----Dcop number : " + dcop.getId() + " neighbor count-----");
			double sumN = 0;
			for (AgentVariable a : dcop.getVariableAgents()) {
				int aN = a.neighborSize();
				System.out.println("\t" + "Agent " + a.getId() + ": " + aN);
				sumN += aN;
			}
			System.out.println();
			System.out.println("The average amount of neighbors per agent is: " + (sumN / A));
			System.out.println();
		}

	}

}
