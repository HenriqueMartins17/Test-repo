package cn.vika.keycloak.dao;

import cn.vika.keycloak.entity.UserLinkEntity;
import lombok.extern.jbosslog.JBossLog;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Objects;

/**
 * <p>
 * 用户第三方平台关联
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/10/14 09:58
 */
@JBossLog
public class UserLinkDao {
    private final EntityManager entityManager;

    public UserLinkDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public UserLinkEntity getUserLinkByOpenId(String openId) {
        log.infov("getUserLinkByOpenId(id: {0})", openId);
        Query query = entityManager.createNativeQuery("select * from vika_user_link where open_id = :openId", UserLinkEntity.class);
        query.setParameter("openId", openId);
        Object result = null;
        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
            // ignore
        }
        return Objects.nonNull(result) ? (UserLinkEntity) result : null;
    }

    public UserLinkEntity getUserLinkByUnionId(String unionId) {
        log.infov("getUserLinkByUnionId(unionId: {0})", unionId);
        Query query = entityManager.createNativeQuery("select * from vika_user_link where union_id = :unionId limit 1", UserLinkEntity.class);
        query.setParameter("unionId", unionId);
        Object result = null;
        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
            // ignore
        }
        return Objects.nonNull(result) ? (UserLinkEntity) result : null;
    }
}
