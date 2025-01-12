package com.apitable.enterprise.apitablebilling.task;

import static net.javacrumbs.shedlock.core.LockAssert.assertLocked;

import com.apitable.enterprise.apitablebilling.service.IBillingTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * list of billing schedule tasks.
 *
 * @author Shawn Deng
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "system.test-enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class BillingTasks {

    @Resource
    private IBillingTaskService iBillingTaskService;

    /**
     * report space unit task.
     * cron: 0 0/60 17-23 * * *
     * preview execute desc: 17:00, 18:00, 19:00, 20:00, 21:00, 22:00 and 23:00 every day.
     * development cron: 0 0/15 * * * * every fifteen minute
     */
    @Scheduled(cron = "${BILLING_TASK_CRON:0 0/5 * * * *}")
    @SchedulerLock(name = "reportSeats", lockAtMostFor = "5m", lockAtLeastFor = "4m")
    public void reportSpaceUnits() {
        assertLocked();
        iBillingTaskService.reportSpaceSubscriptionSeats();
    }
}
