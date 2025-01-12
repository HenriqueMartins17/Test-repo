package com.apitable.appdata.shared.space.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.appdata.shared.constants.CommonConstants;
import com.apitable.appdata.shared.organization.service.IMemberService;
import com.apitable.appdata.shared.organization.service.ITeamMemberRelService;
import com.apitable.appdata.shared.organization.service.ITeamService;
import com.apitable.appdata.shared.space.mapper.SpaceMapper;
import com.apitable.appdata.shared.space.pojo.Space;
import com.apitable.appdata.shared.space.service.ISpaceService;
import com.apitable.appdata.shared.user.pojo.User;
import com.apitable.appdata.shared.util.FilePlusUtil;
import com.apitable.appdata.shared.util.IdUtil;
import com.apitable.appdata.shared.workspace.model.NodeDataPack;
import com.apitable.appdata.shared.workspace.pojo.Node;
import com.apitable.appdata.shared.workspace.service.INodeService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SpaceServiceImpl implements ISpaceService {

    @Resource
    private ITeamService iTeamService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ITeamMemberRelService iTeamMemberRelService;

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    private INodeService iNodeService;

    @Override
    public boolean checkSpaceExist(String spaceId) {
        return spaceMapper.countBySpaceId(spaceId) > 0;
    }

    @Override
    public void cleanSpaceData(String spaceId) {
        spaceMapper.deleteBySpaceId(spaceId);
        iTeamService.deleteBySpaceId(spaceId);
        iMemberService.deleteBySpaceId(spaceId);
        iNodeService.removeNonTemplateNodes(spaceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createConfigSpace(User user, String spaceId, Boolean createConfigTableEnabled) {
        Long userId = user.getId();
        // Create member
        Long memberId = iMemberService.create(userId, spaceId, user.getNickName(), user.getEmail());

        // Create space
        String spaceName = CommonConstants.CONFIG_SPACE_NAME;
        Space space = new Space();
        space.setId(IdWorker.getId());
        space.setSpaceId(spaceId);
        space.setName(spaceName);
        space.setOwner(memberId);
        space.setCreator(memberId);
        space.setProps(JSONUtil.createObj().toString());
        space.setCreatedBy(userId);
        space.setUpdatedBy(userId);
        spaceMapper.insert(space);

        // Create root department
        Long rootTeamId = iTeamService.createRootTeam(spaceId, spaceName);
        // Create root department and member relate
        iTeamMemberRelService.create(rootTeamId, memberId);

        if (Boolean.FALSE.equals(createConfigTableEnabled)) {
            // Create root node
            Node node = Node.builder()
                .id(IdWorker.getId())
                .spaceId(spaceId)
                .nodeId(IdUtil.createFolderId())
                .parentId("0")
                .type(0)
                .isTemplate(false)
                .build();
            iNodeService.createNode(userId, node);
            return;
        }
        log.info("Begin to create config table.");
        // Create node
        NodeDataPack nodeDataPack = JSONUtil.parseObj(this.getTemplateFileContent()).toBean(NodeDataPack.class);
        this.handleTemplateNode(nodeDataPack.getNodes());
        iNodeService.parseNodeDataPack(spaceId, new HashMap<>(), nodeDataPack);
    }

    private String getTemplateFileContent() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(FilePlusUtil.CONFIG_DATASHEET_TEMPLATE_FILE_NAME);
        try {
            return FilePlusUtil.parseInputStream(is, true);
        }
        catch (IOException e) {
            throw new RuntimeException(StrUtil.format("Parse template file failure.Message: {}", e.getMessage()));
        }
    }

    private void handleTemplateNode(List<Node> nodes) {
        List<String> nodeIds = nodes.stream().map(Node::getNodeId).collect(Collectors.toList());
        for (Node node : nodes) {
            node.setId(IdWorker.getId());
            node.setIsTemplate(Boolean.FALSE);
            if (!nodeIds.contains(node.getParentId())) {
                node.setType(0);
                node.setParentId("0");
                node.setNodeName(null);
            }
        }
    }
}
