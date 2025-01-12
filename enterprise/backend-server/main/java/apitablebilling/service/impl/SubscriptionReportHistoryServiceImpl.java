package com.apitable.enterprise.apitablebilling.service.impl;

import com.apitable.enterprise.apitablebilling.entity.SubscriptionReportHistoryEntity;
import com.apitable.enterprise.apitablebilling.enums.SubscriptionReportType;
import com.apitable.enterprise.apitablebilling.mapper.SubscriptionReportHistoryMapper;
import com.apitable.enterprise.apitablebilling.service.ISubscriptionReportHistoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * SubscriptionReportHistory Service Implement.
 *
 * @author Shawn Deng
 */
@Service
public class SubscriptionReportHistoryServiceImpl
    extends ServiceImpl<SubscriptionReportHistoryMapper, SubscriptionReportHistoryEntity>
    implements ISubscriptionReportHistoryService {

    @Override
    public List<SubscriptionReportHistoryEntity> getByReportTime(LocalDate reportDate) {
        return
            list(new QueryWrapper<SubscriptionReportHistoryEntity>().eq("report_date",
                reportDate));
    }

    @Override
    public void create(String subscriptionId, SubscriptionReportType reportType,
                       LocalDate reportDate) {
        SubscriptionReportHistoryEntity entity = new SubscriptionReportHistoryEntity();
        entity.setSubscriptionId(subscriptionId);
        entity.setReportType(reportType.name());
        entity.setReportDate(reportDate);
        save(entity);
    }
}
