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

package com.apitable.enterprise.wechat.vo;

import com.apitable.shared.support.serializer.NullBooleanSerializer;
import com.apitable.shared.support.serializer.NullStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * WeChat login result vo.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "WeChat login result vo")
public class WeChatLoginResultVo {

    @Builder.Default
    @Schema(description = "Whether the vika account has been bound", example = "false")
    private Boolean isBind = false;

    @Builder.Default
    @Schema(description = "Whether it is necessary to create a space indicates that the user does"
        + " not have any space association, which is a standard field for space creation "
        + "guidance", example = "false")
    private Boolean needCreate = true;

    @JsonSerialize(nullsUsing = NullStringSerializer.class)
    @Schema(description = "Nickname", example = "Zhang San, when the content is empty, you need "
        + "to enter the screen name setting page")
    private String nickName;

    @Schema(description = "Is it a new registered user", hidden = true)
    private boolean newUser;

    @Schema(description = "New registered user table ID", hidden = true)
    private Long userId;

    @Schema(description = "Whether the union id already exists", example = "false")
    @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
    private Boolean hasUnion;
}
