package com.example.batchworkshop4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BatchWorkshop4Application {

    public static void main(String[] args) {
        SpringApplication.run(BatchWorkshop4Application.class, args);
    }

}
