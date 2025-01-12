package com.apitable.appdata.shared.widget.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.appdata.shared.constants.CommonConstants;
import com.apitable.appdata.shared.widget.mapper.WidgetPackageAuthSpaceMapper;
import com.apitable.appdata.shared.widget.mapper.WidgetPackageMapper;
import com.apitable.appdata.shared.widget.mapper.WidgetPackageReleaseMapper;
import com.apitable.appdata.shared.widget.model.WidgetCenterConfigInfo;
import com.apitable.appdata.shared.widget.model.WidgetCenterDataPack;
import com.apitable.appdata.shared.widget.pojo.WidgetPackage;
import com.apitable.appdata.shared.widget.pojo.WidgetPackageAuthSpace;
import com.apitable.appdata.shared.widget.pojo.WidgetPackageRelease;
import com.apitable.appdata.shared.widget.service.IWidgetPackageService;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class WidgetPackageServiceImpl implements IWidgetPackageService {

    @Resource
    private WidgetPackageMapper widgetPackageMapper;

    @Resource
    private WidgetPackageReleaseMapper widgetPackageReleaseMapper;

    @Resource
    private WidgetPackageAuthSpaceMapper widgetPackageAuthSpaceMapper;

    @Override
    public WidgetCenterDataPack getWidgetCenterDataPack(List<WidgetCenterConfigInfo> widgetCenterConfigInfos) {
        List<String> packageIds = widgetCenterConfigInfos.stream().map(WidgetCenterConfigInfo::getPackageId).collect(Collectors.toList());
        if (packageIds.isEmpty()) {
            return null;
        }
        // Query data
        List<WidgetPackage> widgetPackages = widgetPackageMapper.selectByPackageIds(packageIds);
        Map<String, WidgetPackage> packageMap = widgetPackages.stream().collect(Collectors.toMap(WidgetPackage::getPackageId, i -> i));
        List<Long> releaseIds = widgetPackages.stream().map(WidgetPackage::getReleaseId).collect(Collectors.toList());
        List<WidgetPackageRelease> widgetPackageReleases = widgetPackageReleaseMapper.selectByIds(releaseIds);
        Map<String, WidgetPackageRelease> packageReleaseMap = widgetPackageReleases.stream().collect(Collectors.toMap(WidgetPackageRelease::getPackageId, i -> i));
        List<WidgetPackageAuthSpace> widgetPackageAuthSpaces = widgetPackageAuthSpaceMapper.selectByPackageIds(packageIds);
        Map<String, WidgetPackageAuthSpace> packageAuthSpaceMap = widgetPackageAuthSpaces.stream().collect(Collectors.toMap(WidgetPackageAuthSpace::getPackageId, i -> i));

        // Cover data
        Set<String> assetTokens = new HashSet<>();
        for (WidgetCenterConfigInfo info : widgetCenterConfigInfos) {
            String packageId = info.getPackageId();
            if (!packageMap.containsKey(packageId) || !packageReleaseMap.containsKey(packageId) || !packageAuthSpaceMap.containsKey(packageId)) {
                throw new RuntimeException(StrUtil.format("Widget「{}」is not exist!", packageId));
            }
            this.coverWidgetPackageData(packageMap.get(packageId), info, assetTokens);
            this.coverWidgetPackageReleaseData(packageReleaseMap.get(packageId), info, assetTokens);
            this.coverWidgetPackageAuthSpaceData(packageAuthSpaceMap.get(packageId), info.getWidgetSort());
        }
        return new WidgetCenterDataPack(assetTokens, widgetPackages, widgetPackageReleases, widgetPackageAuthSpaces);
    }

    private void coverWidgetPackageData(WidgetPackage widgetPackage, WidgetCenterConfigInfo info, Set<String> assetTokens) {
        if (StrUtil.isNotBlank(info.getIcon())) {
            widgetPackage.setIcon(info.getIcon());
        }
        assetTokens.add(widgetPackage.getIcon());
        if (StrUtil.isNotBlank(info.getI18nName()) && JSONUtil.isJsonObj(info.getI18nName())) {
            widgetPackage.setI18nName(info.getI18nName());
        }
        if (StrUtil.isNotBlank(info.getI18nDescription())
            && JSONUtil.isJsonObj(info.getI18nDescription())) {
            widgetPackage.setI18nDescription(info.getI18nDescription());
        }
        if (StrUtil.isNotBlank(info.getCover())) {
            widgetPackage.setCover(info.getCover());
        }
        assetTokens.add(widgetPackage.getCover());
        widgetPackage.setIsTemplate(info.getTemplate());
        if (StrUtil.isNotBlank(info.getWidgetBody())) {
            widgetPackage.setWidgetBody(info.getWidgetBody());
        }
        JSONObject widgetBody = JSONUtil.parseObj(widgetPackage.getWidgetBody());
        if (widgetBody.containsKey("templateCover")) {
            assetTokens.add(widgetBody.getStr("templateCover"));
        }
        if (StrUtil.isNotBlank(info.getAuthorName())) {
            widgetPackage.setAuthorName(info.getAuthorName());
        }
        if (StrUtil.isNotBlank(info.getAuthorEmail())) {
            widgetPackage.setAuthorEmail(info.getAuthorEmail());
        }
        if (StrUtil.isNotBlank(info.getAuthorIcon())) {
            widgetPackage.setAuthorIcon(info.getAuthorIcon());
        }
        assetTokens.add(widgetPackage.getAuthorIcon());
        if (StrUtil.isNotBlank(info.getAuthorLink())) {
            widgetPackage.setAuthorLink(info.getAuthorLink());
        }
        widgetPackage.setInstalledNum(0);
        widgetPackage.setIsEnabled(true);
        widgetPackage.setOwner(CommonConstants.INIT_ACCOUNT_USER_ID);
        widgetPackage.setCreatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
        widgetPackage.setUpdatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
    }

    private void coverWidgetPackageReleaseData(WidgetPackageRelease widgetPackageRelease, WidgetCenterConfigInfo info, Set<String> assetTokens) {
        if (StrUtil.isNotBlank(info.getReleaseCodeBundle())) {
            widgetPackageRelease.setReleaseCodeBundle(info.getReleaseCodeBundle());
        }
        assetTokens.add(widgetPackageRelease.getReleaseCodeBundle());
        if (StrUtil.isNotBlank(info.getSourceCodeBundle())) {
            widgetPackageRelease.setReleaseCodeBundle(info.getSourceCodeBundle());
            assetTokens.add(info.getSourceCodeBundle());
        } else if (StrUtil.isNotBlank(widgetPackageRelease.getSourceCodeBundle())) {
            assetTokens.add(widgetPackageRelease.getSourceCodeBundle());
        }
        widgetPackageRelease.setReleaseUserId(CommonConstants.INIT_ACCOUNT_USER_ID);
        widgetPackageRelease.setCreatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
        widgetPackageRelease.setUpdatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
    }

    private void coverWidgetPackageAuthSpaceData(WidgetPackageAuthSpace widgetPackageAuthSpace, Integer widgetSort) {
        widgetPackageAuthSpace.setSpaceId(null);
        widgetPackageAuthSpace.setWidgetSort(widgetSort);
        widgetPackageAuthSpace.setCreatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
        widgetPackageAuthSpace.setUpdatedBy(CommonConstants.INIT_ACCOUNT_USER_ID);
    }

    @Override
    public void parseWidgetCenterDataPack(String targetSpaceId, WidgetCenterDataPack dataPack) {
        // Invalidate existing widget
        widgetPackageMapper.disableGlobalWidgetPackage(CommonConstants.INIT_ACCOUNT_USER_ID);

        // Insert Data
        widgetPackageMapper.insertBatch(dataPack.getWidgetPackages());
        widgetPackageReleaseMapper.insertBatch(dataPack.getWidgetPackageReleases());
        dataPack.getWidgetPackageAuthSpaces().forEach(i -> i.setSpaceId(targetSpaceId));
        widgetPackageAuthSpaceMapper.insertBatch(dataPack.getWidgetPackageAuthSpaces());
    }
}
