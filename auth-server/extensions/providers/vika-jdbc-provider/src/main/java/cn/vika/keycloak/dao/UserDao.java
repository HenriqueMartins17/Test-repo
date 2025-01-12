package cn.vika.keycloak.dao;

import cn.vika.keycloak.entity.UserEntity;
import lombok.extern.jbosslog.JBossLog;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 维格表用户
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/15 21:52
 */
@JBossLog
public class UserDao {

    private final EntityManager entityManager;

    public UserDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<UserEntity> getUserByUsername(String username) {
        log.infov("getUserByUsername(username: {0})", username);
        Query query = entityManager.createNativeQuery("select * from vika_user where mobile_phone = :mobilePhone", UserEntity.class);
        query.setParameter("mobilePhone", username);
        return query.getResultList().stream().findFirst();
    }

    public Optional<UserEntity> getUserByEmail(String email) {
        log.infov("getUserByEmail(email: {0})", email);
        Query query = entityManager.createNativeQuery("select * from vika_user where email = :email", UserEntity.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst();
    }

    public Optional<String> getPasswordByUsername(String username) {
        log.infov("getPasswordByUsername(username: {0})", username);
        Query query = entityManager.createNativeQuery("select password from vika_user where mobile_phone = :mobilePhone");
        query.setParameter("mobilePhone", username);
        String password = Objects.isNull(query.getSingleResult()) ? null : (String) query.getSingleResult();
        return Optional.ofNullable(password);
    }

    public Optional<String> getPasswordByEmail(String email) {
        log.infov("getPasswordByEmail(email: {0})", email);
        Query query = entityManager.createNativeQuery("select password from vika_user where email = :email");
        query.setParameter("email", email);
        String password = Objects.isNull(query.getSingleResult()) ? null : (String) query.getSingleResult();
        return Optional.ofNullable(password);
    }

    public UserEntity getUserById(Long id) {
        log.infov("getUserById(id: {0})", id);
        Query query = entityManager.createNativeQuery("select * from vika_user where id = :id", UserEntity.class);
        query.setParameter("id", id);
        Object result = query.getSingleResult();
        return Objects.nonNull(result) ? (UserEntity) result : null;
    }

    public void close() {
        this.entityManager.close();
    }
}
