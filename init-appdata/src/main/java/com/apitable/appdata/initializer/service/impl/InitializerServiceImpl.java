package com.apitable.appdata.initializer.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.apitable.appdata.initializer.config.properties.InitConfigSpaceProperties;
import com.apitable.appdata.initializer.config.properties.InitUserProperties;
import com.apitable.appdata.initializer.service.IInitializerService;
import com.apitable.appdata.shared.constants.CommonConstants;
import com.apitable.appdata.shared.space.service.ISpaceService;
import com.apitable.appdata.shared.user.pojo.User;
import com.apitable.appdata.shared.user.service.IUserService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class InitializerServiceImpl implements IInitializerService {

    @Resource
    private IUserService iUserService;

    @Resource
    private InitUserProperties properties;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private InitConfigSpaceProperties configSpaceProperties;

    @Override
    public void initUsers() {
        this.initDefaultAccount();

        if (!properties.getBatchEnabled()) {
            return;
        }
        String prefix = properties.getEmailPrefix();
        String suffix = properties.getEmailSuffix();
        List<String> emails = new ArrayList<>();
        for (int i = 0; i < properties.getCount(); i++) {
            String email = i < 10 ? String.format("%s00%d%s", prefix, i, suffix) : String.format("%s0%d%s", prefix, i, suffix);
            emails.add(email);
        }
        if (emails.isEmpty()) {
            log.warn("No account needs to be created.");
            return;
        }
        List<String> existingEmails = iUserService.getExistingEmails(emails);
        if (!existingEmails.isEmpty()) {
            log.warn("The following mailboxes already exist, initialization will be skipped.");
            log.warn("{}", existingEmails);
        }
        Collection<String> subtract = CollUtil.subtract(emails, existingEmails);
        if (subtract.isEmpty()) {
            log.warn("No account needs to be created.");
            return;
        }
        iUserService.create(subtract, CommonConstants.INIT_ACCOUNT_PASSWORD);
        log.info("Init Finish.");
    }

    private void initDefaultAccount() {
        User existingUser = iUserService.getByUserId(CommonConstants.INIT_ACCOUNT_USER_ID);
        if (existingUser != null) {
            log.info("Init account have been existing.");
            return;
        }
        User user = new User();
        user.setId(CommonConstants.INIT_ACCOUNT_USER_ID);
        user.setUuid(CommonConstants.INIT_ACCOUNT_UUID);
        user.setColor(0);
        iUserService.create(user);
        log.info("Init Default Account Finish.");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initConfigSpace() {
        if (Boolean.FALSE.equals(configSpaceProperties.getEnabled())) {
            return;
        }
        log.info("Begin to init config space.");
        String credential = configSpaceProperties.getAdminUserCredential();
        if (StrUtil.isBlank(credential)) {
            throw new RuntimeException("User Credentials cannot be empty.");
        }
        User user = Validator.isEmail(credential) ? iUserService.getByEmail(credential)
            : iUserService.getByMobile(credential);
        if (user == null) {
            throw new RuntimeException(StrUtil.format("User[{}] does not exist.", credential));
        }
        String spaceId = configSpaceProperties.getConfigSpaceId();
        boolean spaceExist = iSpaceService.checkSpaceExist(spaceId);
        if (spaceExist) {
            log.warn("Config space have been existing.");
            if (Boolean.FALSE.equals(configSpaceProperties.getMandatoryCoverageEnabled())) {
                return;
            }
            log.warn("Mandatory cover space organization members.");
            iSpaceService.cleanSpaceData(spaceId);
        }
        log.info("Begin to create config space.");
        iSpaceService.createConfigSpace(user, spaceId,
            configSpaceProperties.getCreateConfigTableEnabled());
    }
}
