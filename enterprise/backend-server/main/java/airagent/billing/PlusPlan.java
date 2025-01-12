package com.apitable.enterprise.airagent.billing;

public class PlusPlan extends AbstractPlan {

    public PlusPlan(long numberOfAgents) {
        super(numberOfAgents, 500);
    }

    @Override
    public String getPlanName() {
        return PlanName.PLUS.getValue();
    }

    @Override
    public Long getMaxNumOfAgents() {
        return -1L;
    }
}
