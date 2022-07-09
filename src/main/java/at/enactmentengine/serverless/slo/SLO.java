package at.enactmentengine.serverless.slo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SLO<E> {
    protected class SloEntry<E>{
        private E value;
        private SloOperator operator;
        private String timeFrame;

        public SloEntry (E value, SloOperator operator, String timeFrame){
            this.value = value;
            this.operator = operator;
            this.timeFrame = timeFrame;
        }

        public E getValue() {
            return value;
        }

        public SloOperator getOperator() {
            return operator;
        }

        public String getTimeFrame() {
            return timeFrame;
        }
    }

    private List<SloEntry> entries;
    // data is used for agreement calculation (made by user)
    private SloData data;


    public abstract boolean isInAgreement();

    public SLO(SloOperator operator, E value){
        this.entries = new ArrayList<>();
        this.entries.add(new SloEntry(value, operator, "24h"));
    }

    public List<SloEntry> getEntries(){
        return Collections.unmodifiableList(this.entries);
    }

    public SloData getData() {
        return data;
    }
}
