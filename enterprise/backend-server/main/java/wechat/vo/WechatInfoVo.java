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

import com.apitable.shared.support.serializer.ImageSerializer;
import com.apitable.shared.support.serializer.LocalDateTimeToMilliSerializer;
import com.apitable.shared.support.serializer.NullNumberSerializer;
import com.apitable.shared.support.serializer.NullStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * WeChat member information vo.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "WeChat member information vo")
public class WechatInfoVo {

    @Schema(description = "Nickname", example = "This is a nickname")
    private String nickName;

    @JsonSerialize(nullsUsing = NullStringSerializer.class, using = ImageSerializer.class)
    @Schema(description = "Avatar", example = "https://wx.qlogo.cn/BRp2a")
    private String avatar;

    @Schema(description = "Phone number", example = "\"13344445555\"")
    private String mobile;

    @Schema(description = "Email", example = "admin@vikadata.com")
    private String email;

    @Schema(description = "Space name", example = "My Workspace")
    private String spaceName;

    @JsonSerialize(nullsUsing = NullStringSerializer.class, using = ImageSerializer.class)
    @Schema(description = "Space logo", example = "http://...")
    private String spaceLogo;

    @Schema(description = "Creator name", example = "Zhang San")
    private String creatorName;

    @Schema(description = "Space owner name", example = "Li Si")
    private String ownerName;

    @Schema(description = "Creation timestamp (ms)", example = "1573561644000")
    @JsonSerialize(using = LocalDateTimeToMilliSerializer.class)
    private LocalDateTime createTime;

    @Schema(description = "Number of people on hand", example = "20")
    private Long memberNumber;

    @Schema(description = "Number of departments", example = "5")
    private Long teamNumber;

    @Schema(description = "Number of documents", example = "5")
    @JsonSerialize(nullsUsing = NullNumberSerializer.class)
    private Long fileNumber;

    @Schema(description = "Total Records", example = "5")
    private Long recordNumber;

    @Schema(description = "Used space (unit: byte)", example = "1024")
    private Long usedSpace;

    @Schema(description = "Total capacity (unit: byte)", example = "1024")
    private Long maxMemory;
}
