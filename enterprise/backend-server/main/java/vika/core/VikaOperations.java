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

package com.apitable.enterprise.vika.core;

import java.util.List;

import com.apitable.enterprise.vika.core.model.BillingOrder;
import com.apitable.enterprise.vika.core.model.BillingOrderItem;
import com.apitable.enterprise.vika.core.model.BillingOrderPayment;
import com.apitable.enterprise.vika.core.model.DingTalkSubscriptionInfo;
import com.apitable.enterprise.vika.core.model.GmPermissionInfo;
import com.apitable.enterprise.vika.core.model.IntegralRewardInfo;
import com.apitable.enterprise.vika.core.model.UserContactInfo;
import com.apitable.enterprise.vika.core.model.template.TemplateCenterConfigInfo;
import com.apitable.enterprise.vika.core.model.template.TemplateConfigDatasheetParam;
import com.apitable.widget.vo.GlobalWidgetInfo;

/**
 * <p>
 * vika sdk interface
 * </p>
 *
 */
public interface VikaOperations {

    /**
     * Get GM permission configuration information
     *
     * @param dstId datasheet id
     * @return GMPermission information
     */
    List<GmPermissionInfo> getGmPermissionConfiguration(String dstId);

    /**
     * Get Template Center Config Infos
     *
     * @param host  request host
     * @param token request bearer token
     * @param param config datasheet object
     * @return TemplateCenterConfigInfos
     */
    List<TemplateCenterConfigInfo> getTemplateCenterConfigInfos(String host, String token, TemplateConfigDatasheetParam param);

    /**
     * Get the global widget/widget template configuration information
     *
     * @param datasheetId datasheet id
     * @param viewId view id
     * @return config information
     */
    List<GlobalWidgetInfo> getGlobalWidgetPackageConfiguration(String datasheetId, String viewId);

    /**
     * Save DingTalk subscription information
     *
     * @param dstId datasheet id
     * @param subscriptionInfo Subscription information
     */
    void saveDingTalkSubscriptionInfo(String dstId, DingTalkSubscriptionInfo subscriptionInfo);

    /**
     * Get integral reward information
     *
     * @param host host
     * @param token token
     * @param dstId datasheet id
     * @param viewId view id
     * @return IntegralRewardInfo
     */
    List<IntegralRewardInfo> getIntegralRewardInfo(String host, String token, String dstId, String viewId);

    /**
     * Update integral reward result
     *
     * @param host host
     * @param token token
     * @param dstId datasheet id
     * @param recordId view id
     * @param result result
     * @param processor processor
     */
    void updateIntegralRewardResult(String host, String token, String dstId, String recordId, String result, String processor);

    /**
     * Synchronize orders
     *
     * @param order order
     * @param items order items
     * @param payments order payment details
     */
    void syncOrder(BillingOrder order, List<BillingOrderItem> items, List<BillingOrderPayment> payments);

    /**
     * get user's id from datasheet
     *
     * @param host        host
     * @param datasheetId datasheet's id
     * @param viewId      view's id
     * @param token       api token
     * @return UserContactInfo    recordId and user's id
     */
    List<UserContactInfo> getUserIdFromDatasheet(String host, String datasheetId, String viewId, String token);

    /**
     * write back user's contact info
     *
     * @param host        host
     * @param token       token
     * @param dstId       datasheet's id
     * @param userContactInfo user's contact information
     */
    void writeBackUserContactInfo(String host, String token, String dstId, UserContactInfo userContactInfo);
}
