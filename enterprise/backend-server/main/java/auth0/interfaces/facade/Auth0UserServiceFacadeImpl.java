package com.apitable.enterprise.auth0.interfaces.facade;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.auth0.service.Auth0Service;
import com.apitable.interfaces.auth.model.UserAuth;
import com.apitable.interfaces.user.facade.AbstractUserServiceFacadeImpl;
import com.apitable.user.enums.UserException;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import lombok.extern.slf4j.Slf4j;

/**
 * user service facade implement by auth0.
 */
@Slf4j
public class Auth0UserServiceFacadeImpl extends AbstractUserServiceFacadeImpl {

    private final Auth0Service auth0Service;

    public Auth0UserServiceFacadeImpl(Auth0Service auth0Service) {
        this.auth0Service = auth0Service;
    }

    @Override
    public void onUserChangeAvatarAction(Long userId, String avatarUrl) {
        User user = new User();
        user.setPicture(avatarUrl);
        try {
            auth0Service.updateUser(userId, user);
        } catch (Exception e) {
            log.error("updateUserAvatarError:{}", userId, e);
        }
    }

    @Override
    public void onUserChangeNicknameAction(Long userId, String nickname, Boolean init) {
        User user = new User();
        user.setNickname(nickname);
        try {
            auth0Service.updateUser(userId, user);
        } catch (Exception e) {
            log.error("updateUserNicknameError:{}", userId, e);
        }
    }

    @Override
    public void onUserChangeEmailAction(Long userId, String email, String oldEmail) {
        User user = new User();
        user.setEmailVerified(true);
        user.setEmail(email);
        user.setName(email);
        // if you are updating email, email_verified,you need to specify the connection property too
        user.setConnection(auth0Service.getDbConnectionName());
        try {
            auth0Service.updateUser(userId, user);
            if (null != oldEmail) {
                auth0Service.updateAppsumoUser(email, oldEmail);
            }
        } catch (Exception e) {
            log.error("updateUserEmailError:{}", email, e);
            throw new BusinessException(UserException.LINK_EMAIL_ERROR);
        }
    }

    @Override
    public void onUserCloseAccount(Long userId) {
        try {
            auth0Service.deleteUser(userId);
        } catch (Exception e) {
            log.error("deleteUserError:{}", userId, e);
        }
    }

    @Override
    public boolean resetPassword(UserAuth userAuth) {
        return auth0Service.resetPassword(userAuth.getUserId());
    }

    @Override
    public boolean verifyEmail(String email) {
        try {
            auth0Service.sendVerificationEmail(email);
        } catch (Auth0Exception exception) {
            log.error("SendVerificationEmailError", exception);
            return false;
        }
        return true;
    }
}
