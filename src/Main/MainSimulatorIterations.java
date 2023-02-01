package Main;

import Problem.*;

import java.util.ArrayList;
import java.util.List;



public class MainSimulatorIterations {


    public enum Algorithm {maxsum}
    public enum GraphType{circle,uniform}
    public enum CostType{color,uniform}

    public static Algorithm algorithm= Algorithm.maxsum;
    public static GraphType graphType= GraphType.circle;
    public static CostType costType = CostType.uniform;


    public static int numberOfCircles = 1;
    public static int constantColorCost = 100;
    public static int uniformCostLB = 0;
    public static int uniformCostUB = 100;

    public static int[] agentSizeList = {10,20,30};
    public static int[] domainsSizeList = {10,20,30};


    public static int  start= 0;
    public static int end = 10;






    public static <DCOP> void main(String[] args) {
        setAlgorithmVariable();

        Dcop[] dcops = generateDcops();


    }

    private static void setAlgorithmVariable() {
        if (algorithm == Algorithm.maxsum){
            MainSimulator.agentType = 11;
        }
    }


    // ------------ 1. DCOP CREATION------------
    private static Dcop[] generateDcops() {
        Dcop[] ans = new Dcop[end - start];
        for (int domainSize:domainsSizeList) {
            for(int agentSize: agentSizeList){
                for (int i = 0; i < end - start; i++) {
                    int dcopId = i + start;
                    ans[i] = createDcop(dcopId,agentSize,domainSize).initiate();
                }
            }

        }


        return ans;
    }

    private static Dcop createDcop(int dcopId, int agentSize, int domainSize) {

        Dcop ans = null;
        // use default Domain contractors
        if (graphType == GraphType.circle){
            if (costType == CostType.color) {
                ans = new DcopCircle(dcopId, agentSize, domainSize, numberOfCircles,constantColorCost);
            }
            if (costType == CostType.uniform) {
                ans = new DcopCircle(dcopId, agentSize, domainSize, numberOfCircles,uniformCostLB,uniformCostUB);
            }
        }

        return ans;
    }

}

