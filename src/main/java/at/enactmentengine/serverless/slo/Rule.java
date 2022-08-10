package at.enactmentengine.serverless.slo;

import java.util.ArrayList;
import java.util.List;

public class Rule {
    private SLO mainSlo;
    private List<SLO> additionalSlos;
    private SloData data;

    private String currentExecution;

    public Rule(SLO mainSlo, List<SLO> slos, String functionName, String startExecution){
        if(slos != null)
            this.additionalSlos = new ArrayList<>(slos);
        else
            this.additionalSlos = null;

        this.data = new SloData(functionName);

        if(startExecution == null)
            this.currentExecution = resolve();
        else
            this.currentExecution = startExecution;

        this.mainSlo = mainSlo;
        mainSlo.setData(this.data);

    }

    public boolean check(){
        if (!this.mainSlo.isInAgreement()){
            return false;
        }
        if (this.additionalSlos != null)
            for (SLO additionalSlo : additionalSlos) {
                if(!additionalSlo.isInAgreement()) return false;
            }
        return true;
    }

    public void addDataEntry(int id, long rtt, long timeStamp, boolean success, String resourceLink){
        this.data.addEntry(id, rtt, timeStamp, success, resourceLink);
    }

    public void addResourceEntry(String resource){
        this.data.addResourceLink(resource);
    }


    public String resolve() {
        if(check()) return currentExecution;
        // returns "new" service to execute function on
        // call scheduler etc.
        return null;
    }

}
