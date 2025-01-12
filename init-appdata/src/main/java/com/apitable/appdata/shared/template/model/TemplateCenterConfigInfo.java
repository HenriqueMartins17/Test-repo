package com.apitable.appdata.shared.template.model;

import java.util.List;

public class TemplateCenterConfigInfo {

    private String i18n;

    private RecommendSetting recommend;

    private List<TemplateCategorySetting> categories;

    private List<TemplateAlbumSetting> albums;

    private List<TemplateSetting> templatesWithTag;

    public String getI18n() {
        return i18n;
    }

    public void setI18n(String i18n) {
        this.i18n = i18n;
    }

    public RecommendSetting getRecommend() {
        return recommend;
    }

    public void setRecommend(RecommendSetting recommend) {
        this.recommend = recommend;
    }

    public List<TemplateCategorySetting> getCategories() {
        return categories;
    }

    public void setCategories(List<TemplateCategorySetting> categories) {
        this.categories = categories;
    }

    public List<TemplateAlbumSetting> getAlbums() {
        return albums;
    }

    public void setAlbums(List<TemplateAlbumSetting> albums) {
        this.albums = albums;
    }

    public List<TemplateSetting> getTemplatesWithTag() {
        return templatesWithTag;
    }

    public void setTemplatesWithTag(List<TemplateSetting> templatesWithTag) {
        this.templatesWithTag = templatesWithTag;
    }
}
