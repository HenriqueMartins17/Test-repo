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

package com.apitable.enterprise.social.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;

/**
 * OneAccess base information.
 */
@Data
@Schema(description = "OneAccess base information")
public class OneAccessBaseVo implements Serializable {

    private static final long serialVersionUID = 6941456238190558553L;

    @Schema(description = " The request ID sent by the OneAccess each time the interface is called")
    private String bimRequestId;

    @Schema(description = "The result code of the interface call processing")
    private String resultCode;

    @Schema(description = "Interface call processing information")
    private String message;

    /**
     * OneAccessBaseVo.
     */
    public OneAccessBaseVo(String bimRequestId) {
        this.bimRequestId = bimRequestId;
        this.resultCode = "0";
        this.message = "success";
    }
}
