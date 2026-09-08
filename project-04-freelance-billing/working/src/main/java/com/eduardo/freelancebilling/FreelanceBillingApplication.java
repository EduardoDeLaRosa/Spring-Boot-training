package com.eduardo.freelancebilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FreelanceBillingApplication {
    public static void main(String[] args) {
        SpringApplication.run(FreelanceBillingApplication.class, args);
    }
}
