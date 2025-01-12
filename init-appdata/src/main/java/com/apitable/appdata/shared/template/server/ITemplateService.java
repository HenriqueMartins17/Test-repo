package com.apitable.appdata.shared.template.server;

import java.util.List;

import com.apitable.appdata.shared.template.model.TemplateCenterConfigInfo;
import com.apitable.appdata.shared.template.model.TemplateCenterDataPack;
import com.apitable.appdata.shared.template.pojo.Template;

public interface ITemplateService {

    List<Template> getTemplatesBySpaceId(String spaceId);

    TemplateCenterDataPack getTemplateCenterDataPack(String templateSpaceId,
        List<TemplateCenterConfigInfo> templateCenterConfigInfos);

    void parseTemplateCenterDataPack(String targetSpaceId,
        TemplateCenterDataPack dataPack, boolean skipConfig);
}
