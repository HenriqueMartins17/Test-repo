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

import java.util.List;
import java.util.Objects;

import cn.vika.core.utils.StringUtil;
import com.apitable.enterprise.infoflow.mapper.InfoflowMemberMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import com.apitable.enterprise.infoflow.service.IUnitThirdPartService;
import com.apitable.shared.context.SessionContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Unit aspect for infoflow.
 */
@Aspect
@Component
@ConditionalOnProperty(value = "permission-control.infoflow.enabled", havingValue = "true")
@Slf4j
public class ThirdPartUnitServiceAspect {

    @Resource
    private InfoflowMemberMapper infoflowMemberMapper;

    @Resource
    private IUnitThirdPartService iUnitThirdPartService;

    @Pointcut("execution(* com.apitable.organization.mapper.TeamMapper.selectTeamIdsLikeName(..))")
    private void selectTeamIdsLikeNamePointcut() {
    }

    @Pointcut("execution(* com.apitable.organization.service.impl.UnitServiceImpl.getUnitInfoList(..))")
    private void getUnitInfoListPointcut() {
    }

    /**
     * Aop for SelectTeamIdsLikeName.
     *
     * @param process ProceedingJoinPoint Object
     * @return Object
     * @throws Throwable exception
     */
    @Around(value = "selectTeamIdsLikeNamePointcut()")
    public Object aroundSelectTeamIdsLikeNamePointcut(ProceedingJoinPoint process)
        throws Throwable {
        Object obj = process.proceed();
        Object[] args = process.getArgs();
        if (null != args) {
            Object spaceId = args[0];
            Object likeName = args[1];
            obj = infoflowMemberMapper.selectMemberIdsLikeName(String.valueOf(spaceId),
                String.valueOf(likeName));
        }
        return obj;
    }

    /**
     * GetUnitInfo with infoflow recent user
     *
     * @param process ProceedingJoinPoint Object
     * @return Object
     * @throws Throwable exception
     */
    @Around(value = "getUnitInfoListPointcut()")
    public Object aroundGetUnitInfoList(ProceedingJoinPoint process) throws Throwable {
        HttpServletRequest
            request = ((ServletRequestAttributes) Objects.requireNonNull(
            RequestContextHolder.getRequestAttributes())).getRequest();
        Object keyword = request.getParameter("keyword");
        // Only query if the keyword is empty and load the most recent active users.
        if (StringUtil.isEmpty(String.valueOf(keyword))) {
            Object[] args = process.getArgs();
            String spaceId = String.valueOf(args[0]);
            try {
                List<String> unionIds =
                    iUnitThirdPartService.getUserRecent(SessionContext.getUserId());
                List<Long> unitIds =
                    iUnitThirdPartService.getUnitIdsByUnionIdsAndSpaceId(spaceId, unionIds);
                args[1] = unitIds;
                return process.proceed(args);
            } catch (Exception exception) {
                log.error("get UserRecent error {}", exception);
            }
        }
        return process.proceed();
    }
}
