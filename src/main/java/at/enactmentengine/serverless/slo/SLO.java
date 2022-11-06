package at.enactmentengine.serverless.slo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SLO<E> {
    protected class SloEntry<E>{
        private E value;
        private SloOperator operator;
        private String timeFrame;

        private long timeFrameInMs;

        public SloEntry (E value, SloOperator operator, String timeFrame){
            this.value = value;
            this.operator = operator;
            this.timeFrame = timeFrame;
            this.timeFrameInMs = parseStringToMs(timeFrame);
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

        public long getTimeFrameInMs(){
            return timeFrameInMs;
        }

        private long parseStringToMs(String s){
            return parseStringToMsHelper(s.toLowerCase(), "w");
        }

        private long parseStringToMsHelper(String s, String currentSearch){
            String[] splits = s.split(currentSearch);
            String nextSearch = "w";
            long multiplier = 604800000L;
            switch (currentSearch){
                case "w": nextSearch = "d"; break;
                case "d": nextSearch = "h"; multiplier = 86400000L; break;
                case "h": nextSearch = "m"; multiplier = 3600000L; break;
                case "m": multiplier = 60000L; break;
            }
            long value = 0;
            for (String a: splits){
                if(!a.isEmpty()) {
                    if (a.matches("^[0-9]+")) {
                        value += multiplier * Long.parseLong(a);
                    } else {
                        value += parseStringToMsHelper(a.replace(" ", ""), nextSearch);
                    }
                }
            }
            return value;
        }


    }

    private List<SloEntry> entries;
    // data is used for agreement calculation (made by user)
    private SloData data;


    public abstract boolean isInAgreement(String resourceLink);

    protected abstract Map<String, Double> getPoints();

    protected Map<String, List<SloData.DataEntry>> getDataByResourceLink(){
        List<SloData.DataEntry> entries = this.getData().getList();

        // group by resourcelinks:
        Map<String, List<SloData.DataEntry>> dataByResource =
                entries.stream().collect(Collectors.groupingBy(elem -> elem.getResourceLink()));

        return dataByResource;
    }

    public SLO(SloOperator operator, E value){
        this.entries = new ArrayList<>();
        this.entries.add(new SloEntry(value, operator, "24h"));
    }

    public SLO(SloOperator operator, E value, String timeFrame){
        this.entries = new ArrayList<>();
        this.entries.add(new SloEntry(value, operator, timeFrame));
    }

    public List<SloEntry> getEntries(){
        return Collections.unmodifiableList(this.entries);
    }

    public void addEntry(SloOperator operator, E value, String timeFrame){
        this.entries.add(new SloEntry(value, operator, timeFrame));
    }

    protected void setData(SloData data){
        this.data = data;
    }

    public SloData getData() {
        return data;
    }
}
