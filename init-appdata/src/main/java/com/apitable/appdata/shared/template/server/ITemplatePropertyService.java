package com.apitable.appdata.shared.template.server;

import java.util.List;
import java.util.Map;

import com.apitable.appdata.shared.template.pojo.TemplateProperty;
import com.apitable.appdata.shared.template.pojo.TemplatePropertyRel;

public interface ITemplatePropertyService {

    List<TemplateProperty> getAllTemplateProperty();

    void parseTemplatePropertyData(Map<String, String> newTemplateIdMap, List<TemplateProperty> properties, List<TemplatePropertyRel> propertyRelations);
}
