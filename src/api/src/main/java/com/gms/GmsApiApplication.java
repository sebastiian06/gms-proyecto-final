package com.gms;

import com.gms.platform.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class GmsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmsApiApplication.class, args);
    }
}