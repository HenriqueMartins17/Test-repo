package com.apitable.appdata.shared.template.model;

import java.util.ArrayList;
import java.util.List;

import com.apitable.appdata.shared.base.pojo.SystemConfig;
import com.apitable.appdata.shared.template.pojo.Template;
import com.apitable.appdata.shared.template.pojo.TemplateAlbum;
import com.apitable.appdata.shared.template.pojo.TemplateAlbumRel;
import com.apitable.appdata.shared.template.pojo.TemplateProperty;
import com.apitable.appdata.shared.template.pojo.TemplatePropertyRel;
import com.apitable.appdata.shared.workspace.model.FileDataPack;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TemplateCenterDataPack extends FileDataPack {

    private List<Template> templates = new ArrayList<>();

    private List<TemplateAlbum> templateAlbums = new ArrayList<>();

    private List<TemplateAlbumRel> templateAlbumRelations = new ArrayList<>();

    private List<TemplateProperty> templateProperties = new ArrayList<>();

    private List<TemplatePropertyRel> templatePropertyRelations = new ArrayList<>();

    private List<SystemConfig> systemConfigs = new ArrayList<>();

}
