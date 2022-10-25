package Problem;

import AgentsAbstract.AgentVariable;

public class DcopCircle extends Dcop{

    private int costLb;
    private int costUb;

    public DcopCircle(int dcopId, int A, int D,int costLb,int costUb) {
        super(dcopId, A, D);
        this.costLb = costLb;
        this.costUb =costUb  ;
    }

    @Override
    protected void setDcopName() {
        Dcop.dcopName = "Circle ";



    }

    @Override
    protected void setDcopHeader() {
        Dcop.dcopHeader = "Domain Size";

    }

    @Override
    protected void setDcopParameters() {
        Dcop.dcopParameters = this.D+"";
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

