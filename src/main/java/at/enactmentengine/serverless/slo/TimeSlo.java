package at.enactmentengine.serverless.slo;

public class TimeSlo extends SLO<Double>{

    public TimeSlo(SloOperator operator, Double value) {
        super(operator, value);
    }

    @Override
    public boolean isInAgreement() {
        // do some magic here
        return false;
    }
}
