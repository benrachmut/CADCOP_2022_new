package Delays;

import java.util.Random;

abstract public class ProtocolDelayMessageInSystemBased extends ProtocolDelay{
    public ProtocolDelayMessageInSystemBased(boolean imperfectCommunicationScenario, boolean isTimeStamp, double gamma) {
        super(imperfectCommunicationScenario, isTimeStamp, gamma);
    }


    @Override
    protected Double createDelay(Random r) {
        throw new RuntimeException("should use createDelay with "
                + "matrix which means that in the input should reiceve indexes for 2d matrix");
    }


    abstract protected Double createDelay(Random r, int msgAmount);
}
