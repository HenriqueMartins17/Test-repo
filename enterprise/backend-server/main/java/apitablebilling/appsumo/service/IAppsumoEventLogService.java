package com.apitable.enterprise.apitablebilling.appsumo.service;

import com.apitable.enterprise.apitablebilling.appsumo.entity.AppsumoEventLogEntity;
import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoHandleStatus;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoEventDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * appsumo event log service interface.
 */
public interface IAppsumoEventLogService extends IService<AppsumoEventLogEntity> {
    /**
     * create event.
     *
     * @param action action:activate,enhance_tier,reduce_tier,refund,update
     * @param planId our plan id
     * @param uuid appsumo product uuid
     * @param activationEmail user email
     * @param invoiceItemUuid price id
     */
    Long create(String action, String planId, String uuid, String activationEmail,
                String invoiceItemUuid);

    /**
     * update handle status.
     *
     * @param logId primary key
     * @param  status status
     */
    void updateStatus(Long logId, AppsumoHandleStatus status);

    /**
     * get by id.
     *
     * @param id primary key
     * @return AppsumoEventDTO
     */
    AppsumoEventDTO getSimpleInfoById(Long id);

    /**
     * get by email.
     *
     * @param email user email
     * @return AppsumoEventDTO
     */
    AppsumoEventDTO getHandlingActivationEvent(String email);

    /**
     * update user email.
     *
     * @param activationEmail activation email
     */
    void updateUserEmail(String activationEmail, String userEmail);

    /**
     * get user email.
     *
     * @param activationEmail activation email
     * @param uuid license uuid
     *
     * @return user email
     */
    String getUserEmailByActivationEmailAndUuid(String activationEmail, String uuid);
}
