package com.warehouse.picking.config;

import com.warehouse.picking.repository.UserRepository;
import com.warehouse.picking.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleUserLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(SampleUserLoader.class);

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
                logger.info("✅ Default admin user created: admin / admin123");

                // Create sample customer users
                try {
                    userService.registerCustomer(
                            "customer1",
                            "customer1@example.com",
                            "customer123"
                    );
                    logger.info("✅ Sample customer user 1 created");
                } catch (IllegalArgumentException e) {
                    // Ignore if already exists
                }

                try {
                    userService.registerCustomer(
                            "customer2",
                            "customer2@example.com",
                            "customer123"
                    );
                    logger.info("✅ Sample customer user 2 created");
                } catch (IllegalArgumentException e) {
                    // Ignore if already exists
                }
            } else {
                logger.info("✅ Sample users already exist, skipping user load");
            }
        };
    }
}
