package at.enactmentengine.serverless.slo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimeSlo extends SLO<Double>{

    public TimeSlo(SloOperator operator, Double value) {
        super(operator, value);
    }

    private double getAverageRtt(long currentTime, long timeFrameInMs, List<String> resourceLinks){
        return this.getData().getList().stream().filter(c -> c.getTimestamp() > currentTime - timeFrameInMs).filter(c -> resourceLinks.contains(c.getResourceLink())).mapToDouble(SloData.DataEntry::getRtt).average().orElse(Double.NaN);
    }

    @Override
    public boolean isInAgreement() {
        // not tested in any way:
        double averageRtt = getAverageRtt(System.currentTimeMillis(), 86400000L, Arrays.asList("A", "B"));
        return false;
    }
}
