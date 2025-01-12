package com.apitable.enterprise.ai.model;

import com.apitable.workspace.enums.NodeType;
import lombok.Getter;

/**
 * data source.
 */
@Getter
public class AiTrainingDataSource {

    private final String nodeId;
    private final String nodeName;
    private final NodeType nodeType;
    private final AiSetting setting;

    public AiTrainingDataSource(String nodeId, String nodeName, NodeType nodeType, AiSetting setting) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.setting = setting;
    }

    public boolean isAfter(Long revision) {
        return revision != null && this.setting.getRevision() != null &&
            this.setting.getRevision() < revision;
    }
}
