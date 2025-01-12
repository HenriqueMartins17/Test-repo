package com.apitable.enterprise.ai.autoconfigure;

import static net.javacrumbs.shedlock.core.LockAssert.assertLocked;

import com.apitable.enterprise.ai.scheduler.CreditSummaryTask;
import com.apitable.shared.clock.spring.ClockManager;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * ai task scheduler.
 *
 * @author Shawn Deng
 */
@Configuration(proxyBeanMethods = false)
@Conditional(AiTaskCondition.class)
@Slf4j
public class AiTasks {

    private final CreditSummaryTask creditSummaryTask;

    public AiTasks(CreditSummaryTask creditSummaryTask) {
        this.creditSummaryTask = creditSummaryTask;
    }

    /**
     * execute credit summary task every ten minutes.
     */
    @Scheduled(cron = "${AI_SUMMARY_TASK_CRON:0 0/10 * * * *}")
    @SchedulerLock(name = "creditSummary", lockAtMostFor = "9m", lockAtLeastFor = "2m")
    public void creditSummaryEveryTenMinutes() {
        assertLocked();
        LocalDateTime dateTimeNow = ClockManager.me().getLocalDateTimeNow();
        log.info("begin execute date: {}", dateTimeNow);
        creditSummaryTask.summary();
        log.info("end execute date");
    }
}
