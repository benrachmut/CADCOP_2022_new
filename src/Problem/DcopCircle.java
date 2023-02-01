package Problem;

import AgentsAbstract.AgentVariable;
import Main.MainSimulatorIterations;

public class DcopCircle extends Dcop{


    private int constantColorCost;
    private int uniformCostLB;
    private int uniformCostUB;
    private int numberOfCircles;

    public DcopCircle(int dcopId, int A, int D,int numberOfCircles, int uniformCostLB,int uniformCostUB) {
        super(dcopId, A, D);
        this.uniformCostLB = uniformCostLB;
        this.uniformCostUB =uniformCostUB;
        this.numberOfCircles = numberOfCircles;
    }


    public DcopCircle(int dcopId, int A, int D,int numberOfCircles, int constantColorCost) {
        super(dcopId, A, D);
        this.numberOfCircles = numberOfCircles;
        this.constantColorCost = constantColorCost;
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







        for (int i = 0; i < agentsVariables.length; i++) {
            AgentVariable a1 = agentsVariables[i];
            AgentVariable a2 = null;
            if (i == agentsVariables.length-1) {
                 a2 = agentsVariables[0];
            }else {
                 a2 = agentsVariables[i + 1];
            }
            this.neighbors.add(new Neighbor(a1, a2, D, costLb,costUb, dcopId, 1));
        }
    }
}


