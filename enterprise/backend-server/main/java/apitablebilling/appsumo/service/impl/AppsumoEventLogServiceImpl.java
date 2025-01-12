package com.apitable.enterprise.apitablebilling.appsumo.service.impl;

import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.apitablebilling.appsumo.entity.AppsumoEventLogEntity;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoAction;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoHandleStatus;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoEventDTO;
import com.apitable.enterprise.apitablebilling.appsumo.service.IAppsumoEventLogService;
import com.apitable.enterprise.apitablebilling.mapper.AppsumoEventLogMapper;
import com.apitable.user.service.IUserService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * appsumo event log service implementation.
 */
@Service
public class AppsumoEventLogServiceImpl
    extends ServiceImpl<AppsumoEventLogMapper, AppsumoEventLogEntity>
    implements IAppsumoEventLogService {

    @Resource
    private IUserService iUserService;

    @Override
    public Long create(String action, String planId, String uuid, String activationEmail,
                       String invoiceItemUuid) {
        Long id = IdWorker.getId();
        AppsumoEventLogEntity entity =
            AppsumoEventLogEntity.builder().id(id).action(action).uuid(uuid).planId(planId)
                .activationEmail(activationEmail).build();
        if (null != invoiceItemUuid) {
            entity.setInvoiceItemUuid(invoiceItemUuid);
        }
        save(entity);
        return id;
    }

    @Override
    public void updateStatus(Long logId, AppsumoHandleStatus status) {
        AppsumoEventLogEntity entity =
            AppsumoEventLogEntity.builder().id(logId).handleStatus(status.getStatus()).build();
        updateById(entity);
    }

    @Override
    public AppsumoEventDTO getSimpleInfoById(Long id) {
        return baseMapper.selectSimpleById(id);
    }

    @Override
    public AppsumoEventDTO getHandlingActivationEvent(String email) {
        return baseMapper.selectByActivationEmailAndActionAndHandleStatus(email,
            AppsumoAction.ACTIVATE.getAction(), AppsumoHandleStatus.HANDLING.getStatus());
    }

    @Override
    public void updateUserEmail(String activationEmail, String userEmail) {
        baseMapper.updateUserEmailByActivationEmail(activationEmail, userEmail);
    }

    @Override
    public String getUserEmailByActivationEmailAndUuid(String activationEmail, String uuid) {
        String userEmail =
            baseMapper.selectUserEmailByActivationEmailAndUuid(activationEmail, uuid);
        return StrUtil.isNotBlank(userEmail) ? userEmail : activationEmail;
    }
}
