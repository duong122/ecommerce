package org.example.vti_ecommerce_inventory_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VtiEcommerceInventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VtiEcommerceInventoryServiceApplication.class, args);
    }

}
