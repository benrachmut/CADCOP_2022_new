package AlgorithmSearch;

import AgentsAbstract.AgentVariable;
import AgentsAbstract.AgentVariableSearch;
import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.Msg;
import Messages.MsgAlgorithm;
import Messages.MsgMSOS;
import Messages.MsgValueAssignmnet;

import java.util.*;

public class MonotonicStochasticOrderSearch extends AgentVariableSearch {
    public static  char typeDecision = 'c' ;
    private Map<NodeId,Integer> neighborCounters;
    private Map<NodeId,Map<Integer,Double>>neighborDocsIds;
    private Map<NodeId,MsgMSOS> msgsFromNeighbors;
    private int selfCounter;
    private double selfDocId;
    private Random rndForDoc;
    private boolean isConsistent;


    public MonotonicStochasticOrderSearch(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        AMDLS_V1.typeDecision = 'c';
        updateAlgorithmHeader();
        updateAlgorithmData();
        resetAgentGivenParametersV3();
    }

    @Override
    protected void resetAgentGivenParametersV3() {
        rndForDoc = new Random(this.nodeId.getId1()*99999);
        this.selfDocId = rndForDoc.nextDouble();
        this.selfCounter = 1;
        this.neighborCounters = new HashMap<NodeId,Integer>();
        this.neighborDocsIds = new HashMap<NodeId,Map<Integer,Double>>();
        this.msgsFromNeighbors= new HashMap<NodeId,MsgMSOS>();
        for (NodeId nodeId: this.neighborsConstraint.keySet()) {
            neighborCounters.put(nodeId,0);
            Map<Integer,Double>temp_=new HashMap<>();
            int id_ = nodeId.getId1();
            temp_.put(0,(double)id_ );
            neighborDocsIds.put(nodeId,temp_);
            msgsFromNeighbors.put(nodeId,null);
        }
        isConsistent = false;
        //System.out.println(this.nodeId+" "+this.selfCounter);

    }


    @Override
    public void initialize() {
        this.sendMsgs();


    }
    @Override
    public void updateAlgorithmHeader() {
        AgentVariable.algorithmHeader = "";
    }

    @Override
    public void updateAlgorithmData() {
        AgentVariable.algorithmData = "";
    }

    @Override
    public void updateAlgorithmName() {
        AgentVariable.AlgorithmName = "MSOS";
    }




    @Override
    public boolean getDidComputeInThisIteration() {

        return isConsistent;
    }

    @Override
    protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgMSOS) {
            NodeId sender = msgAlgorithm.getSenderId();
            MsgMSOS msg = (MsgMSOS)msgAlgorithm;
            this.msgsFromNeighbors.put(sender, msg);
            this.neighborDocsIds.get(sender).put(msg.getTimeStamp(),msg.getDocId());
            this.neighborCounters.put(sender,msg.getCounter());


        }else{
            throw new RuntimeException("can receive only MsgMSOS");
        }

        if (MainSimulator.isMSOSDebug){
            NodeId sender = msgAlgorithm.getSenderId();

            System.out.println(this.nodeId+ " " +
                    "recieve message  from: "+sender);
        }
        return true;
    }

    @Override
    protected int getSenderCurrentTimeStampFromContext(MsgAlgorithm msgAlgorithm) {
        MsgAlgorithm msg = this.msgsFromNeighbors.get(msgAlgorithm.getSenderId());

        return msg.getTimeStamp();
    }


    @Override
    protected boolean compute() {

        changeValueAssignment();
        this.selfDocId = rndForDoc.nextDouble();
        this.selfCounter = this.selfCounter +1;

        //if (this.countersEqualOrAboveMe()){
          //  if (this.countersRelativeToDocsId()){
            //    this.selfCounter = this.selfCounter +1;
              //  this.timeStampCounter = this.timeStampCounter+1;
            //}
        //}

        return true;
    }

    private void changeValueAssignment() {
        if (typeDecision == 'a'|| typeDecision == 'A') {
            this.valueAssignment = getCandidateToChange_A();
        }
        if (typeDecision == 'b'|| typeDecision == 'B') {
            this.valueAssignment = getCandidateToChange_B();
        }
        if (typeDecision == 'c' || typeDecision == 'C') {
            this.valueAssignment = getCandidateToChange_C();
        }
    }


    @Override
    public void sendMsgs() {
        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
        for (NodeId recieverNodeId : neighborsConstraint.keySet()) {
            MsgMSOS mva = new MsgMSOS(this.nodeId, recieverNodeId, this.valueAssignment,
                    this.timeStampCounter, this.time,this.selfDocId,this.selfCounter);

            if (MainSimulator.isMSOSDebug){
                System.out.println("sent message "+this.nodeId+ ", valueAssignment: "+this.valueAssignment

                        + ", selfCounter: "+this.selfCounter

                        + ", selfDocId: "+this.selfDocId

                        + ", recieverNodeId: "+recieverNodeId);
            }

            msgsToInsertMsgBox.add(mva);
        }

        outbox.insert(msgsToInsertMsgBox);
    }

    @Override
    protected void changeReceiveFlagsToTrue(MsgAlgorithm msgAlgorithm) {


        if (this.countersEqualOrAboveMe()){
            if (this.countersRelativeToDocsId()){
                isConsistent = true;
                return;
            }
        }
        isConsistent = false;
    }

    private boolean countersRelativeToDocsId() {
        Set<NodeId> smallerDoc = new HashSet<NodeId>();
        Set<NodeId> largerDoc = new HashSet<NodeId>();
        createSmallerLargerSet(smallerDoc,largerDoc);

        for (NodeId nodeId:  largerDoc) {
            int counterN = this.neighborCounters.get(nodeId);
            if (counterN!=selfCounter){
                return false;
            }
        }

        for (NodeId nodeId: smallerDoc) {
            int counterN = this.neighborCounters.get(nodeId);
            if (counterN!=selfCounter+1){
                return false;
            }
        }

        return true;
    }

    private void createSmallerLargerSet(Set<NodeId> smallerDoc, Set<NodeId> largerDoc) {
        for (NodeId nodeId: this.neighborDocsIds.keySet()) {
            double neighborDocId = this.neighborDocsIds.get(nodeId).get(this.timeStampCounter);
            if (this.selfDocId<neighborDocId){
                largerDoc.add(nodeId);
            }
            if (this.selfDocId>neighborDocId){
                smallerDoc.add(nodeId);
            }
            if (this.selfDocId==neighborDocId){
                throw new RuntimeException("cannot have the same id as neighbor, myId:"+this.selfDocId+", neighbor id:"+neighborDocId);
            }
        }
    }


    private boolean countersEqualOrAboveMe() {
        for (NodeId nodeId:this.neighborCounters.keySet()) {
            int nCounter = this.neighborCounters.get(nodeId);
            if (nCounter<this.selfCounter) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void changeReceiveFlagsToFalse() {
        isConsistent = false;
    }




}
