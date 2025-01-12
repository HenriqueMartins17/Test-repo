package com.apitable.appdata.shared.workspace.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.appdata.shared.asset.pojo.Asset;
import com.apitable.appdata.shared.asset.service.IAssetService;
import com.apitable.appdata.shared.constants.CommonConstants;
import com.apitable.appdata.shared.space.pojo.SpaceAsset;
import com.apitable.appdata.shared.space.service.ISpaceAssetService;
import com.apitable.appdata.shared.util.IdUtil;
import com.apitable.appdata.shared.workspace.mapper.NodeDescMapper;
import com.apitable.appdata.shared.workspace.mapper.NodeMapper;
import com.apitable.appdata.shared.workspace.mapper.NodeRelMapper;
import com.apitable.appdata.shared.workspace.model.FileDataPack;
import com.apitable.appdata.shared.workspace.model.NodeDataPack;
import com.apitable.appdata.shared.workspace.model.ResourceDataPack;
import com.apitable.appdata.shared.workspace.pojo.Node;
import com.apitable.appdata.shared.workspace.pojo.NodeDesc;
import com.apitable.appdata.shared.workspace.pojo.NodeRel;
import com.apitable.appdata.shared.workspace.service.INodeService;
import com.apitable.appdata.shared.workspace.service.IResourceService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NodeServiceImpl implements INodeService {

    @Resource
    private IAssetService iAssetService;

    @Resource
    private ISpaceAssetService iSpaceAssetService;

    @Resource
    private IResourceService iResourceService;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private NodeDescMapper nodeDescMapper;

    @Resource
    private NodeRelMapper nodeRelMapper;

    @Override
    public FileDataPack getFileDataPack(Set<String> nodeIds) {
        List<NodeDataPack> nodeDataPacks = new ArrayList<>();
        Set<String> coverTokens = new HashSet<>();
        Set<Long> assetIds = new HashSet<>();
        for (String nodeId : nodeIds) {
            NodeDataPack nodeDataPack = this.getNodeDataPack(nodeId);
            nodeDataPacks.add(nodeDataPack);
            coverTokens.addAll(nodeDataPack.getNodes().stream().map(Node::getCover).filter(StrUtil::isNotBlank).collect(Collectors.toSet()));
            assetIds.addAll(nodeDataPack.getSpaceAssets().stream().map(SpaceAsset::getAssetId).collect(Collectors.toSet()));
        }
        List<Asset> assets = assetIds.isEmpty() ? new ArrayList<>() : iAssetService.getAssets(assetIds);
        // Avoid cover missing
        if (!coverTokens.isEmpty()) {
            List<String> existFileUrl = assets.stream().map(Asset::getFileUrl).collect(Collectors.toList());
            existFileUrl.forEach(coverTokens::remove);
            assets.addAll(iAssetService.getAssetsByFileUrls(coverTokens));
        }
        return new FileDataPack(assets, nodeDataPacks);
    }

    private NodeDataPack getNodeDataPack(String nodeId) {
        Set<String> nodeIds = new HashSet<>();
        nodeIds.add(nodeId);
        if (nodeId.startsWith(IdUtil.FOD)) {
            List<String> subNodeIds = nodeMapper.selectAllSubNodeIds(nodeIds);
            nodeIds.addAll(subNodeIds);
        }
        NodeDataPack dataPack = new NodeDataPack();
        List<Node> nodes = nodeMapper.selectByNodeIds(nodeIds);
        dataPack.setNodes(nodes);
        List<NodeDesc> nodeDescriptions = nodeDescMapper.selectByNodeIds(nodeIds);
        dataPack.setNodeDescriptions(nodeDescriptions);
        List<String> datasheetIds = nodes.stream().filter(i -> i.getType() == 2).map(Node::getNodeId).collect(Collectors.toList());
        if (!datasheetIds.isEmpty()) {
            List<NodeRel> nodeRelations = nodeRelMapper.selectByMainNodeIds(nodeIds);
            if (!nodeRelations.isEmpty()) {
                dataPack.setNodeRelations(nodeRelations.stream().filter(i -> nodeIds.contains(i.getRelNodeId())).collect(Collectors.toList()));
            }
        }

        List<SpaceAsset> spaceAssets = iSpaceAssetService.getSpaceAsset(nodeIds);
        dataPack.setSpaceAssets(spaceAssets);
        ResourceDataPack resourceDataPack = iResourceService.getResourceDataPack(nodes);
        BeanUtil.copyProperties(resourceDataPack, dataPack);
        return dataPack;
    }

    @Override
    public void parseNodeDataPack(String targetSpaceId, Map<Long, Long> newAssetIdMap, NodeDataPack dataPack) {
        if (dataPack.getNodes().isEmpty()) {
            return;
        }
        log.info("Begin to parse node data.");
        dataPack.getNodes().forEach(i -> i.setSpaceId(targetSpaceId));
        nodeMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, dataPack.getNodes());
        if (!dataPack.getNodeDescriptions().isEmpty()) {
            nodeDescMapper.insertBatch(dataPack.getNodeDescriptions());
        }
        if (!dataPack.getNodeRelations().isEmpty()) {
            nodeRelMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, dataPack.getNodeRelations());
        }

        log.info("Begin to parse resource data.");
        iResourceService.parseResourceDataPack(targetSpaceId, BeanUtil.copyProperties(dataPack, ResourceDataPack.class));

        log.info("Begin to parse space asset data.");
        iSpaceAssetService.parseSpaceAsset(targetSpaceId, newAssetIdMap, dataPack.getSpaceAssets());
    }

    @Override
    public void remove(Set<String> nodeIds) {
        Set<String> fodIds = nodeIds.stream().filter(i -> i.startsWith(IdUtil.FOD)).collect(Collectors.toSet());
        if (!fodIds.isEmpty()) {
            List<String> subNodeIds = nodeMapper.selectAllSubNodeIds(fodIds);
            nodeIds.addAll(subNodeIds);
        }
        nodeMapper.remove(CommonConstants.INIT_ACCOUNT_USER_ID, nodeIds);
    }

    @Override
    public void removeNonTemplateNodes(String spaceId) {
        nodeMapper.removeBySpaceIdAnd(spaceId, false);
    }

    @Override
    public void createNode(Long userId, Node node) {
        nodeMapper.insertBatch(userId, Collections.singletonList(node));
    }
}
