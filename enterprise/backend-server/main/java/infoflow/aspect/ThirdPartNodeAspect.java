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

import cn.hutool.core.util.StrUtil;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.infoflow.model.NodesResponse;
import com.apitable.enterprise.infoflow.service.INodeThirdPartService;
import com.apitable.shared.context.SessionContext;
import com.apitable.workspace.vo.NodeInfoVo;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Node aspect for infoflow.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(value = "permission-control.infoflow.enabled", havingValue = "true")
public class ThirdPartNodeAspect {

    @Resource
    private INodeThirdPartService iNodeThirdPartService;

    @Pointcut("execution(* com.apitable.workspace.controller.NodeController.getNodeChildrenList(..))")
    private void getNodeChildrenListPointcut() {
    }

    /**
     * *
     *
     * @param process ProceedingJoinPoint Object
     * @return ResponseData<List < NodeInfoVo>>
     * @throws Throwable exception
     */
    @Around(value = "getNodeChildrenListPointcut()")
    public Object aroundGetNodeChildrenList(ProceedingJoinPoint process) throws Throwable {
        // Internal request without check.
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
            .currentRequestAttributes())
            .getRequest();
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        Object obj = process.proceed();

        if (StrUtil.isNotBlank(bearerToken)) {
            return obj;
        }

        //Check by normal request.
        Object[] args = process.getArgs();
        List<NodeInfoVo> nodeChildrenList = null;
        ResponseData<List<NodeInfoVo>> responseData = ((ResponseData<List<NodeInfoVo>>) obj);
        //Get nodeChildrenList from NodeThirdPartService.
        if (null != args) {
            Object nodeId = args[0];
            log.info("aroundGetNodeChildrenList nodeId: {}", nodeId);
            NodesResponse nodesResponse =
                iNodeThirdPartService.getChildNodesByNodeId("", String.valueOf(nodeId),
                    SessionContext.getUserId());
            List<String> nodeList = Arrays.stream(nodesResponse.getData()).map(
                    NodesResponse.Data::getNodeId)
                .collect(Collectors.toList());
            //filter intersection childrenList
            nodeChildrenList = responseData.getData()
                .stream()
                .filter(m -> nodeList.contains(m.getNodeId()))
                .collect(Collectors.toList());
        }
        responseData.setData(nodeChildrenList);
        return responseData;
    }
}
