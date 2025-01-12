package com.apitable.appdata.shared.organization.service;

public interface ITeamService {

    Long createRootTeam(String spaceId, String spaceName);

    void deleteBySpaceId(String spaceId);
}
