package com.apitable.enterprise.airagent.billing;

public class FreePlan extends AbstractPlan {

    private final long maxNumOfAgents;

    public FreePlan() {
        super(5, 50);
        this.maxNumOfAgents = 5;
    }

    @Override
    public String getPlanName() {
        return PlanName.FREE.getValue();
    }

    @Override
    public Long getMaxNumOfAgents() {
        return maxNumOfAgents;
    }
}
