package at.enactmentengine.serverless.slo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class CostSloBudget extends BudgetSlo<Double>{
    private static final Logger LOGGER = LoggerFactory.getLogger(Rule.class);
    public CostSloBudget(SloOperator operator, Double value) {
        super(operator, value, null);
    }

    public CostSloBudget(SloOperator operator, Double value, String timeFrame){
        super (operator, value, timeFrame, null);
    }

    public CostSloBudget(SloOperator operator, Double value, String timeFrame, Integer budget) {
        super (operator, value, timeFrame, budget);
    }

    private double getTotalCost(long currentTime, long timeFrameInMs, List<String> resourceLinks){
        return this.getData().getList().stream().filter(c -> c.getTimestamp() > currentTime - timeFrameInMs).filter(c -> resourceLinks.contains(c.getResourceLink())).mapToDouble(SloData.DataEntry::getCost).sum();
    }

    private List<SloData.DataEntry> getEntriesWithinTimeFrame(long currentTime, long timeFrameInMs, List<String> resourceLinks){
        return Collections.unmodifiableList(this.getData().getList().stream().filter(c -> c.getTimestamp() > currentTime - timeFrameInMs).filter(c -> resourceLinks.contains(c.getResourceLink())).collect(Collectors.toList()));
    }

    protected int usedBudgetByTimeFrame(long currentTime, long timeFrameInMs, List<String> resourceLinks, SloOperator operator, Double value){
        int hits = 0;
        List<SloData.DataEntry> entries = this.getEntriesWithinTimeFrame(currentTime, timeFrameInMs, resourceLinks);

        switch(operator){
            case LESS_THAN: for(SloData.DataEntry entry : entries) {
                if(entry.getCost() > value) hits++;
            } break;
            case GREATER_THAN: for(SloData.DataEntry entry : entries) {
                if(entry.getCost() < value) hits++;
            } break;
            case LESS_EQUALS: for(SloData.DataEntry entry : entries) {
                if(entry.getCost() >= value) hits++;
            } break;
            case GREATER_EQUALS: for(SloData.DataEntry entry : entries) {
                if(entry.getCost() <= value) hits++;
            } break;
            case EQUALS: for(SloData.DataEntry entry : entries) {
                if(entry.getCost() == value) hits++;
            } break;
            case RANGE: break; // TODO: implement range for TimeSLOBudget
        }

        return hits;
    }

    @Override
    protected Map<String, Integer> getTotalBudgetLeft() {
        Map<String, Integer> totalBudget = new HashMap<>();
        List<String> allResourceLinks = new LinkedList<>(this.getData().getResourceLinks());


        long timestamp = System.currentTimeMillis();
        for(String resource : this.getDataByResourceLink().keySet()) {
            allResourceLinks.remove(resource);
            for (SloEntry s : this.getEntries()) {
                if (s.getBudget() == null) return null;
                totalBudget.put(resource, totalBudget.getOrDefault(resource, 0) + s.getBudget() - usedBudgetByTimeFrame(timestamp, s.getTimeFrameInMs(), Arrays.asList(resource), s.getOperator(), (Double) s.getValue()));
            }
        }

        if(allResourceLinks.size() > 0) {
            int defaultBudget = 0;
            for (SloEntry s : this.getEntries()) {
                defaultBudget += s.getBudget();
            }

            for (String resource : allResourceLinks) {
                totalBudget.put(resource, defaultBudget);
            }
        }

        return totalBudget;
    }


    @Override
    public boolean isInAgreement(String resourceLink) {
        // create timestamp:
        long timestamp = System.currentTimeMillis();
        for (SloEntry s : this.getEntries()) {
            if (s.getBudget() == null) return false; // no budget defined "throw error"
            System.out.println("Budget: " + usedBudgetByTimeFrame(timestamp, s.getTimeFrameInMs(), Arrays.asList(resourceLink), s.getOperator(), (Double) s.getValue()));
            if(usedBudgetByTimeFrame(timestamp, s.getTimeFrameInMs(), Arrays.asList(resourceLink), s.getOperator(), (Double) s.getValue()) >= s.getBudget()){
                LOGGER.info("SLO: Budget (" + s.getBudget() + ") used up for " + resourceLink );
                return false;
            }
        }
        LOGGER.info("SLO: " + resourceLink + " is ok");
        return true;
    }

    @Override
    protected Map<String, Double> getPoints() {
        Map<String, Double> res = new HashMap<>();
        Map<String, Double> fullResults = new HashMap<>();
        Map<String, List<SloData.DataEntry>> dataByResourceLink = this.getDataByResourceLink();
        List<String> allResourceLinks = new LinkedList<>(this.getData().getResourceLinks());

        long timestamp = System.currentTimeMillis();
        String bestExecution = "";
        double bestValue = Double.MAX_VALUE;
        int maxValue = 0;

        for(String resourceLink : dataByResourceLink.keySet()){
            if (!allResourceLinks.contains(resourceLink)) {
                // throw exception here?
            }
            allResourceLinks.remove(resourceLink); // remove from list -> list should only contain non executed resources at end

            double val = 0d;
            double fullValue = 0d;

            for (SloEntry slo : this.getEntries()){

                double average = getTotalCost(timestamp, slo.getTimeFrameInMs(), new ArrayList<>(Arrays.asList(resourceLink)));

                if(!Double.isNaN(average)) {
                    switch (slo.getOperator()) {
                        case LESS_THAN:
                        case LESS_EQUALS:
                            if (average / (Double) slo.getValue() >= 1) {
                                val += 1;
                                fullValue += average / (Double) slo.getValue();
                            } else {
                                val += average / (Double) slo.getValue();
                                fullValue += average / (Double) slo.getValue();
                            }
                            break;
                        case GREATER_THAN:
                        case GREATER_EQUALS:
                            if ((Double) slo.getValue() / average >= 1) {
                                val += 1;
                                fullValue +=(Double) slo.getValue() / average;
                            } else {
                                val += (Double) slo.getValue() / average;
                                fullValue += (Double) slo.getValue() / average;
                            }
                            break;
                        case EQUALS:
                            break;
                        case RANGE:
                            break; // TODO: implement range for TimeSLO
                    }
                }else{
                    val = 0.0d;
                }
            }

            if(fullValue < bestValue) {
                bestExecution = resourceLink;
                bestValue = fullValue;
            }

            res.put(resourceLink, val);
            fullResults.put(resourceLink, fullValue);
        }

        if(allResourceLinks.size() > 0){
            for(String resourceLink : allResourceLinks){
                res.put(resourceLink, 0d);
                fullResults.put(resourceLink, 0d);
            }
        }

        // normalize all values in res map:
        double worstExecution = 0.1d; // no divide / 0
        for (String resourceLink : fullResults.keySet()){
            if (fullResults.get(resourceLink) > worstExecution) {
                worstExecution = fullResults.get(resourceLink);
            }
        }

        for(String resourceLink : res.keySet()){
            double val = fullResults.get(resourceLink);
            fullResults.put(resourceLink, val / worstExecution);
        }

        // checking if any resources are maxed out
        int maxedOut = 0;
        for(String resourceLink : res.keySet()){
            if(res.get(resourceLink) >= ((double) this.getEntries().size() - 0.1d)){
                maxedOut++;
            }
        }

        if(maxedOut >= res.keySet().size()){

            // first check if any budget is left somewhere:
            Map<String, Integer> budget = this.getTotalBudgetLeft();
            int bestBudget = 0;
            String bestBudgetResource = null;

            for(String resource : budget.keySet()){
                if(budget.get(resource) > bestBudget){
                    bestBudget = budget.get(resource);
                    bestBudgetResource = resource;
                }
            }

            if(bestBudgetResource != null){
                if (bestBudgetResource.equals(bestExecution)){
                    //fullResults.put(bestExecution, 0d);
                } else {
                    fullResults.put(bestBudgetResource, 0d);
                }
            } else {
                //res.put(bestExecution, 0d);
            }

        }


        return Collections.unmodifiableMap(fullResults);
    }

}
