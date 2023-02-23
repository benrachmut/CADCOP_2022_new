package Problem;

import AgentsAbstract.AgentVariable;
import Main.MainSimulatorIterations;

import java.util.*;

public class DcopCircle extends Dcop{


    private int lambda;
    private int uniformCostLB;
    private int uniformCostUB;
    private int numberOfCircles;
    private MainSimulatorIterations.CostType costType;

    public DcopCircle(int dcopId, int A, int D,int numberOfCircles, int uniformCostLB,int uniformCostUB,MainSimulatorIterations.CostType costType) {
        super(dcopId, A, D);
        this.uniformCostLB = uniformCostLB;
        this.uniformCostUB =uniformCostUB;
        this.numberOfCircles = numberOfCircles;
        this.costType = costType;
    }

    public DcopCircle(int dcopId, int A, int D,int numberOfCircles, int lambda,MainSimulatorIterations.CostType costType) {
        super(dcopId, A, D);
        this.numberOfCircles = numberOfCircles;
        this.lambda = lambda;
        this.costType = costType;
    }

    public DcopCircle(int dcopId, int A, int D,int numberOfCircles, MainSimulatorIterations.CostType costType) {
        super(dcopId, A, D);
        this.numberOfCircles = numberOfCircles;
        this.costType = costType;
    }



    @Override
    protected void setDcopName() {
        Dcop.dcopName = "Circle_"+String.valueOf(numberOfCircles)+"_" +MainSimulatorIterations.costType;

    }

    @Override
    protected void setDcopHeader() {
        if (MainSimulatorIterations.CostType.uniform == MainSimulatorIterations.costType)
        Dcop.dcopHeader = "D"+","+"circles";

    }

    @Override
    protected void setDcopParameters() {
        Dcop.dcopParameters = this.D+","+String.valueOf(this.numberOfCircles);
    }



    @Override
    public void createNeighbors() {
        //Set<AgentVariable> chosenForCircle = getChosenForCircle();
        //Set<AgentVariable> notChosenForCircle = getNotChosenForCircle(chosenForCircle);

        for (int i = 0; i < agentsVariables.length; i++) {
            AgentVariable a1 = agentsVariables[i];
            AgentVariable a2 = null;
            if (i == agentsVariables.length-1) {
                 a2 = agentsVariables[0];
            }else {
                 a2 = agentsVariables[i + 1];
            }
            boolean flag = false;
            if (this.costType == MainSimulatorIterations.CostType.uniform) {
                this.neighbors.add(new Neighbor(a1, a2, D, uniformCostLB, uniformCostUB, dcopId, 1));
                flag = true;
            }

            if (this.costType == MainSimulatorIterations.CostType.poisson) {
                Neighbor n = new Neighbor(a1, a2, D, lambda, dcopId, 1.0);
                this.neighbors.add(n);
                flag = true;
            }

            if (this.costType == MainSimulatorIterations.CostType.poissonIndexBase) {
                Neighbor n = new Neighbor(a1, a2, D, dcopId, 1.0);
                this.neighbors.add(n);
                flag = true;
            }




            if (!flag){
                throw new RuntimeException("must enter ifs");
            }
        }

    }

}


