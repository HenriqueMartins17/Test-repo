package com.apitable.appdata.shared.template.pojo;

import lombok.Data;

@Data
public class TemplateAlbum {

    private Long id;

    private String albumId;

    private String i18nName;

    private String name;

    /**
     * Template Album Cover Token(The Relative Path of Asset)
     */
    private String cover;

    private String description;

    private String content;

    private String authorName;

    /**
     * Author Logo Token(The Relative Path of Asset)
     */
    private String authorLogo;

    private String authorDesc;

}
