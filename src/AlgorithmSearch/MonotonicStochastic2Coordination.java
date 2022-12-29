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
    private Map<NodeId, MS2CHelperInfoIntAndCounter> neighborsDocIdsT;
    private Map<NodeId, MS2CHelperInfoIntAndCounter> neighborsDocIdsT1;
    private Map<NodeId, MS2CHelperInfoIntAndCounter> neighborsColorT;
    private Map<NodeId, MS2CHelperInfoIntAndCounter> neighborsColorT1;
    private double myDocId;


    public MonotonicStochastic2Coordination(int dcopId, int D, int id1) {
        super(dcopId, D, id1);
        this.docIdRandom = new Random((dcopId+nodeId.getId1()+1)*17);
        this.neighborsDocIdsT = new HashMap<NodeId, MS2CHelperInfoIntAndCounter>();
        this.neighborsDocIdsT1 = new HashMap<NodeId, MS2CHelperInfoIntAndCounter>();
        this.neighborsColorT1 = new HashMap<NodeId, MS2CHelperInfoIntAndCounter>();
        this.neighborsColorT = new HashMap<NodeId, MS2CHelperInfoIntAndCounter>();
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
        this.neighborsDocIdsT = new HashMap<NodeId, MS2CHelperInfoIntAndCounter>();
        this.neighborsDocIdsT1 = new HashMap<NodeId, MS2CHelperInfoIntAndCounter>();
        for (NodeId nId: this.neighborsConstraint.keySet()){
            neighborsDocIdsT.put(nId, new MS2CHelperInfoIntAndCounter(0,(double) nId.getId1()));
            neighborsDocIdsT1.put(nId, null);
            neighborsColorT1.put(nId, null);
            neighborsColorT.put(nId, null);
        }

    }


    protected boolean updateMessageInContext(MsgAlgorithm msgAlgorithm) {
        super.updateMessageInContext(msgAlgorithm);
        NodeId senderId = msgAlgorithm.getSenderId();
        if (msgAlgorithm instanceof MsgAMDLSColorAndDoc){
            extractDocInformation(msgAlgorithm,senderId);
        }

        if (msgAlgorithm instanceof MsgAMDLSColor) {
            extractColorInformation(msgAlgorithm,senderId);
        }

        if (MainSimulator.isMS2SDebug){
            System.out.println(this+" view of docs is: "+this.neighborsColorT);
            System.out.println(this+" view of docs is: "+this.neighborsColorT1);
        }
        return true;

    }

    private void extractColorInformation(MsgAlgorithm msgAlgorithm, NodeId senderId) {
        if (this.neighborsColorT.get(senderId)!=null){
            checkIfToPlaceColor(msgAlgorithm,senderId);
        }else{
            placeColorInT(msgAlgorithm);
        }
    }



    private void checkIfToPlaceColor(MsgAlgorithm msgAlgorithm, NodeId senderId) {
        int currentNodeCounter = this.neighborsColorT.get(senderId).getCounter();
        int msgCounter = ((MsgAMDLSColor)msgAlgorithm).getCounter();
        if (currentNodeCounter<msgCounter){
            int neighborColor = ((MsgAMDLSColor)msgAlgorithm).getColor();
            MS2CHelperInfoIntAndCounter helper = new MS2CHelperInfoIntAndCounter(msgCounter,neighborColor);
            this.neighborsColorT1.put(msgAlgorithm.getSenderId(),helper);
        }


    }
    private void checkIfToPlaceDocId(MsgAlgorithm msgAlgorithm, NodeId senderId) {
        int currentNodeCounter = this.neighborsDocIdsT.get(senderId).getCounter();
        int msgCounter = ((MsgAMDLSColorAndDoc)msgAlgorithm).getCounter();
        if (currentNodeCounter<msgCounter){
            double neighborDocId = ((MsgAMDLSColorAndDoc)msgAlgorithm).getDocId();
            MS2CHelperInfoIntAndCounter helper = new MS2CHelperInfoIntAndCounter(msgCounter,neighborDocId);
            this.neighborsDocIdsT1.put(msgAlgorithm.getSenderId(),helper);
        }
    }
    private void extractDocInformation(MsgAlgorithm msgAlgorithm, NodeId senderId) {
        if (this.neighborsDocIdsT.get(senderId)!=null) {
            checkIfToPlaceDocId(msgAlgorithm,senderId);
        }else{
            placeDocIdInT(msgAlgorithm);
        }

    }

    private void placeDocIdInT(MsgAlgorithm msgAlgorithm) {
        int msgCounter = ((MsgAMDLSColorAndDoc)msgAlgorithm).getCounter();
        double neighborDocId = ((MsgAMDLSColorAndDoc)msgAlgorithm).getDocId();
        MS2CHelperInfoIntAndCounter helper = new MS2CHelperInfoIntAndCounter(msgCounter,neighborDocId);
        this.neighborsDocIdsT.put(msgAlgorithm.getSenderId(),helper);
    }

    private void placeColorInT(MsgAlgorithm msgAlgorithm) {
        int msgCounter = ((MsgAMDLSColor)msgAlgorithm).getCounter();
        int neighborColor = ((MsgAMDLSColor)msgAlgorithm).getColor();
        MS2CHelperInfoIntAndCounter helper = new MS2CHelperInfoIntAndCounter(msgCounter,neighborColor);
        this.neighborsColorT.put(msgAlgorithm.getSenderId(),helper);
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
        updateNeighborColors();
    }

    private void updateNeighborColors() {
        for (NodeId nid: this.neighborsColorT.keySet()) {
            this.neighborsColorT.put(nid,this.neighborsColorT1.get(nid));
            this.neighborsColorT1.put(nid,null);
        }
    }

    private void updateNeighborDocs() {
        for (NodeId nid: this.neighborsDocIdsT.keySet()) {
            this.neighborsDocIdsT.put(nid,this.neighborsDocIdsT1.get(nid));
            this.neighborsDocIdsT1.put(nid,null);
        }

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
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    ///////////----------------colors override----------------///////////
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    protected boolean allNeighborsHaveColor() {
        for (NodeId ni:this.neighborsColorT.keySet()) {
            if (this.neighborsColorT.get(ni)==null){
                return false;
            }
        }
        return true;
    }

    protected Set<NodeId> getLargerColorNeighbors() {
        Set<NodeId>largerColorNeighbors = new HashSet<NodeId>();

        for (NodeId ni:this.neighborsColorT.keySet()) {
            try {
                if (this.neighborsColorT.get(ni).getInfoInt() > this.myColor) {
                    largerColorNeighbors.add(ni);
                }
            }catch (Exception e){
                return null;
            }
        }
        return  largerColorNeighbors;

    }



    protected Set<NodeId> getSmallerColorNeighbors() {

        Set<NodeId>smallerColorNeighbors = new HashSet<NodeId>();

        for (NodeId ni:this.neighborsColorT.keySet()) {
            if (this.neighborsColorT.get(ni).getInfoInt()<this.myColor){
                smallerColorNeighbors.add(ni);
            }

        }
        return  smallerColorNeighbors;
    }

    protected Set<NodeId> getNeighborsThatHaveColor() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (Map.Entry<NodeId, MS2CHelperInfoIntAndCounter> e : this.neighborsColorT.entrySet()) {
            if (e.getValue() != null) {
                ans.add(e.getKey());
            }
        }
        return ans;
    }

    protected boolean isColorValid(Integer currentColor) {
        for (MS2CHelperInfoIntAndCounter nColor : neighborsColorT.values()) {
            if (nColor != null) {
                if (nColor.getInfoInt()==(currentColor)) {
                    return false;
                }
            }
        }
        return true;
    }


    protected Set<NodeId> getPotentialNeighbors() {
        Set<NodeId> ans = new HashSet<NodeId>();
        for (NodeId neighbor : this.neighborsColorT.keySet()) {
            int nColor = this.neighborsColorT.get(neighbor).getInfoInt();
            if (nColor - 1 == this.myColor && this.neighborCounters.get(neighbor)== this.selfCounter ){
                ans.add(neighbor);
            }
        }
        return ans;
    }

    protected Set<NodeId> getColorMinusOne(Set<NodeId> smallerColor) {
        Set<NodeId>colorMinusOne = new HashSet<NodeId>();
        for (NodeId ni:smallerColor) {
            int nColor = this.neighborsColorT.get(ni).getInfoInt();
            if (this.myColor -1  == nColor){
                colorMinusOne.add(ni);
            }

        }
        return colorMinusOne;
    }
}
