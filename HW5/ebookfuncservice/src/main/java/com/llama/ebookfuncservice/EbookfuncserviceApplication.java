package com.llama.ebookfuncservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EbookfuncserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbookfuncserviceApplication.class, args);
    }

}
