package Problem;

import AgentsAbstract.AgentVariable;
import Main.MainSimulatorIterations;

import java.util.*;

public class DcopCircle extends Dcop{


    public enum CostType{uniform,normal,poisson}
    private int lambda;
    private int uniformCostLB;
    private int uniformCostUB;
    private int numberOfCircles;
    private CostType myCostType;

    public DcopCircle(int dcopId, int A, int D,int numberOfCircles, int uniformCostLB,int uniformCostUB) {
        super(dcopId, A, D);
        this.uniformCostLB = uniformCostLB;
        this.uniformCostUB =uniformCostUB;
        this.numberOfCircles = numberOfCircles;
        myCostType = CostType.uniform;

    }


    public DcopCircle(int dcopId, int A, int D,int numberOfCircles, int lambda) {
        super(dcopId, A, D);
        this.numberOfCircles = numberOfCircles;
        this.lambda = lambda;
        myCostType = CostType.poisson;

    }

    public DcopCircle(int dcopId, int A, int D,int numberOfCircles) {
        super(dcopId, A, D);
        this.numberOfCircles = numberOfCircles;
        myCostType = CostType.normal;

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
            if (this.myCostType == CostType.uniform) {
                this.neighbors.add(new Neighbor(a1, a2, D, uniformCostLB, uniformCostUB, dcopId, 1));
                flag = true;
            }

            if (this.myCostType == CostType.poisson) {

                Neighbor n = new Neighbor(a1, a2, D, lambda, dcopId, 1.0);
                this.neighbors.add(n);
                flag = true;
            }
            if (!flag){
                throw new RuntimeException("must enter ifs");
            }
        }


    }
/*
    private Set<AgentVariable> getNotChosenForCircle(Set<AgentVariable> chosenForCircle) {
        Set<AgentVariable> notChosenForCircle = new HashSet<>();
        for (AgentVariable av:this.agentsVariables) {
            if (!chosenForCircle.contains(av)){
                notChosenForCircle.add(av);
            }
        }
        return notChosenForCircle;

    }

    private Set<AgentVariable> getChosenForCircle() {

        Set<AgentVariable> chosenForCircle = new HashSet<>();

        Iterator<AgentVariable> it = List.of(this.agentsVariables).iterator();
        int tempNumOfCircle = this.numberOfCircles;
        while (it.hasNext()) {
            if (tempNumOfCircle > 0) {
                AgentVariable chosen = it.next();
                chosenForCircle.add(chosen);
                tempNumOfCircle--;
            }
        }
    }

 */
}


