package cn.vika.keycloak.dao;

import cn.vika.keycloak.entity.SocialUserBindEntity;
import cn.vika.keycloak.entity.UserLinkEntity;
import lombok.extern.jbosslog.JBossLog;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Objects;

/**
 * <p>
 * 社交用户第三方平台关联
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/10/14 09:58
 */
@JBossLog
public class SocialUserBindDao {
    private final EntityManager entityManager;

    public SocialUserBindDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public SocialUserBindEntity getSocialUserByUnionId(String unionId) {
        log.infov("getSocialUserByUnionId(unionId: {0})", unionId);
        Query query = entityManager.createNativeQuery("select * from vika_social_user_bind where union_id = :unionId", SocialUserBindEntity.class);
        query.setParameter("unionId", unionId);
        Object result = null;
        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
            // ignore
        }
        return Objects.nonNull(result) ? (SocialUserBindEntity) result : null;
    }
}
