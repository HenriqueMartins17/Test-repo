package com.apitable.enterprise.airagent.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sorted agent info List implement treeSet.
 */
public class SortedAgents extends ArrayList<AiAgent> {

    private final Map<String, AiAgent> preAgentMap;

    public SortedAgents() {
        super();
        this.preAgentMap = new HashMap<>();
    }

    @Override
    public boolean add(AiAgent agent) {
        boolean res = super.add(agent);
        if (agent.getPreAgentId() != null) {
            preAgentMap.put(agent.getPreAgentId(), agent);
        }
        return res;
    }

    public void sort() {
        AiAgent startAgent = null;
        for (AiAgent agent : this) {
            if (agent.getPreAgentId() == null) {
                startAgent = agent;
                break;
            }
        }

        List<AiAgent> sortedAgents = new ArrayList<>();

        // Traverse
        traverse(startAgent, sortedAgents);

        this.clear();
        this.addAll(sortedAgents);
    }

    private void traverse(AiAgent startAgent, List<AiAgent> sortedAgents) {
        if (startAgent == null) {
            return;
        }
        sortedAgents.add(startAgent);

        if (preAgentMap.containsKey(startAgent.getAgentId())) {
            AiAgent nextAgent = preAgentMap.get(startAgent.getAgentId());
            if (nextAgent != null) {
                traverse(nextAgent, sortedAgents);
            }
        }
    }

}
