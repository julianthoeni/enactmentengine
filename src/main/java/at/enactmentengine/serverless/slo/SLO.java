package at.enactmentengine.serverless.slo;

import java.util.ArrayList;
import java.util.List;

public abstract class SLO<E> {
    private List<E> values = null;
    private List<SloOperator> operators = null;
    private List<String> timeFrames = null;

    public abstract boolean isInAgreement();
    public SLO(SloOperator operator, E value){
        this.values = new ArrayList<>();
        this.operators = new ArrayList<>();
        this.timeFrames = new ArrayList<>();

        this.values.add(value);
        this.operators.add(operator);
        this.timeFrames.add("24h");
    }

    public List<E> getValues() {
        return values;
    }

    public List<SloOperator> getOperators() {
        return operators;
    }

    public List<String> getTimeFrames() {
        return timeFrames;
    }

    public void setValues(List<E> values) {
        this.values = values;
    }

    public void setOperators(List<SloOperator> operators) {
        this.operators = operators;
    }

    public void setTimeFrames(List<String> timeFrames) {
        this.timeFrames = timeFrames;
    }



}
