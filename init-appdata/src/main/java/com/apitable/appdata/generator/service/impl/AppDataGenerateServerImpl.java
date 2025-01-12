package com.apitable.appdata.generator.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.appdata.generator.config.properties.GeneratorProperties;
import com.apitable.appdata.generator.config.properties.InitSettingOssProperties;
import com.apitable.appdata.generator.service.IAppDataGenerateServer;
import com.apitable.appdata.shared.automation.model.AutomationDataPack;
import com.apitable.appdata.shared.automation.pojo.AutomationService;
import com.apitable.appdata.shared.base.enums.LabFeatureScope;
import com.apitable.appdata.shared.base.enums.LabFeatureType;
import com.apitable.appdata.shared.base.pojo.LabsFeatures;
import com.apitable.appdata.shared.base.pojo.SystemConfig;
import com.apitable.appdata.shared.starter.api.ApiTemplate;
import com.apitable.appdata.shared.starter.api.model.AttachmentField;
import com.apitable.appdata.shared.starter.api.model.ConfigDatasheet;
import com.apitable.appdata.shared.starter.api.model.SettingOssInfo;
import com.apitable.appdata.shared.starter.oss.OssProperties;
import com.apitable.appdata.shared.template.model.TemplateCenterConfigInfo;
import com.apitable.appdata.shared.template.model.TemplateCenterDataPack;
import com.apitable.appdata.shared.template.server.ITemplateService;
import com.apitable.appdata.shared.util.FilePlusUtil;
import com.apitable.appdata.shared.widget.model.WidgetCenterConfigInfo;
import com.apitable.appdata.shared.widget.model.WidgetCenterDataPack;
import com.apitable.appdata.shared.widget.service.IWidgetPackageService;
import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AppDataGenerateServerImpl implements IAppDataGenerateServer {

    @Resource
    private ITemplateService iTemplateService;

    @Resource
    private IWidgetPackageService iWidgetPackageService;

    @Resource
    private InitSettingOssProperties initSettingOssProperties;

    @Resource
    private GeneratorProperties properties;

    @Autowired(required = false)
    private ApiTemplate apiTemplate;

    @Resource
    private OssProperties ossProperties;

    @Override
    public void generate() {
        FilePlusUtil.clean();
        Map<String, String> tokenToMimeTypeMap = new HashMap<>();

        this.generateTemplateCenter(tokenToMimeTypeMap);
        this.generateWidgetCenter(tokenToMimeTypeMap);
        this.generateAutomation(tokenToMimeTypeMap);
        this.generateLabFeature();
        this.generateWizardConfig();

        // Involving resource coverage, put it at the end
        this.initSettingOssProcess(tokenToMimeTypeMap);

        FilePlusUtil.writeDataFile(JSONUtil.parseObj(tokenToMimeTypeMap).toString(), FilePlusUtil.ASSET_INFO);
        log.info("Generate finish.");
    }

    private void generateTemplateCenter(Map<String, String> tokenToMimeTypeMap) {
        if (properties.isSkipTemplateCenter()) {
            log.info("Skip generating template central data..");
            return;
        }
        // Read template center configuration
        log.info("Begin to read the configuration of the template center.");
        List<TemplateCenterConfigInfo> configInfo =
            this.readSysConfigMap("sysconfig/template_center.json", TemplateCenterConfigInfo.class);
        if (configInfo.isEmpty()) {
            log.info("None any language template center.");
            return;
        }

        // Get template center data
        log.info("Begin to get the data pack of the template center.");
        String templateSpaceId = properties.getTemplateSpaceId();
        TemplateCenterDataPack templateCenterDataPack =
            iTemplateService.getTemplateCenterDataPack(templateSpaceId, configInfo);

        log.info("Begin to write data file about the template center.");
        templateCenterDataPack.getNodeDataPacks()
            .forEach(nodeDataPack -> FilePlusUtil.writeNodeDataFile(JSONUtil.parseObj(nodeDataPack).toString()));
        templateCenterDataPack.setNodeDataPacks(null);
        FilePlusUtil.writeDataFile(JSONUtil.parseObj(templateCenterDataPack).toString(), FilePlusUtil.TEMPLATE_FILE_NAME);

        log.info("Begin to write asset file about the template center.");
        templateCenterDataPack.getAssets().forEach(asset -> {
            this.writeAssetFileAndRecordContentType(asset.getFileUrl(), tokenToMimeTypeMap);
            if (StrUtil.isNotBlank(asset.getPreview())) {
                this.writeAssetFileAndRecordContentType(asset.getPreview(), tokenToMimeTypeMap);
            }
        });
    }

    private void generateWidgetCenter(Map<String, String> tokenToMimeTypeMap) {
        if (properties.isSkipWidgetCenter()) {
            log.info("Skip generating widget central data..");
            return;
        }
        log.info("Begin to read the configuration of the widget center.");
        List<WidgetCenterConfigInfo> infos =
            this.readSysConfigMap("sysconfig/widget_center.json",
                WidgetCenterConfigInfo.class);
        if (infos.isEmpty()) {
            log.info("None any widget.");
            return;
        }
        log.info("Begin to get the data pack of the widget center.");
        WidgetCenterDataPack widgetCenterDataPack =
            iWidgetPackageService.getWidgetCenterDataPack(infos);

        log.info("Begin to write asset file about the widget center.");
        widgetCenterDataPack.getAssetTokens()
            .forEach(token -> this.writeAssetFileAndRecordContentType(token, tokenToMimeTypeMap));
        widgetCenterDataPack.setAssetTokens(null);
        log.info("Begin to write data file about the widget center.");
        FilePlusUtil.writeDataFile(JSONUtil.parseObj(widgetCenterDataPack).toString(),
            FilePlusUtil.WIDGET_PACKAGE_FILE_NAME);
    }

    private void generateAutomation(Map<String, String> tokenToMimeTypeMap) {
        if (properties.isSkipAutomation()) {
            log.info("Skip generating automation data..");
            return;
        }
        log.info("Begin to read the configuration of the automation.");
        AutomationDataPack dataPack =
            this.readSysConfig("sysconfig/automation.json", AutomationDataPack.class);

        log.info("Begin to write data file about the automation.");
        FilePlusUtil.writeDataFile(JSONUtil.parseObj(dataPack).toString(),
            FilePlusUtil.AUTOMATION_FILE_NAME);

        log.info("Begin to write asset file about the automation.");
        dataPack.getServices().stream().map(AutomationService::getLogo).filter(StrUtil::isNotBlank)
            .forEach(i -> {
                Map<String, String> map = JSONUtil.toBean(i, Map.class);
                map.values()
                    .forEach(v -> this.writeAssetFileAndRecordContentType(v, tokenToMimeTypeMap));
            });
    }

    private void generateLabFeature() {
        if (properties.isSkipLabFeature()) {
            log.info("Skip generating lab feature data..");
            return;
        }
        List<LabsFeatures> labsFeatures = new ArrayList<>();
        List<Object> objects =
            this.readSysConfigMap("sysconfig/lab_feature.json", Object.class);
        for (Object value : objects) {
            if (value == null || !JSONUtil.isTypeJSONObject(value.toString())) {
                continue;
            }
            JSONObject jsonObject = JSONUtil.parseObj(value);
            LabsFeatures feature = new LabsFeatures();
            feature.setFeatureKey(MapUtil.getStr(jsonObject, "feature_key"));
            feature.setFeatureScope(
                LabFeatureScope.of(MapUtil.getStr(jsonObject, "feature_scope")).getScopeCode());
            feature.setType(LabFeatureType.of(MapUtil.getStr(jsonObject, "type")).getType());
            feature.setUrl(MapUtil.getStr(jsonObject, "url"));
            labsFeatures.add(feature);
        }
        log.info("Begin to write data file about lab feature.");
        FilePlusUtil.writeDataFile(JSONUtil.parseArray(labsFeatures).toString(),
            FilePlusUtil.LAB_FEATURE_FILE_NAME);
    }

    private void generateWizardConfig() {
        if (properties.isSkipWizard()) {
            log.info("Skip generating wizard config data..");
            return;
        }
        List<SystemConfig> wizardConfigMap =
            this.readSysConfigMap("sysconfig/wizard.json", SystemConfig.class);
        log.info("Begin to write data file about wizard config.");
        FilePlusUtil.writeDataFile(JSONUtil.parseArray(wizardConfigMap).toString(),
            FilePlusUtil.WIZARD_CONFIG_FILE_NAME);
    }

    private <T> List<T> readSysConfigMap(String path, Class<T> valueType) {
        Map infoMap = this.readSysConfig(path, Map.class);
        List<T> list = new ArrayList<>();
        for (Object v : infoMap.values()) {
            list.add(JSONUtil.parseObj(v).toBean(valueType));
        }
        return list;
    }

    private <T> T readSysConfig(String path, Class<T> valueType) {
        BufferedReader bufferedReader = ResourceUtil.getUtf8Reader(path);
        try {
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
            return JSONUtil.parseObj(content.toString()).toBean(valueType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load system configuration!", e);
        }
    }

    private void initSettingOssProcess(Map<String, String> tokenToMimeTypeMap) {
        if (initSettingOssProperties.isSkip()) {
            log.info("Skip init setting oss data..");
            return;
        }
        String host = initSettingOssProperties.getHost();
        String token = initSettingOssProperties.getToken();
        ConfigDatasheet customization = initSettingOssProperties.getCustomization();
        Map<String, SettingOssInfo> customizationOssInfoMap = new HashMap<>();
        if (customization != null && StrUtil.isNotBlank(customization.getDatasheetId())) {
            log.info("Begin to read the configuration datasheet of customization oss.");
            List<SettingOssInfo> settingCustomizationOss = apiTemplate.getSettingCustomizationOss(host, token, customization.getDatasheetId(), customization.getViewId());
            for (SettingOssInfo info : settingCustomizationOss) {
                customizationOssInfoMap.put(info.getOverrideKey(), info);
            }
        }

        log.info("Begin to read the configuration datasheet of base oss.");
        ConfigDatasheet base = initSettingOssProperties.getBase();
        List<AttachmentField> settingBaseOss = apiTemplate.getSettingBaseOss(host, token, base.getDatasheetId(), base.getViewId());

        log.info("Begin to write asset file about base oss.");
        for (AttachmentField attachment : settingBaseOss) {
            String relativePath = attachment.getToken();
            if (customizationOssInfoMap.containsKey(relativePath)) {
                SettingOssInfo info = customizationOssInfoMap.get(relativePath);
                FilePlusUtil.writeAssetFileAndReturnContentType(properties.getOssHost(), info.getOriginAssetToken(), relativePath, ossProperties.getSignature());
                tokenToMimeTypeMap.put(relativePath, info.getMimeType());
                customizationOssInfoMap.remove(relativePath);
                continue;
            }
            this.writeAssetFileAndRecordContentType(relativePath, tokenToMimeTypeMap);
        }

        if (customizationOssInfoMap.isEmpty()) {
            return;
        }
        log.info("Begin to write additional asset file about customization oss.");
        customizationOssInfoMap.values().forEach(v -> {
            FilePlusUtil.writeAssetFileAndReturnContentType(properties.getOssHost(), v.getOriginAssetToken(), v.getOverrideKey(), ossProperties.getSignature());
            tokenToMimeTypeMap.put(StrUtil.isNotBlank(v.getOverrideKey()) ? v.getOverrideKey() : v.getOriginAssetToken(), v.getMimeType());
        });
    }

    private void writeAssetFileAndRecordContentType(String relativePath, Map<String, String> tokenToMimeTypeMap) {
        String contentType = FilePlusUtil.writeAssetFileAndReturnContentType(properties.getOssHost(), relativePath, ossProperties.getSignature());
        if (contentType != null) {
            tokenToMimeTypeMap.put(relativePath, contentType);
        }
    }
}
