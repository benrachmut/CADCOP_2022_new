package Delays;

import java.util.Random;

abstract public class ProtocolDelayMessageAmount extends ProtocolDelay{
    public ProtocolDelayMessageAmount(boolean imperfectCommunicationScenario, boolean isTimeStamp, double gamma) {
        super(imperfectCommunicationScenario, isTimeStamp, gamma);
    }


    @Override
    protected Double createDelay(Random r) {
        throw new RuntimeException("should use createDelay with "
                + "matrix which means that in the input should reiceve indexes for 2d matrix");
    }


    public Double createDelay(boolean isAlgorithmicMsg, int amountOfMsgs,boolean isLoss) {
        Random whichRandom;
        if (isAlgorithmicMsg) {
            whichRandom = rndGammaAlgorthmic;
        } else {
            whichRandom = rndGammaAnytime;
        }
        double rnd = whichRandom.nextDouble();
        if (rnd < gamma && isLoss) {
            return null;
        } else {

            Random whichRandomDelay;
            if (isAlgorithmicMsg) {
                whichRandomDelay = rndDelayAlgorthmic;
            } else {
                whichRandomDelay = rndDelayAnytime;
            }

            return createDelay(whichRandomDelay, amountOfMsgs);
        }
    }
    abstract protected Double createDelay(Random r, int msgAmount);
}
