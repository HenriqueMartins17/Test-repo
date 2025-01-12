package cn.vika.keycloak.providers.storage;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * 用户存储查询接口
 * @author Shawn Deng
 * @date 2021-09-17 11:01:29
 */
public class UserRepository {

    private final EntityManager em;

    public UserRepository(EntityManager em) {
        this.em = em;
    }

    public UserEntity findUserById(Long id) {
        return em.find(UserEntity.class, id);
    }

    public List<UserEntity> findUserByMobilePhone(String mobilePhone) {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT c FROM UserEntity c WHERE c.mobilePhone = :mobilePhone", UserEntity.class);
        return query.setParameter("mobilePhone", mobilePhone).getResultList();
    }

    public List<UserEntity> findUsersByEmail(String email) {
        TypedQuery<UserEntity> query = em.createQuery(
                "SELECT c FROM UserEntity c WHERE c.email = :email", UserEntity.class);
        return query.setParameter("email", email).getResultList();
    }
}
