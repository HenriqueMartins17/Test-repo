package com.apitable.appdata.shared.organization.service;

public interface IMemberService {

    Long create(Long userId, String spaceId, String name, String email);

    void deleteBySpaceId(String spaceId);
}
