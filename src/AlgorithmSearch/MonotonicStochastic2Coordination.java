package AlgorithmSearch;

import AgentsAbstract.NodeId;
import Main.MainSimulator;
import Messages.Msg;
import Messages.MsgAMDLSColor;
import Messages.MsgAMDLSColorAndDoc;
import Messages.MsgAlgorithm;

import java.util.*;

public class MonotonicStochastic2Coordination extends MonotonicDeterministicColors2Coordination{
    public static int moduleChangeColor=1;
    private  Random docIdRandom;
    private Map<NodeId, Double> neighborsDocIds;
    private Map<NodeId, Double> neighborsDocIdsT1;

    private double myDocId;
    private Map<NodeId, Integer> neighborColorT1;


    public MonotonicStochastic2Coordination(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        this.docIdRandom = new Random((dcopId+nodeId.getId1()+1)*17);
        this.neighborsDocIds = new HashMap<NodeId, Double>();

    }


    @Override
    public void initialize() {
        myDocId = this.nodeId.getId1();

        if (canSetColor()){
            this.myColor = 1;
            this.sendColorMsgs();
            if (MainSimulator.isMDC2CDebug){
                System.out.println(this.nodeId+", color: "+ this.myColor);
            }
        }
    }



    @Override
    protected void resetAgentGivenParametersV3() {
        super.resetAgentGivenParametersV3();
        docIdRandom = new Random((dcopId+nodeId.getId1()+1)*17);
        this.neighborsDocIds = new HashMap<NodeId, Double>();
        for (NodeId nId: this.neighborsConstraint.keySet()){
            neighborsDocIds.put(nId, (double) nId.getId1());
        }

    }


    protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        if (msgAlgorithm instanceof MsgAMDLSColorAndDoc){
            double neighborDocId = ((MsgAMDLSColorAndDoc)msgAlgorithm).getDocId();
            this.neighborsDocIds.put(msgAlgorithm.getSenderId(),neighborDocId);
        }
        if (MainSimulator.isMS2SDebug){
            System.out.println(this+" view of docs is: "+this.neighborsDocIds);

        }
        return super.updateMessageInContext(msgAlgorithm);

    }
        @Override
    protected void sendColorMsgs() {
        if (this.selfCounter%moduleChangeColor == 0) {
            moveToNextDocPhase();
            this.sendColorWithDocId();
        }else {
            super.sendColorMsgs();
        }
    }

    private void moveToNextDocPhase() {
        this.myDocId = docIdRandom.nextDouble();
        updateNeighborDocs();
        updateNeigborColors();

    }

    private void updateNeighborDocs() {

    }


    @Override
    protected void sendColorToTheRest(NodeId whoNotToSendColor) {
        if (this.selfCounter%moduleChangeColor == 0) {
            moveToNextDocPhase();

            sendColorToTheRestWithDoc(whoNotToSendColor);

        }else {
            super.sendColorToTheRest(whoNotToSendColor);
        }



        //if (MainSimulator.isMDC2CDebug){
        //  System.out.println(this+" sent color msg to the rest: "+whoISent);
        //}

    }

    private void sendColorToTheRestWithDoc(NodeId whoNotToSendColor) {

        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
        Set<NodeId>whoISent = new HashSet<NodeId>();
        for (NodeId receiverNodeId : neighborsConstraint.keySet()) {
            if (receiverNodeId.getId1()!=whoNotToSendColor.getId1()){
                MsgAMDLSColorAndDoc msg= new MsgAMDLSColorAndDoc(this.nodeId, receiverNodeId, this.valueAssignment,  this.timeStampCounter, this.time, this.selfCounter , this.myColor,this.myDocId);
                msgsToInsertMsgBox.add(msg);
                whoISent.add(receiverNodeId);
            }
        }

        if(!msgsToInsertMsgBox.isEmpty()) {
            outbox.insert(msgsToInsertMsgBox);
        }


    }


    private void sendColorWithDocId(){
        if (this.checkConsistencyV2()&& this.selfCounter!=1){
            this.selfCounter = this.selfCounter +1;
        }

        List<Msg> msgsToInsertMsgBox = new ArrayList<Msg>();
        for (NodeId receiverNodeId : neighborsConstraint.keySet()) {
            MsgAMDLSColorAndDoc msg= new MsgAMDLSColorAndDoc(this.nodeId, receiverNodeId, this.valueAssignment,  this.timeStampCounter, this.time, this.selfCounter , this.myColor,this.myDocId);
            msgsToInsertMsgBox.add(msg);
        }
        outbox.insert(msgsToInsertMsgBox);
    }
}
