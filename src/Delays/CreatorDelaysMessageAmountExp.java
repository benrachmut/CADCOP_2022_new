package Delays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreatorDelaysMsgMailerHoldExp extends CreatorDelays {
    private double[] base = {1.1};

    @Override
    protected ProtocolDelay createDefultProtocol(double gamma) {
        return new  ProtocolDelaysMsgMailerHoldExp(gamma);
    }


    @Override
    protected Collection<? extends ProtocolDelay> createCombinationsDelay(boolean timestampBoolean, double gamma) {
        List<ProtocolDelay> ans = new ArrayList<ProtocolDelay>();
        for (double base : base) {
            ans.add(new ProtocolDelaysMsgMailerHoldExp(timestampBoolean, gamma, base));
        } // sigma
        return ans;
    }

    @Override
    protected String header() {
        return "base";
    }

    @Override
    public String name() {
        return "MsgDepandentExp";
    }
}
