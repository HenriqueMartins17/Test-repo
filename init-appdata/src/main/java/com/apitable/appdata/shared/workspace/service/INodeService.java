package com.apitable.appdata.shared.workspace.service;

import com.apitable.appdata.shared.workspace.pojo.Node;
import java.util.Map;
import java.util.Set;

import com.apitable.appdata.shared.workspace.model.FileDataPack;
import com.apitable.appdata.shared.workspace.model.NodeDataPack;

public interface INodeService {

    FileDataPack getFileDataPack(Set<String> nodeIds);

    void parseNodeDataPack(String targetSpaceId, Map<Long, Long> newAssetIdMap, NodeDataPack dataPack);

    void remove(Set<String> nodeIds);

    void removeNonTemplateNodes(String spaceId);

    void createNode(Long userId, Node node);
}
