package AlgorithmSearch;

public class MS2CHelperInfoIntAndCounter {
    private Integer neighborCounter;
    private Double infoInt;

    public MS2CHelperInfoIntAndCounter(int neighborCounter, double infoInt){
        this.neighborCounter = neighborCounter;
        this.infoInt = infoInt;
    }

    public String toString(){
        return neighborCounter.toString()+":"+infoInt.toString();
    }


}
