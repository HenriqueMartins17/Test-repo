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

package com.apitable.enterprise.idaas.model;

import com.apitable.enterprise.idaas.entity.IdaasGroupBindEntity;
import com.apitable.enterprise.idaas.entity.IdaasUserBindEntity;
import com.apitable.enterprise.idaas.infrastructure.model.GroupsResponse.GroupResponse;
import com.apitable.enterprise.idaas.infrastructure.model.UsersResponse.UserResponse;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * IDaaS Address book changes.
 * </p>
 */
@Setter
@Getter
public class IdaasContactChange {

    /**
     * add user group.
     */
    private List<GroupResponse> addGroups;

    /**
     * update user group.
     */
    private List<IdaasGroupBindEntity> updateGroups;

    /**
     * delete user group.
     */
    private List<IdaasGroupBindEntity> deleteGroups;

    /**
     * add users.
     */
    private List<UserResponse> addUsers;

    /**
     * add members.
     */
    private List<IdaasUserBindEntity> addMembers;

    /**
     * updater users.
     */
    private List<IdaasUserBindEntity> updateUsers;

    /**
     * delete members.
     */
    private List<Long> deleteMemberIds;

}
