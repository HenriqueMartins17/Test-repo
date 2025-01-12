package com.apitable.appdata.shared.space.pojo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Space {

    private Long id;

    private String spaceId;

    private String name;

    private String logo;

    private String props;

    private LocalDateTime preDeletionTime;

    private Long owner;

    private Long creator;

    private Long createdBy;

    private Long updatedBy;

}
