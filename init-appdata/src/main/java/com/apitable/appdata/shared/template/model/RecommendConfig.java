package com.apitable.appdata.shared.template.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendConfig {

    /**
     * top banner
     */
    private List<Banner> top;

    /**
     * custom album group config
     */
    private List<AlbumGroup> albumGroups;

    /**
     * custom template group config
     */
    private List<TemplateGroup> templateGroups;

    @Data
    @AllArgsConstructor
    public static class Banner {
        private String templateId;

        private String image;

        private String title;

        private String desc;

        private String color;
    }

    @Data
    public static class AlbumGroup {
        private String name;

        private List<String> albumIds;
    }

    @Data
    public static class TemplateGroup {
        private String name;

        private List<String> templateIds;
    }

}
