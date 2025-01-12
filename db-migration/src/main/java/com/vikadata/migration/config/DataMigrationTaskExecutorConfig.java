package com.vikadata.migration.config;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class DataMigrationTaskExecutorConfig implements AsyncConfigurer {
    public static final String MIGRATION_EXECUTOR_BEAN_NAME = "MIGRATION";
    public static final String DATA_CHECK_EXECUTOR_BEAN_NAME = "CHECK";
    @Resource
    private MultipleTaskProperties multipleTaskProperties;

    private ThreadPoolTaskExecutor taskExecutor;

    public ThreadPoolTaskExecutor getTaskExecutor(){
        return this.taskExecutor;
    }

    @Override
    @Bean(MIGRATION_EXECUTOR_BEAN_NAME)
    public Executor getAsyncExecutor() {
        log.info("begin crate thread pool");
        taskExecutor = new VisibleThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(multipleTaskProperties.getTasks().get(0).getPool().getCoreSize());
        taskExecutor.setMaxPoolSize(multipleTaskProperties.getTasks().get(0).getPool().getMaxSize());
        taskExecutor.setQueueCapacity(multipleTaskProperties.getTasks().get(0).getPool().getQueueCapacity());
        taskExecutor.setThreadNamePrefix(multipleTaskProperties.getTasks().get(0).getThreadNamePrefix());
        taskExecutor.initialize();
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        log.info("crate thread pool complete");
        return taskExecutor;
    }

    @Bean(DATA_CHECK_EXECUTOR_BEAN_NAME)
    public Executor getDataCheckExecutor() {
        log.info("begin crate data check thread pool");
        ThreadPoolTaskExecutor taskExecutor = new VisibleThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(multipleTaskProperties.getTasks().get(1).getPool().getCoreSize());
        taskExecutor.setMaxPoolSize(multipleTaskProperties.getTasks().get(1).getPool().getMaxSize());
        taskExecutor.setQueueCapacity(multipleTaskProperties.getTasks().get(1).getPool().getQueueCapacity());
        taskExecutor.setThreadNamePrefix(multipleTaskProperties.getTasks().get(1).getThreadNamePrefix());
        taskExecutor.initialize();
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        log.info("crate thread pool complete");
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error("注解异步任务异常", ex);
    }

    public class VisibleThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

        protected final Log logger = LogFactory.getLog(getClass());

        private void showThreadPoolInfo(String prefix) {
            ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();
            logger.info("线程名称" + this.getThreadNamePrefix() + prefix
                + ", 任务总数量: " + threadPoolExecutor.getTaskCount()
                + ", 已完成任务数量: " + threadPoolExecutor.getCompletedTaskCount()
                + ", 正在活动线程任务数量:" + threadPoolExecutor.getActiveCount()
                + ", 缓冲线程队列数量: " + threadPoolExecutor.getQueue().size());
        }

        @Override
        public void execute(Runnable task) {
            showThreadPoolInfo("执行任务");
            super.execute(task);
        }

        @Override
        public void execute(Runnable task, long startTimeout) {
            showThreadPoolInfo(String.format("执行任务[Timeout:%d]", startTimeout));
            super.execute(task, startTimeout);
        }

        @Override
        public Future<?> submit(Runnable task) {
            showThreadPoolInfo("执行有状态的任务");
            return super.submit(task);
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            showThreadPoolInfo("执行有状态的任务");
            return super.submit(task);
        }

        @Override
        public ListenableFuture<?> submitListenable(Runnable task) {
            showThreadPoolInfo("提交线程任务监听器");
            return super.submitListenable(task);
        }

        @Override
        public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
            showThreadPoolInfo("提交线程任务回调监听器");
            return super.submitListenable(task);
        }
    }
}
