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

package com.apitable.enterprise.social.event.dingtalk;

import static com.apitable.enterprise.social.notification.SocialNotificationManagement.DINGTALK_ENTRY_URL;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.social.service.IDingTalkService;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.workspace.observer.remind.NotifyDataSheetMeta;
import com.vikadata.social.dingtalk.message.ActionCardMessage;
import com.vikadata.social.dingtalk.message.Message;
import com.vikadata.social.dingtalk.message.element.BtnActionCard;
import com.vikadata.social.dingtalk.message.element.BtnActionCard.BtnJson;
import com.vikadata.social.dingtalk.message.element.SingleActionCard;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * Message Card Factory.
 * </p>
 */
public class DingTalkCardFactory {
    public static final String DINGTALK_OA_OPEN =
        "dingtalk://dingtalkclient/action/openapp?corpid={}&container_type=work_platform&app_id=0_{}&redirect_type=jump&redirect_url={}";

    public static Message createEntryCardMsg(String agentId) {
        ConstProperties constProperties = SpringContextHolder.getBean(ConstProperties.class);
        IDingTalkService dingTalkService = SpringContextHolder.getBean(IDingTalkService.class);
        String corpId = dingTalkService.getAgentAppById(agentId).getCorpId();
        BtnActionCard btnActionCard = new BtnActionCard();
        // Vertical arrangement
        btnActionCard.setBtnOrientation("0");
        List<BtnJson> btnJsonList = new ArrayList<>();
        String redirectUrl = StrUtil.format("{}/user/dingtalk_callback?corpId={}&agentId={}",
            constProperties.getServerDomain(), corpId, agentId);
        String useUrl =
            StrUtil.format(DINGTALK_OA_OPEN, corpId, agentId, URLUtil.encodeAll(redirectUrl));
        String helpUrl = StrUtil.format(DINGTALK_OA_OPEN, corpId, agentId,
            URLUtil.encodeAll("https://vika.cn/help/"));
        btnJsonList.add(BtnJson.builder().title("开始使用").actionUrl(useUrl).build());
        btnJsonList.add(BtnJson.builder().title("查看帮助").actionUrl(helpUrl).build());
        btnActionCard.setBtnJsonList(btnJsonList);
        btnActionCard.setTitle("开始使用");
        btnActionCard.setMarkdown(
            "![](https://s1.vika.cn/space/2021/06/29/9487bd92778c49748aebb74da45c9c8d)  \n  "
                + "### 🎉欢迎使用维格表  \n  "
                + "在这里，你可以收到来自维格表的成员通知、评论通知。也能够进入维格表协同工作、查看信息，随时掌握空间站动态。  \n  "
                + "新一代的团队数据协作与项目管理工具");
        return new ActionCardMessage(btnActionCard);
    }

    public static Message createSeatsOverLimitCardMsg(String agentId) {
        ConstProperties constProperties = SpringContextHolder.getBean(ConstProperties.class);
        IDingTalkService dingTalkService = SpringContextHolder.getBean(IDingTalkService.class);
        String corpId = dingTalkService.getAgentAppById(agentId).getCorpId();
        SingleActionCard singleActionCard = new SingleActionCard();
        singleActionCard.setTitle("🔔同步通讯录失败");
        String redirectUrl = StrUtil.format("{}/user/dingtalk_callback?corpId={}&agentId={}",
                constProperties.getServerDomain(), corpId, agentId);
        String useUrl =
                StrUtil.format(DINGTALK_OA_OPEN, corpId, agentId, URLUtil.encodeAll(redirectUrl));
        String pattern = DatePattern.CHINESE_DATE_PATTERN + " " + DatePattern.NORM_TIME_PATTERN;
        String operateAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        String markdownTextTmpl = "### 🔔席位数量已达上限，请升级重试。  \n  "
                + "**操作时间:** <font color=black>%s</font>";
        singleActionCard.setMarkdown(
                String.format(markdownTextTmpl,operateAt));
        return new ActionCardMessage(singleActionCard);
    }

    public static Message createRecordRemindMemberCardMsg(String agentId, String recordTitle,
                                                          String memberName,
                                                          String nodeName, String uri) {
        IDingTalkService dingTalkService = SpringContextHolder.getBean(IDingTalkService.class);
        String corpId = dingTalkService.getAgentAppById(agentId).getCorpId();
        ConstProperties constProperties = SpringContextHolder.getBean(ConstProperties.class);
        SingleActionCard singleActionCard = new SingleActionCard();
        singleActionCard.setTitle("🔔成员通知");
        singleActionCard.setSingleTitle("进入查看");
        String entryUrl =
            StrUtil.format(DINGTALK_ENTRY_URL, constProperties.getServerDomain(), corpId, agentId);
        String url = entryUrl.concat("&reference=" + constProperties.getServerDomain() + uri);
        singleActionCard.setSingleUrl(StrUtil.format(DINGTALK_OA_OPEN, corpId, agentId,
            URLUtil.encodeAll(url)));
        String pattern = DatePattern.CHINESE_DATE_PATTERN + " " + DatePattern.NORM_TIME_PATTERN;
        String operateAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        String markdownTextTmpl = "### 🔔有人在记录中提及你  \n  "
            + "**记录:** <font color=black>%s</font>  \n  "
            + "**提及人:** <font color=black>%s</font>  \n  "
            + "**维格表:** <font color=black>%s</font>  \n  "
            + "**操作时间:** <font color=black>%s</font>";
        singleActionCard.setMarkdown(
            String.format(markdownTextTmpl, recordTitle, memberName, nodeName, operateAt));
        return new ActionCardMessage(singleActionCard);
    }

    public static Message createCommentRemindCardMsg(String agentId, String recordTitle,
                                                     String commentContent,
                                                     String memberName, String nodeName,
                                                     String uri) {
        ConstProperties constProperties = SpringContextHolder.getBean(ConstProperties.class);
        IDingTalkService dingTalkService = SpringContextHolder.getBean(IDingTalkService.class);
        String corpId = dingTalkService.getAgentAppById(agentId).getCorpId();
        SingleActionCard singleActionCard = new SingleActionCard();
        singleActionCard.setTitle("🔔评论通知");
        singleActionCard.setSingleTitle("进入查看");
        String entryUrl =
            StrUtil.format(DINGTALK_ENTRY_URL, constProperties.getServerDomain(), corpId, agentId);
        String url = entryUrl.concat("&reference=" + constProperties.getServerDomain() + uri);
        singleActionCard.setSingleUrl(StrUtil.format(DINGTALK_OA_OPEN, corpId, agentId,
            URLUtil.encodeAll(url)));
        String pattern = DatePattern.CHINESE_DATE_PATTERN + " " + DatePattern.NORM_TIME_PATTERN;
        String operateAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        String markdownTextTmpl = "### 🔔有人在评论中@你  \n  "
            + "**记录:** <font color=black>%s</font>  \n  "
            + "**内容:** <font color=black>%s</font>  \n  "
            + "**评论人:** <font color=black>%s</font>  \n  "
            + "**维格表:** <font color=black>%s</font>  \n  "
            + "**操作时间:** <font color=black>%s</font>";
        singleActionCard.setMarkdown(
            String.format(markdownTextTmpl, recordTitle, commentContent, memberName, nodeName,
                operateAt));
        return new ActionCardMessage(singleActionCard);
    }

    public static HashMap<String, String> createIsvEntryCardData(String suiteId, String authCorpId,
                                                                 String appId) {
        String redirectUrl =
            StrUtil.format("/user/dingtalk/social_bind_space?corpId={}&suiteId={}", authCorpId,
                suiteId);
        HashMap<String, String> data = new HashMap<>();
        data.put("domain", getServerDomain());
        data.put("appId", appId);
        data.put("corpId", authCorpId);
        data.put("suiteId", suiteId);
        data.put("redirectUrl", getIsvPcRedirectUrl(redirectUrl));
        return data;
    }

    public static HashMap<String, String> createIsvRecordRemindMemberData(String authCorpId,
                                                                          String appId,
                                                                          NotifyDataSheetMeta meta,
                                                                          String memberName,
                                                                          String nodeName,
                                                                          String uri) {
        HashMap<String, String> data = new HashMap<>();
        data.put("corpId", authCorpId);
        data.put("appId", appId);
        data.put("redirectUrl", getIsvPcRedirectUrl(uri));
        data.put("domain", getServerDomain());
        data.put("nodeId", meta.getNodeId());
        data.put("viewId", meta.getViewId());
        data.put("recordId", meta.getRecordId());
        data.put("notifyId", StrUtil.blankToDefault(meta.getNotifyId(), ""));
        data.put("recordTitle", meta.getRecordTitle());
        data.put("memberName", memberName);
        data.put("nodeName", nodeName);
        data.put("operateAt", meta.getCreatedAt());
        return data;
    }

    public static HashMap<String, String> createIsvCommentRemindData(String authCorpId,
                                                                     String appId,
                                                                     NotifyDataSheetMeta meta,
                                                                     String memberName,
                                                                     String nodeName,
                                                                     String commentContent,
                                                                     String uri) {
        HashMap<String, String> data =
            createIsvRecordRemindMemberData(authCorpId, appId, meta, memberName, nodeName, uri);
        data.put("commentContent", commentContent);
        return data;
    }

    public static String getIsvPcRedirectUrl(String uri) {
        ConstProperties constProperties = SpringContextHolder.getBean(ConstProperties.class);
        return URLUtil.encodeAll(constProperties.getServerDomain() + uri);
    }

    public static String getServerDomain() {
        ConstProperties constProperties = SpringContextHolder.getBean(ConstProperties.class);
        return URLUtil.url(constProperties.getServerDomain()).getHost();
    }
}
