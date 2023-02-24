package at.enactmentengine.serverless.slo.cost;

import java.util.LinkedHashMap;

public class CostHandler {
    private LinkedHashMap<String, Float> regionPricingList = new LinkedHashMap<>();
    public CostHandler(){
    }

    public void addEntries(LinkedHashMap<String, Float> regionPricingList){
        this.regionPricingList.putAll(regionPricingList);
    }

    public double getPricingFromRegion(String region, int ms, int memoryMB,float multiplierMemory){
        float costPerGBsecond = this.regionPricingList.get(region);
        float cost = ms * memoryMB * multiplierMemory * (costPerGBsecond/1024000);

        return cost;
    }

    public String getRegionCodeFromARN(String arn){
        String[] stringArray = arn.split(":",7);
        String regionCode = stringArray[3];
        return regionCode;
    }
}