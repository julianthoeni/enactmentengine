package at.enactmentengine.serverless.slo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class YamlFunctionExtractor {

    private String yamlFile;

    YamlFunctionExtractor(String yamlFile) {
        this.yamlFile = yamlFile;
    }

    public List<String> getFunctions() throws FileNotFoundException {
        List<String> functionList = new ArrayList<String>();
        File file = new File(this.yamlFile);
        final Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            final String lineFromFile = scanner.nextLine();
            if(lineFromFile.contains("- function:")) {
                String functionName = scanner.nextLine().replace(" ","").
                replace("name:","").replace("\"","");
                functionList.add(functionName);
            }
        }
        return functionList;
    }
}