package com.erenalyoruk.cashgrid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CashGridApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashGridApplication.class, args);
    }
}
