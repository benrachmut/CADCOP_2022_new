package AlgorithmInference;

import AgentsAbstract.AgentFunction;
import AgentsAbstract.NodeId;
import Main.MainSimulatorIterations;
import Main.UnboundedBuffer;
import Messages.Msg;
import Messages.MsgAlgorithm;

import java.util.*;

public class MaxSumFunctionBen extends AgentFunction {


    private  int id1;
    private  int id2;
    private Integer[][] constraintsTemp;
    private double[][] constraints;

    private Map<NodeId,double[][]> constraintsMap;
    private Random r;
    private Map<NodeId,Map<Integer,Double>> localView;
    private Map<NodeId,Map<Integer,Double>> infoToSend;

    public MaxSumFunctionBen(int dcopId, int D, int id1, int id2, Integer[][] constraints) {
        super(dcopId, D, id1, id2);
        this.nodeId = new NodeId(id1,id2,false);
        this.outbox = new UnboundedBuffer<Msg>();
        this.inbox = new UnboundedBuffer<Msg>();
        this.id1 = id1;
        this.id2 = id2;
        r = new Random((dcopId+1)*17+id1*18+id2*23);
        createConstraintMap(constraints);


        localView = new HashMap<NodeId,Map<Integer,Double>>();
        updateInitialLocalView();





    }

    private void createConstraintMap(Integer[][] constraints) {
        this.constraintsTemp = constraints;
        this.constraints = new double[constraints.length][constraints[0].length];
        if (MaxSumVariableBen.isWithDust) {
            addDust();
        }
        this.constraintsMap = new HashMap<>();
        this.constraintsMap.put(new NodeId(id1),this.constraints);
        this.constraintsMap.put(new NodeId(id2),transposeConstraintMatrix(this.constraints));
        constraintsTemp = null;
    }

    private void updateInitialLocalView() {

        for (NodeId nId:constraintsMap.keySet()) {
            Map <Integer,Double> nIdInfo = new HashMap<Integer,Double>();
            for (int i = 0; i < this.domainSize; i++) {
                nIdInfo.put(i,0.0);
            }
            this.localView.put(nId,nIdInfo);
        }
    }


    public static double[][] transposeConstraintMatrix(double[][] input) {
        double[][] ans = new double[input.length][input.length];
        for (int i = 0; i < ans.length; i++) {
            for (int j = 0; j < ans.length; j++) {
                ans[j][i] = input[i][j];
            }
        }
        return ans;
    }

    protected void addDust() {
        for (int i = 0; i < constraints.length; i++) {
            for (int j = 0; j < constraints[i].length; j++) {
                double randomValue = this.r.nextDouble()/ 10000;
                constraints[i][j] = (double)constraintsTemp[i][j] + randomValue;
            }
        }
    }

    @Override
    public String toString() {
        return "AgentFunction:"+this.nodeId;
    }

    @Override
    public void initialize() {
        compute();
        sendMsgs();
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
        return true;

    }

    @Override
    public boolean getDidComputeInThisIteration() {
        return false;
    }

    @Override
    public boolean compute() {
        resetInfoToSend();
        for (NodeId sendTo:localView.keySet()) {
            Map<Integer,Double> infoToSendMap = new HashMap<Integer,Double>();
            NodeId othersNodeId = getOtherNodeIds(sendTo);
            Set<Integer> domainsSet = getDomainSet(sendTo);
            Set<Integer> neighborsDomainSet = getDomainSet(othersNodeId);
            for (Integer potentialDomain:domainsSet) {
                List<Double>listOfSums = new ArrayList<Double>();
                for (Integer otherDomain:neighborsDomainSet) {
                    double infoFromMsg = getInfoFromMsg(othersNodeId,otherDomain);
                    double infoFromConstraint = getInfoFromConstraint(sendTo,potentialDomain,otherDomain);
                    listOfSums.add(infoFromConstraint+ infoFromMsg);
                }
                double doubleToSend = Collections.min(listOfSums);
                infoToSendMap.put(potentialDomain,doubleToSend);
                this.infoToSend.put(sendTo,infoToSendMap);
            }
        }

        if (MainSimulatorIterations.debugMaxsumR){
            printRInfo();
        }

        return true;
    }

    private void printRInfo() {
        System.out.println("******"+this.toString()+" R:");
        for (NodeId nodeId: this.infoToSend.keySet()) {
            System.out.print("**"+nodeId+":{");
            for (Integer domain:this.infoToSend.get(nodeId).keySet()) {
                System.out.print("<"+domain+","+this.infoToSend.get(nodeId).get(domain)+">;");
            }
            System.out.println("}");


        }

    }

    private double getInfoFromConstraint(NodeId sendTo, Integer potentialDomain, Integer otherDomain) {
    double[][] constraintOfSendTo  = this.constraintsMap.get(sendTo);
    double cost = constraintOfSendTo[potentialDomain][otherDomain];
    return cost;

    }



    private double getInfoFromMsg(NodeId othersNodeId, Integer otherDomain) {
        Map<Integer,Double> infoFromOtherNodeId = this.localView.get(othersNodeId);
        Double numberReceivedFromDomain = infoFromOtherNodeId.get(otherDomain);
        return numberReceivedFromDomain;
    }

    private void resetInfoToSend() {
        this.infoToSend = new HashMap<NodeId,Map<Integer,Double>>();

        for (NodeId nodeId: this.infoToSend.keySet()){
            this.infoToSend.put(nodeId,new HashMap<Integer,Double>());
        }
    }

    private Set<Integer> getDomainSet(NodeId sendTo) {
        Map<Integer,Double> infoMap = this.localView.get(sendTo);
        return infoMap.keySet();
    }

    private NodeId getOtherNodeIds(NodeId sendTo) {
        Set<NodeId>ans = new HashSet<NodeId>();
        for (NodeId nodeId:localView.keySet()) {
            if (!nodeId.equals(sendTo)){
                ans.add(nodeId);
            }
        }

        if (ans.size()!=1){
            throw new RuntimeException("function node has more then two neighbors");
        }
        for (NodeId nId: ans) {
            return nId;
        }
        throw new RuntimeException("there is a mistake");


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
    protected void resetAgentGivenParametersV2() {

    }
}
