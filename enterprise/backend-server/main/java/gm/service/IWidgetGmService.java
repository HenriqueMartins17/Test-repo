package com.apitable.enterprise.gm.service;

import java.util.List;

import com.apitable.enterprise.gm.ro.SingleGlobalWidgetRo;
import com.apitable.enterprise.widget.ro.WidgetPackageBanRo;
import com.apitable.widget.vo.GlobalWidgetInfo;

public interface IWidgetGmService {

    /**
     * @param opUserId  opUserId
     * @param widget prohibited and unblocked request parameters
     * @return operation status of forbidden and unsealed widgets
     */
    boolean banWidget(Long opUserId, WidgetPackageBanRo widget);

    /**
     * @param nodeId node id
     * @return Global widget information for external network space configuration
     */
    List<GlobalWidgetInfo> getGlobalWidgetPackageConfiguration(String nodeId, String viewId);

    /**
     * refresh global widget db data
     *
     * @param nodeId node id
     */
    void globalWidgetDbDataRefresh(String nodeId, String viewId);

    /**
     * single widget data refresh
     *
     * @param body  requestor
     */
    void singleGlobalWidgetRefresh(SingleGlobalWidgetRo body);
}
