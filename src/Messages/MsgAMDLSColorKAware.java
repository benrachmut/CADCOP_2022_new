package Messages;

import AgentsAbstract.NodeId;

import java.util.Map;

public class MsgAMDLSColorKAware extends MsgAMDLSKAware {
    private Integer color;

    public MsgAMDLSColorKAware(NodeId sender, NodeId reciever, Object context, int timeStamp, long time, int counter,
                               Map<NodeId, Map<NodeId, Integer>> timestampMap,
                               Integer color) {
        super(sender, reciever, context, timeStamp, time, counter,timestampMap);
        this.color = color;
    }

    public Integer getColor() {
        return this.color;
    }
}