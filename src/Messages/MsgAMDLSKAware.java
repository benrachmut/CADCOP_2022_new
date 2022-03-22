package Messages;

import AgentsAbstract.NodeId;

import java.util.HashMap;
import java.util.Map;

public class MsgAMDLSKAware extends MsgValueAssignmnet {

    private int counter;
    private boolean fromFuture;
    protected Map<NodeId, Map<NodeId, Integer>> timestampMap;
    public MsgAMDLSKAware(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int counter,
                          Map<NodeId, Map<NodeId, Integer>> timestampMap) {
        super(sender, reciever, context, timeStamp, time);
        this.counter = counter;
        this.timestampMap = new HashMap<NodeId, Map<NodeId, Integer>>();
        for (NodeId nodeId:timestampMap.keySet()) {
            Map<NodeId, Integer> map = timestampMap.get(nodeId);
            Map<NodeId, Integer> t_map = new HashMap<>();
            for (NodeId nodeIdT:map.keySet()) {
                int i = map.get(nodeIdT);
                t_map.put(nodeIdT,i);
            }
            this.timestampMap.put(nodeId,t_map);

        }


        fromFuture = false;
    }

    public MsgAMDLSKAware(MsgAMDLSColorKAware m) {
        super(m.getSenderId(), m.getRecieverId(), m.getContext(), m.getTimeStamp(),m.getTimeOfMsg());
        this.counter = m.getCounter();
        this.timestampMap = m.getTimestampMap();
        fromFuture = false;

    }

    public Map<NodeId, Map<NodeId, Integer>> getTimestampMap(){
        return this.timestampMap;
    }

    public int getCounter() {
        return this.counter;
    }

    public boolean isFromFuture() {
        return fromFuture;
    }

    public void setFromFutureToTrue() {
        fromFuture = true;
    }

}

