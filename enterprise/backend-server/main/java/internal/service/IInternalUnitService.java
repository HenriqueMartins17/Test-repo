package com.apitable.enterprise.internal.service;

import com.apitable.control.infrastructure.permission.space.resource.ResourceCode;
import com.apitable.core.exception.BaseException;

/**
 * Internal--unit interface.
 */
public interface IInternalUnitService {
    /**
     * check permission.
     *
     * @param code resource code
     * @param exception the exception should throw
     */
    void checkResourcePermission(ResourceCode code, BaseException exception);
}
