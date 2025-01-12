package com.apitable.enterprise.airagent.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class SortedAgentsTest {

    @Test
    void testSort() {
        SortedAgents sortedAgents = new SortedAgents();
        sortedAgents.add(new AiAgent("Agent3", "Agent2"));
        sortedAgents.add(new AiAgent("Agent5", "Agent4"));
        sortedAgents.add(new AiAgent("Agent2", "Agent1"));
        sortedAgents.add(new AiAgent("Agent4", "Agent3"));
        sortedAgents.add(new AiAgent("Agent1", null));

        sortedAgents.sort();

        assertThat(sortedAgents).containsSequence(
            new AiAgent("Agent1", null),
            new AiAgent("Agent2", "Agent1"),
            new AiAgent("Agent3", "Agent2"),
            new AiAgent("Agent4", "Agent3"),
            new AiAgent("Agent5", "Agent4")
        );
    }
}
