package com.eduardo.equipmentrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EquipmentRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(EquipmentRentalApplication.class, args);
    }
}
