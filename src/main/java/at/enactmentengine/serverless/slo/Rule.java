package at.enactmentengine.serverless.slo;

import java.util.*;

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

        //this.currentExecution = FunctionScheduler.runSchedulerInit(functionName);

        this.mainSlo = mainSlo;
        mainSlo.setData(this.data);

        additionalSlos.forEach(s -> s.setData(this.data));

    }

    public boolean check(){
        if (!this.mainSlo.isInAgreement(currentExecution)){
            return false;
        }
        if (this.additionalSlos.size() > 0)
            for (SLO additionalSlo : additionalSlos) {
                if(!additionalSlo.isInAgreement(currentExecution)) return false;
            }
        return true;
    }

    public String resolve(boolean doCheck){
        if(doCheck && check()){
            return currentExecution;
        }
        String nextResourceLink = null;
        Map<String, Double> points = new HashMap<>();

        // give all resourceLinks zero points to start with:
        for(String resourceLink : data.getResourceLinks()){
            points.put(resourceLink, 0d);
        }

        // lowest points is best:
        // merge maps together and save to points map
        mainSlo.getPoints().forEach((key, value) -> points.merge((String) key, (Double) value, (v1, v2) -> 2 * (Double)(v1 + v2)));

        for(SLO slo : additionalSlos){
            slo.getPoints().forEach((key, value) -> points.merge((String) key, (Double) value, (v1, v2) -> (Double)(v1 + v2)));
        }

        double minVal = 1000d;

        for(Map.Entry<String, Double> entry : points.entrySet()){
            if (entry.getValue() < minVal){
                minVal = entry.getValue();
                nextResourceLink = entry.getKey();
            }
        }

        if(nextResourceLink == null || !data.getResourceLinks().contains(nextResourceLink)){ // safe-fail if no solution found:
            nextResourceLink = currentExecution;
        }

        setCurrentExecution(nextResourceLink);
        return nextResourceLink;
    }


    public void addDataEntry(long rtt, long timeStamp, double cost, boolean success, String resourceLink){
        this.data.addEntry(this.data.getList().size(), rtt, timeStamp, cost, success, resourceLink);
    }

    public void addResourceEntry(String resource){
        this.data.addResourceLink(resource);
    }

    public void setCurrentExecution(String currentExecution) {
        this.currentExecution = currentExecution;
    }

}