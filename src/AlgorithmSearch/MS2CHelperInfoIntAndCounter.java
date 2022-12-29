package AlgorithmSearch;

public class MS2CHelperInfoIntAndCounter {
    private Integer neighborCounter;
    private Double info;

    public MS2CHelperInfoIntAndCounter(int neighborCounter, double info){
        this.neighborCounter = neighborCounter;
        this.info = info;
    }

    public String toString(){
        return neighborCounter.toString()+":"+info.toString();
    }
    public Double getInfo(){
        return this.info;
    }

    public int getInfoInt(){
        return info.intValue();
    }


    public int getCounter() {
        return this.neighborCounter;
    }
}
