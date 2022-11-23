package at.enactmentengine.serverless.slo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Rule {
    private SLO mainSlo;
    private List<SLO> additionalSlos;
    private SloData data;
    private String currentExecution;
    private final long timeBetweenResolves = 500L; // in milliseconds
    private long lastExecution = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(Rule.class);
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
            LOGGER.info("Function " + this.currentExecution + " do not meet the main-slo");
            return false;
        }
        if (this.additionalSlos.size() > 0)
            for (SLO additionalSlo : additionalSlos) {
                if(!additionalSlo.isInAgreement(currentExecution)){
                    LOGGER.info("Function " + this.currentExecution + " do not meet the additional-slo");
                    return false;
                }
            }
        LOGGER.info("Function " + this.currentExecution + " meets all slos");
        return true;
    }

    public String resolve(){
        if(this.currentExecution == null){
            //TODO: Call scheduler
            System.out.println("Call scheduler plz"); //This will throw an error for now
        }

        if(System.currentTimeMillis() - timeBetweenResolves < lastExecution){
            return currentExecution;
        }

        if(check()){
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
        System.out.println("-------------------------------------");
        for (Map.Entry<String, Double> entry : points.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        System.out.println("------------------------------------_");
        double minVal = 1000d;
        Scheduler.run(points);
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
        this.lastExecution = System.currentTimeMillis();
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