package com.apitable.enterprise.gm.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.annotation.Resource;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;

import com.apitable.base.enums.DatabaseException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.gm.ro.SingleGlobalWidgetRo;
import com.apitable.enterprise.gm.service.IWidgetGmService;
import com.apitable.enterprise.vika.core.VikaOperations;
import com.apitable.enterprise.widget.ro.WidgetPackageBanRo;
import com.apitable.widget.service.IWidgetPackageService;
import com.apitable.widget.entity.WidgetPackageEntity;
import com.apitable.widget.enums.WidgetPackageStatus;
import com.apitable.widget.mapper.WidgetPackageMapper;
import com.apitable.widget.vo.GlobalWidgetInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apitable.widget.enums.WidgetException.WIDGET_NOT_EXIST;

@Service
@Slf4j
public class WidgetGmServiceImpl implements IWidgetGmService {

    @Autowired(required = false)
    private VikaOperations vikaOperations;

    @Resource
    private IWidgetPackageService iWidgetPackageService;

    @Resource
    private WidgetPackageMapper widgetPackageMapper;

    @Override
    public boolean banWidget(Long opUserId, WidgetPackageBanRo widget) {
        log.info("ban/unban widget");
        // check if the widget exists
        WidgetPackageEntity wpk = iWidgetPackageService.getByPackageId(widget.getPackageId());
        boolean flag;
        if (widget.getUnban()) {
            // ban
            flag = SqlHelper.retBool(widgetPackageMapper.updateStatusAndReleaseIdByPackageId(
                WidgetPackageStatus.UNPUBLISH.getValue(), null, wpk.getPackageId(), opUserId));
        } else {
            // unban, release the release version Id association after the ban
            flag = SqlHelper.retBool(widgetPackageMapper.updateStatusAndReleaseIdByPackageId(
                WidgetPackageStatus.BANNED.getValue(), null, wpk.getPackageId(), opUserId));
        }
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
        return true;
    }

    @Override
    public List<GlobalWidgetInfo> getGlobalWidgetPackageConfiguration(String nodeId,
                                                                      String viewId) {
        return vikaOperations.getGlobalWidgetPackageConfiguration(nodeId, viewId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void globalWidgetDbDataRefresh(String nodeId, String viewId) {
        // Currently, it is paging. If there is more data in the later period, you can consider using paging.
        List<GlobalWidgetInfo> globalWidgetDatas =
            getGlobalWidgetPackageConfiguration(nodeId, viewId);
        // Divide into two batches of SQL for batch operations.
        if (CollUtil.isNotEmpty(globalWidgetDatas)) {
            for (GlobalWidgetInfo globalWidgetData : globalWidgetDatas) {
                widgetPackageMapper.singleUpdateGlobalAndTemplateConfig(globalWidgetData);
            }
        }
    }

    @Override
    public void singleGlobalWidgetRefresh(SingleGlobalWidgetRo body) {
        List<GlobalWidgetInfo> globalWidgetPackage =
            this.getGlobalWidgetPackageConfiguration(body.getNodeId(), body.getViewId());
        if (CollUtil.isEmpty(globalWidgetPackage)) {
            return;
        }

        Map<String, Integer> globalWidgetSort = new LinkedHashMap<>();
        for (int i = 0; i < globalWidgetPackage.size(); i++) {
            globalWidgetSort.put(globalWidgetPackage.get(i).getPackageId(), i + 1);
        }
        // query whether widget changes order
        Integer newWidgetSort = globalWidgetSort.get(body.getPackageId());
        Integer oldWidgetSort = widgetPackageMapper.selectGlobalWidgetSort(body.getPackageId());
        ExceptionUtil.isNotNull(oldWidgetSort, WIDGET_NOT_EXIST);

        GlobalWidgetInfo updateWpk;
        BeanUtil.copyProperties(body, updateWpk = new GlobalWidgetInfo(),
            CopyOptions.create().ignoreError());
        if (!Objects.equals(newWidgetSort, oldWidgetSort)) {
            // Widget changes in sequence, the corresponding order of exchange
            String oldPackageId = MapUtil.inverse(globalWidgetSort).get(oldWidgetSort);

            GlobalWidgetInfo updateWpkSort = new GlobalWidgetInfo();
            updateWpkSort.setPackageId(oldPackageId);
            updateWpkSort.setWidgetSort(oldWidgetSort);
            widgetPackageMapper.singleUpdateGlobalAndTemplateConfig(updateWpkSort);
        }

        updateWpk.setWidgetSort(newWidgetSort);
        boolean flag =
            SqlHelper.retBool(widgetPackageMapper.singleUpdateGlobalAndTemplateConfig(updateWpk));
        ExceptionUtil.isTrue(flag, DatabaseException.EDIT_ERROR);
    }
}
