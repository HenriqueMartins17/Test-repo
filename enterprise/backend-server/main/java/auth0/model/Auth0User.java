package com.apitable.enterprise.auth0.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * auth0 user.
 *
 * @author Shawn Deng
 */
@Data
public class Auth0User {

    private Long userId;

    private List<String> queryString = new ArrayList<>();

    public Auth0User() {
    }

    public void addQueryString(String queryString) {
        this.queryString.add(queryString);
    }
}
