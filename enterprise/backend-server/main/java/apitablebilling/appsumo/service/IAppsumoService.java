package com.apitable.enterprise.apitablebilling.appsumo.service;

import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoAction;
import com.apitable.enterprise.apitablebilling.appsumo.model.AppsumoEventDTO;
import com.apitable.enterprise.apitablebilling.appsumo.model.EventVO;
import com.auth0.exception.Auth0Exception;

/**
 * Appsumo interface.
 */
public interface IAppsumoService {
    /**
     * get appsumo api jwt token.
     *
     * @param userName user name
     * @param password password
     * @return jwt token
     */
    String getAccessToken(String userName, String password);

    /**
     * check access token.
     *
     * @param token jwt token
     * @return boolean
     */
    boolean verifyAccessToken(String token);

    /**
     * handle event.
     *
     * @param eventLogId event log id
     * @param action appsumo action
     * @return EventVO
     */
    EventVO handleEvent(Long eventLogId, AppsumoAction action);

    /**
     * handle appsumo activation event.
     *
     * @param userId user id
     * @param spaceId space id
     * @param event event
     */
    void createSubscription(Long userId, String spaceId, AppsumoEventDTO event);

    /**
     * appsumo user signup.
     *
     * @param eventId activation event id
     * @param password password
     * @return user id
     */
    Long userSignup(Long eventId, String password) throws Auth0Exception;

    /**
     * link user email with appsumo event email.
     * @param userEmail user's new email address
     * @param activationEmail user's old email address
     */
    void linkAppsumoActivationEmail(String userEmail, String activationEmail);


    /**
     * handle subscription.
     *
     * @param spaceId space id
     * @param eventId event id
     */
    void manuHandleSubscription(Long eventId, String spaceId);

    /**
     * check the license for current user.
     *
     * @param metadataStr subscription metadata
     * @param uuid        Is the license product key. It's unique and the same in every request.
     * @return boolean
     */
    boolean isCurrentLicense(String metadataStr, String uuid);
}
