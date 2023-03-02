package Delays;

import java.util.Random;

public class ProtocolDelaysMsgInSystemLinear extends ProtocolDelayMessageAmount {
    private double slope;
    public ProtocolDelaysMsgInSystemLinear(double gamma) {
        super(true, false, gamma);
        this.slope = 0;
    }

    public ProtocolDelaysMsgInSystemLinear(boolean isTimeStamp, double gamma, double slope) {
        super(true, isTimeStamp, gamma);
        this.slope =slope;
    }



    @Override
    protected String getStringParameters() {
        return this.slope+"";
    }

    @Override
    protected boolean checkSpecificEquals(ProtocolDelay other) {
        return false;
    }

    @Override
    protected Double createDelay(Random r, int msgAmount) {
        int rndPois = getRandomPoisson(r,msgAmount);
        rndPois = Math.max(5,rndPois);
        return rndPois*slope;//Math.pow(this.base,rndPois);
    }


    private int getRandomPoisson(Random random, int lambda) {
        int rndCost = 0;
        if (lambda <30) {
            rndCost = getPoisRandomNumber(random, lambda);
        }else{
            double Z = random.nextGaussian();
            rndCost =(int) (Z*Math.sqrt(lambda)+lambda);
        }
        return rndCost;
    }

    private int getPoisRandomNumber(Random random, int lambda) {
        int r = 0;
        double a = random.nextDouble();
        double p = Math.exp(-lambda);

        while (a > p) {
            r++;
            a = a - p;
            p = p * lambda / r;
        }
        return r;

    }
}
