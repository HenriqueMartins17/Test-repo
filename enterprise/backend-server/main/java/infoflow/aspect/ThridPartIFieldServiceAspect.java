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

import jakarta.annotation.Resource;

import com.apitable.enterprise.infoflow.service.IFieldThridPartService;
import com.apitable.internal.vo.UrlAwareContentsVo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * IField aspect for infoflow.
 */
@Aspect
@Component
@ConditionalOnProperty(value = "permission-control.infoflow.enabled", havingValue = "true")
@Slf4j
public class ThridPartIFieldServiceAspect {

    @Resource
    private IFieldThridPartService iFieldThridPartService;

    @Pointcut("execution(* com.apitable.internal.service.impl.FieldServiceImpl.getUrlAwareContents(..))")
    private void getUrlAwareContentsPointcut() {

    }

    /**
     * Get Url AwareContents from infoflouw link identify
     *
     * @param process ProceedingJoinPoint Object
     * @return Object
     * @throws Throwable exception
     */
    @Around(value = "getUrlAwareContentsPointcut()")
    public Object aroundGetUrlAwareContents(ProceedingJoinPoint process) throws Throwable {
        Object[] args = process.getArgs();
        List<String> urls = (List<String>) args[0];

        try {
            UrlAwareContentsVo injectUrlAwareContentsVo =
                iFieldThridPartService.getUrlAwareContents(urls);
            //Remove ruliu awared urls.
            injectUrlAwareContentsVo.getContents().forEach((url, urlAwareContentVo) -> {
                if (urls.contains(url)) {
                    urls.remove(url);
                }
            });
            args[0] = urls;
            UrlAwareContentsVo urlAwareContentsVo = (UrlAwareContentsVo) process.proceed(args);
            //Merge to default UrlAwareContents
            urlAwareContentsVo.getContents().putAll(injectUrlAwareContentsVo.getContents());
            return urlAwareContentsVo;
        } catch (Exception e) {
            log.error("aroundGetUrlAwareContents error {}", e);
        }

        return process.proceed(args);
    }
}
