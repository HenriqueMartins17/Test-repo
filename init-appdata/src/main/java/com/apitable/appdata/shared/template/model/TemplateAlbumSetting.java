package com.apitable.appdata.shared.template.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TemplateAlbumSetting {

    private String name;

    private String cover;

    private String description;

    private String content;

    @JsonProperty("publisher_name")
    private String publisherName;

    @JsonProperty("publisher_logo")
    private String publisherLogo;

    @JsonProperty("publisher_desc")
    private String publisherDesc;

    @JsonProperty("template_names")
    private List<String> templateNames;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getPublisherLogo() {
        return publisherLogo;
    }

    public void setPublisherLogo(String publisherLogo) {
        this.publisherLogo = publisherLogo;
    }

    public String getPublisherDesc() {
        return publisherDesc;
    }

    public void setPublisherDesc(String publisherDesc) {
        this.publisherDesc = publisherDesc;
    }

    public List<String> getTemplateNames() {
        return templateNames;
    }

    public void setTemplateNames(List<String> templateNames) {
        this.templateNames = templateNames;
    }
}
