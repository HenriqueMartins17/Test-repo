package com.apitable.enterprise.license.service.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import com.apitable.base.entity.SystemConfigEntity;
import com.apitable.base.mapper.SystemConfigMapper;
import com.apitable.enterprise.license.model.LicenseInfo;
import com.apitable.enterprise.license.service.LicenseService;
import com.apitable.enterprise.license.util.LicenseUtil;
import com.google.gson.Gson;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LicenseServiceImpl implements LicenseService {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Resource
    private RedisLockRegistry redisLockRegistry;

    //license config type
    final int  LICENSE_CONFIG = 100;


    /**
     * license save to system_config with type
     */
    @Override
    public LicenseInfo getCurrentLicense() {
        String configMap = systemConfigMapper.selectConfigMapByType(LICENSE_CONFIG,null);
        if (configMap == null){
            return null;
        }
        licenseConfigMap licenseConfigMap = new Gson().fromJson(configMap, licenseConfigMap.class);
        return parseVerifiedLicenseInfo(licenseConfigMap.getCode());
    }

    @Override
    public void saveOrUpdateLicense(String license) {

        LicenseInfo licenseInfo = parseVerifiedLicenseInfo(license);
        if (licenseInfo == null) {
            return;
        }

        // License can only be imported within 7 days
        if (System.currentTimeMillis() > licenseInfo.getLicense().getCreatedAt() + 604800000) {
            log.info("license can only be imported within 7 days.");
            return;
        }

        // Save license
        Lock lock = redisLockRegistry.obtain("license:lock");
        try {
            if (lock.tryLock(200, TimeUnit.MILLISECONDS)) {
                SystemConfigEntity systemConfigEntity = SystemConfigEntity.builder()
                        .type(LICENSE_CONFIG)
                        .configMap(new Gson().toJson(new licenseConfigMap(license)))
                        .createdBy(1L)
                        .updatedBy(1L)
                        .build();
                Long id = systemConfigMapper.selectIdByTypeAndLang(LICENSE_CONFIG, null);
                if (id != null) {
                    systemConfigEntity.setId(id);
                    systemConfigMapper.updateById(systemConfigEntity);
                }else {
                    systemConfigMapper.insert(systemConfigEntity);
                }
            }
        } catch (Exception e) {
            log.error("failed to  save license", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     *
     * @param encryptLicenseInfo
     * @return
     */
    private LicenseInfo parseVerifiedLicenseInfo(String encryptLicenseInfo){
        LicenseInfo licenseInfo;
        try {
            // Decode license code
            byte[] licenseByte = Base64.decodeBase64(encryptLicenseInfo);
            String licenseStr = new String(licenseByte);
            licenseInfo = new Gson().fromJson(licenseStr, LicenseInfo.class);
            if (licenseInfo == null) {
                log.info("license format error.");
                return null;
            }

            if (!LicenseUtil.verifyData(licenseInfo.getLicenseString(), licenseInfo.getSignature())) {
                return null;
            }

            byte[] licenseStringByte = Base64.decodeBase64(licenseInfo.getLicenseString());
            licenseInfo.setLicense(new Gson().fromJson(new String(licenseStringByte), LicenseInfo.License.class));
            return licenseInfo;
        }catch (Exception e){
            log.info("license verifyData fail, err = {}", e.getMessage());
            return null;
        }
    }

    class licenseConfigMap {

        private String code;

        public licenseConfigMap(String code){
            this.code = code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCode(){
            return this.code;
        }
    }
}
