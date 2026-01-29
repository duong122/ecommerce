package org.example.vti_ecommerce_discovery_sever;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class VtiEcommerceDiscoverySeverApplication {

    public static void main(String[] args) {
        SpringApplication.run(VtiEcommerceDiscoverySeverApplication.class, args);
    }

}
