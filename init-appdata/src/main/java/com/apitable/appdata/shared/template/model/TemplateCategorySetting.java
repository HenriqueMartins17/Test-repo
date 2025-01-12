package com.apitable.appdata.shared.template.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TemplateCategorySetting {

    private String name;

    @JsonProperty("album_names")
    private List<String> albumNames;

    @JsonProperty("template_names")
    private List<String> templateNames;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAlbumNames() {
        return albumNames;
    }

    public void setAlbumNames(List<String> albumNames) {
        this.albumNames = albumNames;
    }

    public List<String> getTemplateNames() {
        return templateNames;
    }

    public void setTemplateNames(List<String> templateNames) {
        this.templateNames = templateNames;
    }
}
