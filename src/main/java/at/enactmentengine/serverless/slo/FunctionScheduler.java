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


    static String runScheduler(String functionName){
        //Todo: Do some magic here or Math.random()
        SLOhandler slohandler = SLOhandler.getInstance();
        return slohandler.getMDBhandler().getRandomResourceFromFunctionName(functionName);
    }

    static String runSchedulerInit(String functionName){
        SLOhandler slohandler = SLOhandler.getInstance();
        FunctionARNs function = slohandler.functions.get(functionName);
        if(function == null){
            return "NotInUse";
        }
        System.out.println(function.getARN());
        System.out.println(function.getAlternatives());
        if(function.getARN() != null){
            return function.getARN();
        }else{
            if(function.getAlternatives().size() != 0){
                // Run scheduler
                return function.getAlternatives().get(0);
            }
            else{
                // Throw error!
                return null;
            }
        }
    }
}