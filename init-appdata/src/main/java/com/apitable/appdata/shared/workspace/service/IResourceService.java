package com.apitable.appdata.shared.workspace.service;

import java.util.List;

import com.apitable.appdata.shared.workspace.model.ResourceDataPack;
import com.apitable.appdata.shared.workspace.pojo.Node;

public interface IResourceService {

    ResourceDataPack getResourceDataPack(List<Node> nodes);

    void parseResourceDataPack(String targetSpaceId, ResourceDataPack copyProperties);
}
