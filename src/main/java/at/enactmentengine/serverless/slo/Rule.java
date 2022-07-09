package at.enactmentengine.serverless.slo;

import java.util.ArrayList;
import java.util.List;

public class Rule {
    private SLO mainSlo;
    private List<SLO> additionalSlos;
    private SloData data;

    private String currentExecution;

    public Rule(SLO mainSlo, List<SLO> slos, String functionName, String startExecution){
        this.mainSlo = mainSlo;
        this.additionalSlos = new ArrayList<>(slos);
        this.data = new SloData(functionName);
        if(startExecution == null)
            this.currentExecution = resolve();
         else
             this.currentExecution = startExecution;
    }

    public boolean check(){
        if (!this.mainSlo.isInAgreement()){
            return false;
        }
        for (SLO additionalSlo : additionalSlos) {
            if(!additionalSlo.isInAgreement()) return false;
        }
        return true;
    }

    public void addDataEntry(int id, long rtt, long timeStamp, boolean success, String resourceLink){
        this.data.addEntry(id, rtt, timeStamp, success, resourceLink);
    }


    public String resolve() {
        if(check()) return currentExecution;
        // returns "new" service to execute function on
        // call scheduler etc.
        return null;
    }

}
