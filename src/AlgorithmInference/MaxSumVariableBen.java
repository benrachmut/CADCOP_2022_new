package AlgorithmInference;

import AgentsAbstract.AgentVariableInference;
import AgentsAbstract.NodeId;
import Main.MainSimulatorIterations;
import Main.UnboundedBuffer;
import Messages.Msg;
import Messages.MsgAlgorithm;

import java.util.*;

public class MaxSumVariableBen extends AgentVariableInference {
    private Map<Integer,Double> currentContext;
    private Map<NodeId, Map<Integer,Double>> localView;
    private Map<NodeId,Map<Integer,Double>> infoToSend;

    public static boolean isWithDust = true;
    public static boolean isWithValueEqualityNotOnlyMin = false;
    public static boolean isWithValueEqualityMin = false;
    public static boolean isWithMsgEqualityNotOnlyMin = false;
    public static boolean isWithMsgEqualityMin = false;


    public MaxSumVariableBen(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        this.localView = new HashMap<NodeId,Map<Integer,Double>>();
        this.infoToSend = new HashMap<NodeId,Map<Integer,Double>>();
        this.currentContext = new HashMap<Integer,Double>();
        for (int i = 0; i <D ; i++) {
            currentContext.put(i,0.0);
        }

        this.outbox = new UnboundedBuffer<Msg>();
        this.inbox = new UnboundedBuffer<Msg>();
        this.nodeId = new NodeId(id1);
        isWithValueEqualityNotOnlyMin = false;
        isWithValueEqualityMin = false;
        isWithMsgEqualityNotOnlyMin = false;
        isWithMsgEqualityMin = false;
    }




    @Override
    public String toString() {
        return "AgentVariable:"+this.nodeId;
    }

    @Override
    public void initialize() {

    }

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        return 0;
    }

    @Override
    public boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        NodeId sender = msgAlgorithm.getSenderId();
        Map<Integer,Double> infoFromMsg = (HashMap<Integer,Double>)msgAlgorithm.getContext();
        this.localView.put(sender,infoFromMsg);
        this.checkForMsgEquality(sender);

        return true;
    }

    private void checkForMsgEquality(NodeId sender) {
        if (!isWithMsgEqualityMin) {
            isWithMsgEqualityMin = this.checkForEqualityMinMsg(sender);
        }
        if (!isWithMsgEqualityNotOnlyMin) {
            isWithMsgEqualityNotOnlyMin = this.checkForEqualityNotOnlyMinMsg(sender);
        }
    }


    private boolean checkForEqualityMinMsg(NodeId sender) {
       /*
        Map<Double,Integer> repetitions= getByRepetitions(currentContext.values());
        double minVal = Collections.min(currentContext.values());
        repetitions = filterAboveOne(repetitions);
        return  repetitions.containsKey(minVal);
        */

        Map<Integer, Double>  infoFromMsg = this.localView.get(sender);
        Map<Double,Integer> repetitions= getByRepetitions(infoFromMsg.values());
        double minVal = Collections.min(infoFromMsg.values());
        repetitions = filterAboveOne(repetitions);
        boolean ans = repetitions.containsKey(minVal);
        return  ans;
    }


    private boolean checkForEqualityNotOnlyMinMsg(NodeId sender) {
        Map<Integer, Double>  infoFromMsg = this.localView.get(sender);
        Map<Double,Integer> repetitions= getByRepetitions(infoFromMsg.values());
        repetitions = filterAboveOne(repetitions);
        return !repetitions.isEmpty();
    }



    private static Map<Double, Integer> filterAboveOne(Map<Double, Integer> repetitions) {
        Map<Double, Integer> ans = new HashMap<>();
        for (Double d:repetitions.keySet()) {
            if (repetitions.get(d)>1) {
                ans.put(d, repetitions.get(d));
            }
        }
        return ans;
    }



    private static Map<Double, Integer> getByRepetitions(Collection<Double> values) {
        Map<Double, Integer>  ans = new HashMap<>();
        for (Double d:values) {
            if (!ans.containsKey(d)){
                ans.put(d,0);
            }
            int updatedCounter = ans.get(d)+1;
            ans.put(d,updatedCounter);
        }
        return ans;
    }


    @Override
    public boolean getDidComputeInThisIteration() {
        return false;
    }

    @Override
    public boolean compute() {
        selectValueForVariable();
        createMsgs();
        return true;
    }

    private void createMsgs() {
        for (NodeId sendTo: this.localView.keySet()) {
            Map<Integer,Double> infoMap = createInfoMap(sendTo);
            this.infoToSend.put(sendTo,infoMap);
        }


        if (MainSimulatorIterations.debugMaxsumQ){
                printQInfo();
        }
        normalizeInfo();



    }

    private void printQInfo() {
        System.out.println("******"+this.toString()+" Q:");
        for (NodeId nodeId: this.infoToSend.keySet()) {
            System.out.print("**"+nodeId+":{");
            for (Integer domain:this.infoToSend.get(nodeId).keySet()) {
                System.out.print("<"+domain+","+this.infoToSend.get(nodeId).get(domain)+">;");
            }
            System.out.println("}");


        }

    }

    private void normalizeInfo() {
        for (NodeId nodeId : this.infoToSend.keySet()) {
            Map<Integer, Double> map = this.infoToSend.get(nodeId);
            double minVal = Collections.min(map.values());
            for (Integer domain : map.keySet()) {
                double currentVal = map.get(domain);
                double updatedVal = currentVal - minVal;
                if (updatedVal < 0) {
                    throw new RuntimeException("something with normalization doesnt make sense");
                }
                this.infoToSend.get(nodeId).put(domain, updatedVal);
            }
        }
    }
        /*
        double minValSent =  this.getMinValSent();
        boolean flag = false;

        for (NodeId nodeId:this.infoToSend.keySet()) {
            Map<Integer,Double> map = this.infoToSend.get(nodeId);
            for (Integer domain:map.keySet()) {
                double currentVal = map.get(domain);
                double updatedVal = currentVal-minValSent;
                if (updatedVal == 0){
                    flag = true;
                }
                if (updatedVal<0){
                    throw  new RuntimeException("something with normalization doesnt make sense");
                }
                this.infoToSend.get(nodeId).put(domain,updatedVal);
            }
        }
        if (!flag){
            throw  new RuntimeException("something with normalization doesnt make sense");
        }
        */


/*
    private double getMinValSent() {
        List<Double>list = new ArrayList<Double>();
        for (NodeId nodeId:this.infoToSend.keySet()) {
            Map<Integer,Double> m = this.infoToSend.get(nodeId);
            list.addAll(m.values());
        }
        return Collections.min(list);
    }

 */

    private Map<Integer, Double> createInfoMap(NodeId sendTo) {
        Map<Integer, Double> infoMap = new HashMap<Integer, Double>();
        for (NodeId otherNodeId : this.localView.keySet()) {
            if (!sendTo.equals(otherNodeId)) {
                addValFromAMsgToAllDomains(otherNodeId, infoMap);
            }
        }
        return infoMap;
    }


    private void addValFromAMsgToAllDomains(NodeId otherNodeId, Map<Integer, Double> infoMap) {

        Set<Integer>domains = this.localView.get(otherNodeId).keySet();
        for (Integer domain:domains ) {
            if (!infoMap.containsKey(domain)){
                infoMap.put(domain,0.0);
            }
            double previousValue =infoMap.get(domain);
            double infoFromMsg = this.localView.get(otherNodeId).get(domain);
            double updatedVal = previousValue+infoFromMsg;
            infoMap.put(domain,updatedVal);
        }
    }

    private void selectValueForVariable() {
        this.cleanCurrentContext();
        this.updateCurrentContextFromLocalView();
        this.selectValueFromCurrentContext();
        if (MainSimulatorIterations.debugMaxsumBelief){
            printBelief();
        }
    }

    private void printBelief() {
        System.out.println(this.toString()+ " belief:");
        for (Integer domain: this.currentContext.keySet()) {
            System.out.println(domain+":"+this.currentContext.get(domain));
        }
    }

    private void selectValueFromCurrentContext() {
        double minValue = Collections.min(this.currentContext.values());
        checkValueEquality();

        //if (isWithValueEqualityMin){
          //  System.out.println(1);
        //}

        List<Integer> domains = getDomains(minValue);

        this.valueAssignment = domains.get(0);
    }

    private void checkValueEquality() {
        if (!isWithValueEqualityNotOnlyMin) {
            isWithValueEqualityNotOnlyMin = checkForEqualityNotOnlyMinValue();
        }
        if (!isWithValueEqualityMin) {
            isWithValueEqualityMin = checkForEqualityMinMsgValue();
        }
    }


    private boolean checkForEqualityMinMsgValue() {
        Map<Double,Integer> repetitions= getByRepetitions(currentContext.values());
        double minVal = Collections.min(currentContext.values());
        repetitions = filterAboveOne(repetitions);
        return  repetitions.containsKey(minVal);
    }


    private boolean checkForEqualityNotOnlyMinValue() {
        Map<Double,Integer> repetitions= getByRepetitions(this.currentContext.values());
        repetitions = filterAboveOne(repetitions);
        return !repetitions.isEmpty();
    }


    private List<Integer> getDomains(double minValue) {
        List<Integer> domains =new ArrayList<Integer>();
        for (Integer d:  this.currentContext.keySet()) {
            double v = this.currentContext.get(d);
            if (v == minValue){
                domains.add(d);
            }
        }
        if (domains.isEmpty()){
            throw new RuntimeException("Doesnt make sense!!!");
        }
        return domains;
    }

    private void updateCurrentContextFromLocalView() {

        for (Integer potentialDomain:this.currentContext.keySet()) {
            double sum = 0.0;
            for (NodeId neighborId: this.localView.keySet()) {
                double infoFromNeighbor= this.localView.get(neighborId).get(potentialDomain);
                sum =sum + infoFromNeighbor;
            }
            this.currentContext.put(potentialDomain,sum);
        }
    }

    private void cleanCurrentContext() {

        for (Integer d:this.currentContext.keySet()) {
            this.currentContext.put(d,null);

        }
    }

    @Override
    public void sendMsgs() {
        List<Msg>msgsList = new ArrayList<Msg>();
        for (NodeId receiver: this.infoToSend.keySet()) {
            Map<Integer,Double> info = this.infoToSend.get(receiver);
            MsgAlgorithm msg = new MsgAlgorithm (this.nodeId,receiver,info,0,0) ;
            msgsList.add(msg);
        }
        this.outbox.insert(msgsList);

    }

    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {

    }

    @Override
    public void changeReceiveFlagsToFalse() {

    }

    @Override
    public void updateAlgorithmHeader() {

    }

    @Override
    public void updateAlgorithmData() {

    }

    @Override
    public void updateAlgorithmName() {

    }

    @Override
    protected void resetAgentGivenParametersV3() {

    }
}
