package com.vikadata.migration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// @Configuration
// @EnableTransactionManagement
// @EnableMongoRepositories(basePackages = "com.vikadata.migration.mapper")
public class MongoTransactionConfig extends AbstractMongoClientConfiguration {

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Override
    protected String getDatabaseName() {
        return "vikadata";
    }
}
