/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.vika.core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * Recommend Config Info
 * </p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendTemplateInfo {

    @JsonProperty("SUBJECT")
    private List<String> subject;

    @JsonProperty("LAYOUT")
    private String layout;

    @JsonProperty("CUSTOM_GROUP")
    private String customGroup;

    @JsonProperty("TITLE")
    private String title;

    @JsonProperty("DESC")
    private String description;

    @JsonProperty("BANNER")
    private List<AttachmentField> banners;

    @JsonProperty("COLOR")
    private String color;

    @JsonProperty("i18n")
    private String i18n;

    public String getSubjectValue() {
        return subject == null || subject.isEmpty() ? null : subject.get(0);
    }

    public void setSubject(List<String> subject) {
        this.subject = subject;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getCustomGroup() {
        return customGroup;
    }

    public void setCustomGroup(String customGroup) {
        this.customGroup = customGroup;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AttachmentField> getBanners() {
        return banners;
    }

    public void setBanners(List<AttachmentField> banners) {
        this.banners = banners;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getI18n() {
        return i18n;
    }

    public void setI18n(String i18n) {
        this.i18n = i18n;
    }
}
