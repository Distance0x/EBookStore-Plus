package com.llama.ebookeureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EbookeurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbookeurekaApplication.class, args);
    }

}
