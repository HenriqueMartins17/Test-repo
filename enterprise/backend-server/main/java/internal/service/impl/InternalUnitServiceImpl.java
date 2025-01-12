package com.apitable.enterprise.internal.service.impl;

import com.apitable.control.infrastructure.permission.space.resource.ResourceCode;
import com.apitable.core.exception.BaseException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.internal.service.IInternalUnitService;
import com.apitable.shared.context.LoginContext;
import com.apitable.space.service.ISpaceRoleService;
import java.util.Collections;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Internal--unit service implement.
 */
@Slf4j
@Service
public class InternalUnitServiceImpl implements IInternalUnitService {
    @Resource
    private ISpaceRoleService iSpaceRoleService;

    @Override
    public void checkResourcePermission(ResourceCode code, BaseException exception) {
        String spaceId = LoginContext.me().getSpaceId();
        Long memberId = LoginContext.me().getMemberId();
        // check resource permission
        iSpaceRoleService.checkCanOperate(spaceId, memberId,
            Collections.singletonList(code.getCode()),
            status -> ExceptionUtil.isFalse(status, exception));
    }
}
