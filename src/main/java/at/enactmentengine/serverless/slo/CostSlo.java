package at.enactmentengine.serverless.slo;

import java.util.*;

public class CostSlo extends SLO<Double>{

    public CostSlo(SloOperator operator, Double value) {
        super(operator, value);
    }

    public CostSlo(SloOperator operator, Double value, String timeFrame){
        super (operator, value, timeFrame);
    }

    private double getTotalCost(long currentTime, long timeFrameInMs, List<String> resourceLinks){
        return this.getData().getList().stream().filter(c -> c.getTimestamp() > currentTime - timeFrameInMs).filter(c -> resourceLinks.contains(c.getResourceLink())).mapToDouble(SloData.DataEntry::getCost).sum();
    }

    @Override
    public boolean isInAgreement() {
        // create timestamp:
        long timestamp = System.currentTimeMillis();
        for (SloEntry s : this.getEntries()){
            double totalCost = getTotalCost(timestamp, s.getTimeFrameInMs(), Arrays.asList("start"));
            System.out.println(totalCost);
            switch(s.getOperator()){
                case LESS_THAN: if (!(totalCost < (Double) s.getValue())){
                    return false;
                } break;
                case GREATER_THAN: if (!(totalCost > (Double) s.getValue())){
                    return false;
                } break;
                case LESS_EQUALS: if (!(totalCost <= (Double) s.getValue())){
                    return false;
                } break;
                case GREATER_EQUALS: if (!(totalCost >= (Double) s.getValue())){
                    return false;
                } break;
                case EQUALS: if (!(totalCost == (Double) s.getValue())){
                    return false;
                } break;
                case RANGE: return false; // TODO: implement range for CostSlo
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

        for(String resourceLink : dataByResourceLink.keySet()){
            if (!allResourceLinks.contains(resourceLink)) {
                // throw exception here?
            }
            allResourceLinks.remove(resourceLink); // remove from list -> list should only contain non executed resources at end

            double val = 0d;

            for (SloEntry slo : this.getEntries()){
                double average = getTotalCost(timestamp, slo.getTimeFrameInMs(), new ArrayList<>(Arrays.asList(resourceLink)));

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
