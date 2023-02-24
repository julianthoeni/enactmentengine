package at.enactmentengine.serverless.slo;


import java.util.*;
import java.util.stream.Collectors;

public class SuccessRateSlo extends SLO<Double>{

    public SuccessRateSlo(SloOperator operator, Double value) {
        super(operator, value, null);
    }

    public SuccessRateSlo(SloOperator operator, Double value, String timeFrame){
        super (operator, value, timeFrame, null);
    }

    public SuccessRateSlo(SloOperator operator, Double value, String timeFrame, Integer budget) {
        super (operator, value, timeFrame, budget);
    }

    private double getSuccessRate(long currentTime, long timeFrameInMs, List<String> resourceLinks){

        return this.getData().getList().stream().filter(c -> c.getTimestamp() > currentTime - timeFrameInMs).filter(c -> resourceLinks.contains(c.getResourceLink())).mapToDouble(entry -> entry.isSuccess() ? 1.0d : 0.0d).summaryStatistics().getAverage();
    }

    @Override
    public boolean isInAgreement(String resourceLink) {
        // create timestamp:
        long timestamp = System.currentTimeMillis();

        for (SloEntry s : this.getEntries()){
            double successRate = getSuccessRate(timestamp, s.getTimeFrameInMs(), Arrays.asList(resourceLink));
            //System.out.println("Average Success rate: " + successRate);
            if(this.getData().getList().stream().filter(c -> c.getTimestamp() > timestamp - s.getTimeFrameInMs()).filter(c -> Arrays.asList(resourceLink).contains(c.getResourceLink())).collect(Collectors.toList()).size() < 1){
                continue;
            }
            switch(s.getOperator()){
                case LESS_THAN: if (!(successRate < (Double) s.getValue())){
                    return false;
                } break;
                case GREATER_THAN: if (!(successRate > (Double) s.getValue())){
                    return false;
                } break;
                case LESS_EQUALS: if (!(successRate <= (Double) s.getValue())){
                    return false;
                } break;
                case GREATER_EQUALS: if (!(successRate >= (Double) s.getValue())){
                    return false;
                } break;
                case EQUALS: if (!(successRate == (Double) s.getValue())){
                    return false;
                } break;
                case RANGE: return false; // TODO: implement range for SuccessRateSlo
            }
        }


        return true;
    }

    @Override
    protected Map<String, Double> getPoints() {
        Map<String, Double> res = new HashMap<>();
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
                maxValue++;
                double average = getSuccessRate(timestamp, slo.getTimeFrameInMs(), new ArrayList<>(Arrays.asList(resourceLink)));

                switch(slo.getOperator()){
                    case LESS_THAN:
                    case LESS_EQUALS:
                        val += average / (Double) slo.getValue();
                        fullValue += average / (Double) slo.getValue();
                        break;
                    case GREATER_THAN:
                    case GREATER_EQUALS:
                        val += (Double) slo.getValue() / average;
                        fullValue += (Double) slo.getValue() / average;
                        break;
                    case EQUALS:  break;
                    case RANGE: break; // TODO: implement range for TimeSLO
                }

            }
            if(fullValue < bestValue) {
                bestExecution = resourceLink;
                bestValue = fullValue;
            }
            res.put(resourceLink, val);
        }

        if(allResourceLinks.size() > 0){
            for(String resourceLink : allResourceLinks){
                res.put(resourceLink, 0d);
            }
        }

        // normalize all values in res map:
        double worstExecution = 0.1d; // no divide / 0
        for (String resourceLink : res.keySet()){
            if (res.get(resourceLink) > worstExecution) {
                worstExecution = res.get(resourceLink);
            }
        }

        for(String resourceLink : res.keySet()){
            double val = res.get(resourceLink);
            res.put(resourceLink, this.getEntries().size() * (val / worstExecution));
        }

        // checking if any resources are maxed out
        int maxedOut = 0;
        for(String resourceLink : res.keySet()){
            if(res.get(resourceLink) >= (maxValue - 0.1)){
                maxedOut++;
            }
        }

        //if(maxedOut >= res.keySet().size()){
        //    res.put(bestExecution, 0d);
        //}

        return Collections.unmodifiableMap(res);
    }
}
