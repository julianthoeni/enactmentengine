package at.enactmentengine.serverless.slo;

public enum SloOperator {
    GREATER_THAN(">"), GREATER_EQUALS(">="), RANGE("[...]"), EQUALS("="), LESS_THAN("<"), LESS_EQUALS("<=");
    private String value;

    SloOperator(String match){
        this.value = match;
    }

    @Override
    public String toString(){
        return this.value;
    }

    public static SloOperator getOperator(String value){
        switch (value){
            case ">": return GREATER_THAN;
            case ">=": return GREATER_EQUALS;
            case "=": return EQUALS;
            case "<=": return LESS_EQUALS;
            case "<": return LESS_THAN;
            case "[...]": return RANGE;
        }
        return null;
    }

}
