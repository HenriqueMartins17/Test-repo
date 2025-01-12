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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.vika.client.api.VikaApiClient;
import cn.vika.client.api.exception.ApiException;
import cn.vika.client.api.model.ApiQueryParam;
import cn.vika.client.api.model.CellFormat;
import cn.vika.client.api.model.CreateRecordRequest;
import cn.vika.client.api.model.FieldKey;
import cn.vika.client.api.model.Pager;
import cn.vika.client.api.model.Record;
import cn.vika.client.api.model.RecordMap;
import cn.vika.client.api.model.UpdateRecord;
import cn.vika.client.api.model.UpdateRecordRequest;
import cn.vika.core.utils.JacksonConverter;
import com.apitable.enterprise.vika.core.model.BillingOrder;
import com.apitable.enterprise.vika.core.model.BillingOrderItem;
import com.apitable.enterprise.vika.core.model.BillingOrderPayment;
import com.apitable.enterprise.vika.core.model.DingTalkSubscriptionInfo;
import com.apitable.enterprise.vika.core.model.GmPermissionInfo;
import com.apitable.enterprise.vika.core.model.IntegralRewardInfo;
import com.apitable.enterprise.vika.core.model.RecommendTemplateInfo;
import com.apitable.enterprise.vika.core.model.UserContactInfo;
import com.apitable.enterprise.vika.core.model.template.RecommendInfo;
import com.apitable.enterprise.vika.core.model.template.RecommendInfo.AlbumGroup;
import com.apitable.enterprise.vika.core.model.template.RecommendInfo.BannerInfo;
import com.apitable.enterprise.vika.core.model.template.RecommendInfo.TemplateGroup;
import com.apitable.enterprise.vika.core.model.template.Template;
import com.apitable.enterprise.vika.core.model.template.TemplateAlbum;
import com.apitable.enterprise.vika.core.model.template.TemplateCategory;
import com.apitable.enterprise.vika.core.model.template.TemplateCenterConfigInfo;
import com.apitable.enterprise.vika.core.model.template.TemplateConfigDatasheetParam;
import com.apitable.widget.vo.GlobalWidgetInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * vika sdk implementation class
 * </p>
 *
 */
public class VikaTemplate extends VikaAccessor implements VikaOperations {
    private static final Integer MAX_PAGE_SIZE = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(VikaTemplate.class);

    public VikaTemplate(String hostUrl, String token) {
        super(hostUrl, token);
    }

    @Override
    public List<GmPermissionInfo> getGmPermissionConfiguration(String dstId) {
        LOGGER.info("Get GM Permission Configuration Information");
        String resource = "ACTION";
        String role = "PERMISSION_UNIT";
        // build query criteria
        ApiQueryParam queryParam = new ApiQueryParam()
                .withFields(Arrays.asList(resource, role))
                .withFilter("{ShouldUpdate} = 1");
        // query results
        Pager<Record> records = this.getClient().getRecordApi().getRecords(dstId, queryParam);
        List<GmPermissionInfo> infos = new ArrayList<>(records.getTotalItems());
        while (records.hasNext()) {
            for (Record record : records.next()) {
                // get unit ids
                JSONArray jsonArray = JSONUtil.parseArray(record.getFields().get(role));
                List<Long> unitIds = new ArrayList<>();
                jsonArray.jsonIter().forEach(unit -> unitIds.add(unit.getLong("id")));
                // build information
                GmPermissionInfo info = new GmPermissionInfo(record.getFields().get(resource).toString(), unitIds);
                infos.add(info);
            }
        }
        return infos;
    }

    @Override
    public List<TemplateCenterConfigInfo> getTemplateCenterConfigInfos(String host, String token, TemplateConfigDatasheetParam param) {
        List<TemplateCenterConfigInfo> infos = new ArrayList<>();
        // get template config
        VikaApiClient client = this.getClient(host, token);
        Map<String, RecommendInfo> i18nToRecommendMap = this.getI18nToRecommendMap(client, param.getRecommendDatasheetId(), param.getRecommendViewId());
        Map<String, List<TemplateCategory>> i18nToCategoriesMap = this.getI18nToCategoriesMap(client, param.getCategoryDatasheetId(), param.getCategoryViewId());
        Map<String, List<TemplateAlbum>> i18nToAlbumsMap = this.getI18nToAlbumsMap(client, param.getAlbumDatasheetId(), param.getAlbumViewId());
        Map<String, List<Template>> i18nToTemplatesMap = this.getI18nToTemplatesMap(client, param.getTemplateDatasheetId(), param.getTemplateViewId());
        // build config info
        for (Entry<String, RecommendInfo> entry : i18nToRecommendMap.entrySet()) {
            TemplateCenterConfigInfo info = new TemplateCenterConfigInfo();
            String i18n = entry.getKey();
            info.setI18n(i18n);
            info.setRecommend(entry.getValue());
            info.setTemplateCategories(i18nToCategoriesMap.get(i18n));
            info.setAlbums(i18nToAlbumsMap.get(i18n));
            info.setTemplate(i18nToTemplatesMap.get(i18n));
            infos.add(info);
        }
        return infos;
    }

    private Map<String, RecommendInfo> getI18nToRecommendMap(VikaApiClient client, String datasheetId, String viewId) {
        // build query param
        ApiQueryParam queryParam = new ApiQueryParam(1, MAX_PAGE_SIZE)
                .withFilter("{CHECK} = 1")
                .withView(viewId);
        // get all records
        List<Record> records = client.getRecordApi().getRecords(datasheetId, queryParam).all();
        // convert to object
        ObjectMapper mapper = new ObjectMapper();
        List<RecommendTemplateInfo> infos = records.stream()
                .filter(record -> record.getFields() != null && !record.getFields().isEmpty())
                .map(record -> mapper.convertValue(record.getFields(), RecommendTemplateInfo.class))
                .collect(Collectors.toList());

        // build i18n to recommend map
        Map<String, List<RecommendTemplateInfo>> i18nToInfosMap = infos.stream().collect(Collectors.groupingBy(RecommendTemplateInfo::getI18n));
        Map<String, RecommendInfo> i18nToRecommendMap = new HashMap<>(i18nToInfosMap.size());
        for (Entry<String, List<RecommendTemplateInfo>> i18nEntry : i18nToInfosMap.entrySet()) {
            // build top banner info
            List<BannerInfo> top = i18nEntry.getValue().stream().filter(info -> info.getLayout().equals("BANNER"))
                    .map(info -> new BannerInfo(info.getSubjectValue(), info.getBanners().get(0).getToken(), info.getTitle(), info.getDescription(), info.getColor()))
                    .collect(Collectors.toList());

            // build album/template custom group
            List<AlbumGroup> albumGroups = new ArrayList<>();
            List<TemplateGroup> templateGroups = new ArrayList<>();
            Map<String, List<RecommendTemplateInfo>> layoutToInfosMap = i18nEntry.getValue().stream().filter(info -> !info.getLayout().equals("BANNER"))
                    .collect(Collectors.groupingBy(RecommendTemplateInfo::getLayout));
            for (Entry<String, List<RecommendTemplateInfo>> layoutEntry : layoutToInfosMap.entrySet()) {
                Map<String, List<String>> groupToSubjectsMap = layoutEntry.getValue().stream()
                        .collect(Collectors.groupingBy(RecommendTemplateInfo::getCustomGroup, Collectors.mapping(RecommendTemplateInfo::getSubjectValue, Collectors.toList())));
                switch (layoutEntry.getKey()) {
                    case "ALBUM_GROUP":
                        groupToSubjectsMap.forEach((k, v) -> albumGroups.add(new AlbumGroup(k, v)));
                        break;
                    case "TEMPLATE_GROUP":
                        groupToSubjectsMap.forEach((k, v) -> templateGroups.add(new TemplateGroup(k, v)));
                        break;
                    default:
                        break;
                }
            }
            i18nToRecommendMap.put(i18nEntry.getKey(), new RecommendInfo(top, albumGroups, templateGroups));
        }
        return i18nToRecommendMap;
    }

    private Map<String, List<TemplateCategory>> getI18nToCategoriesMap(VikaApiClient client, String datasheetId, String viewId) {
        // build query param
        ApiQueryParam queryParam = new ApiQueryParam(1, MAX_PAGE_SIZE)
                .withCellFormat(CellFormat.STRING)
                .withFilter("{CHECK} = 1")
                .withView(viewId);
        // get all records
        List<Record> records = client.getRecordApi().getRecords(datasheetId, queryParam).all();

        // build i18n to categories map
        Map<String, List<TemplateCategory>> i18nToCategoriesMap = new HashMap<>();
        for (Record record : records) {
            Map<String, Object> fields = record.getFields();
            if (fields == null || fields.isEmpty()) {
                continue;
            }
            String i18n = MapUtil.getStr(fields, "i18n");
            TemplateCategory category = new TemplateCategory();
            category.setName(MapUtil.getStr(fields, "TEMPLATE_CATEGORY"));
            if (fields.containsKey("TEMPLATE_ALBUM")) {
                category.setAlbumNames(Arrays.asList(MapUtil.getStr(fields, "TEMPLATE_ALBUM").split(", ")));
            }
            if (fields.containsKey("TEMPLATE")) {
                category.setTemplateNames(Arrays.asList(MapUtil.getStr(fields, "TEMPLATE").split(", ")));
            }
            if (i18nToCategoriesMap.containsKey(i18n)) {
                i18nToCategoriesMap.get(i18n).add(category);
            }
            else {
                List<TemplateCategory> categories = new ArrayList<>();
                categories.add(category);
                i18nToCategoriesMap.put(i18n, categories);
            }
        }
        return i18nToCategoriesMap;
    }

    private Map<String, List<TemplateAlbum>> getI18nToAlbumsMap(VikaApiClient client, String datasheetId, String viewId) {
        // build query param
        ApiQueryParam queryParam = new ApiQueryParam(1, MAX_PAGE_SIZE)
                .withCellFormat(CellFormat.STRING)
                .withFilter("{SHELF_STATUS} = 1")
                .withView(viewId);
        // get all records
        List<Record> records = client.getRecordApi().getRecords(datasheetId, queryParam).all();

        // build i18n to albums map
        Map<String, List<TemplateAlbum>> i18nToAlbumsMap = new HashMap<>();
        for (Record record : records) {
            Map<String, Object> fields = record.getFields();
            if (fields == null || fields.isEmpty()) {
                continue;
            }
            String i18n = MapUtil.getStr(fields, "i18n");
            TemplateAlbum album = new TemplateAlbum();
            album.setName(MapUtil.getStr(fields, "ALBUM_NAME"));
            album.setCover(Utils.getRelativePath(MapUtil.getStr(fields, "ALBUM_COVER")));
            album.setDescription(MapUtil.getStr(fields, "ALBUM_DESC", ""));
            album.setContent(MapUtil.getStr(fields, "ALBUM_CONTENT", "{}"));
            album.setPublisherName(MapUtil.getStr(fields, "PUBLISHER_NAME"));
            album.setPublisherLogo(Utils.getRelativePath(MapUtil.getStr(fields, "PUBLISHER_LOGO")));
            album.setPublisherDesc(MapUtil.getStr(fields, "PUBLISHER_DESC"));
            if (fields.containsKey("TEMPLATE")) {
                album.setTemplateNames(Arrays.asList(MapUtil.getStr(fields, "TEMPLATE").split(", ")));
            }
            if (fields.containsKey("TEMPLATE_TAG")) {
                album.setTemplateTags(Arrays.asList(MapUtil.getStr(fields, "TEMPLATE_TAG").split(", ")));
            }
            if (i18nToAlbumsMap.containsKey(i18n)) {
                i18nToAlbumsMap.get(i18n).add(album);
            }
            else {
                List<TemplateAlbum> albums = new ArrayList<>();
                albums.add(album);
                i18nToAlbumsMap.put(i18n, albums);
            }
        }
        return i18nToAlbumsMap;
    }

    private Map<String, List<Template>> getI18nToTemplatesMap(VikaApiClient client, String datasheetId, String viewId) {
        // build query param
        ApiQueryParam queryParam = new ApiQueryParam(1, MAX_PAGE_SIZE)
                .withCellFormat(CellFormat.STRING)
                .withFilter("{SHELF_STATUS} = 1")
                .withView(viewId);
        // get all records
        List<Record> records = client.getRecordApi().getRecords(datasheetId, queryParam).all();

        // build i18n to templates map
        Map<String, List<Template>> i18nToAlbumsMap = new HashMap<>();
        for (Record record : records) {
            Map<String, Object> fields = record.getFields();
            if (fields == null || fields.isEmpty()) {
                continue;
            }
            String i18n = MapUtil.getStr(fields, "i18n");
            Template template = new Template();
            template.setName(MapUtil.getStr(fields, "TEMPLATE_NAME"));
            if (fields.containsKey("TEMPLATE_TAG")) {
                template.setTemplateTags(Arrays.asList(MapUtil.getStr(fields, "TEMPLATE_TAG").split(", ")));
            }
            if (i18nToAlbumsMap.containsKey(i18n)) {
                i18nToAlbumsMap.get(i18n).add(template);
            }
            else {
                List<Template> templates = new ArrayList<>();
                templates.add(template);
                i18nToAlbumsMap.put(i18n, templates);
            }
        }
        return i18nToAlbumsMap;
    }

    @Override
    public List<GlobalWidgetInfo> getGlobalWidgetPackageConfiguration(String datasheetId, String viewId) {
        LOGGER.info("Get Widget Package Configuration Information");
        // query results
        List<GlobalWidgetInfo> result = new ArrayList<>();
        try {
            // build query criteria
            ApiQueryParam queryParam = new ApiQueryParam(1, MAX_PAGE_SIZE).withView(viewId).withCellFormat(CellFormat.STRING);

            Pager<Record> records = this.getClient().getRecordApi().getRecords(datasheetId, queryParam);
            int widgetSort = 1;
            while (records.hasNext()) {
                for (Record record : records.next()) {
                    Map<String, Object> fields = record.getFields();
                    if (fields != null) {
                        GlobalWidgetInfo globalWidgetInfo = new GlobalWidgetInfo();
                        globalWidgetInfo.setPackageId(MapUtil.getStr(fields, "PACKAGE_ID"));
                        globalWidgetInfo.setPackageName(MapUtil.getStr(fields, "PACKAGE_NAME"));
                        globalWidgetInfo.setIsEnabled(MapUtil.getBool(fields, "IS_ENABLED"));
                        globalWidgetInfo.setIsTemplate(MapUtil.getBool(fields, "IS_TEMPLATE"));
                        globalWidgetInfo.setVersion(MapUtil.getStr(fields, "VERSION"));
                        String openSourceAddress = MapUtil.getStr(fields, "WIDGET_OPEN_SOURCE");
                        if (StrUtil.isNotBlank(openSourceAddress)) {
                            globalWidgetInfo.setOpenSourceAddress(openSourceAddress);
                        }
                        String templateCover = MapUtil.getStr(fields, "TEMPLATE_COVER");
                        if (StrUtil.isNotBlank(templateCover)) {
                            // Get the image address of the relative path
                            globalWidgetInfo.setTemplateCover(StrUtil.removePrefix(URLUtil.getPath(ReUtil.getGroup1("\\((.+)\\)", templateCover)), "/"));
                        }
                        String website = MapUtil.getStr(fields, "WEBSITE");
                        if (StrUtil.isNotBlank(website)) {
                            globalWidgetInfo.setWebsite(website);
                        }
                        globalWidgetInfo.setWidgetSort(widgetSort);
                        result.add(globalWidgetInfo);
                    }
                    widgetSort++;
                }
            }
        }
        catch (ApiException e) {
            LOGGER.error("Exception in obtaining online template configuration", e);
        }
        return result;
    }

    @Override
    public void saveDingTalkSubscriptionInfo(String dstId, DingTalkSubscriptionInfo subscriptionInfo) {
        // put record map into fields name, warp record into array node
        ObjectNode fieldMap = JsonNodeFactory.instance.objectNode()
                .put("SPACE_ID", subscriptionInfo.getSpaceId())
                .put("SPACE_NAME", subscriptionInfo.getSpaceName())
                .put("ORDER_TYPE", subscriptionInfo.getOrderType())
                .put("GOODS_CODE", subscriptionInfo.getGoodsCode())
                .put("SUBSCRIPTION_TYPE", subscriptionInfo.getSubscriptionType())
                .put("SEAT", subscriptionInfo.getSeat())
                .put("SERVICE_START_TIME", subscriptionInfo.getServiceStartTime())
                .put("SERVICE_STOP_TIME", subscriptionInfo.getServiceStopTime())
                .put("ORDER_LABEL", subscriptionInfo.getOrderLabel())
                .put("DATA", subscriptionInfo.getData());
        ObjectNode fields = JsonNodeFactory.instance.objectNode().set("fields", fieldMap);
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode().add(fields);
        try {
            // convert json to Map List
            List<RecordMap> recordMaps = JacksonConverter.unmarshalToList(RecordMap.class, arrayNode);
            // create record request
            CreateRecordRequest recordRequest = new CreateRecordRequest().withRecords(recordMaps);
            this.getClient().getRecordApi().addRecords(dstId, recordRequest);
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<IntegralRewardInfo> getIntegralRewardInfo(String host, String token, String dstId, String viewId) {
        // build query criteria
        ApiQueryParam queryParam = new ApiQueryParam()
                .withView(viewId)
                .withFilter("{RULE} = 1");
        // query results
        Pager<Record> records = this.getClient(host, token).getRecordApi().getRecords(dstId, queryParam);
        List<IntegralRewardInfo> infos = new ArrayList<>(records.getTotalItems());
        while (records.hasNext()) {
            for (Record record : records.next()) {
                Map<String, Object> recordMap = record.getFields();
                // build information
                IntegralRewardInfo info = new IntegralRewardInfo();
                info.setRecordId(record.getRecordId());
                info.setAreaCode(MapUtil.getStr(recordMap, "AREA_CODE"));
                info.setTarget(MapUtil.getStr(recordMap, "TARGET"));
                info.setCount(MapUtil.getInt(recordMap, "COUNT"));
                info.setActivityName(MapUtil.getStr(recordMap, "ACTIVITY_NAME"));
                infos.add(info);
            }
        }
        return infos;
    }

    @Override
    public void updateIntegralRewardResult(String host, String token, String dstId, String recordId, String result, String processor) {
        UpdateRecord record = new UpdateRecord()
                .withRecordId(recordId)
                .withField("RESULT", result)
                .withField("PROCESS_TIME", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .withField("PROCESSOR", processor);
        UpdateRecordRequest updateRecordRequest = new UpdateRecordRequest()
                .withRecords(Collections.singletonList(record));
        this.getClient(host, token).getRecordApi().updateRecords(dstId, updateRecordRequest);
    }

    @Override
    public void syncOrder(BillingOrder order, List<BillingOrderItem> items, List<BillingOrderPayment> payments) {
        URI uri = URLUtil.toURI(getHostUrl());
        String env = Arrays.stream(uri.getHost().split("\\.")).iterator().next();
        JSONObject config = loadOrderConfig(env);
        // order list
        CreateRecordRequest orderRecord = new CreateRecordRequest()
                .withFieldKey(FieldKey.Name)
                .withRecords(Collections.singletonList(new RecordMap().withFields(JacksonConverter.toMap(order))));
        List<Record> records = getClient().getRecordApi().addRecords(config.getStr("order"), orderRecord);
        String recordId = records.iterator().next().getRecordId();

        // order detail
        List<RecordMap> orderItemRecordMaps = new ArrayList<>();
        items.forEach(item -> {
            item.setOrderIds(Collections.singletonList(recordId));
            orderItemRecordMaps.add(new RecordMap().withFields(JacksonConverter.toMap(item)));
        });
        getClient().getRecordApi().addRecords(config.getStr("order_item"), new CreateRecordRequest()
                .withFieldKey(FieldKey.Name)
                .withRecords(orderItemRecordMaps));

        // pay detail
        if (CollUtil.isEmpty(payments)) {
            return;
        }
        List<RecordMap> orderPaymentRecordMaps = new ArrayList<>();
        payments.forEach(item -> {
            item.setOrderIds(Collections.singletonList(recordId));
            orderPaymentRecordMaps.add(new RecordMap().withFields(JacksonConverter.toMap(item)));
        });
        getClient().getRecordApi().addRecords(config.getStr("order_payment"), new CreateRecordRequest()
                .withFieldKey(FieldKey.Name)
                .withRecords(orderPaymentRecordMaps));
    }

    @Override
    public List<UserContactInfo> getUserIdFromDatasheet(String host, String datasheetId, String viewId, String token) {
        // build return object
        List<UserContactInfo> userContactInfos = new ArrayList<>();
        // build query condition
        ApiQueryParam apiQueryParam = new ApiQueryParam()
                .withView(viewId);
        // read user's id from datasheet by vika api
        Pager<Record> records = this.getClient(host, token).getRecordApi().getRecords(datasheetId, apiQueryParam);
        while (records.hasNext()) {
            for (Record record : records.next()) {
                Map<String, Object> recordMap = record.getFields();
                UserContactInfo userContactInfo = new UserContactInfo();
                userContactInfo.setRecordId(record.getRecordId());
                userContactInfo.setUuid(MapUtil.getStr(recordMap, "USER_ID"));
                userContactInfos.add(userContactInfo);
            }
        }
        return userContactInfos;
    }

    @Override
    public void writeBackUserContactInfo(String host, String token, String dstId, UserContactInfo userContactInfo) {
        UpdateRecord updateRecord = new UpdateRecord()
                .withRecordId(userContactInfo.getRecordId())
                .withField("AREA_CODE", userContactInfo.getCode())
                .withField("MOBILE_PHONE", userContactInfo.getMobilePhone())
                .withField("EMAIL", userContactInfo.getEmail())
                .withField("STATUS", true);
        UpdateRecordRequest updateRecordRequest = new UpdateRecordRequest()
                .withRecords(Collections.singletonList(updateRecord));
        this.getClient(host, token).getRecordApi().updateRecords(dstId, updateRecordRequest);
    }

    private JSONObject loadOrderConfig(String env) {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/order.json");
        assert resourceAsStream != null;
        InputStreamReader reader = new InputStreamReader(resourceAsStream);
        String json = IoUtil.read(reader, true);
        return JSONUtil.parseObj(json).getJSONObject(env);
    }
}
