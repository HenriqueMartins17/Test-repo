package com.apitable.enterprise.apitablebilling.service;

import com.apitable.enterprise.apitablebilling.entity.SubscriptionReportHistoryEntity;
import com.apitable.enterprise.apitablebilling.enums.SubscriptionReportType;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalDate;
import java.util.List;

/**
 * SubscriptionReportHistory Service.
 *
 * @author Shawn Deng
 */
public interface ISubscriptionReportHistoryService
    extends IService<SubscriptionReportHistoryEntity> {


    List<SubscriptionReportHistoryEntity> getByReportTime(LocalDate reportDate);

    void create(String subscriptionId, SubscriptionReportType reportType, LocalDate reportDate);
}
