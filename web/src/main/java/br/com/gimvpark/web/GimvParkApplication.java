package br.com.gimvpark.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GimvParkApplication {
    public static void main(String[] args) {
        SpringApplication.run(GimvParkApplication.class, args);
    }
}
