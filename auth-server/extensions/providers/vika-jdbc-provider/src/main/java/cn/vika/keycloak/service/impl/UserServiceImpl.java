package cn.vika.keycloak.service.impl;

import cn.vika.keycloak.dao.SocialUserBindDao;
import cn.vika.keycloak.dao.UserDao;
import cn.vika.keycloak.dao.UserLinkDao;
import cn.vika.keycloak.entity.SocialUserBindEntity;
import cn.vika.keycloak.entity.UserEntity;
import cn.vika.keycloak.entity.UserLinkEntity;
import cn.vika.keycloak.service.UserService;

import javax.persistence.EntityManagerFactory;
import java.util.Objects;

/**
 * <p>
 * 用户相关服务
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/10/14 10:46
 */
public class UserServiceImpl implements UserService {
    private UserDao userDao;
    private UserLinkDao userLinkDao;
    private SocialUserBindDao socialUserBindDao;

    public UserServiceImpl(EntityManagerFactory entityManagerFactory) {
        this.userDao = new UserDao(entityManagerFactory.createEntityManager());
        this.userLinkDao = new UserLinkDao(entityManagerFactory.createEntityManager());
        this.socialUserBindDao = new SocialUserBindDao(entityManagerFactory.createEntityManager());
    }

    @Override
    public UserEntity getUserByOpenId(String openId) {
        UserLinkEntity userLinkEntity = userLinkDao.getUserLinkByOpenId(openId);
        if (Objects.nonNull(userLinkEntity)) {
            return userDao.getUserById(userLinkEntity.getUserId());
        }
        return null;
    }

    @Override
    public UserEntity getUserByUnionId(String unionId) {
        UserLinkEntity userLinkEntity = userLinkDao.getUserLinkByUnionId(unionId);
        if (Objects.nonNull(userLinkEntity)) {
            return userDao.getUserById(userLinkEntity.getUserId());
        }
        return null;
    }

    @Override
    public UserEntity getSocialUserByUnionId(String unionId) {
        SocialUserBindEntity socialUserBindEntity = socialUserBindDao.getSocialUserByUnionId(unionId);
        if (Objects.nonNull(socialUserBindEntity)) {
            return userDao.getUserById(socialUserBindEntity.getUserId());
        }
        return null;
    }
}
