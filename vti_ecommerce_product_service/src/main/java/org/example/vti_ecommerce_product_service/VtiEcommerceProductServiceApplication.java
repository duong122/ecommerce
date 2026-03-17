package org.example.vti_ecommerce_product_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VtiEcommerceProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VtiEcommerceProductServiceApplication.class, args);
    }

}
