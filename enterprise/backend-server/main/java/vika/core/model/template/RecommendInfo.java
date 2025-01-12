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

package com.apitable.enterprise.vika.core.model.template;

import java.util.List;

/**
 * <p>
 * Template Center - Recommend Info
 * </p>
 *
 */
public class RecommendInfo {

    private List<BannerInfo> top;

    private List<AlbumGroup> albumGroups;

    private List<TemplateGroup> templateGroups;

    public RecommendInfo() {
    }

    public RecommendInfo(List<BannerInfo> top, List<AlbumGroup> albumGroups, List<TemplateGroup> templateGroups) {
        this.top = top;
        this.albumGroups = albumGroups;
        this.templateGroups = templateGroups;
    }

    public List<BannerInfo> getTop() {
        return top;
    }

    public void setTop(List<BannerInfo> top) {
        this.top = top;
    }

    public List<AlbumGroup> getAlbumGroups() {
        return albumGroups;
    }

    public void setAlbumGroups(List<AlbumGroup> albumGroups) {
        this.albumGroups = albumGroups;
    }

    public List<TemplateGroup> getTemplateGroups() {
        return templateGroups;
    }

    public void setTemplateGroups(List<TemplateGroup> templateGroups) {
        this.templateGroups = templateGroups;
    }


    public static class BannerInfo {
        private String templateName;

        private String image;

        private String title;

        private String desc;

        private String color;

        public BannerInfo(String templateName, String image, String title, String desc, String color) {
            this.templateName = templateName;
            this.image = image;
            this.title = title;
            this.desc = desc;
            this.color = color;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }

    public static class AlbumGroup {
        private String name;

        private List<String> albumNames;

        public AlbumGroup(String name, List<String> albumNames) {
            this.name = name;
            this.albumNames = albumNames;
        }

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
    }

    public static class TemplateGroup {
        private String name;

        private List<String> templateNames;

        public TemplateGroup(String name, List<String> templateNames) {
            this.name = name;
            this.templateNames = templateNames;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getTemplateNames() {
            return templateNames;
        }

        public void setTemplateNames(List<String> templateNames) {
            this.templateNames = templateNames;
        }
    }

}
