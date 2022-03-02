package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreatorDelaysWithKLoss extends CreatorDelays{
    private int[] ks= {10};


    @Override
    protected ProtocolDelay createDefultProtocol(double gamma) {
        return new ProtocolDelayOneInK(false, true, 0.0, 1);
    }

    @Override
    protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean isTimeStamp, double gamma) {
        List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
        for (Integer k : ks) {
            ans.add(new ProtocolDelayWithK(isTimeStamp, gamma, k));
        } // sigma
        return ans;
    }

    @Override
    protected String header() {
        return "k";
    }

    @Override
    public String name() {
        return "K LOSS";
    }
}
