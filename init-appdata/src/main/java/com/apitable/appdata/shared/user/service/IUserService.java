package com.apitable.appdata.shared.user.service;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.user.pojo.User;

public interface IUserService {

    User getByUserId(Long userId);

    User getByEmail(String email);

    User getByMobile(String mobile);

    List<String> getExistingEmails(List<String> emails);

    void create(Collection<String> emails, String password);

    void create(User user);
}
