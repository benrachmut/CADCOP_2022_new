package Delays;

import java.util.Random;

public class ProtocolDelayWithK extends ProtocolDelay{

    private int k;
    //private Map<Integer, int[]> counterMap;
    public static int k_public;

    public ProtocolDelayWithK(boolean imperfectCommunicationScenario, boolean isTimeStamp, double gamma, int k) {
        super(imperfectCommunicationScenario, isTimeStamp, gamma);
        this.k = k;

        //counterMap = new HashMap<Integer, int[]>();
    }
    public  void  updatePublicK(){
        k_public = this.k;
    }

    public ProtocolDelayWithK(boolean isTimeStamp, double gamma, int k) {
        this(true,isTimeStamp, gamma, k);

    }



    @Override
    protected Double createDelay(Random r) {
        return 0.0;
    }

    @Override
    protected String getStringParamets() {
        return Integer.toString(this.k);
    }

    @Override
    protected boolean checkSpecificEquals(ProtocolDelay other) {
        if (other instanceof ProtocolDelayWithK){
            return ((ProtocolDelayWithK)other).k == this.k;
        }
        return false;
    }

}
