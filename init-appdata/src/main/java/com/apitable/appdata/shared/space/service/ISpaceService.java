package com.apitable.appdata.shared.space.service;

import com.apitable.appdata.shared.user.pojo.User;

public interface ISpaceService {

    boolean checkSpaceExist(String spaceId);

    void cleanSpaceData(String spaceId);

    void createConfigSpace(User user, String spaceId,
        Boolean createConfigTableEnabled);
}
