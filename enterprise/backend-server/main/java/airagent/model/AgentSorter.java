package com.apitable.enterprise.airagent.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent sorter.
 */
public class AgentSorter {

    private final Map<String, AiAgent> preAgentMap;

    private AiAgent rootAgent;

    public AgentSorter() {
        this(new ArrayList<>());
    }

    public AgentSorter(List<AiAgent> sourceList) {
        this.preAgentMap = new HashMap<>();
        sourceList.forEach(agent -> {
            if (agent.getPreAgentId() != null) {
                preAgentMap.put(agent.getPreAgentId(), agent);
            } else {
                rootAgent = agent;
            }
        });
    }

    public void addAgent(AiAgent agent) {
        if (agent.getPreAgentId() != null) {
            preAgentMap.put(agent.getPreAgentId(), agent);
        } else {
            rootAgent = agent;
        }
    }

    public Map<String, AiAgent> getPreAgentMap() {
        return preAgentMap;
    }

    public List<AiAgent> sort() {
        List<AiAgent> sortedAgents = new ArrayList<>();

        // Traverse
        traverse(rootAgent, sortedAgents);

        return sortedAgents;
    }

    private void traverse(AiAgent startAgent, List<AiAgent> sortedAgents) {
        sortedAgents.add(startAgent);

        if (preAgentMap.containsKey(startAgent.getAgentId())) {
            AiAgent nextAgent = preAgentMap.get(startAgent.getAgentId());
            if (nextAgent != null) {
                traverse(nextAgent, sortedAgents);
            }
        }
    }
}
