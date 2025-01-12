package com.apitable.appdata.shared.user.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.appdata.shared.player.pojo.PlayerActivity;
import com.apitable.appdata.shared.player.service.IPlayerActivityService;
import com.apitable.appdata.shared.user.mapper.DeveloperMapper;
import com.apitable.appdata.shared.user.mapper.UserMapper;
import com.apitable.appdata.shared.user.pojo.Developer;
import com.apitable.appdata.shared.user.pojo.User;
import com.apitable.appdata.shared.user.service.IUserService;
import com.apitable.appdata.shared.util.RandomExtendUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private IPlayerActivityService iPlayerActivityService;

    @Resource
    private DeveloperMapper developerMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public User getByUserId(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public User getByMobile(String mobile) {
        return userMapper.selectByMobilePhone(mobile);
    }

    @Override
    public List<String> getExistingEmails(List<String> emails) {
        return userMapper.selectEmailByEmailIn(emails);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Collection<String> emails, String password) {
        List<User> users = new ArrayList<>();
        List<Developer> developers = new ArrayList<>();
        List<PlayerActivity> playerActivities = new ArrayList<>();
        for (String email : emails) {
            User user = new User();
            user.setId(IdWorker.getId());
            user.setUuid(IdUtil.fastSimpleUUID());
            user.setEmail(email);
            user.setNickName(StrUtil.subBefore(email, '@', true));
            user.setPassword(password);
            user.setColor(RandomUtil.randomInt(0, 10));
            users.add(user);
            developers.add(new Developer(IdWorker.getId(), user.getId(), this.createApiKey()));
            playerActivities.add(new PlayerActivity(IdWorker.getId(), user.getId(), JSONUtil.createObj().toString()));
        }
        userMapper.insertBatch(users);
        developerMapper.insertBatch(developers);
        iPlayerActivityService.create(playerActivities);
    }

    @Override
    public void create(User user) {
        userMapper.insertBatch(Collections.singletonList(user));
        developerMapper.insertBatch(Collections.singletonList(new Developer(IdWorker.getId(), user.getId(), this.createApiKey())));
        iPlayerActivityService.create(Collections.singletonList(new PlayerActivity(IdWorker.getId(), user.getId(), JSONUtil.createObj().toString())));
    }

    private String createApiKey() {
        String apiKeyPrefix = "usk";
        return apiKeyPrefix + RandomExtendUtil.randomString(20);
    }
}
