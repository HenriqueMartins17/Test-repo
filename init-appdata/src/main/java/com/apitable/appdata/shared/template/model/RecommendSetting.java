package com.apitable.appdata.shared.template.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RecommendSetting {

    private List<BannerSetting> banners;

    @JsonProperty("album_groups")
    private List<AlbumGroupSetting> albumGroups;

    @JsonProperty("template_groups")
    private List<TemplateGroupSetting> templateGroups;

    public RecommendSetting() {
    }

    public RecommendSetting(List<BannerSetting> banners, List<AlbumGroupSetting> albumGroups, List<TemplateGroupSetting> templateGroups) {
        this.banners = banners;
        this.albumGroups = albumGroups;
        this.templateGroups = templateGroups;
    }

    public List<BannerSetting> getBanners() {
        return banners;
    }

    public void setBanners(List<BannerSetting> banners) {
        this.banners = banners;
    }

    public List<AlbumGroupSetting> getAlbumGroups() {
        return albumGroups;
    }

    public void setAlbumGroups(List<AlbumGroupSetting> albumGroupSettings) {
        this.albumGroups = albumGroupSettings;
    }

    public List<TemplateGroupSetting> getTemplateGroups() {
        return templateGroups;
    }

    public void setTemplateGroups(List<TemplateGroupSetting> templateGroupSettings) {
        this.templateGroups = templateGroupSettings;
    }


    public static class BannerSetting {

        @JsonProperty("template_name")
        private String templateName;

        private String image;

        private String title;

        private String desc;

        private String color;

        public BannerSetting(String templateName, String image, String title, String desc, String color) {
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

    public static class AlbumGroupSetting {
        private String name;

        @JsonProperty("album_names")
        private List<String> albumNames;

        public AlbumGroupSetting(String name, List<String> albumNames) {
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

    public static class TemplateGroupSetting {
        private String name;

        @JsonProperty("template_names")
        private List<String> templateNames;

        public TemplateGroupSetting(String name, List<String> templateNames) {
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
