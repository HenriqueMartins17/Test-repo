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

package com.apitable.enterprise.gm.controller;

import static com.apitable.user.enums.DeveloperException.INVALID_DEVELOPER_TOKEN;

import cn.hutool.core.collection.CollUtil;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.gm.vo.DevelopUserVo;
import com.apitable.enterprise.gm.vo.DeveloperVo;
import com.apitable.enterprise.gm.vo.SpaceShowcaseVo;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.constants.ParamsConstants;
import com.apitable.shared.context.SessionContext;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.mapper.SpaceMapper;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.mapper.DeveloperMapper;
import com.apitable.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * vika-cli command line tool, directly call API commands.
 * </p>
 */
@RestController
@Tag(name = "Cli Authorization API")
@ApiResource(path = "/developer")
public class CliController {

    @Resource
    private DeveloperMapper developerMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private SpaceMapper spaceMapper;

    /**
     * Login authorization.
     */
    @PostResource(path = "/auth/login", requiredLogin = false)
    @Operation(summary = "Login authorization, using the developer's Api Key.")
    public ResponseData<DevelopUserVo> authLogin(@RequestParam("apiKey") String apiKey) {
        Long userId = developerMapper.selectUserIdByApiKey(apiKey);
        ExceptionUtil.isNotNull(userId, INVALID_DEVELOPER_TOKEN);
        UserEntity userEntity = userMapper.selectById(userId);
        DevelopUserVo vo = new DevelopUserVo();
        vo.setUserName(userEntity.getNickName());
        return ResponseData.success(vo);
    }

    /**
     * Create Developer Token.
     */
    @GetResource(path = "/new/token")
    @Operation(summary = "Create Developer Token", description = "The developer token is passed "
        + "for login. The network verifies whether the token is valid. The user name is returned "
        + "and cached locally. \n Generally speaking, this API is not used by vika-cli, but for "
        + "Web side web page operations.")
    @Parameters({
        @Parameter(name = "user_session_token", description = "Normal login Session Token of the "
            + "user.", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY,
            example = "AAABBB"),
    })
    public ResponseData<DeveloperVo> newToken() {

        // String sessionToken = getSessionToken();
        // checkSessionToken
        // String developerToken = developerService.newToken();
        // NewTokenRo ro = new NewTokenRo(developerToken)
        // return ResponseData.success(ro);

        return null;
    }

    /**
     * space list.
     */
    @GetResource(path = "/show/spaces", requiredPermission = false)
    @Operation(summary = "space list", description = "List the space owned by the user.")
    public ResponseData<List<SpaceShowcaseVo>> showSpaces() {
        Long userId = SessionContext.getUserId();
        List<SpaceShowcaseVo> spaceList = new ArrayList<>();
        List<SpaceEntity> spaces = spaceMapper.selectByUserId(userId);
        if (CollUtil.isEmpty(spaces)) {
            return ResponseData.success(spaceList);
        }
        for (SpaceEntity space : spaces) {
            SpaceShowcaseVo vo = new SpaceShowcaseVo();
            vo.setSpaceId(space.getSpaceId());
            vo.setSpaceName(space.getName());
            vo.setCreatedAt(space.getCreatedAt());
            spaceList.add(vo);
        }
        return ResponseData.success(spaceList);
    }

    /**
     * Listing cloud applications.
     */
    @GetResource(path = "/show/applets")
    @Operation(summary = "Listing cloud applications",
        description = "Lists all cloud applications in the specified space.")
    @Parameters({
        @Parameter(name = "Developer-Token", description = "developer token", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "AABBCC"),
        @Parameter(name = ParamsConstants.SPACE_ID, description = "space id", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "spcyQkKp9XJEl"),
    })
    public ResponseData<String> showApplets(String spaceId) {
        // String userId = developerService.getUserFromRequest();
        // var spaces = developerService.getApplets(userId);

        return null;
    }

    /**
     * Listing cloud hooks.
     */
    @GetResource(path = "/show/webhooks")
    @Operation(summary = "Listing cloud hooks",
        description = "Lists all cloud hooks in the specified applet.")
    @Parameters({
        @Parameter(name = "Developer-Token", description = "developer token", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "AABBCC"),
    })
    public ResponseData<String> showWebhooks(String appletId) {

        // String userId = developerService.getUserFromRequest();
        // var spaces = developerService.getWebhooks(appletId);

        return null;
    }

    /**
     * New Cloud application.
     */
    @GetResource(path = "/new/applet")
    @Operation(summary = "New Cloud application",
        description = "Create a new cloud application in the specified space.")
    @Parameters({
        @Parameter(name = "Developer-Token", description = "developer token", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "AABBCC"),
    })
    public ResponseData<String> newApplet(String spaceId) {
        return null;
    }

    /**
     * Creating a Cloud Hook.
     */
    @GetResource(path = "/new/webhook")
    @Operation(summary = "Creating a Cloud Hook",
        description = "Creates a cloud hook in the specified applet.")
    @Parameters({
        @Parameter(name = "Developer-Token", description = "developer token", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "AABBCC"),
    })
    public ResponseData<String> newWebhook() {
        return null;
    }

    /**
     * Upload plug-ins.
     */
    @GetResource(path = "/upload/plugin")
    @Operation(summary = "Upload plug-ins", description = "Specifies the applet upload plug-in.")
    @Parameters({
        @Parameter(name = "Developer-Token", description = "developer token", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "AABBCC"),
    })
    public ResponseData<String> uploadPlugin() {
        return null;
    }

    /**
     * Publish cloud applications.
     */
    @GetResource(path = "/publish/applet")
    @Operation(summary = "Publish cloud applications",
        description = "Specifies that the applet is published to the marketplace.")
    @Parameters({
        @Parameter(name = "Developer-Token", description = "developer token", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "AABBCC"),
    })
    public ResponseData<String> publishApplet() {
        return null;
    }

    /**
     * GraphQL Query.
     */
    @GetResource(path = "/graphql")
    @Operation(summary = "GraphQL Query", description = "Query using Graph QL")
    @Parameters({
        @Parameter(name = "Developer-Token", description = "developer token", required = true,
            schema = @Schema(type = "string"), in = ParameterIn.HEADER, example = "AABBCC"),
    })
    public ResponseData<String> graphql() {
        return null;
    }
}
