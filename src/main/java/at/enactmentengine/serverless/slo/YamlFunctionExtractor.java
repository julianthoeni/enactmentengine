package at.enactmentengine.serverless.slo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class YamlFunctionExtractor {

    private String yamlFile;
    private List<String> functionNames = new ArrayList<>();
    private HashMap<String, FunctionARNs> functions = new HashMap<>();
    YamlFunctionExtractor(String yamlFile) {
        this.yamlFile = yamlFile;
    }

    public void init() throws FileNotFoundException {
        File file = new File(this.yamlFile);
        final Scanner scanner = new Scanner(file);
        String lineFromFile = "";
        while (scanner.hasNextLine()) {
            if(!lineFromFile.contains("- function:")){
                lineFromFile = scanner.nextLine();
            }
            if(lineFromFile.contains("- function:")) {
                String name;
                String arn;
                List<String> alternatives = new ArrayList<>();
                //Find functionName
                String functionName = scanner.nextLine().replace(" ","").
                replace("name:","").replace("\"","");
                this.functionNames.add(functionName);
                name = functionName;
                //Find property_resource
                while (!lineFromFile.contains("properties:"))lineFromFile = scanner.nextLine();
                while (!lineFromFile.contains("- name: \"resource\""))lineFromFile = scanner.nextLine();
                String function = scanner.nextLine().replace(" ","").
                        replace("value:","").replace("\"","");
                if(function.contains("arn:aws")){
                    arn = function;
                }
                else {
                    arn = null;
                }
                if(scanner.hasNextLine()){
                    //Find property_alternatives
                    lineFromFile = scanner.nextLine();
                    if(lineFromFile.contains("- name: \"alternatives\"")){
                        String functionAlternatives = scanner.nextLine().replace(" ","").
                                replace("value:","").replace("\"","");
                        if(functionAlternatives.isEmpty()){
                            alternatives = Collections.emptyList();
                        }else{
                            String[] alternativeString = functionAlternatives.replace("[","").replace("]","").split(",");
                            for(String alt : alternativeString){
                                alternatives.add(alt);
                            }
                        }
                    }else{
                        alternatives = Collections.emptyList();
                }
                }else{
                    alternatives = Collections.emptyList();
                }
                if(!this.functions.containsKey(name)){
                    this.functions.put(name,new FunctionARNs(arn,alternatives));
                }
            }
        }
    }
    public List<String> getFunctionName(){
        return this.functionNames;
    }

    public HashMap<String, FunctionARNs> getFunctions(){
        return this.functions;
    }
}