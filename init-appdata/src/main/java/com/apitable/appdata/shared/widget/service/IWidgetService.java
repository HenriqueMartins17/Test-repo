package com.apitable.appdata.shared.widget.service;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.widget.pojo.Widget;

public interface IWidgetService {

    List<Widget> getWidgets(Collection<String> nodeIds);

    void save(Long userId, List<Widget> widgets);
}
