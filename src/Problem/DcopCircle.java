package Problem;

import AgentsAbstract.AgentVariable;
import Main.MainSimulatorIterations.CostType ;

import java.util.*;

public class DcopCircle extends Dcop{


    //private  double initMean;
    //private  int moveMeanCounter;
    //private  double moveMeanDistance;
    //private  double sd;


    //private int lambda;
    //private int uniformCostLB;
    //private int uniformCostUB;
    private int numberOfCircles;
    private CostType costType;


    public DcopCircle(int dcopId, int A, int D,int numberOfCircles, CostType costType) {
        super(dcopId, A, D);
        this.numberOfCircles = numberOfCircles;
        this.costType = costType;
        addPreferencesToAgents();

    }

    public void addPreferencesToAgents(){
        int numberOfHills = getNumberOfHills();
        if (numberOfHills == 0){
            return;
        }

        for (AgentVariable a: this.agentsVariables) {
            List<Integer> selectedNumbers = getSelectedRandomNumbersFromDomain(a,numberOfHills);
            a.addPreferenceDomain(selectedNumbers);
        }
    }

    private int getNumberOfHills() {
    int ans = 0;

        if (this.costType == CostType.softScheduleSd10Hill1 || this.costType == CostType.hardScheduleSd10Hill1){
            ans = 1;
        }

        if (this.costType == CostType.softScheduleSd10Hill3 || this.costType == CostType.hardScheduleSd10Hill3){
            ans = 3;
        }
        return ans;
    }

    private List<Integer> getSelectedRandomNumbersFromDomain(AgentVariable a, int hillNumber  ) {
        Random rand = new Random((a.getId()+1)*17894);
        ArrayList<Integer>domainArrayList = getDomainArrayList(a);
        Collections.shuffle(domainArrayList,rand);
       return domainArrayList.subList(0, hillNumber);

    }

    private ArrayList<Integer> getDomainArrayList(AgentVariable a) {
        int [] domainArray = a.getDomainArray();
        ArrayList<Integer> ans = new ArrayList<Integer>();
        for (int num: domainArray) {
            ans.add(num);
        }
        return ans;
    }


    @Override
    protected void setDcopName() {
        Dcop.dcopName = "Circle_"+String.valueOf(numberOfCircles)+"_" +costType;

    }

    @Override
    protected void setDcopHeader() {
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
            this.neighbors.add(new Neighbor(a1,  a2,  D,  dcopId,  1,  costType));

        }

    }

}


