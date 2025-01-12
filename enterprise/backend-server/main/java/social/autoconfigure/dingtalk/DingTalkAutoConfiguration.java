/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.autoconfigure.dingtalk;

import static com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkQueueConstants.DINGTALK_ISV_EVENT_QUEUE;
import static com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkQueueConstants.DING_TALK_ISV_HIGH_TOPIC;
import static com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkQueueConstants.DING_TALK_ISV_TOPIC_QUEUE_NAME_BUFFER;
import static com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkQueueConstants.DING_TALK_ISV_TOPIC_QUEUE_NAME_DEAD;
import static com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkQueueConstants.DING_TALK_TOPIC_EXCHANGE_BUFFER;
import static com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkQueueConstants.DING_TALK_TOPIC_EXCHANGE_DEAD;
import static com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkQueueConstants.SOCIAL_ISV_DINGTALK_ROUTING_KEY;
import static com.apitable.enterprise.social.autoconfigure.wecom.WeComQueueConstants.SOCIAL_ISV_EVENT_EXCHANGE;
import static com.vikadata.social.core.StringUtil.fixBasePath;

import com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkProperties.AgentAppProperty;
import com.apitable.enterprise.social.autoconfigure.dingtalk.DingTalkProperties.IsvAppProperty;
import com.vikadata.social.core.AppTicketStorage;
import com.vikadata.social.core.ConfigStorage;
import com.vikadata.social.dingtalk.CheckCreateSuiteUrlEventCallbackHandler;
import com.vikadata.social.dingtalk.CheckUpdateSuiteUrlEventCallbackHandler;
import com.vikadata.social.dingtalk.CheckUrlEventCallbackHandler;
import com.vikadata.social.dingtalk.DingTalkEventListenerManager;
import com.vikadata.social.dingtalk.DingTalkServiceProvider;
import com.vikadata.social.dingtalk.DingTalkTemplate;
import com.vikadata.social.dingtalk.DingtalkConfig;
import com.vikadata.social.dingtalk.DingtalkConfig.AgentApp;
import com.vikadata.social.dingtalk.DingtalkConfig.IsvApp;
import com.vikadata.social.dingtalk.EventListenerFactory;
import com.vikadata.social.dingtalk.SuiteTicketEventCallbackHandler;
import com.vikadata.social.dingtalk.config.AccessTokenInRedisStorage;
import com.vikadata.social.dingtalk.config.DingTalkConfigInRedisStorage;
import com.vikadata.social.dingtalk.config.DingTalkConfigStorage;
import com.vikadata.social.dingtalk.config.DingTalkRedisOperations;
import com.vikadata.social.dingtalk.config.RedisTemplateDingTalkOperations;
import com.vikadata.social.dingtalk.config.SuiteTicketInRedisStorage;
import com.vikadata.social.dingtalk.event.CheckCreateSuiteUrlEvent;
import com.vikadata.social.dingtalk.event.CheckUpdateSuiteUrlEvent;
import com.vikadata.social.dingtalk.event.CheckUrlEvent;
import com.vikadata.social.dingtalk.event.sync.http.SuiteTicketEvent;
import java.util.HashMap;
import java.util.List;
import jakarta.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;

/**
 * <p>
 * autoconfiguration of dingtalk component.
 * </p>
 *
 * @author Shawn Deng
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DingTalkProperties.class)
@ConditionalOnClass(DingTalkServiceProvider.class)
@ConditionalOnProperty(value = "social.dingtalk.enabled", havingValue = "true")
public class DingTalkAutoConfiguration
    implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor,
    BeanPostProcessor, EnvironmentAware {

    private static final String DINGTALK_CONFIG_STORAGE_BEAN_NAME = "dingtalkConfigStorage";

    private static final String DINGTALK_SUITE_TICKET_STORAGE_BEAN_NAME =
        "dingtalkSuiteTicketStorage";

    private static final Logger LOGGER = LoggerFactory.getLogger(DingTalkAutoConfiguration.class);

    private DingTalkProperties properties;

    private DingTalkServiceProvider instance;

    private Environment environment;

    private ApplicationContext applicationContext;

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "social.dingtalk.enabled", havingValue = "true")
    protected static class DingTalkQueueCreator {

        /**
         * create dingtalk DLX exchange.
         */
        @Bean("dingTalkDeadExchange")
        TopicExchange dingTalkDeadExchange() {
            return new TopicExchange(DING_TALK_TOPIC_EXCHANGE_DEAD);
        }

        /**
         * create dingtalk isv dead queue.
         */
        @Bean("dingTalkIsvDeadQueue")
        public Queue dingTalkIsvDeadQueue() {
            return new Queue(DING_TALK_ISV_TOPIC_QUEUE_NAME_DEAD);
        }

        /**
         * bind dlx dingtalk dead queue.
         */
        @Bean
        Binding bindDingTalkDlxExchangeWithOrgSuiteQueue(
            @Qualifier("dingTalkIsvDeadQueue") Queue dingTalkIsvDeadQueue,
            @Qualifier("dingTalkDeadExchange") TopicExchange dingTalkDeadExchange) {
            return BindingBuilder.bind(dingTalkIsvDeadQueue)
                .to(dingTalkDeadExchange)
                .with(DING_TALK_ISV_HIGH_TOPIC);
        }

        /**
         * define dingtalk topic buffer exchange.
         */
        @Bean("dingTalkBufferExchange")
        TopicExchange dingTalkBufferExchange() {
            return new TopicExchange(DING_TALK_TOPIC_EXCHANGE_BUFFER);
        }

        /**
         * create buffer queue.
         */
        @Bean("dingTalkIsvBufferQueue")
        Queue dingTalkIsvBufferQueue() {
            return QueueBuilder.durable(DING_TALK_ISV_TOPIC_QUEUE_NAME_BUFFER)
                .withArgument("x-dead-letter-exchange", DING_TALK_TOPIC_EXCHANGE_DEAD)
                .build();
        }

        /**
         * bind dingtalk exchange.
         */
        @Bean
        public Binding bindDingTalkExchangeWithOrgSuiteQueue(
            @Qualifier("dingTalkIsvBufferQueue") Queue dingTalkIsvBufferQueue,
            @Qualifier("dingTalkBufferExchange") TopicExchange dingTalkBufferExchange) {
            return BindingBuilder.bind(dingTalkIsvBufferQueue).to(dingTalkBufferExchange)
                .with(DING_TALK_ISV_HIGH_TOPIC);
        }

        @Bean("socialIsvEventExchange")
        @ConditionalOnMissingBean(name = "socialIsvEventExchange")
        TopicExchange socialIsvEventExchange() {
            return new TopicExchange(SOCIAL_ISV_EVENT_EXCHANGE);
        }

        /**
         * dingtalk isv event queue
         */
        @Bean("dingtalkIsvEventQueue")
        public Queue dingtalkIsvEventQueue() {
            return new Queue(DINGTALK_ISV_EVENT_QUEUE);
        }

        @Bean
        public Binding bindDingtalkIsvEventExchange(
            @Qualifier("dingtalkIsvEventQueue") Queue dingtalkIsvEventQueue,
            @Qualifier("socialIsvEventExchange") TopicExchange socialIsvEventExchange) {
            return BindingBuilder.bind(dingtalkIsvEventQueue)
                .to(socialIsvEventExchange)
                .with(SOCIAL_ISV_DINGTALK_ROUTING_KEY);

        }
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext)
        throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean(name = DINGTALK_CONFIG_STORAGE_BEAN_NAME)
    @ConditionalOnMissingBean(name = DINGTALK_CONFIG_STORAGE_BEAN_NAME)
    public ConfigStorage dingtalkConfigStorage(DingTalkRedisOperations dingTalkRedisOperations) {
        LOGGER.info("Register Dingtalk Access Token Storage Bean");
        return new AccessTokenInRedisStorage(dingTalkRedisOperations, "vikadata");
    }

    @Bean(name = DINGTALK_SUITE_TICKET_STORAGE_BEAN_NAME)
    @ConditionalOnMissingBean(name = DINGTALK_SUITE_TICKET_STORAGE_BEAN_NAME)
    public HashMap<String, AppTicketStorage> suiteTicketStorage(
        DingTalkRedisOperations dingTalkRedisOperations) {
        LOGGER.info("Register DingTalk Suite Ticket Storage Bean");
        List<IsvAppProperty> isvAppList = properties.getIsvAppList();
        HashMap<String, AppTicketStorage> ticketStorageMap = new HashMap<>(isvAppList.size());
        isvAppList.forEach(item -> {
            SuiteTicketInRedisStorage ticketStorage =
                new SuiteTicketInRedisStorage(dingTalkRedisOperations, "vikadata");
            ticketStorage.setTicketKey(item.getSuiteId(), properties.getIsvCorpId());
            ticketStorageMap.put(item.getSuiteId(), ticketStorage);
        });
        return ticketStorageMap;
    }

    @Bean
    @ConditionalOnMissingBean(name = "dingTalkRedisOperations")
    public DingTalkRedisOperations dingTalkRedisOperations(StringRedisTemplate stringRedisTemplate,
                                                           RedisTemplate<String, Object> redisTemplate) {
        LOGGER.info("Register DingTalk Redis Operation Bean");
        return new RedisTemplateDingTalkOperations(stringRedisTemplate, redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(name = "dingTalkConfigStorage")
    public DingTalkConfigStorage dingTalkConfigStorage(
        DingTalkRedisOperations dingTalkRedisOperations) {
        LOGGER.info("Register DingTalk Config Storage Bean");
        return new DingTalkConfigInRedisStorage(dingTalkRedisOperations, "vikadata");
    }

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry)
        throws BeansException {
        BindResult<DingTalkProperties> bind =
            Binder.get(environment).bind("social.dingtalk", Bindable.of(DingTalkProperties.class));
        properties = bind.get();
        if (properties == null) {
            return;
        }
        // check properties
        DingTalkProperties.checkAppProperties(properties);
        // SDK config
        DingtalkConfig dingtalkConfig = applyProperties();
        // Instantiation service class entry
        DingTalkServiceProvider serviceProvider = new DingTalkServiceProvider(dingtalkConfig);
        instance = serviceProvider;
        // register dingtalk bean
        DefaultListableBeanFactory beanFactory =
            (DefaultListableBeanFactory) ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        beanFactory.registerSingleton(DingTalkServiceProvider.class.getSimpleName(),
            serviceProvider);
        LOGGER.info("DingTalk Provider Bean Name: {}",
            DingTalkServiceProvider.class.getSimpleName());
        beanFactory.registerSingleton(DingTalkTemplate.class.getSimpleName(),
            serviceProvider.getDingTalkTemplate());
        LOGGER.info("DingTalk Template Bean Name: {}", DingTalkTemplate.class.getSimpleName());
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory)
        throws BeansException {
        // nothing to do
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName)
        throws BeansException {
        if (DINGTALK_SUITE_TICKET_STORAGE_BEAN_NAME.equals(beanName)) {
            // App Ticket storage instance
            HashMap<String, AppTicketStorage> ticketStorage =
                (HashMap<String, AppTicketStorage>) bean;
            if (!properties.getIsvAppList().isEmpty()) {
                instance.setSuiteTicketStorage(ticketStorage);
                instance.getEventListenerManager()
                    .registerEventCallbackHandler(SuiteTicketEvent.class,
                        new SuiteTicketEventCallbackHandler(ticketStorage));
                instance.getEventListenerManager().registerEventCallbackHandler(CheckUrlEvent.class,
                    new CheckUrlEventCallbackHandler());
                instance.getEventListenerManager()
                    .registerEventCallbackHandler(CheckCreateSuiteUrlEvent.class,
                        new CheckCreateSuiteUrlEventCallbackHandler());
                instance.getEventListenerManager()
                    .registerEventCallbackHandler(CheckUpdateSuiteUrlEvent.class,
                        new CheckUpdateSuiteUrlEventCallbackHandler());
            }
        }
        // bean has been instantiated. You can read all BEAN of the container and set properties
        if (bean instanceof ConfigStorage && DINGTALK_CONFIG_STORAGE_BEAN_NAME.equals(beanName)) {
            // Token storage instance
            instance.setConfigStorage((ConfigStorage) bean);
        }

        if (bean instanceof DingTalkConfigStorage) {
            DingTalkConfigStorage agentAppStorage = (DingTalkConfigStorage) bean;
            instance.getDingtalkConfig().setAgentAppStorage(agentAppStorage);
            if (!properties.getAgentApp().isEmpty()) {
                for (AgentAppProperty appInfo : properties.getAgentApp()) {
                    if (!agentAppStorage.hasAgentApp(appInfo.getAgentId())) {
                        AgentApp agentApp = new AgentApp();
                        agentApp.setAgentId(appInfo.getAgentId());
                        agentApp.setCustomKey(appInfo.getCustomKey());
                        agentApp.setCustomSecret(appInfo.getCustomSecret());
                        agentApp.setToken(appInfo.getToken());
                        agentApp.setAesKey(appInfo.getAesKey());
                        agentApp.setCorpId(appInfo.getCorpId());
                        agentApp.setSuiteTicket(appInfo.getSuiteTicket());
                        agentAppStorage.setAgentApp(agentApp);
                    }
                }
            }
        }
        return bean;
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> registerDingTalkApi() {
        DingTalkServlet dingTalkServlet = new DingTalkServlet();
        dingTalkServlet.setApplicationContext(applicationContext);
        ServletRegistrationBean<HttpServlet> servletRegistrationBean =
            new ServletRegistrationBean<>(
                dingTalkServlet,
                fixBasePath(properties.getBasePath()) + fixBasePath(properties.getEventPath()) +
                    "/*",
                fixBasePath(properties.getBasePath()) + fixBasePath(properties.getSyncEventPath()) +
                    "/*"
            );
        servletRegistrationBean.setName("DingTalkServlet");
        return servletRegistrationBean;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onDingTalkEventListenerScan() {
        LOGGER.info("Starting scan ding talk event");
        DingTalkEventScanner scanner = new DingTalkEventScanner(applicationContext);
        EventListenerFactory factory = scanner.scan();

        DingTalkEventListenerManager manager = instance.getEventListenerManager();
        factory.getEventHandlerMap().forEach(manager::registerEventCallbackHandler);
        if (LOGGER.isInfoEnabled()) {
            // print event list
            StringBuilder output = new StringBuilder(256);
            output.append(
                "\n---------------- Enable dingtalk subscribed events -----------------\n");
            manager.getEventHandlerMap().keySet()
                .forEach(e -> output.append("Event Name:").append(e.getSimpleName()).append("\n"));
            output.append("---------------------------------------------------");
            LOGGER.info(output.toString());
        }
        // Initialize instance entry class
        instance.init();
        LOGGER.info("Ding talk Event initializing complete. Congratulations.....");
    }

    private DingtalkConfig applyProperties() {
        DingtalkConfig config = new DingtalkConfig();
        config.setIsvCorpId(properties.getIsvCorpId());
        config.setSuiteId(properties.getSuiteId());
        config.setSuiteKey(properties.getSuiteKey());
        config.setSuiteSecret(properties.getSuiteSecret());
        config.setCorpSecret(properties.getCorpSecret());

        if (null != properties.getMobileApp()) {
            DingtalkConfig.Mobile mobileAppConfig = new DingtalkConfig.Mobile();
            mobileAppConfig.setAppId(properties.getMobileApp().getAppId());
            mobileAppConfig.setAppSecret(properties.getMobileApp().getAppSecret());
            config.setMobile(mobileAppConfig);
        }

        if (null != properties.getCorpH5App()) {
            DingtalkConfig.H5app h5appConfig = new DingtalkConfig.H5app();
            h5appConfig.setAppKey(properties.getCorpH5App().getAppKey());
            h5appConfig.setAppSecret(properties.getCorpH5App().getAppSecret());
            config.setH5app(h5appConfig);
        }

        if (!properties.getIsvAppList().isEmpty()) {
            HashMap<String, IsvApp> isvAppMap = new HashMap<>(properties.getIsvAppList().size());
            for (IsvAppProperty isvAppProperty : properties.getIsvAppList()) {
                IsvApp isvApp = new IsvApp();
                isvApp.setToken(isvAppProperty.getToken());
                isvApp.setSuiteKey(isvAppProperty.getSuiteKey());
                isvApp.setAesKey(isvAppProperty.getAesKey());
                isvApp.setSuiteSecret(isvAppProperty.getSuiteSecret());
                isvApp.setSuiteId(isvAppProperty.getSuiteId());
                isvAppMap.put(isvAppProperty.getSuiteId(), isvApp);
            }
            config.setIsvAppMap(isvAppMap);
        }
        return config;
    }
}
