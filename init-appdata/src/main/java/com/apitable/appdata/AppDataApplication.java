package com.apitable.appdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AppDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppDataApplication.class, args);
    }
}
