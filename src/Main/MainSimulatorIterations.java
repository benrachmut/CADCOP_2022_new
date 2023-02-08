package Main;

import AgentsAbstract.Agent;
import AgentsAbstract.NodeId;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Problem.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainSimulatorIterations {
    public static boolean debugConstraints =false;
    public static boolean debugMaxsumBelief =false;
    public static boolean debugMaxsumQ =false;
    public static boolean debugMaxsumR =false;


    public enum Algorithm {maxsum}
    public enum GraphType{circle,uniform}
    public enum CostType{color,uniform}

    public static Algorithm algorithm= Algorithm.maxsum;
    public static GraphType graphType= GraphType.circle;
    public static CostType costType = CostType.uniform;


    public static int numberOfCircles = 1;
    public static int constantColorCost = 100;
    public static int uniformCostLB = 0;
    public static int uniformCostUB = 100;

    public static int parameterForConverges = 30;
    public static int[] agentSizeList = {3};
    public static int[] domainsSizeList = {3};

    public static boolean runKnownAmount = false;
    // if run known amount
    public static int  start= 0;
    public static int end = 100000;
    // if NOT run known amount
    public static int amountNotConverged=100;


    public static int numberOfIterations = 1000;
    public static boolean amIRunning = false;
    public static Map<Integer,Map<Integer,Double>> globalCostsData = new HashMap<Integer,Map<Integer,Double>>(); // dcop id, <Iteration,globalCost>
    public static Map<String,List<Integer>> bitData = new HashMap<String,List<Integer>>();

    public static <DCOP> void main(String[] args) {
        MainSimulator.agentType = 9999999;
        amIRunning = true;
        if (runKnownAmount) {
            Dcop[] dcops = generateDcops();
            runDcops(dcops);
        }else {
            runUntilReachNonConvergeLimit();
        }

    }

    private static void runUntilReachNonConvergeLimit() {
        for (int domainSize:domainsSizeList) {
            for(int agentSize: agentSizeList){
                int dcopId = 0;
                int amountNotConvergedCounter = 0;
                initializeDataStructures();

                while(amountNotConvergedCounter!=amountNotConverged){
                    globalCostsData.put(dcopId,new HashMap<Integer,Double>());
                    Dcop dcop = createDcop(dcopId,agentSize,domainSize);
                    dcop.initiate();
                    boolean isConverged = runDcop(dcop);
                    if (!isConverged) {
                        amountNotConvergedCounter = amountNotConvergedCounter + 1;
                    }
                    dcopId = dcopId+1;
                }

                createData(domainSize,agentSize);
            }

        }
    }

    private static void initializeDataStructures() {
        globalCostsData = new HashMap<Integer,Map<Integer,Double>>();// dcop id, <Iteration,globalCost>
        bitData = new HashMap<String,List<Integer>>();
        bitData.put("DCOP_ID",new ArrayList<Integer>());
        bitData.put("Dust",new ArrayList<Integer>());
        bitData.put("Converges",new ArrayList<Integer>());
        bitData.put("Value_Equality",new ArrayList<Integer>());
        bitData.put("Msg_Equality",new ArrayList<Integer>());


    }

    private static void runDcops(Dcop[] dcops) {

        for (Dcop dcop:dcops) {
            System.out.println("*************DCOP: "+dcop.getId()+"*************");
            if (debugConstraints){
                printConstraints(dcop);
            }
            runDcop(dcop);
        }
    }

    private static boolean runDcop(Dcop dcop) {

        //----------------------

        List<Agent> agents = dcop.getAllAgents();
        agentsInitialize(agents);
        dcop.getVariableNodeMap();
        List<Map<NodeId,Integer>>  valuesInDcop = new ArrayList<Map<NodeId,Integer>>() ;


        for (int i = 0; i < numberOfIterations; i++) {
            globalCostsData.get(dcop.getId()).put(i,dcop.getGlobalCost());
            addVariablesMapToArray(i,valuesInDcop,dcop);
            boolean isConverged = isConvergedFunction(i,valuesInDcop);
            if(isConverged){
                return true;
            }
            //updateData(dataMapPerIteration,dataMapSummary);
            deliverMsgs(agents);
            for (Agent a : agents) {
                placeInformationInLocalView(a);
                a.compute();
                a.sendMsgs();
            }
        } // for single DCOP run
        isConvergedFunction(numberOfIterations,valuesInDcop);
        return false;
    }

    private static boolean isConvergedFunction(int i,List<Map<NodeId, Integer>> valuesInDcop) {
        if(i<parameterForConverges){
            return false;
        }

        Map<NodeId,List<Integer>> valuesInPreviousPerNodeId = getValuesInPreviousPerNodeId(valuesInDcop);
        for (NodeId nId:valuesInPreviousPerNodeId.keySet()) {
            List<Integer>previousSelections = valuesInPreviousPerNodeId.get(nId);
            int selection = previousSelections.get(0);
            for (int j = 1; j < previousSelections.size(); j++) {
                int anotherSelection = previousSelections.get(j);
                if(anotherSelection!=selection){
                    return false;
                }
            }
        }
        return true;

    }

    private static Map<NodeId, List<Integer>> getValuesInPreviousPerNodeId(List<Map<NodeId, Integer>> valuesInDcop) {


        Map<NodeId,List<Integer>> valuesInPreviousPerNodeId = new HashMap<NodeId,List<Integer>>();
        for (Map<NodeId, Integer>map:valuesInDcop) {
            for (NodeId nId :map.keySet()) {
                if (!valuesInPreviousPerNodeId.containsKey(nId)){
                    valuesInPreviousPerNodeId.put(nId,new ArrayList<>());
                }
                valuesInPreviousPerNodeId.get(nId).add(map.get(nId));
            }
        }
        return valuesInPreviousPerNodeId;
    }

    private static void addVariablesMapToArray(int i, List<Map<NodeId,Integer>> valuesInDcop, Dcop dcop) {
        int locationInList = i%parameterForConverges;
        if (i<parameterForConverges){
            valuesInDcop.add(dcop.getVariableNodeMap());
        }else {
            valuesInDcop.set(locationInList, dcop.getVariableNodeMap());
        }
    }


    private static void printConstraints(Dcop dcop) {
        for (Neighbor n:dcop.getNeighbors()) {
            System.out.print("constraints between: "+n.getA1().getNodeId()+","+n.getA2().getNodeId());
            Integer[][] ccc = n.getConstraints();
            for (int i = 0; i < ccc.length; i++) {
                System.out.println();
                for (int j = 0; j < ccc[i].length; j++) {
                    System.out.print(ccc[i][j]+",");
                }
            }
            System.out.println();
        }
    }

    private static void agentsInitialize(List<Agent> agents) {
        for (Agent a : agents) {
            a.initialize();
        }
    }

    private static void placeInformationInLocalView(Agent a) {
        List<Msg> msgs = a.inbox.extractForIteration();
        for (Msg msg:msgs) {
            a.updateMessageInContext((MsgAlgorithm)msg);
        }
    }

    private static void deliverMsgs(List<Agent> agents) {
        Map<NodeId,List<Msg>> msgsByReceiver = getMsgsByReceiver(agents);
        deliverMsgToAgentInbox(msgsByReceiver,agents);
    }

    private static void deliverMsgToAgentInbox(Map<NodeId, List<Msg>> msgsByReceiver,List<Agent> agents) {
        for (NodeId receiver: msgsByReceiver.keySet()) {
            Agent agent = getAgentById(agents,receiver);
            agent.inbox.insert(msgsByReceiver.get(receiver));
        }
    }

    private static Agent getAgentById(List<Agent> agents, NodeId receiver) {
        for (Agent agent: agents) {
            if (receiver.equals(agent.getNodeId())){
                return agent;
            }
        }
        throw  new RuntimeException("agent's id was not found");
    }

    private static Map<NodeId, List<Msg>> getMsgsByReceiver(List<Agent> agents) {
        Map<NodeId,List<Msg>> msgsByReceiver =new HashMap<NodeId,List<Msg>>();
        for (Agent agent:agents) {
            List<Msg> msgs = agent.outbox.extractForIteration();
            for (Msg msg: msgs) {
                NodeId receiver = msg.getRecieverId();
                if (!msgsByReceiver.containsKey(receiver)){
                    msgsByReceiver.put(receiver,new ArrayList<Msg>());
                }
                msgsByReceiver.get(receiver).add(msg);
            }
        }
        return msgsByReceiver;
    }


    // ------------ 1. DCOP CREATION------------
    private static Dcop[] generateDcops() {
        Dcop[] ans = new Dcop[end - start];
        for (int domainSize:domainsSizeList) {
            for(int agentSize: agentSizeList){
                for (int i = 0; i < end - start; i++) {
                    int dcopId = i + start;
                    ans[i] = createDcop(dcopId,agentSize,domainSize).initiate();
                }
            }

        }
        return ans;
    }

    private static Dcop createDcop(int dcopId, int agentSize, int domainSize) {

        Dcop ans = null;
        // use default Domain contractors
        if (graphType == GraphType.circle){
            if (costType == CostType.color) {
                ans = new DcopCircle(dcopId, agentSize, domainSize, numberOfCircles,constantColorCost);
            }
            if (costType == CostType.uniform) {
                ans = new DcopCircle(dcopId, agentSize, domainSize, numberOfCircles,uniformCostLB,uniformCostUB);
            }
        }

        return ans;
    }

}

