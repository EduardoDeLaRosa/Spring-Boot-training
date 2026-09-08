package com.eduardo.maintenancerequests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MaintenanceRequestsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaintenanceRequestsApplication.class, args);
    }
}
