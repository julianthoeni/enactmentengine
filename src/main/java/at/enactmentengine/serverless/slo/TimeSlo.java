package at.enactmentengine.serverless.slo;

import java.util.Arrays;
import java.util.List;

public class TimeSlo extends SLO<Double>{

    public TimeSlo(SloOperator operator, Double value) {
        super(operator, value);
    }

    public TimeSlo(SloOperator operator, Double value, String timeFrame){
        super (operator, value, timeFrame);
    }

    private double getAverageRtt(long currentTime, long timeFrameInMs, List<String> resourceLinks){
        return this.getData().getList().stream().filter(c -> c.getTimestamp() > currentTime - timeFrameInMs).filter(c -> resourceLinks.contains(c.getResourceLink())).mapToDouble(SloData.DataEntry::getRtt).average().orElse(Double.NaN);
    }

    @Override
    public boolean isInAgreement() {
        // create timestamp:
        long timestamp = System.currentTimeMillis();
        for (SloEntry s : this.getEntries()){
            double averageRtt = getAverageRtt(timestamp, s.getTimeFrameInMs(), Arrays.asList("start"));
            System.out.println(averageRtt);
            switch(s.getOperator()){
                case LESS_THAN: if (!(averageRtt < (Double) s.getValue())){
                    return false;
                } break;
                case GREATER_THAN: if (!(averageRtt > (Double) s.getValue())){
                    return false;
                } break;
                case LESS_EQUALS: if (!(averageRtt <= (Double) s.getValue())){
                    return false;
                } break;
                case GREATER_EQUALS: if (!(averageRtt >= (Double) s.getValue())){
                    return false;
                } break;
                case EQUALS: if (!(averageRtt == (Double) s.getValue())){
                    return false;
                } break;
                case RANGE: return false;
            }
        }


        return true;
    }
}
