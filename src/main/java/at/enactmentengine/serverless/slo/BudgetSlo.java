package at.enactmentengine.serverless.slo;

import java.util.List;
import java.util.Map;

public abstract class BudgetSlo<T> extends SLO<T>{
    protected Map<String, Integer> leftBudget = null;
    long lastBudgetCalculation = 0;
    public BudgetSlo(SloOperator operator, T value, Integer budget) {
        super(operator, value, budget);
    }

    public BudgetSlo(SloOperator operator, T value, String timeFrame, Integer budget) {
        super(operator, value, timeFrame, budget);
    }

    protected abstract int usedBudgetByTimeFrame(long currentTime, long timeFrameInMs, List<String> resourceLinks, SloOperator operator, Double value);

    protected abstract Map<String, Integer> getTotalBudgetLeft();

}
