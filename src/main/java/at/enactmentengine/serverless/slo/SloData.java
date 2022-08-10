package at.enactmentengine.serverless.slo;

import java.util.*;

//stores data for a single Slo through all functions
public class SloData {
    protected class DataEntry{
        private int id;
        private long rtt;
        private long timestamp;
        private boolean success;
        private String resourceLink;

        public DataEntry(int id, long rtt, long timeStamp, boolean success, String resourceLink){
            this.id = id;
            this.rtt = rtt;
            this.timestamp = timeStamp;
            this.success = success;
            this.resourceLink = resourceLink;
        }

        public int getId() {
            return id;
        }

        public long getRtt() {
            return rtt;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getResourceLink() {
            return resourceLink;
        }
    }

    private List<DataEntry> entries;
    private Set<String> availableResourceLinks;

    public SloData(String name) {
        entries = new LinkedList<DataEntry>();
        availableResourceLinks = new HashSet<>();
        init(name);
    }

    public void init(String name){
        //do some database magic here
    }

    public void addEntry(int id, long rtt, long timeStamp, boolean success, String resourceLink){
        entries.add(new DataEntry(id, rtt, timeStamp, success, resourceLink));
    }

    public void addResourceLink(String resourceLink){
        this.availableResourceLinks.add(resourceLink);
    }

    public Set<String> getResourceLinks(){
        return Collections.unmodifiableSet(this.availableResourceLinks);
    }

    public List<DataEntry> getList(){
        return Collections.unmodifiableList(this.entries);
    }


}
