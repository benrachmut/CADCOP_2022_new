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

    public static int[] agentSizeList = {4};
    public static int[] domainsSizeList = {4};


    public static int  start= 0;
    public static int end = 10;
    public static int numberOfIterations = 1000;
    public static boolean amIRunning = false;





    public static <DCOP> void main(String[] args) {
        MainSimulator.agentType = 9999999;
        amIRunning = true;
        Dcop[] dcops = generateDcops();
        runDcops(dcops);

    }

    private static void runDcops(Dcop[] dcops) {

        for (Dcop dcop:dcops) {
            List<Agent> agents = dcop.getAllAgents();
            agentsInitialize(agents);

            for (int i = 0; i < numberOfIterations; i++) {
                deliverMsgs(agents);
                for (Agent a : agents) {
                    placeInformationInLocalView(a);
                    a.compute();
                    a.sendMsgs();
                }
            }// for single DCOP run
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

