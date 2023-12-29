package Messages;

import AgentsAbstract.NodeId;

public class MsgDALOLockRequest extends MsgAlgorithm {
    public MsgDALOLockRequest(NodeId nodeId, NodeId node, Object o, int timeStampCounter, long time) {
        super(nodeId,node,o,timeStampCounter,time);
    }
}
