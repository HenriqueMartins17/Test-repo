package com.apitable.appdata.shared.starter.api;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.vika.client.api.VikaApiClient;
import cn.vika.client.api.model.ApiQueryParam;
import cn.vika.client.api.model.Record;
import com.apitable.appdata.shared.starter.api.model.AttachmentField;
import com.apitable.appdata.shared.starter.api.model.SettingOssInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiTemplate extends ApiAccessor {
    private static final Integer MAX_PAGE_SIZE = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiTemplate.class);

    public ApiTemplate(String hostUrl, String token) {
        super(hostUrl, token);
    }

    public List<AttachmentField> getSettingBaseOss(String host, String token, String datasheetId, String viewId) {
        VikaApiClient client = this.getClient(host, token);
        // build query param
        ApiQueryParam queryParam = new ApiQueryParam(1, MAX_PAGE_SIZE)
                .withFields(Collections.singletonList("FILE"))
                .withView(viewId);
        // get all records
        List<Record> records = client.getRecordApi().getRecords(datasheetId, queryParam).all();
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        List<AttachmentField> attachments = new ArrayList<>();
        for (Record record : records) {
            Map<String, Object> fields = record.getFields();
            if (fields == null || fields.isEmpty() || !fields.containsKey("FILE")) {
                continue;
            }
            attachments.addAll(JSONUtil.parseArray(fields.get("FILE")).toList(AttachmentField.class));
        }
        return attachments;
    }

    public List<SettingOssInfo> getSettingCustomizationOss(String host, String token, String datasheetId, String viewId) {
        VikaApiClient client = this.getClient(host, token);
        // build query param
        ApiQueryParam queryParam = new ApiQueryParam(1, MAX_PAGE_SIZE).withView(viewId);
        // get all records
        List<Record> records = client.getRecordApi().getRecords(datasheetId, queryParam).all();
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        List<SettingOssInfo> settingOssInfos = new ArrayList<>();
        for (Record record : records) {
            Map<String, Object> fields = record.getFields();
            if (fields == null || fields.isEmpty()) {
                continue;
            }
            AttachmentField attachmentField = this.getFirstAttachment(fields, "FILE");
            if (attachmentField == null) {
                continue;
            }
            SettingOssInfo info = new SettingOssInfo();
            info.setOriginAssetToken(attachmentField.getToken());
            info.setMimeType(attachmentField.getMimeType());
            if (fields.containsKey("OVERRIDDEN_KEY")) {
                String overriddenKey = MapUtil.getStr(fields, "OVERRIDDEN_KEY");
                info.setOverrideKey(StrUtil.removePrefix(overriddenKey.trim(), "/"));
            }
            settingOssInfos.add(info);
        }
        return settingOssInfos;
    }

    private AttachmentField getFirstAttachment(Map<String, Object> fields, String key) {
        if (fields.containsKey(key)) {
            List<AttachmentField> values = Convert.toList(AttachmentField.class, MapUtil.get(fields, key, List.class));
            if (!values.isEmpty()) {
                return values.stream().findFirst().get();
            }
        }
        return null;
    }

}
