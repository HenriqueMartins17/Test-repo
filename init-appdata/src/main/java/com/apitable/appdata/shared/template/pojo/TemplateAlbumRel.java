package com.apitable.appdata.shared.template.pojo;

import lombok.Data;

@Data
public class TemplateAlbumRel {

    private Long id;

    private String albumId;

    /**
     * Relate Type(0: template category, 1: template, 2: template tag)
     */
    private Integer type;

    /**
     * Relate Object Custom ID(0: category_code, 1: temlate_id, 2: tag_code)
     */
    private String relateId;

    public TemplateAlbumRel() {
    }

    public TemplateAlbumRel(String albumId, Integer type, String relateId) {
        this.albumId = albumId;
        this.type = type;
        this.relateId = relateId;
    }
}
