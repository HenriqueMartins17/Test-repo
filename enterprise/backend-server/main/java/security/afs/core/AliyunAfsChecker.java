/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.security.afs.core;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.afs.model.v20180112.AnalyzeNvcRequest;
import com.aliyuncs.afs.model.v20180112.AnalyzeNvcResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Alibaba Cloud Shield human-machine verification interface implementation class.
 * </p>
 *
 * @author Chambers
 */
public class AliyunAfsChecker implements AfsChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AliyunAfsChecker.class);

    private final String regionId;

    private final String accessKeyId;

    private final String secret;

    public AliyunAfsChecker(String regionId, String accessKeyId, String secret) {
        this.regionId = regionId;
        this.accessKeyId = accessKeyId;
        this.secret = secret;
    }

    @Override
    public String noTraceCheck(String data, String scoreJsonStr) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Cloud shield machine Non-trace validation");
        }
        AnalyzeNvcRequest request = new AnalyzeNvcRequest();
        // required parameters, the front end obtains the value of the getNVCVal function
        request.setData(data);
        // fill in as required
        request.setScoreJsonStr(scoreJsonStr);
        try {
            AnalyzeNvcResponse response = getClient().getAcsResponse(request);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Man-machine verification result: {}", response.getBizCode());
            }
            return response.getBizCode();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    private IAcsClient getClient() {
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, secret);
        IAcsClient client = new DefaultAcsClient(profile);
        DefaultProfile.addEndpoint(regionId, "afs", "afs.aliyuncs.com");
        return client;
    }
}
