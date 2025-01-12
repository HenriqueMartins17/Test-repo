/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.infoflow.aspect;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.control.infrastructure.role.ControlRoleManager;
import com.apitable.control.infrastructure.role.RoleConstants.Node;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.infoflow.config.properties.PermissionControlProperties;
import com.apitable.enterprise.infoflow.service.IControlThirdPartService;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.IdUtil;
import com.apitable.space.service.ISpaceService;
import com.apitable.space.vo.SpaceGlobalFeature;
import com.apitable.workspace.enums.PermissionException;
import com.apitable.workspace.service.INodeService;
import com.apitable.workspace.vo.DatasheetPermissionView;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * third part control aspect.
 */
@Aspect
@Component
@Slf4j
public class ThirdPartControlAspect {

    @Resource
    private IControlThirdPartService iControlThirdPartService;

    @Resource
    private PermissionControlProperties permissionControlProperties;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private INodeService iNodeService;

    /**
     * pointcut.
     */
    @Pointcut("@annotation(com.apitable.control.annotation.ThirdPartControl)")
    private void thirdControlPointcut() {

    }

    /**
     * around.
     *
     * @param process process
     * @return Object
     * @throws Throwable exception
     */
    @Around(value = "thirdControlPointcut()")
    public Object around(ProceedingJoinPoint process) throws Throwable {
        HttpServletRequest
            request = ((ServletRequestAttributes) Objects.requireNonNull(
            RequestContextHolder.getRequestAttributes())).getRequest();
        String shareId = request.getParameter("shareId");
        Object obj = process.proceed();
        if (null != obj && null != permissionControlProperties
            && null != permissionControlProperties.getInfoflow()
            && permissionControlProperties.getInfoflow().getEnabled()) {
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            DatasheetPermissionView data = injectPermission(BeanUtil.toBean(jsonObject.get("data"),
                DatasheetPermissionView.class), shareId);
            obj = ResponseData.success(data);
        }
        return obj;
    }

    private DatasheetPermissionView injectPermission(DatasheetPermissionView data, String shareId) {
        // except share form
        if (IdUtil.isForm(data.getNodeId()) && StrUtil.isNotBlank(shareId)) {
            return data;
        }
        String spaceId = iNodeService.getSpaceIdByNodeId(data.getNodeId());
        String spaceIds = permissionControlProperties.getInfoflow().getSpaceId();
        if (StrUtil.isNotBlank(spaceIds)) {
            List<String> spaceIdList = Arrays.asList(spaceIds.split(","));
            if (!spaceIdList.contains(spaceId)) {
                return data;
            }
        }
        // String shareId = HttpContextUtil.getRequest().getParameter("shareId");
        // -1：deleted，0：no permission， 1：readonly ，2：read and write 3 update，4：manager
        int thirdPermission;
        try {
            thirdPermission = iControlThirdPartService.getPermission(data.getNodeId(),
                SessionContext.getUserId());
        } catch (Exception exception) {
            log.error("get infoflow error", exception);
            throw new BusinessException(PermissionException.NODE_ACCESS_DENIED);
        }
        switch (thirdPermission) {
            case -1:
            case 0:
                throw new BusinessException(PermissionException.NODE_ACCESS_DENIED);
            case 1:
                return transformPermissionView(spaceId, data, Node.READER);
            case 2:
                return transformPermissionView(spaceId, data, Node.EDITOR);
            case 3:
                return transformPermissionView(spaceId, data, Node.UPDATER);
            case 4:
                return transformPermissionView(spaceId, data, Node.MANAGER);
            default:
                break;
        }
        return data;
    }

    private DatasheetPermissionView transformPermissionView(String spaceId,
                                                            DatasheetPermissionView data,
                                                            String role) {
        SpaceGlobalFeature feature = iSpaceService.getSpaceGlobalFeature(spaceId);
        DatasheetPermissionView view =
            ControlRoleManager.parseNodeRole(role)
                .permissionToBean(DatasheetPermissionView.class, feature);
        view.setHasRole(true);
        view.setUserId(data.getUserId());
        view.setUuid(data.getUuid());
        view.setRole(role);
        view.setNodeId(data.getNodeId());
        view.setDatasheetId(data.getNodeId());
        view.setFieldPermissionMap(data.getFieldPermissionMap());
        DatasheetPermissionView defaultPermission = new DatasheetPermissionView();
        BeanUtil.copyProperties(view, defaultPermission);
        return defaultPermission;
    }
}
