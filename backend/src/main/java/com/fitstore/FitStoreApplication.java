package com.fitstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FitStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitStoreApplication.class, args);
    }
}
