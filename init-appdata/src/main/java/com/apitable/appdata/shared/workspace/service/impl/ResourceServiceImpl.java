package com.apitable.appdata.shared.workspace.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.appdata.shared.constants.CommonConstants;
import com.apitable.appdata.shared.widget.pojo.Widget;
import com.apitable.appdata.shared.widget.service.IWidgetService;
import com.apitable.appdata.shared.workspace.mapper.DatasheetMapper;
import com.apitable.appdata.shared.workspace.mapper.DatasheetMetaMapper;
import com.apitable.appdata.shared.workspace.mapper.DatasheetRecordMapper;
import com.apitable.appdata.shared.workspace.mapper.DatasheetWidgetMapper;
import com.apitable.appdata.shared.workspace.mapper.ResourceMetaMapper;
import com.apitable.appdata.shared.workspace.model.ResourceDataPack;
import com.apitable.appdata.shared.workspace.pojo.Datasheet;
import com.apitable.appdata.shared.workspace.pojo.DatasheetMeta;
import com.apitable.appdata.shared.workspace.pojo.DatasheetRecord;
import com.apitable.appdata.shared.workspace.pojo.DatasheetWidget;
import com.apitable.appdata.shared.workspace.pojo.Node;
import com.apitable.appdata.shared.workspace.pojo.ResourceMeta;
import com.apitable.appdata.shared.workspace.service.IResourceService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ResourceServiceImpl implements IResourceService {

    @Resource
    private ResourceMetaMapper resourceMetaMapper;

    @Resource
    private DatasheetMapper datasheetMapper;

    @Resource
    private DatasheetMetaMapper datasheetMetaMapper;

    @Resource
    private DatasheetRecordMapper datasheetRecordMapper;

    @Resource
    private DatasheetWidgetMapper datasheetWidgetMapper;

    @Resource
    private IWidgetService iWidgetService;

    @Override
    public ResourceDataPack getResourceDataPack(List<Node> nodes) {
        ResourceDataPack dataPack = new ResourceDataPack();
        List<String> resourceIds = nodes.stream().filter(i -> i.getType() > 2).map(Node::getNodeId).collect(Collectors.toList());
        if (!resourceIds.isEmpty()) {
            List<ResourceMeta> resourceMetas = resourceMetaMapper.selectByResourceIds(resourceIds);
            dataPack.setResourceMetas(resourceMetas);
        }
        List<String> datasheetIds = nodes.stream().filter(i -> i.getType() == 2).map(Node::getNodeId).collect(Collectors.toList());
        if (!datasheetIds.isEmpty()) {
            List<Datasheet> datasheets = datasheetMapper.selectByDatasheetIds(datasheetIds);
            dataPack.setDatasheets(datasheets);
            List<DatasheetMeta> datasheetMetas = datasheetMetaMapper.selectByDatasheetIds(datasheetIds);
            dataPack.setDatasheetMetas(datasheetMetas);
            List<DatasheetRecord> datasheetRecords = datasheetRecordMapper.selectByDatasheetIds(datasheetIds);
            dataPack.setDatasheetRecords(datasheetRecords);
            List<DatasheetWidget> datasheetWidgets = datasheetWidgetMapper.selectByDatasheetIds(datasheetIds);
            dataPack.setDatasheetWidgets(datasheetWidgets);

        }
        Collection<String> nodeIds = CollUtil.addAll(resourceIds, datasheetIds);
        if (!nodeIds.isEmpty()) {
            List<Widget> widgets = iWidgetService.getWidgets(nodeIds);
            dataPack.setWidgets(widgets);
        }
        return dataPack;
    }

    @Override
    public void parseResourceDataPack(String targetSpaceId, ResourceDataPack dataPack) {
        if (!dataPack.getResourceMetas().isEmpty()) {
            resourceMetaMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, dataPack.getResourceMetas());
        }
        if (!dataPack.getWidgets().isEmpty()) {
            dataPack.getWidgets().forEach(i -> i.setSpaceId(targetSpaceId));
            iWidgetService.save(CommonConstants.INIT_ACCOUNT_USER_ID, dataPack.getWidgets());
        }
        if (!dataPack.getDatasheets().isEmpty()) {
            dataPack.getDatasheets().forEach(i -> i.setSpaceId(targetSpaceId));
            datasheetMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, dataPack.getDatasheets());
            datasheetMetaMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, dataPack.getDatasheetMetas());
            String initRecordMeta = this.getInitRecordMeta();
            dataPack.getDatasheetRecords().forEach(i -> {
                i.setId(IdWorker.getId());
                i.setFieldUpdatedInfo(initRecordMeta);
            });
            datasheetRecordMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, dataPack.getDatasheetRecords());
            if (!dataPack.getDatasheetWidgets().isEmpty()) {
                dataPack.getDatasheetWidgets().forEach(i -> i.setSpaceId(targetSpaceId));
                datasheetWidgetMapper.insertBatch(dataPack.getDatasheetWidgets());
            }
        }
    }

    private String getInitRecordMeta() {
        long createdAt = Instant.now().toEpochMilli();
        JSONObject recordMeta = JSONUtil.createObj();
        recordMeta.set("createdAt", createdAt);
        recordMeta.set("createdBy", CommonConstants.INIT_ACCOUNT_UUID);
        return recordMeta.toString();
    }
}
