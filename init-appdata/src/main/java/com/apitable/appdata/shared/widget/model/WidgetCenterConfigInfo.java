package com.apitable.appdata.shared.widget.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WidgetCenterConfigInfo extends WidgetAuthorInfo {

    @JsonProperty("package_id")
    private String packageId;

    @JsonProperty("i18n_name")
    private String i18nName;

    private String icon;

    @JsonProperty("i18n_description")
    private String i18nDescription;

    private String cover;

    @JsonProperty("is_template")
    private Boolean isTemplate;

    @JsonProperty("widget_body")
    private String widgetBody;

    @JsonProperty("release_code_bundle")
    private String releaseCodeBundle;

    @JsonProperty("source_code_bundle")
    private String sourceCodeBundle;

    @JsonProperty("widget_sort")
    private Integer widgetSort;

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getI18nName() {
        return i18nName;
    }

    public void setI18nName(String i18nName) {
        this.i18nName = i18nName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getI18nDescription() {
        return i18nDescription;
    }

    public void setI18nDescription(String i18nDescription) {
        this.i18nDescription = i18nDescription;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public Boolean getTemplate() {
        return isTemplate;
    }

    public void setTemplate(Boolean template) {
        isTemplate = template;
    }

    public String getWidgetBody() {
        return widgetBody;
    }

    public void setWidgetBody(String widgetBody) {
        this.widgetBody = widgetBody;
    }

    public String getReleaseCodeBundle() {
        return releaseCodeBundle;
    }

    public void setReleaseCodeBundle(String releaseCodeBundle) {
        this.releaseCodeBundle = releaseCodeBundle;
    }

    public String getSourceCodeBundle() {
        return sourceCodeBundle;
    }

    public void setSourceCodeBundle(String sourceCodeBundle) {
        this.sourceCodeBundle = sourceCodeBundle;
    }

    public Integer getWidgetSort() {
        return widgetSort;
    }

    public void setWidgetSort(Integer widgetSort) {
        this.widgetSort = widgetSort;
    }
}
