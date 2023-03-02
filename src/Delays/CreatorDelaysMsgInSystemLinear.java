package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreatorDelaysMsgInSystemLinear extends CreatorDelays {
    private double[] slopes = {100};

    @Override
    protected ProtocolDelay createDefultProtocol(double gamma) {
        return new ProtocolDelaysMsgInSystemLinear(gamma);
    }


    @Override
    protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean timestampBoolean, double gamma) {
        List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
        for (double slope : slopes) {
            ans.add(new ProtocolDelaysMsgInSystemLinear(timestampBoolean, gamma, slope));
        } // sigma
        return ans;
    }

    @Override
    protected String header() {
        return "base";
    }

    @Override
    public String name() {
        return "MsgInSystemExp";
    }
}
