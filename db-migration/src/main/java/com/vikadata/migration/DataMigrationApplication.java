package com.vikadata.migration;

import org.mybatis.spring.annotation.MapperScan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = KafkaAutoConfiguration.class)
@ConfigurationPropertiesScan
@EnableTransactionManagement
@MapperScan("com.vikadata.migration.mapper")
@EnableAsync
public class DataMigrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataMigrationApplication.class, args);
    }

}
