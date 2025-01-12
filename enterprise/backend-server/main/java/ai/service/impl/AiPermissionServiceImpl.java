package com.apitable.enterprise.ai.service.impl;

import com.apitable.control.infrastructure.ControlTemplate;
import com.apitable.control.infrastructure.permission.NodePermission;
import com.apitable.control.infrastructure.permission.PermissionDefinition;
import com.apitable.control.infrastructure.role.ControlRole;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.ai.service.IAiPermissionService;
import com.apitable.organization.service.IMemberService;
import com.apitable.workspace.enums.PermissionException;
import com.apitable.workspace.service.INodeService;
import com.apitable.workspace.service.INodeShareService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * ai permission service implementation.
 */
@Service
public class AiPermissionServiceImpl implements IAiPermissionService {

    @Resource
    private INodeService iNodeService;

    @Resource
    private INodeShareService iNodeShareService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ControlTemplate controlTemplate;

    @Override
    public void anonymousValid(String aiId, Long userId) {
        if (userId != null) {
            String spaceId = iNodeService.getSpaceIdByNodeId(aiId);
            Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
            if (memberId == null) {
                boolean isShared = iNodeShareService.isNodeShared(aiId);
                if (!isShared) {
                    throw new BusinessException(PermissionException.NODE_ACCESS_DENIED);
                }
            } else {
                // look up the member's permission
                ControlRole role = controlTemplate.fetchNodeRole(memberId, aiId);
                if (!role.hasPermission(NodePermission.READ_NODE)) {
                    boolean isShared = iNodeShareService.isNodeShared(aiId);
                    if (!isShared) {
                        throw new BusinessException(PermissionException.NODE_ACCESS_DENIED);
                    }
                }
            }
        } else {
            iNodeShareService.checkNodeShareStatus(aiId);
        }
    }

    @Override
    public void validPermission(String aiId, Long userId, PermissionDefinition permission) {
        String spaceId = iNodeService.getSpaceIdByNodeId(aiId);
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        if (memberId == null) {
            throw new BusinessException(PermissionException.NODE_ACCESS_DENIED);
        }
        ControlRole role = controlTemplate.fetchNodeRole(memberId, aiId);
        if (!role.hasPermission(permission)) {
            throw new BusinessException(PermissionException.NODE_ACCESS_DENIED);
        }
    }
}
