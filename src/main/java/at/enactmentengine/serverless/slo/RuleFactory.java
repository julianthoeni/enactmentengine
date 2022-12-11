package at.enactmentengine.serverless.slo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RuleFactory {

    private static class SloTempObject{
        public int sloId;
        public String constraintType;
        public String unit;
        public double successRate;

        SloTempObject(int sloId, String constraintType, String unit, double successRate){
            this.sloId = sloId;
            this.constraintType = constraintType;
            this.unit = unit;
            this.successRate = successRate;
        }

        @Override
        public String toString() {
            return "SloTempObject{" +
                    "sloId=" + sloId +
                    ", constraintType='" + constraintType + '\'' +
                    ", unit='" + unit + '\'' +
                    ", successRate=" + successRate +
                    '}';
        }
    }

    private static class SloSettingsObject{
        public String value;
        public SloOperator operator;
        public String period;
        public Integer budget;

        SloSettingsObject(String value, SloOperator operator, String period, Integer budget){
            this.value = value;
            this.operator = operator;
            this.period = period;
            this.budget = budget;
        }

        @Override
        public String toString() {
            return "SloSettingsObject{" +
                    "value='" + value + '\'' +
                    ", operator=" + operator +
                    ", period='" + period + '\'' +
                    ", budget='" + budget + '\'' +
                    '}';
        }
    }

    public static Map<String, Rule> create(ResultSet ruleData, ResultSet sloPeriod) throws Exception{
        Map<String, Rule> rules = new HashMap<>();
        Map<String, List<SloTempObject>> ruleSlos = new HashMap<>();
        Map<Integer, List<SloSettingsObject>> sloTimePeriodMap = new HashMap<>();

        while (true){
            try {
                if (!ruleData.next()) break;
                String functionName = ruleData.getString("functiontype");
                int sloId = ruleData.getInt("sloid");
                String constraintType = ruleData.getString("constraintType");
                String unit = ruleData.getString("unit");
                double successRate = ruleData.getDouble("successRate");

                List<SloTempObject> slos = ruleSlos.getOrDefault(functionName, new LinkedList<>());
                slos.add(new SloTempObject(sloId, constraintType, unit, successRate));
                ruleSlos.put(functionName, slos);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        while (true){ // comment this section if not prepared yet
            try {
                if (!sloPeriod.next()) break;
                String value = sloPeriod.getString("value");
                String operatorString = sloPeriod.getString("operator");
                String period = sloPeriod.getString("period");
                Integer budget = sloPeriod.getInt("budget");
                int sloid = sloPeriod.getInt("sloid");

                SloOperator operator = SloOperator.getOperator(operatorString);

                List<SloSettingsObject> settings = sloTimePeriodMap.getOrDefault(sloid, new LinkedList<>());
                settings.add(new SloSettingsObject(value, operator, period, budget));
                sloTimePeriodMap.put(sloid, settings);
                System.out.println(settings.toString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        for (String r : ruleSlos.keySet()){
            // TODO: use data from slo_period table
            List<SloTempObject> data = ruleSlos.get(r);
            SLO mainSlo = null;
            List<SLO> additionalSlos = new ArrayList<>();

            for (SloTempObject entry : data){
                SLO temp = null;
                List<SloSettingsObject> settings = sloTimePeriodMap.get(entry.sloId);
                // TODO: sort settings list from smallest time frame (first) to highest (last)

                for (SloSettingsObject singleSloSetting : settings) {
                    if (temp != null){
                        temp.addEntry(singleSloSetting.operator, Double.parseDouble(singleSloSetting.value), singleSloSetting.period, singleSloSetting.budget);
                    }
                    else if(temp == null) {
                        switch (entry.unit) { // temp data TODO: add first entry from list, and remove from list
                            case "$":
                                if(singleSloSetting.budget == null){
                                    temp = new CostSlo(singleSloSetting.operator, Double.parseDouble(singleSloSetting.value), singleSloSetting.period);
                                } else {
                                    temp = new CostSloBudget(singleSloSetting.operator, Double.parseDouble(singleSloSetting.value), singleSloSetting.period, singleSloSetting.budget);
                                }

                                break;
                            case "%":
                                if(singleSloSetting.budget == null){
                                    temp = new SuccessRateSlo(singleSloSetting.operator, Double.parseDouble(singleSloSetting.value), singleSloSetting.period);
                                } else {
                                    temp = new SuccessRateSloBudget(singleSloSetting.operator, Double.parseDouble(singleSloSetting.value), singleSloSetting.period, singleSloSetting.budget);
                                }
                                break;
                            case "s": // why is language level 8?
                            case "h":
                            case "ms":
                                if(singleSloSetting.budget == null){
                                    temp = new TimeSlo(singleSloSetting.operator, Double.parseDouble(singleSloSetting.value), singleSloSetting.period);
                                } else {
                                    temp = new TimeSloBudget(singleSloSetting.operator, Double.parseDouble(singleSloSetting.value), singleSloSetting.period, singleSloSetting.budget);
                                }
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + entry.unit);
                        }
                    }
                }

                if(temp == null){
                    throw new Exception("Failure declaring SLO (how did you get this?)");
                }

                switch(entry.constraintType){
                    case "constraint": mainSlo = temp; break;
                    case "objective": additionalSlos.add(temp); break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + entry.constraintType);
                }
            }

            if (mainSlo == null) throw new Exception("No main SLO defined for " + r);
            Rule rule = new Rule(mainSlo, additionalSlos, r);
            SLOhandler slohandler = SLOhandler.getInstance();
            if(slohandler.functions.containsKey(r)){
                if(slohandler.functions.get(r).getARN() != null){
                    rule.setCurrentExecution(slohandler.functions.get(r).getARN());
                    rule.addResourceEntry(slohandler.functions.get(r).getARN());
                }
                for(String alternative : slohandler.functions.get(r).getAlternatives()){
                    rule.addResourceEntry(alternative);
                }
            }
            rules.put(r, rule);
        }

        return rules;
    }
}
