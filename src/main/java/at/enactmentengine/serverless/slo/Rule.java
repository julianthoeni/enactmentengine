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
    private static final Logger logger = LoggerFactory.getLogger(Rule.class);
    SLO_LOGGER slologger = SLO_LOGGER.getINSTANCE();
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
            logger.info("SLO: Function " + this.currentExecution + " does not meet the main-slo");
            slologger.writeToLog("SLO: Function " + this.currentExecution + " does not meet the main-slo");
            return false;
        }
        if (this.additionalSlos.size() > 0)
            for (SLO additionalSlo : additionalSlos) {
                if(!additionalSlo.isInAgreement(currentExecution)){
                    logger.info("SLO: Function " + this.currentExecution + " does not meet the additional-slo");
                    slologger.writeToLog("SLO: Function " + this.currentExecution + " does not meet the additional-slo");
                    return false;
                }
            }
        logger.info("SLO: Function " + this.currentExecution + " meets all slos");
        slologger.writeToLog("SLO: Function " + this.currentExecution + " meets all slos");
        return true;
    }

    public String resolve(){
        if(this.currentExecution == null){
            this.currentExecution = scheduler();
            return this.currentExecution;
        }

        if(System.currentTimeMillis() - timeBetweenResolves < lastExecution){
            return currentExecution;
        }

        if(check()){
            return currentExecution;
        }

        this.currentExecution = scheduler();

        this.lastExecution = System.currentTimeMillis();
        return this.currentExecution;
    }

    public String scheduler(){
        String nextResourceLink = null;
        Map<String, Double> points = new HashMap<>();

        // give all resourceLinks zero points to start with:
        for(String resourceLink : data.getResourceLinks()){
            points.put(resourceLink, 0d);
        }

        // get remaining budget (if applied)
        //Map<String, Integer> budget = new HashMap<>();
        Map<String, Integer> mainSloBudget;

        if(this.mainSlo.isBudgetType()){
            mainSloBudget = ((BudgetSlo) mainSlo).getTotalBudgetLeft();
        }

        // lowest points is best:
        // merge maps together and save to points map
        mainSlo.getPoints().forEach((key, value) -> points.merge((String) key, (Double) value, (v1, v2) -> 2 * (Double)(v1 + v2)));

        for(SLO slo : additionalSlos){
            slo.getPoints().forEach((key, value) -> points.merge((String) key, (Double) value, (v1, v2) -> (Double)(v1 + v2)));
        }
        if(logger.isDebugEnabled()||true){
            System.out.println("----------- SLO: Points -------------");
            for (Map.Entry<String, Double> entry : points.entrySet()) {
                System.out.println(entry.getKey());
                System.out.println(entry.getValue());
            }
            System.out.println("-------------------------------------");
        }

        double minVal = 1000d;

        //Remove missing resourceLinks from points-map
        Map<String, Double> cleaned_points = new HashMap<>();
        for(Map.Entry<String, Double> entry : points.entrySet()){
            if (data.getResourceLinks().contains(entry.getKey())){
                cleaned_points.put(entry.getKey(),entry.getValue());
            }
        }

        for(Map.Entry<String, Double> entry : cleaned_points.entrySet()){
            if (entry.getValue() < minVal){
                minVal = entry.getValue();
                nextResourceLink = entry.getKey();
            }
        }

        if(nextResourceLink == null || !data.getResourceLinks().contains(nextResourceLink)){ // safe-fail if no solution found:
            nextResourceLink = currentExecution;
        }
        slologger.writeToLog("New resource_link: " + nextResourceLink);
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