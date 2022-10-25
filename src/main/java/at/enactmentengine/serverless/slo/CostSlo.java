package at.enactmentengine.serverless.slo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        return null;
    }

}
