package br.com.sge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan("br.com.sge.config")
@EnableScheduling
public class SgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SgeApplication.class, args);
    }
}
