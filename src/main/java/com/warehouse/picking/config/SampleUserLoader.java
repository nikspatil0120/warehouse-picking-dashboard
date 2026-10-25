package com.warehouse.picking.config;

import com.warehouse.picking.repository.UserRepository;
import com.warehouse.picking.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class SampleUserLoader {

    @Bean
    CommandLineRunner loadSampleUsers(UserRepository userRepository, UserService userService) {
        return args -> {
            if (userRepository.count() == 0) {
                // Create admin user
                userService.createAdminIfNotExists(
                        "admin",
                        "admin@warehouse.local",
                        "admin123"
                );

                // Create sample customer users
                try {
                    userService.registerCustomer(
                            "customer1",
                            "customer1@example.com",
                            "customer123"
                    );
                } catch (IllegalArgumentException e) {
                    // Ignore if already exists
                }

                try {
                    userService.registerCustomer(
                            "customer2",
                            "customer2@example.com",
                            "customer123"
                    );
                } catch (IllegalArgumentException e) {
                    // Ignore if already exists
                }
            }
        };
    }
}
