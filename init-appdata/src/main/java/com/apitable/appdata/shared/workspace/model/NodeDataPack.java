package com.apitable.appdata.shared.workspace.model;

import java.util.ArrayList;
import java.util.List;

import com.apitable.appdata.shared.space.pojo.SpaceAsset;
import com.apitable.appdata.shared.workspace.pojo.Node;
import com.apitable.appdata.shared.workspace.pojo.NodeDesc;
import com.apitable.appdata.shared.workspace.pojo.NodeRel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NodeDataPack extends ResourceDataPack {

    private List<SpaceAsset> spaceAssets = new ArrayList<>();

    private List<Node> nodes = new ArrayList<>();

    private List<NodeDesc> nodeDescriptions = new ArrayList<>();

    private List<NodeRel> nodeRelations = new ArrayList<>();
}
