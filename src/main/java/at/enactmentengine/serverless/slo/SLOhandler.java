package at.enactmentengine.serverless.slo;

import at.enactmentengine.serverless.slo.cost.CostHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SLOhandler {
    private static SLOhandler SLOhandler_instance = null;

    private DBhandler dbhandler;
    private MoDBhandler mdbhandler;
    private CostHandler costHandler;
    private String yamlFile;
    private List<String> functionName;
    private Map<String, Rule> ruleMap;
    public HashMap<String, FunctionARNs> functions = new HashMap<>();

    public void SLOhandler() {
    }

    public static SLOhandler getInstance() {
        if (SLOhandler_instance == null) {
            SLOhandler_instance = new SLOhandler();
        }
        return SLOhandler_instance;
    }

    public void init(String filenameSLOdb, String filenameMdb, String yamlFile) throws Exception,IOException, SQLException {
        this.yamlFile = yamlFile;

        //Create dbhandler-instance and connect to MariaDB
        this.dbhandler = new DBhandler(filenameSLOdb);
        this.dbhandler.connectDB();

        //Create MongoDB-instance and connect to MongoDB
        this.mdbhandler = new MoDBhandler(filenameMdb);
        this.mdbhandler.init();

        //Create local costHandler (calculator) and load current cost-models from MariaDB
        this.costHandler = new CostHandler();
        this.costHandler.addEntries(dbhandler.getLambdaPricing());

        //Extract all needed function-names from the YAML-File
        YamlFunctionExtractor yaml = new YamlFunctionExtractor(this.yamlFile);
        yaml.init();
        this.functionName = yaml.getFunctionName();
        this.functions = yaml.getFunctions();
        //Create a map of all rules and fill them with previous data from the mongoDB
        this.ruleMap = RuleFactory.create(this.dbhandler.getSLOs(),this.dbhandler.getSloPeriods());
        for(String functionName : this.functionName){
            this.mdbhandler.addEntriesToRule(functionName, this.ruleMap.get(functionName));
        }
    }

    public DBhandler getDbhandler() {
        return this.dbhandler;
    }

    public MoDBhandler getMDBhandler() {
        return this.mdbhandler;
    }

    public CostHandler getCostHandler(){ return this.costHandler; }

    public void addEntryToRule(String functionName, long rtt, long timestemp, double cost, boolean success, String resourceLink){
        this.ruleMap.get(functionName).addDataEntry(rtt, timestemp, cost, success, resourceLink);
    }

    public Map<String, Rule> getRuleMap(){
        return Collections.unmodifiableMap(this.ruleMap);
    }

    public boolean checkIfSLOisMet(){
        //TODO: Implement time and function-name based checkinterval
        return false;
    }

}