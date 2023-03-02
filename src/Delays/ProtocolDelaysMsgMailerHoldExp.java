package Delays;

import java.util.Random;

public class ProtocolDelaysMsgMailerHoldExp extends ProtocolDelayMessageInSystemBased {
    private double base;
    public ProtocolDelaysMsgMailerHoldExp( double gamma) {
        super(true, false, gamma);
        this.base = 0;
    }

    public ProtocolDelaysMsgMailerHoldExp(boolean isTimeStamp, double gamma,double base) {
        super(true, isTimeStamp, gamma);
        this.base =base;
    }



    @Override
    protected String getStringParameters() {
        return this.base+"";
    }

    @Override
    protected boolean checkSpecificEquals(ProtocolDelay other) {
        return false;
    }

    @Override
    protected Double createDelay(Random r, int msgAmount) {
        int rndPois = getRandomPoisson(r,msgAmount);

        return Math.pow(this.base,rndPois);
    }


    private int getRandomPoisson(Random random, int lambda) {
        int rndCost = 0;
        if (lambda <20) {
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
