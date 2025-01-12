package com.apitable.appdata.shared.widget.service.impl;

import com.apitable.appdata.shared.constants.CommonConstants;
import com.apitable.appdata.shared.widget.mapper.WidgetMapper;
import com.apitable.appdata.shared.widget.pojo.Widget;
import com.apitable.appdata.shared.widget.service.IWidgetService;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WidgetServiceImpl implements IWidgetService {

    @Resource
    private WidgetMapper widgetMapper;

    @Override
    public List<Widget> getWidgets(Collection<String> nodeIds) {
        return widgetMapper.selectByNodeIds(nodeIds);
    }

    @Override
    public void save(Long userId, List<Widget> widgets) {
        widgetMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, widgets);
    }
}
