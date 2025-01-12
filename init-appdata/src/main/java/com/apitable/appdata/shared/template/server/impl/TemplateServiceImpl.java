package com.apitable.appdata.shared.template.server.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.appdata.shared.asset.pojo.Asset;
import com.apitable.appdata.shared.asset.service.IAssetService;
import com.apitable.appdata.shared.base.enums.SystemConfigType;
import com.apitable.appdata.shared.base.pojo.SystemConfig;
import com.apitable.appdata.shared.base.service.ISystemConfigService;
import com.apitable.appdata.shared.constants.CommonConstants;
import com.apitable.appdata.shared.template.enums.TemplateAlbumRelType;
import com.apitable.appdata.shared.template.enums.TemplatePropertyType;
import com.apitable.appdata.shared.template.mapper.TemplateMapper;
import com.apitable.appdata.shared.template.model.RecommendConfig;
import com.apitable.appdata.shared.template.model.RecommendConfig.AlbumGroup;
import com.apitable.appdata.shared.template.model.RecommendConfig.Banner;
import com.apitable.appdata.shared.template.model.RecommendConfig.TemplateGroup;
import com.apitable.appdata.shared.template.model.RecommendSetting;
import com.apitable.appdata.shared.template.model.RecommendSetting.AlbumGroupSetting;
import com.apitable.appdata.shared.template.model.RecommendSetting.BannerSetting;
import com.apitable.appdata.shared.template.model.RecommendSetting.TemplateGroupSetting;
import com.apitable.appdata.shared.template.model.TemplateAlbumSetting;
import com.apitable.appdata.shared.template.model.TemplateCategorySetting;
import com.apitable.appdata.shared.template.model.TemplateCenterConfigInfo;
import com.apitable.appdata.shared.template.model.TemplateCenterDataPack;
import com.apitable.appdata.shared.template.model.TemplateSetting;
import com.apitable.appdata.shared.template.pojo.Template;
import com.apitable.appdata.shared.template.pojo.TemplateAlbum;
import com.apitable.appdata.shared.template.pojo.TemplateAlbumRel;
import com.apitable.appdata.shared.template.pojo.TemplateProperty;
import com.apitable.appdata.shared.template.pojo.TemplatePropertyRel;
import com.apitable.appdata.shared.template.server.ITemplateAlbumService;
import com.apitable.appdata.shared.template.server.ITemplatePropertyService;
import com.apitable.appdata.shared.template.server.ITemplateService;
import com.apitable.appdata.shared.util.FilePlusUtil;
import com.apitable.appdata.shared.util.IdUtil;
import com.apitable.appdata.shared.workspace.model.FileDataPack;
import com.apitable.appdata.shared.workspace.model.NodeDataPack;
import com.apitable.appdata.shared.workspace.service.INodeService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TemplateServiceImpl implements ITemplateService {

    @Resource
    private ISystemConfigService iSystemConfigService;

    @Resource
    private IAssetService iAssetService;

    @Resource
    private INodeService iNodeService;

    @Resource
    private ITemplateAlbumService iTemplateAlbumService;

    @Resource
    private ITemplatePropertyService iTemplatePropertyService;

    @Resource
    private TemplateMapper templateMapper;

    @Override
    public List<Template> getTemplatesBySpaceId(String spaceId) {
        return templateMapper.selectByTypeId(spaceId);
    }

    @Override
    public TemplateCenterDataPack getTemplateCenterDataPack(String spaceId, List<TemplateCenterConfigInfo> templateCenterConfigInfos) {
        TemplateCenterDataPack dataPack = new TemplateCenterDataPack();
        // get official space template name map
        Map<String, Template> tplNameToTemplateMap = this.getTplNameToTemplateMap(spaceId);

        // record all template relate node
        Set<String> nodeIds = new HashSet<>();
        for (TemplateCenterConfigInfo info : templateCenterConfigInfos) {
            Set<String> tplNames = new HashSet<>();
            info.getRecommend().getBanners().stream()
                .map(BannerSetting::getTemplateName)
                .forEach(tplNames::add);
            info.getRecommend().getTemplateGroups().stream()
                .map(TemplateGroupSetting::getTemplateNames)
                .forEach(tplNames::addAll);
            info.getAlbums().stream()
                .map(TemplateAlbumSetting::getTemplateNames)
                .forEach(tplNames::addAll);
            info.getCategories().stream()
                .map(TemplateCategorySetting::getTemplateNames)
                .forEach(tplNames::addAll);

            for (String tplName : tplNames) {
                // check if template from datasheet config is existed
                if (!tplNameToTemplateMap.containsKey(tplName)) {
                    throw new RuntimeException(StrUtil.format("Template「{}」is not exist!", tplName));
                }
                nodeIds.add(tplNameToTemplateMap.get(tplName).getNodeId());
                dataPack.getTemplates().add(tplNameToTemplateMap.get(tplName));
            }
        }
        FileDataPack fileDataPack = iNodeService.getFileDataPack(nodeIds);
        BeanUtil.copyProperties(fileDataPack, dataPack);

        // prepare template existing data
        TemplateExistingData existingData = new TemplateExistingData(tplNameToTemplateMap);
        // datasheet config convert to db data
        for (TemplateCenterConfigInfo configInfo : templateCenterConfigInfos) {
            // 1. template tag data
            this.handleTemplateTag(configInfo, existingData, dataPack);

            // 2. template album data(keep order, maybe depends on the new tag id)
            this.handleTemplateAlbum(configInfo, existingData, dataPack);

            // 3. template category data & recommend config db data (keep order, maybe depends on the new album id & new template id)
            this.handleTemplateCategory(configInfo, existingData, dataPack);
            this.handleRecommend(configInfo, existingData, dataPack);
        }
        if (!existingData.templateSettingAssetTokens.isEmpty()) {
            List<String> existFileUrl = dataPack.getAssets().stream().map(Asset::getFileUrl).collect(Collectors.toList());
            existFileUrl.forEach(existingData.templateSettingAssetTokens::remove);
            List<Asset> assets = iAssetService.getAssetsByFileUrls(existingData.templateSettingAssetTokens);
            dataPack.getAssets().addAll(assets);
        }
        return dataPack;
    }

    private Map<String, Template> getTplNameToTemplateMap(String spaceId) {
        List<Template> templates = this.getTemplatesBySpaceId(spaceId);
        if (templates.isEmpty()) {
            throw new RuntimeException("There is no template in the official space.");
        }
        return templates.stream().collect(Collectors.toMap(Template::getName, i -> i));
    }

    private void handleRecommend(TemplateCenterConfigInfo configInfo, TemplateExistingData existingData, TemplateCenterDataPack dataPack) {
        String i18n = configInfo.getI18n();
        Map<String, String> tplNameToTplIdMap = existingData.tplNameToTplIdMap;

        // convert recommend setting db data
        RecommendSetting recommendSetting = configInfo.getRecommend();
        RecommendConfig recommendConfig = new RecommendConfig();
        // top banner
        if (CollUtil.isNotEmpty(recommendSetting.getBanners())) {
            List<Banner> top = recommendSetting.getBanners().stream()
                    .map(item -> new Banner(tplNameToTplIdMap.get(item.getTemplateName()), item.getImage(), item.getTitle(), item.getDesc(), item.getColor()))
                    .collect(Collectors.toList());
            recommendConfig.setTop(top);
            existingData.templateSettingAssetTokens.addAll(top.stream().map(Banner::getImage).filter(StrUtil::isNotBlank).collect(Collectors.toSet()));
        }

        // custom album group
        if (CollUtil.isNotEmpty(recommendSetting.getAlbumGroups())) {
            List<AlbumGroup> albumGroups = new ArrayList<>();
            Map<String, String> albumNameToAlbumIdMap = existingData.getAlbumNameToAlbumIdMap(i18n);
            for (AlbumGroupSetting group : recommendSetting.getAlbumGroups()) {
                AlbumGroup albumGroup = new AlbumGroup();
                albumGroup.setName(group.getName());
                List<String> albumIds = group.getAlbumNames().stream().map(albumNameToAlbumIdMap::get).collect(Collectors.toList());
                albumGroup.setAlbumIds(albumIds);
                albumGroups.add(albumGroup);
            }
            recommendConfig.setAlbumGroups(albumGroups);
        }

        // custom template group
        if (CollUtil.isNotEmpty(recommendSetting.getTemplateGroups())) {
            List<TemplateGroup> templateGroups = new ArrayList<>();
            for (TemplateGroupSetting group : recommendSetting.getTemplateGroups()) {
                TemplateGroup templateGroup = new TemplateGroup();
                templateGroup.setName(group.getName());
                List<String> templateIds = group.getTemplateNames().stream().map(item -> existingData.tplNameToTplIdMap.get(item)).collect(Collectors.toList());
                templateGroup.setTemplateIds(templateIds);
                templateGroups.add(templateGroup);
            }
            recommendConfig.setTemplateGroups(templateGroups);
        }
        dataPack.getSystemConfigs().add(new SystemConfig(SystemConfigType.RECOMMEND_CONFIG.getType(), i18n, JSONUtil.toJsonStr(recommendConfig)));
    }

    private void handleTemplateCategory(TemplateCenterConfigInfo configInfo, TemplateExistingData existingData, TemplateCenterDataPack dataPack) {
        if (CollUtil.isEmpty(configInfo.getCategories())) {
            return;
        }
        String i18n = configInfo.getI18n();
        Map<String, String> categoryNameToPropertyCodeMap = existingData.getCategoryNameToPropertyCodeMap(i18n);

        int order = 0;
        for (TemplateCategorySetting category : configInfo.getCategories()) {
            // record all template property about category
            String categoryName = category.getName();
            if (!categoryNameToPropertyCodeMap.containsKey(categoryName)) {
                categoryNameToPropertyCodeMap.put(categoryName, IdUtil.createTempCatCode());
            }
            String propertyCode = categoryNameToPropertyCodeMap.get(categoryName);
            dataPack.getTemplateProperties().add(new TemplateProperty(TemplatePropertyType.CATEGORY.getType(), categoryName, propertyCode, i18n));

            // template category & album rel
            if (CollUtil.isNotEmpty(category.getAlbumNames())) {
                Map<String, String> albumNameToAlbumMap = existingData.getAlbumNameToAlbumIdMap(i18n);
                for (String albumName : category.getAlbumNames()) {
                    // create property rel
                    String albumId = albumNameToAlbumMap.get(albumName);
                    dataPack.getTemplateAlbumRelations().add(new TemplateAlbumRel(albumId, TemplateAlbumRelType.TEMPLATE_CATEGORY.getType(), propertyCode));
                }
            }
            // template category & template rel
            if (CollUtil.isNotEmpty(category.getTemplateNames())) {
                for (String templateName : category.getTemplateNames()) {
                    String templateId = existingData.tplNameToTplIdMap.get(templateName);
                    dataPack.getTemplatePropertyRelations().add(new TemplatePropertyRel(templateId, propertyCode, order));
                }
            }
            order++;
        }
    }

    private void handleTemplateTag(TemplateCenterConfigInfo configInfo, TemplateExistingData existingData, TemplateCenterDataPack dataPack) {
        if (CollUtil.isEmpty(configInfo.getTemplatesWithTag())) {
            return;
        }
        // get all tag name
        Set<String> tagNames = configInfo.getTemplatesWithTag().stream()
                .map(TemplateSetting::getTags)
                .filter(CollUtil::isNotEmpty)
                .reduce(new HashSet<>(),
                        (set, tags) -> {
                            set.addAll(tags);
                            return set;
                        },
                        (set, tags) -> {
                            set.addAll(tags);
                            return set;
                        });

        String i18n = configInfo.getI18n();
        Map<String, String> tagNameToPropertyCodeMap = existingData.getTagNameToPropertyCodeMap(i18n);
        // record all template property about tag
        for (String tagName : tagNames) {
            if (!tagNameToPropertyCodeMap.containsKey(tagName)) {
                tagNameToPropertyCodeMap.put(tagName, IdUtil.createTempTagCode());
            }
            dataPack.getTemplateProperties().add(new TemplateProperty(TemplatePropertyType.TAG.getType(), tagName, tagNameToPropertyCodeMap.get(tagName), i18n));
        }

        // record all template property rel about tag
        for (TemplateSetting template : configInfo.getTemplatesWithTag()) {
            if (CollUtil.isEmpty(template.getTags())) {
                continue;
            }
            String templateId = existingData.tplNameToTplIdMap.get(template.getName());
            int order = 0;
            for (String tagName : template.getTags()) {
                dataPack.getTemplatePropertyRelations().add(new TemplatePropertyRel(templateId, tagNameToPropertyCodeMap.get(tagName), order));
                order++;
            }
        }
    }

    private void handleTemplateAlbum(TemplateCenterConfigInfo configInfo, TemplateExistingData existingData, TemplateCenterDataPack dataPack) {
        if (CollUtil.isEmpty(configInfo.getAlbums())) {
            return;
        }
        String i18n = configInfo.getI18n();
        Map<String, String> albumNameToAlbumIdMap = existingData.getAlbumNameToAlbumIdMap(i18n);

        for (TemplateAlbumSetting albumSetting : configInfo.getAlbums()) {
            // record all template album
            TemplateAlbum album = new TemplateAlbum();
            album.setI18nName(i18n);
            album.setName(albumSetting.getName());
            if (StrUtil.isNotBlank(albumSetting.getCover())) {
                album.setCover(albumSetting.getCover());
                existingData.templateSettingAssetTokens.add(albumSetting.getCover());
            }
            album.setDescription(albumSetting.getDescription());
            album.setContent(albumSetting.getContent());
            album.setAuthorName(albumSetting.getPublisherName());
            if (StrUtil.isNotBlank(albumSetting.getPublisherLogo())) {
                album.setAuthorLogo(albumSetting.getPublisherLogo());
                existingData.templateSettingAssetTokens.add(albumSetting.getPublisherLogo());
            }
            album.setAuthorDesc(albumSetting.getPublisherDesc());
            if (!albumNameToAlbumIdMap.containsKey(albumSetting.getName())) {
                albumNameToAlbumIdMap.put(albumSetting.getName(), IdUtil.createTemplateAlbumId());
            }
            String albumId = albumNameToAlbumIdMap.get(albumSetting.getName());
            album.setAlbumId(albumId);
            dataPack.getTemplateAlbums().add(album);

            // albumSetting & template rel
            if (CollUtil.isNotEmpty(albumSetting.getTemplateNames())) {
                Map<String, List<TemplatePropertyRel>> tplIdToPropRelMap =
                    dataPack.getTemplatePropertyRelations().stream()
                        .collect(Collectors.groupingBy(TemplatePropertyRel::getTemplateId));
                Set<String> tagCodes = new HashSet<>();
                for (String templateName : albumSetting.getTemplateNames()) {
                    String templateId = existingData.tplNameToTplIdMap.get(templateName);
                    TemplateAlbumRel templateAlbumRel = new TemplateAlbumRel(albumId,
                        TemplateAlbumRelType.TEMPLATE.getType(), templateId);
                    dataPack.getTemplateAlbumRelations().add(templateAlbumRel);
                    if (tplIdToPropRelMap.containsKey(templateId)) {
                        tplIdToPropRelMap.get(templateId).stream()
                            .map(TemplatePropertyRel::getPropertyCode).forEach(tagCodes::add);
                    }
                }
                // albumSetting & template tag rel
                for (String tagCode : tagCodes) {
                    TemplateAlbumRel templateAlbumRel = new TemplateAlbumRel(albumId,
                        TemplateAlbumRelType.TEMPLATE_TAG.getType(), tagCode);
                    dataPack.getTemplateAlbumRelations().add(templateAlbumRel);
                }
            }
        }
    }

    class TemplateExistingData {
        Map<String, String> tplNameToTplIdMap;

        Map<String, Map<String, String>> i18nToAlbumNameToAlbumIdMap;

        Map<String, Map<String, String>> i18nToCategoryNameToPropertyCodeMap;

        Map<String, Map<String, String>> i18nToTagNameToPropertyCodeMap;

        Set<String> templateSettingAssetTokens = new HashSet<>();

        public TemplateExistingData(Map<String, Template> templateMap) {
            tplNameToTplIdMap = templateMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getTemplateId()));

            // prepare album data
            List<TemplateAlbum> albums = iTemplateAlbumService.getAllTemplateAlbum();
            i18nToAlbumNameToAlbumIdMap = albums.stream()
                    .collect(Collectors.groupingBy(TemplateAlbum::getI18nName,
                            Collectors.toMap(TemplateAlbum::getName, TemplateAlbum::getAlbumId)));

            // prepare category & tag data
            List<TemplateProperty> templateProperties = iTemplatePropertyService.getAllTemplateProperty();
            i18nToCategoryNameToPropertyCodeMap = templateProperties.stream()
                    .filter(prop -> TemplatePropertyType.CATEGORY.getType() == prop.getPropertyType())
                    .collect(Collectors.groupingBy(TemplateProperty::getI18nName,
                            Collectors.toMap(TemplateProperty::getPropertyName, TemplateProperty::getPropertyCode)));
            i18nToTagNameToPropertyCodeMap = templateProperties.stream()
                    .filter(prop -> TemplatePropertyType.TAG.getType() == prop.getPropertyType())
                    .collect(Collectors.groupingBy(TemplateProperty::getI18nName,
                            Collectors.toMap(TemplateProperty::getPropertyName, TemplateProperty::getPropertyCode)));
        }

        Map<String, String> getAlbumNameToAlbumIdMap(String i18n) {
            if (!i18nToAlbumNameToAlbumIdMap.containsKey(i18n)) {
                i18nToAlbumNameToAlbumIdMap.put(i18n, new HashMap<>());
            }
            return i18nToAlbumNameToAlbumIdMap.get(i18n);
        }

        Map<String, String> getCategoryNameToPropertyCodeMap(String i18n) {
            if (!i18nToCategoryNameToPropertyCodeMap.containsKey(i18n)) {
                i18nToCategoryNameToPropertyCodeMap.put(i18n, new HashMap<>());
            }
            return i18nToCategoryNameToPropertyCodeMap.get(i18n);
        }

        Map<String, String> getTagNameToPropertyCodeMap(String i18n) {
            if (!i18nToTagNameToPropertyCodeMap.containsKey(i18n)) {
                i18nToTagNameToPropertyCodeMap.put(i18n, new HashMap<>());
            }
            return i18nToTagNameToPropertyCodeMap.get(i18n);
        }
    }

    @Override
    public void parseTemplateCenterDataPack(String targetSpaceId,
        TemplateCenterDataPack dataPack, boolean skipConfig) {
        if (dataPack.getTemplates().isEmpty()) {
            return;
        }
        log.info("Begin to parse asset data.");
        Map<Long, Long> newAssetIdMap = iAssetService.parseAssetAndReturnAssetIdMap(dataPack.getAssets());

        log.info("Begin to parse template file data.");
        List<File> files = FileUtil.loopFiles(FilePlusUtil.NODE_DATA_DIR);
        for (File file : files) {
            NodeDataPack nodeDataPack = JSONUtil.parseObj(FilePlusUtil.parseFileContent(file)).toBean(NodeDataPack.class);
            iNodeService.parseNodeDataPack(targetSpaceId, newAssetIdMap, nodeDataPack);
        }

        log.info("Begin to parse template data.");
        Map<String, String> newTemplateIdMap = this.parseTemplateData(targetSpaceId, dataPack.getTemplates());

        if (skipConfig) {
            // It does not take effect until it is not the first initialization.
            List<TemplateProperty> templateProperties =
                iTemplatePropertyService.getAllTemplateProperty();
            if (!templateProperties.isEmpty()) {
                log.info("Skip template center configuration update.");
                return;
            }
        }

        log.info("Begin to parse template property data.");
        iTemplatePropertyService.parseTemplatePropertyData(newTemplateIdMap, dataPack.getTemplateProperties(), dataPack.getTemplatePropertyRelations());

        log.info("Begin to parse template album data.");
        iTemplateAlbumService.parseTemplateAlbumData(newTemplateIdMap, dataPack.getTemplateAlbums(), dataPack.getTemplateAlbumRelations());

        log.info("Begin to parse template center recommend config data.");
        iSystemConfigService.parseRecommendConfigData(newTemplateIdMap, dataPack.getSystemConfigs());
    }

    private Map<String, String> parseTemplateData(String targetSpaceId, List<Template> templates) {
        List<String> templateIds = templates.stream().map(Template::getTemplateId).collect(Collectors.toList());
        // Get templates with the same template id exists in the target db
        List<String> duplicateTemplateIds = templateMapper.selectTemplateIdByTemplateIds(templateIds);

        // Get templates have been exists in the target space
        List<Template> existTemplates = this.getTemplatesBySpaceId(targetSpaceId);
        Map<String, Template> existTemplateMap = existTemplates.stream().collect(Collectors.toMap(Template::getName, i -> i));

        // Record data pending to process
        Set<String> deleteTemplateId = new HashSet<>();
        Set<String> removeTemplateId = new HashSet<>();
        Set<String> removeNodeId = new HashSet<>();
        Map<String, String> newTemplateIdMap = new HashMap<>();
        for (Template template : templates) {
            template.setId(IdWorker.getId());
            template.setTypeId(targetSpaceId);
            String templateId = template.getTemplateId();
            // If a template with the same name exists in the target space, it would cover existed template
            if (existTemplateMap.containsKey(template.getName())) {
                Template sameNameTemplate = existTemplateMap.get(template.getName());
                removeNodeId.add(sameNameTemplate.getNodeId());
                // if the template id same too, it would delete existed template record.(Avoid unique conflict about template id)
                if (sameNameTemplate.getTemplateId().equals(templateId)) {
                    deleteTemplateId.add(sameNameTemplate.getTemplateId());
                    continue;
                }
                // else, remove this existed template record.(Avoid having templates with the same name in one space)
                removeTemplateId.add(sameNameTemplate.getTemplateId());
            }
            // If a template with the same template id exists in the target db, it will optionally create new template id
            if (duplicateTemplateIds.contains(templateId)) {
                String newTemplateId = IdUtil.createTemplateId();
                template.setTemplateId(newTemplateId);
                newTemplateIdMap.put(templateId, newTemplateId);
            }
        }

        // Execute old data process before insert
        if (!removeNodeId.isEmpty()) {
            // iNodeService.remove(removeNodeId);
        }
        if (!deleteTemplateId.isEmpty()) {
            templateMapper.delete(deleteTemplateId);
        }
        if (!removeTemplateId.isEmpty()) {
            templateMapper.remove(CommonConstants.INIT_ACCOUNT_USER_ID, removeTemplateId);
        }
        templateMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, templates);
        return newTemplateIdMap;
    }
}
