package at.enactmentengine.serverless.slo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    }

    public static List<Rule> create(ResultSet sloSqlData){
        List<Rule> rules = new LinkedList<>();
        Map<String, List<SloTempObject>> ruleSlos = new HashMap<>();

        while (true){
            try {
                if (!sloSqlData.next()) break;
                String functionName = sloSqlData.getString("functiontype");
                Integer sloId = sloSqlData.getInt("sloid");
                String constraintType = sloSqlData.getString("constraintType");
                String unit = sloSqlData.getString("unit");
                Double successRate = sloSqlData.getDouble("successRate");

                List<SloTempObject> slos = ruleSlos.getOrDefault(functionName, new LinkedList<>());
                slos.add(new SloTempObject(sloId, constraintType, unit, successRate));
                ruleSlos.put(functionName, slos);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }


        return rules;
    }
}
