package at.enactmentengine.serverless.slo;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;

import java.util.*;
import java.util.stream.Collectors;

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
            double averageRtt = getAverageRtt(timestamp, s.getTimeFrameInMs(), Arrays.asList("Test"));
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
                case RANGE: return false; // TODO: implement range for TimeSLO
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
