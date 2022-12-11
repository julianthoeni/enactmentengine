package at.enactmentengine.serverless.slo;

import java.util.*;
import java.util.stream.Collectors;

public class TimeSloBudget extends SLO<Double>{

    public TimeSloBudget(SloOperator operator, Double value) {
        super(operator, value, null);
    }

    public TimeSloBudget(SloOperator operator, Double value, String timeFrame){
        super (operator, value, timeFrame, null);
    }

    public TimeSloBudget(SloOperator operator, Double value, String timeFrame, Integer budget) {
        super (operator, value, timeFrame, budget);
    }

    private double getAverageRtt(long currentTime, long timeFrameInMs, List<String> resourceLinks){
        return this.getData().getList().stream().filter(c -> c.getTimestamp() > currentTime - timeFrameInMs).filter(c -> resourceLinks.contains(c.getResourceLink())).mapToDouble(SloData.DataEntry::getRtt).average().orElse(Double.NaN);
    }

    private List<SloData.DataEntry> getEntriesWithinTimeFrame(long currentTime, long timeFrameInMs, List<String> resourceLinks){
        return Collections.unmodifiableList(this.getData().getList().stream().filter(c -> c.getTimestamp() > currentTime - timeFrameInMs).filter(c -> resourceLinks.contains(c.getResourceLink())).collect(Collectors.toList()));
    }

    private int usedBudgetByTimeFrame(long currentTime, long timeFrameInMs, List<String> resourceLinks, SloOperator operator, Double value){
        int hits = 0;
        List<SloData.DataEntry> entries = this.getEntriesWithinTimeFrame(currentTime, timeFrameInMs, resourceLinks);

        switch(operator){
            case LESS_THAN: for(SloData.DataEntry entry : entries) {
                if(entry.getRtt() > value) hits++;
            } break;
            case GREATER_THAN: for(SloData.DataEntry entry : entries) {
                if(entry.getRtt() < value) hits++;
            } break;
            case LESS_EQUALS: for(SloData.DataEntry entry : entries) {
                if(entry.getRtt() >= value) hits++;
            } break;
            case GREATER_EQUALS: for(SloData.DataEntry entry : entries) {
                if(entry.getRtt() <= value) hits++;
            } break;
            case EQUALS: for(SloData.DataEntry entry : entries) {
                if(entry.getRtt() == value) hits++;
            } break;
            case RANGE: break; // TODO: implement range for TimeSLOBudget
        }

        return hits;
    }

    @Override
    public boolean isInAgreement(String resourceLink) {
        // create timestamp:
        long timestamp = System.currentTimeMillis();
        for (SloEntry s : this.getEntries()) {
            if (s.getBudget() == null) return false; // no budget defined "throw error"
            if(usedBudgetByTimeFrame(timestamp, s.getTimeFrameInMs(), Arrays.asList(resourceLink), s.getOperator(), (Double) s.getValue()) > s.getBudget()) return false;
        }
        return true;
    }

    @Override
    protected Map<String, Double> getPoints() {
        Map<String, Double> res = new HashMap<>();
        Map<String, List<SloData.DataEntry>> dataByResourceLink = this.getDataByResourceLink();
        List<String> allResourceLinks = new LinkedList<>(this.getData().getResourceLinks());

        long timestamp = System.currentTimeMillis();

        for(String resourceLink : dataByResourceLink.keySet()){
            if (!allResourceLinks.contains(resourceLink)) {
                // throw exception here?
            }
            allResourceLinks.remove(resourceLink); // remove from list -> list should only contain non executed resources at end

            double val = 0d;

            for (SloEntry slo : this.getEntries()){
                double average = getAverageRtt(timestamp, slo.getTimeFrameInMs(), new ArrayList<>(Arrays.asList(resourceLink)));

                switch(slo.getOperator()){
                    case LESS_THAN:
                    case LESS_EQUALS: if (average / (Double) slo.getValue() >= 1){
                        val += 1;
                    } else {
                        val += average / (Double) slo.getValue();
                    } break;
                    case GREATER_THAN:
                    case GREATER_EQUALS:  if ((Double) slo.getValue() / average >= 1){
                        val += 1;
                    } else {
                        val += (Double) slo.getValue() / average;
                    } break;
                    case EQUALS:  break;
                    case RANGE: break; // TODO: implement range for TimeSLO
                }

            }

            res.put(resourceLink, val);
        }

        if(allResourceLinks.size() > 0){
            for(String resourceLink : allResourceLinks){
                res.put(resourceLink, 0d);
            }
        }

        return Collections.unmodifiableMap(res);
    }
}
