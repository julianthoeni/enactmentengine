package at.enactmentengine.serverless.slo;

import at.enactmentengine.serverless.nodes.FunctionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FunctionScheduler {
    private String currentResourceLink;
    private Map<String, Rule> ruleMap;
    private String calculatedResourceLink;
    public FunctionScheduler(String currentResourceLink, Map<String, Rule> ruleMap){
        this.currentResourceLink = currentResourceLink;
        this.ruleMap = ruleMap;
        this.runScheduler();
    }
    private static final Logger logger = LoggerFactory.getLogger(FunctionNode.class);


    public String getCalculatedResourceLink(){
        return this.calculatedResourceLink;
    }

    private void runScheduler(){
        logger.info("Scheduler run for: " + this.currentResourceLink);
        //Todo: Do some magic here or Math.random()
        this.calculatedResourceLink = this.currentResourceLink;
        System.out.println(ruleMap.get("convertValues"));
    }

}
