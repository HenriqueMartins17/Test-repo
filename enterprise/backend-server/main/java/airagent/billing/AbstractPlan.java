package com.apitable.enterprise.airagent.billing;

public abstract class AbstractPlan implements Plan {

    private final long numberOfAgents;

    private final long multipleFactor;

    public AbstractPlan(long numberOfAgents, long multipleFactor) {
        this.numberOfAgents = numberOfAgents;
        this.multipleFactor = multipleFactor;
    }

    @Override
    public Long getMaxNumOfCredits() {
        return numberOfAgents * multipleFactor;
    }
}
