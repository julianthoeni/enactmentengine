package at.enactmentengine.serverless.slo;

import at.enactmentengine.serverless.nodes.FunctionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FunctionScheduler {
    private String currentResourceLink;
    private Map<String, Rule> ruleMap;
    private String calculatedResourceLink;
    public FunctionScheduler(){
    }
    private static final Logger logger = LoggerFactory.getLogger(FunctionNode.class);


    public static String runScheduler(String functionName){
        System.out.println("Running scheduler ...");
        SLOhandler slohandler = SLOhandler.getInstance();
        //Todo: Do some magic here or Math.random()
        return slohandler.functions.get(functionName).getAlternatives().get(0);
    }

    static String runSchedulerInit(String functionName){
        SLOhandler slohandler = SLOhandler.getInstance();
        FunctionARNs function = slohandler.functions.get(functionName);
        if(function == null){
            return "NotInUse";
        }
        if(function.getARN() != null){
            return function.getARN();
        }else{
            if(!function.getAlternatives().isEmpty()){
                //Run scheduler
                return runScheduler(functionName);
            }
            else{
                // Throw error!
                System.out.println("No resource and alternative defined - ERROR");
                return null;
            }
        }
    }
}