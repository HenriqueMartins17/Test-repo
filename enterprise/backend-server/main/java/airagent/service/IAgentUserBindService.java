package com.apitable.enterprise.airagent.service;

import com.apitable.enterprise.airagent.entity.AgentUserBindEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * agent user bind service interface.
 */
public interface IAgentUserBindService extends IService<AgentUserBindEntity> {

    /**
     * get by external key.
     *
     * @param externalKey external key
     * @return AgentUserBindEntity
     */
    AgentUserBindEntity getByExternalKey(String externalKey);

    /**
     * get user id by external key.
     *
     * @param externalKey external key
     * @return Long
     */
    Long getUserIdByExternalKey(String externalKey);

    /**
     * create user bind.
     *
     * @param userId      userId
     * @param externalKey externalKey
     */
    void create(Long userId, String externalKey);
}
