package com.apitable.appdata.loader.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.appdata.loader.config.properties.LoaderProperties;
import com.apitable.appdata.loader.config.properties.LoaderProperties.TemplateCenter;
import com.apitable.appdata.loader.config.properties.LoaderProperties.WidgetCenter;
import com.apitable.appdata.loader.service.IAppDataLoaderService;
import com.apitable.appdata.shared.automation.model.AutomationDataPack;
import com.apitable.appdata.shared.automation.service.IAutomationService;
import com.apitable.appdata.shared.base.pojo.LabsFeatures;
import com.apitable.appdata.shared.base.pojo.SystemConfig;
import com.apitable.appdata.shared.base.service.ILabFeatureService;
import com.apitable.appdata.shared.base.service.ISystemConfigService;
import com.apitable.appdata.shared.starter.oss.OssClientTemplate;
import com.apitable.appdata.shared.template.model.TemplateCenterDataPack;
import com.apitable.appdata.shared.template.server.ITemplateService;
import com.apitable.appdata.shared.util.FilePlusUtil;
import com.apitable.appdata.shared.widget.model.WidgetCenterDataPack;
import com.apitable.appdata.shared.widget.service.IWidgetPackageService;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AppDataLoaderServiceImpl implements IAppDataLoaderService {

    @Resource
    private ITemplateService iTemplateService;

    @Resource
    private IWidgetPackageService iWidgetPackageService;

    @Resource
    private IAutomationService iAutomationService;

    @Resource
    private ILabFeatureService iLabFeatureService;

    @Resource
    private ISystemConfigService iSystemConfigService;

    @Resource
    private LoaderProperties properties;

    @Autowired(required = false)
    private OssClientTemplate ossClientTemplate;

    @Override
    public void loadAsset() {
        if (properties.isSkipAssetLoad()) {
            log.info("Skip loading asset.");
            return;
        }
        List<File> files = FileUtil.loopFiles(FilePlusUtil.ASSET_DIR);
        if (files.isEmpty()) {
            log.warn("Asset file don't exist.");
            return;
        }
        File assetInfoFile = FileUtil.file(FilePlusUtil.getTemporaryFilePath(FilePlusUtil.ASSET_INFO));
        JSONObject assetInfoMap = JSONUtil.parseObj(FilePlusUtil.parseFileContent(assetInfoFile));
        for (File file : files) {
            String relativePath = FilePlusUtil.convertToRelativePath(file.getName());
            try {
                InputStream in = new FileInputStream(file);
                if (assetInfoMap.containsKey(relativePath)) {
                    ossClientTemplate.upload(properties.getOssBucketName(), in, relativePath, assetInfoMap.getStr(relativePath), null);
                    continue;
                }
                ossClientTemplate.upload(properties.getOssBucketName(), in, relativePath);
            }
            catch (IOException e) {
                log.warn("Asset[{}] upload failure. Message: {}", relativePath, e.getMessage());
            }
        }
        log.info("Loading asset finish.");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void loadData() {
        List<File> files = FileUtil.loopFiles(FilePlusUtil.DATA_DIR);
        if (files.isEmpty()) {
            log.warn("Data file don't exist.");
            return;
        }
        for (File file : files) {
            switch (file.getName()) {
                case FilePlusUtil.TEMPLATE_FILE_NAME:
                    TemplateCenter templateCenterConfig = properties.getTemplateCenter();
                    if (templateCenterConfig.isSkip()) {
                        log.info("Skip loading template center data.");
                        break;
                    }
                    String fileContent = FilePlusUtil.parseFileContent(file);
                    TemplateCenterDataPack dataPack = JSONUtil.parseObj(fileContent).toBean(TemplateCenterDataPack.class);
                    iTemplateService.parseTemplateCenterDataPack(
                        templateCenterConfig.getTemplateSpaceId(), dataPack,
                        templateCenterConfig.isSkipConfig());
                    log.info("Loading template center data finish.");
                    break;
                case FilePlusUtil.WIDGET_PACKAGE_FILE_NAME:
                    WidgetCenter widgetCenterConfig = properties.getWidgetCenter();
                    if (widgetCenterConfig.isSkip()) {
                        log.info("Skip loading widget center data.");
                        break;
                    }
                    String widgetPackageFile = FilePlusUtil.parseFileContent(file);
                    WidgetCenterDataPack widgetCenterDataPack = JSONUtil.parseObj(widgetPackageFile).toBean(WidgetCenterDataPack.class);
                    iWidgetPackageService.parseWidgetCenterDataPack(widgetCenterConfig.getWidgetSpaceId(), widgetCenterDataPack);
                    log.info("Loading widget center data finish.");
                    break;
                case FilePlusUtil.AUTOMATION_FILE_NAME:
                    if (properties.getAutomation().isSkip()) {
                        log.info("Skip loading automation data.");
                        break;
                    }
                    AutomationDataPack automationDataPack = JSONUtil.parseObj(FilePlusUtil.parseFileContent(file)).toBean(AutomationDataPack.class);
                    iAutomationService.parseAutomationDataPack(automationDataPack);
                    log.info("Loading automation data finish.");
                    break;
                case FilePlusUtil.LAB_FEATURE_FILE_NAME:
                    if (properties.getLabFeature().isSkip()) {
                        log.info("Skip loading lab feature data.");
                        break;
                    }
                    List<LabsFeatures> labsFeatures = JSONUtil.parseArray(FilePlusUtil.parseFileContent(file)).toList(LabsFeatures.class);
                    iLabFeatureService.parseLabFeatureData(labsFeatures);
                    log.info("Loading lab feature data finish.");
                    break;
                case FilePlusUtil.WIZARD_CONFIG_FILE_NAME:
                    if (properties.getWizard().isSkip()) {
                        log.info("Skip loading wizard config data.");
                        break;
                    }
                    List<SystemConfig> systemConfigs = JSONUtil.parseArray(FilePlusUtil.parseFileContent(file)).toList(SystemConfig.class);
                    iSystemConfigService.parseSystemConfigData(systemConfigs);
                    log.info("Loading wizard config data finish.");
                    break;
                default:
                    break;
            }
        }
    }

}
