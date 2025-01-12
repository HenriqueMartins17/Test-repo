package com.apitable.appdata.shared.template.server.impl;

import com.apitable.appdata.shared.constants.CommonConstants;
import com.apitable.appdata.shared.template.mapper.TemplatePropertyMapper;
import com.apitable.appdata.shared.template.mapper.TemplatePropertyRelMapper;
import com.apitable.appdata.shared.template.pojo.TemplateProperty;
import com.apitable.appdata.shared.template.pojo.TemplatePropertyRel;
import com.apitable.appdata.shared.template.server.ITemplatePropertyService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TemplatePropertyServiceImpl implements ITemplatePropertyService {

    @Resource
    private TemplatePropertyMapper propertyMapper;

    @Resource
    private TemplatePropertyRelMapper propertyRelMapper;

    @Override
    public List<TemplateProperty> getAllTemplateProperty() {
        return propertyMapper.selectAllTemplateProperty();
    }

    @Override
    public void parseTemplatePropertyData(Map<String, String> newTemplateIdMap, List<TemplateProperty> properties, List<TemplatePropertyRel> propertyRelations) {
        propertyMapper.delete();
        propertyRelMapper.delete();
        if (properties.isEmpty()) {
            return;
        }
        for (TemplateProperty property : properties) {
            property.setId(IdWorker.getId());
        }
        propertyMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, properties);
        for (TemplatePropertyRel propertyRel : propertyRelations) {
            propertyRel.setId(IdWorker.getId());
            if (newTemplateIdMap.containsKey(propertyRel.getTemplateId())) {
                propertyRel.setTemplateId(newTemplateIdMap.get(propertyRel.getTemplateId()));
            }
        }
        propertyRelMapper.insertBatch(propertyRelations);
    }
}
