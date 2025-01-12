package com.apitable.enterprise.social.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OneAccess UserSchema  define vo.
 */
@Setter
@Getter
@Schema(description = "OneAccess UserInfo vo")
public class OneAccessUserQueryVo extends OneAccessBaseVo {

    @Schema(description = "Account detail", example = "")
    private Account account;

    public OneAccessUserQueryVo(String bimRequestId) {
        super(bimRequestId);
    }
    /**
     * Bind User Info VO.
     */
    @Setter
    @Getter
    @ToString
    @Builder
    public static class Account {

        @Schema(description = "Org ID", example = "org-xxxx")
        private String orgId;

        @Schema(description = "Login Name", example = "tom")
        private String loginName;

        @Schema(description = "Full name", example = "jack tom")
        private String fullName;

        @Schema(description = "Internal userId", example = "D00001")
        private String uid;

    }
}
