/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.audit.model;

import com.apitable.shared.support.serializer.ImageSerializer;
import com.apitable.shared.support.serializer.LocalDateTimeToMilliSerializer;
import com.apitable.shared.support.serializer.NullBooleanSerializer;
import com.apitable.shared.support.serializer.NullStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Space Audit Page VO.
 */
@Data
@Schema(description = "Space Audit Info Page")
public class SpaceAuditPageVO {

    @Schema(description = "created time(millisecond)", type = "java.lang.Long", example =
        "1573561644000")
    @JsonSerialize(using = LocalDateTimeToMilliSerializer.class)
    private LocalDateTime createdAt;

    @Schema(description = "action", type = "java.lang.String", example = "create_space")
    private String action;

    @Schema(description = "operator")
    private Operator operator;

    @Schema(description = "audit content")
    private AuditContent body;

    /**
     * Operator.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Schema(description = "Operator Info")
    public static class Operator {

        @Schema(description = "member id", type = "java.lang.String", example = "1")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long memberId;

        @JsonSerialize(nullsUsing = NullStringSerializer.class, using = ImageSerializer.class)
        @Schema(description = "avatar url", example = "http://wwww.apitable"
            + ".com/2019/11/12/17123187253.png")
        private String avatar;

        @Schema(description = "member name", example = "Tony")
        private String memberName;

        @Schema(description = "is active", example = "true")
        @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
        private Boolean isActive;

        @Schema(description = "is deleted", example = "true")
        @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
        private Boolean isDeleted;

        @Schema(description = "whether the nickname has been modified")
        @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
        private Boolean isNickNameModified;

        @Schema(description = "whether the member name has been modified")
        @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
        private Boolean isMemberNameModified;
    }

    /**
     * AuditContent.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Schema(description = "Audit Content")
    public static class AuditContent {

        @Schema(description = "space info")
        private Space space;

        @Schema(description = "unit infos")
        private List<Unit> units;

        @Schema(description = "control")
        private Control control;

        @Schema(description = "node info")
        private Node node;

        @Schema(description = "temlate info")
        private Template template;
    }

    /**
     * Space.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Schema(description = "Space Info")
    public static class Space {

        @Schema(description = "spaceId")
        private String spaceId;

        @Schema(description = "old space name（before operation, fixed）")
        private String oldSpaceName;

        @Schema(description = "space name（operating at the time，fixed）")
        private String spaceName;

        @Schema(description = "old space logo（before operation, fixed）")
        @JsonSerialize(nullsUsing = NullStringSerializer.class, using = ImageSerializer.class)
        private String oldSpaceLogo;

        @Schema(description = "space log（operating at the time，fixed）")
        @JsonSerialize(nullsUsing = NullStringSerializer.class, using = ImageSerializer.class)
        private String spaceLogo;
    }

    /**
     * Unit.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Schema(description = "Unit Info")
    public static class Unit {

        @Schema(description = "unitId")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long unitId;

        @Schema(description = "unit type")
        private Integer type;

        @Schema(description = "unit name")
        private String name;

        @Schema(description = "member avatar (optional)")
        @JsonSerialize(nullsUsing = NullStringSerializer.class, using = ImageSerializer.class)
        private String avatar;

        @Schema(description = "is dctive", example = "true")
        @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
        private Boolean isActive;

        @Schema(description = "is deleted", example = "true")
        @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
        private Boolean isDeleted;
    }

    /**
     public static class Control {
     * .
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Schema(description = "Control Info")
    public static class Control {

        @Schema(description = "old role")
        private String oldRole;

        @Schema(description = "role")
        private String role;
    }

    /**
     * Node.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Schema(description = "Node Info")
    public static class Node {

        @Schema(description = "node id")
        private String nodeId;

        @Schema(description = "node type")
        private Integer nodeType;

        @Schema(description = "old parent node name（before operation, fixed）")
        private String oldParentName;

        @Schema(description = "parent node name（operating at the time，fixed）")
        private String parentName;

        @Schema(description = "old node name（before operation, fixed）")
        private String oldNodeName;

        @Schema(description = "node name（operating at the time，fixed)")
        private String nodeName;

        @Schema(description = "current node name（the latest，dynamic）")
        private String currentNodeName;

        @Schema(description = "old node icon（before operation, fixed）")
        @JsonSerialize(nullsUsing = NullStringSerializer.class)
        private String oldNodeIcon;

        @Schema(description = "node icon（operating at the time，fixed）")
        @JsonSerialize(nullsUsing = NullStringSerializer.class)
        private String nodeIcon;

        @Schema(description = "current node icon（the latest，dynamic）")
        @JsonSerialize(nullsUsing = NullStringSerializer.class)
        private String currentNodeIcon;

        @Schema(description = "the replicated node id")
        private String sourceNodeId;

        @Schema(description = "the replicated node name（operating at the time，fixed）")
        private String sourceNodeName;
    }

    /**
     * Template.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @Schema(description = "Template Info")
    public static class Template {

        @Schema(description = "template id")
        private String templateId;

        @Schema(description = "template name")
        private String templateName;
    }
}
