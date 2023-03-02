package Delays;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ProtocolDelayOneInK extends ProtocolDelayMatrix {

    private int k;
    private Map<Integer, int[]> counterMap;

    public ProtocolDelayOneInK(boolean imperfectCommunicationScenario, boolean isTimeStamp, double gamma, int k) {
        super(imperfectCommunicationScenario, isTimeStamp, gamma);
        this.k = k;
        counterMap = new HashMap<Integer, int[]>();
    }


    public Double createDelay(boolean isAlgorithmicMsg, int id1, int id2) {
        Random whichRandom = getWhichRnd(isAlgorithmicMsg);
        if (!this.counterMap.containsKey(id1)) {
            resetSenderInCounterMap(id1, whichRandom);
        }

        boolean isSentForSure = isSentForSure(id1);
        decreaseKByOne(id1, whichRandom);
        if (isSentForSure) {
            return 0.0;
        } else {
            return  gammaDependent(whichRandom);

        }
    }

    private Double gammaDependent(Random whichRandom) {
        double rnd = whichRandom.nextDouble();
        if (rnd < gamma) {
            return null;
        } else {
            return 0.0;
        }
    }

    private boolean isSentForSure(int id1) {
        int[] arr = this.counterMap.get(id1);
        if (arr[0] == arr[1]) {
            return true;
        }
        return false;
    }

    private void decreaseKByOne(int id1, Random whichRandom) {
        int[] arr = this.counterMap.get(id1);
        int updatedCounter = arr[0] - 1;
        if (updatedCounter == 0) {
            updatedCounter = k;
        }
        int[] updatedArr = new int[2];


        updatedArr[0] = updatedCounter;
        if (updatedCounter == k) {
            updatedArr[1] = getRandomBetweenOneAndK(whichRandom);
        } else {
            updatedArr[1] = arr[1];
        }
        this.counterMap.put(id1, updatedArr);
    }

    private void resetSenderInCounterMap(int id1, Random whichRandom) {

        int[] arr = new int[2];
        arr[0] = k;
        arr[1] = getRandomBetweenOneAndK(whichRandom);
        this.counterMap.put(id1, arr);
    }

    private int getRandomBetweenOneAndK(Random whichRandom) {
        return whichRandom.nextInt(k) + 1;
    }


    private Random getWhichRnd(boolean isAlgorithmicMsg) {
        Random whichRandom;
        if (isAlgorithmicMsg) {
            whichRandom = rndGammaAlgorthmic;
        } else {
            whichRandom = rndGammaAnytime;
        }
        return whichRandom;
    }


    @Override
    protected String getStringParameters() {
        return "k";
    }

    @Override
    protected boolean checkSpecificEquals(ProtocolDelay other) {
        return false;
    }

    @Override
    protected Double createDelay(Random r, int i, int j) {
        return null;
    }
}
