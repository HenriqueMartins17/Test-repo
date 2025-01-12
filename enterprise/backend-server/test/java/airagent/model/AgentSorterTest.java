package com.apitable.enterprise.airagent.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AgentSorterTest {

    @Test
    void testConstructorEmpty() {
        AgentSorter agentSorter = new AgentSorter();
        agentSorter.addAgent(new AiAgent("Agent3", "Agent2"));
        agentSorter.addAgent(new AiAgent("Agent5", "Agent4"));
        agentSorter.addAgent(new AiAgent("Agent2", "Agent1"));
        agentSorter.addAgent(new AiAgent("Agent4", "Agent3"));
        agentSorter.addAgent(new AiAgent("Agent1", null));
        List<AiAgent> sortedAgents = agentSorter.sort();

        assertThat(sortedAgents).containsSequence(
            new AiAgent("Agent1", null),
            new AiAgent("Agent2", "Agent1"),
            new AiAgent("Agent3", "Agent2"),
            new AiAgent("Agent4", "Agent3"),
            new AiAgent("Agent5", "Agent4")
        );
    }

    @Test
    void testConstructorWithList() {
        List<AiAgent> agents = new ArrayList<>();
        agents.add(new AiAgent("Agent3", "Agent2"));
        agents.add(new AiAgent("Agent5", "Agent4"));
        agents.add(new AiAgent("Agent2", "Agent1"));
        agents.add(new AiAgent("Agent4", "Agent3"));
        agents.add(new AiAgent("Agent1", null));

        AgentSorter agentSorter = new AgentSorter(agents);
        List<AiAgent> sortedAgents = agentSorter.sort();

        assertThat(sortedAgents).containsSequence(
            new AiAgent("Agent1", null),
            new AiAgent("Agent2", "Agent1"),
            new AiAgent("Agent3", "Agent2"),
            new AiAgent("Agent4", "Agent3"),
            new AiAgent("Agent5", "Agent4")
        );
    }
}
