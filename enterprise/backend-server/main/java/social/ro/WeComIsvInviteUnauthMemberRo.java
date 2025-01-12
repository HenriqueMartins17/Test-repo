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

package com.apitable.enterprise.social.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

/**
 * <p>
 * Invite unauthorized users.
 * </p>
 */
@Schema(description = "Invite unauthorized users")
@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode
@Validated
public class WeComIsvInviteUnauthMemberRo {

    @Schema(description = "Space ID", required = true)
    @NotBlank
    private String spaceId;

    @Schema(description = "Select Invited Member Ticket List", required = true)
    @NotEmpty
    private List<String> selectedTickets;

}
