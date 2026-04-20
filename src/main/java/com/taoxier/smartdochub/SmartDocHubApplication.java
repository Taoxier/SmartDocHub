package com.taoxier.smartdochub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = { FreeMarkerAutoConfiguration.class })
@MapperScan(basePackages = "com.taoxier.smartdochub.**.*.mapper")
@EnableScheduling
public class SmartDocHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartDocHubApplication.class, args);
    }

}
