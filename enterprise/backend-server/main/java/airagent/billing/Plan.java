package com.apitable.enterprise.airagent.billing;

public interface Plan {

    String getPlanName();

    Long getMaxNumOfAgents();

    Long getMaxNumOfCredits();
}
