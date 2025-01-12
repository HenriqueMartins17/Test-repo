package com.apitable.appdata.shared.widget.service;

import java.util.List;

import com.apitable.appdata.shared.widget.model.WidgetCenterConfigInfo;
import com.apitable.appdata.shared.widget.model.WidgetCenterDataPack;

public interface IWidgetPackageService {

    WidgetCenterDataPack getWidgetCenterDataPack(List<WidgetCenterConfigInfo> widgetCenterConfigInfos);

    void parseWidgetCenterDataPack(String targetSpaceId, WidgetCenterDataPack dataPack);
}
