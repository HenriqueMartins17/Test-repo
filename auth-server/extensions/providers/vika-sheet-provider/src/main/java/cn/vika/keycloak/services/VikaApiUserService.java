package cn.vika.keycloak.services;

import cn.vika.keycloak.dto.VikaApiUserDto;

public interface VikaApiUserService {
    /**
     * 根据邮箱获取用户信息
     * */
    VikaApiUserDto findUserByEmail(String email);
}
