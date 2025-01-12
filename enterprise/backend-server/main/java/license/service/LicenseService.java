package com.apitable.enterprise.license.service;

import com.apitable.enterprise.license.model.LicenseInfo;

/**
 * License service  interface
 */
public interface LicenseService {

    LicenseInfo getCurrentLicense();

    /**
     * save or update license code
     * @param license encrypt license
     * @return
     */
    void saveOrUpdateLicense(String license);
}
