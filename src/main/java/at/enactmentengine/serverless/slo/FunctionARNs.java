package at.enactmentengine.serverless.slo;

import java.util.ArrayList;
import java.util.List;

public class FunctionARNs {
    private String arn;
    private List<String> alternatives = new ArrayList<>();
    FunctionARNs(String arn, List<String> alternatives){
        this.arn = arn;
        this.alternatives = alternatives;
    }
    public String getARN(){
        return this.arn;
    }

    public List<String> getAlternatives(){
        return this.alternatives;
    }
}
