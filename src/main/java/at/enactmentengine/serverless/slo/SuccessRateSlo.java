package at.enactmentengine.serverless.slo;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SuccessRateSlo extends SLO<Double>{

    public SuccessRateSlo(SloOperator operator, Double value) {
        super(operator, value);
    }

    public SuccessRateSlo(SloOperator operator, Double value, String timeFrame){
        super (operator, value, timeFrame);
    }

    private double getSuccessRate(long currentTime, long timeFrameInMs, List<String> resourceLinks){

        return this.getData().getList().stream().filter(c -> c.getTimestamp() > currentTime - timeFrameInMs).filter(c -> resourceLinks.contains(c.getResourceLink())).mapToDouble(entry -> entry.isSuccess() ? 1.0d : 0.0d).summaryStatistics().getAverage();
    }

    @Override
    public boolean isInAgreement() {
        // create timestamp:
        long timestamp = System.currentTimeMillis();
        for (SloEntry s : this.getEntries()){
            double successRate = getSuccessRate(timestamp, s.getTimeFrameInMs(), Arrays.asList("start"));
            System.out.println(successRate);
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
        return null;
    }

}
