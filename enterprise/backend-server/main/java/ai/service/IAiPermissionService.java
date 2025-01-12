package com.apitable.enterprise.ai.service;

import com.apitable.control.infrastructure.permission.PermissionDefinition;

/**
 * ai permission service.
 */
public interface IAiPermissionService {

    /**
     * anonymous valid.
     *
     * @param aiId   ai id
     * @param userId user id or null
     */
    void anonymousValid(String aiId, Long userId);

    /**
     * valid permission.
     *
     * @param aiId       ai id
     * @param userId     user id
     * @param permission permission
     */
    void validPermission(String aiId, Long userId, PermissionDefinition permission);
}
