package com.apitable.enterprise.social.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * OneAccess UserSchema  define vo.
 */
@Setter
@Getter
@Schema(description = "OneAccess UserInfo vo")
public class OneAccessOrgVo extends OneAccessBaseVo {

    @Schema(description = "Account detail", example = "")
    private Organization organization;

    public OneAccessOrgVo(String bimRequestId) {
        super(bimRequestId);
    }
    /**
     * Bind User Info VO.
     */
    @Setter
    @Getter
    @Builder
    public static class Organization {

        @Schema(description = "Org ID", example = "org-xxxx")
        private String orgId;

        @Schema(description = "Org Name", example = "dpart-1")
        private String orgName;

        @Schema(description = "parent Org id", example = "parent orgId")
        private String parentOrgId;

    }
}
