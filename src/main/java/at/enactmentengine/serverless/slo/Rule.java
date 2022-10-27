package at.enactmentengine.serverless.slo;

import java.util.ArrayList;
import java.util.List;

public class Rule {
    private SLO mainSlo;
    private List<SLO> additionalSlos;
    private SloData data;

    private String currentExecution;

    public Rule(SLO mainSlo, List<SLO> slos, String functionName){
        if(slos != null)
            this.additionalSlos = new ArrayList<>(slos);
        else
            this.additionalSlos = null;
        this.data = new SloData(functionName);

        this.currentExecution = FunctionScheduler.runSchedulerInit(functionName);

        this.mainSlo = mainSlo;
        mainSlo.setData(this.data);

    }

    public boolean check(){
        if (!this.mainSlo.isInAgreement()){
            return false;
        }
        if (this.additionalSlos.size() > 0)
        for (SLO additionalSlo : additionalSlos) {
                if(!additionalSlo.isInAgreement()) return false;
            }
        return true;
    }

    public void addDataEntry(long rtt, long timeStamp, double cost, boolean success, String resourceLink){
        this.data.addEntry(this.data.getList().size(), rtt, timeStamp, cost, success, resourceLink);
    }

    public void addResourceEntry(String resource){
        this.data.addResourceLink(resource);
    }

}
