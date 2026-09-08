package com.eduardo.shipmentworkflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ShipmentWorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShipmentWorkflowApplication.class, args);
    }
}
