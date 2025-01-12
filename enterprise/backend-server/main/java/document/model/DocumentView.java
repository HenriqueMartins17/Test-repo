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

package com.apitable.enterprise.document.model;

import com.apitable.shared.support.serializer.ImageSerializer;
import com.apitable.shared.support.serializer.LocalDateTimeToMilliSerializer;
import com.apitable.shared.support.serializer.NullNumberSerializer;
import com.apitable.shared.support.serializer.NullStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * <p>
 * Document View.
 * </p>
 *
 * @author Chambers
 */
@Data
public class DocumentView {

    @Schema(description = "Creator uuid", example = "f468e447ea")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String creatorUuid;

    @Schema(description = "Creator name", example = "Tom")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String creatorName;

    @Schema(description = "Creator's avatar", example = "https://aitable.ai/xxx")
    @JsonSerialize(nullsUsing = NullStringSerializer.class, using = ImageSerializer.class)
    private String creatorAvatar;

    @Schema(description = "Creation timestamp (ms)", example = "1573561644000")
    @JsonSerialize(using = LocalDateTimeToMilliSerializer.class,
        nullsUsing = NullNumberSerializer.class)
    private LocalDateTime createdAt;

    @Schema(description = "Last modified by's uuid", example = "497685430b8fb9")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String lastModifiedByUuid;

    @Schema(description = "Last modified by", example = "Jack")
    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    private String lastModifiedBy;

    @Schema(description = "Last modified by's avatar", example = "https://aitable.ai/xxx")
    @JsonSerialize(nullsUsing = NullStringSerializer.class, using = ImageSerializer.class)
    private String lastModifiedByAvatar;

    @Schema(description = "Last modified timestamp (ms)", example = "1573561644000")
    @JsonSerialize(using = LocalDateTimeToMilliSerializer.class,
        nullsUsing = NullNumberSerializer.class)
    private LocalDateTime lastModifiedAt;

}
