package cn.vika.keycloak.service;

import cn.vika.keycloak.entity.UserEntity;

/**
 * <p>
 * 用户相关服务
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/10/14 10:44
 */
public interface UserService {

    /**
     * 通过openId查询用户
     *
     * @param openId
     * @return
     */
    UserEntity getUserByOpenId(String openId);

    /**
     * 通过unionId查询用户
     *
     * @param unionId
     * @return
     */
    UserEntity getUserByUnionId(String unionId);

    /**
     * 通过unionId查询社交用户
     *
     * @param unionId
     * @return
     */
    UserEntity getSocialUserByUnionId(String unionId);
}
