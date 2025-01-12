package com.apitable.enterprise.apitablebilling.mapper;

import com.apitable.enterprise.apitablebilling.appsumo.entity.AppsumoEventLogEntity;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoEventDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * appsumo event log mapper.
 */
public interface AppsumoEventLogMapper extends BaseMapper<AppsumoEventLogEntity> {

    /**
     * query bu id.
     *
     * @param id primary key
     * @return AppsumoEventDTO
     */
    AppsumoEventDTO selectSimpleById(@Param("id") Long id);

    /**
     * query by email.
     *
     * @param activationEmail email
     * @param action          action
     * @param handleStatus    status
     * @return AppsumoEventDTO
     */

    AppsumoEventDTO selectByActivationEmailAndActionAndHandleStatus(
        @Param("activationEmail") String activationEmail, @Param("action") String action,
        @Param("handleStatus") Integer handleStatus);

    /**
     * update user email.
     *
     * @param activationEmail activation email
     * @param userEmail user email
     * @return rows updated
     */
    int updateUserEmailByActivationEmail(@Param("activationEmail") String activationEmail, @Param("userEmail") String userEmail);

    /**
     * query user email.
     *
     * @param activationEmail activation email
     * @param uuid license uuid
     * @return user email
     */
    String selectUserEmailByActivationEmailAndUuid(@Param("activationEmail") String activationEmail,
                                                   @Param("uuid") String uuid);
}
