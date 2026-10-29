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
            try {
                long userCount = userRepository.count();
                logger.info("📊 User count in database: {}", userCount);
                
                if (userCount == 0) {
                    logger.info("🔧 No users found, creating default users...");
                    
                    // Create admin user
                    var adminOpt = userService.createAdminIfNotExists(
                            "admin",
                            "admin@warehouse.local",
                            "admin123"
                    );
                    if (adminOpt.isPresent()) {
                        logger.info("✅ Default admin user created: admin / admin123");
                    } else {
                        logger.warn("⚠️  Admin user already exists");
                    }

                    // Create sample customer users
                    try {
                        userService.registerCustomer(
                                "customer1",
                                "customer1@example.com",
                                "customer123"
                        );
                        logger.info("✅ Sample customer user 1 created");
                    } catch (IllegalArgumentException e) {
                        logger.warn("⚠️  Customer 1 creation failed: {}", e.getMessage());
                    }

                    try {
                        userService.registerCustomer(
                                "customer2",
                                "customer2@example.com",
                                "customer123"
                        );
                        logger.info("✅ Sample customer user 2 created");
                    } catch (IllegalArgumentException e) {
                        logger.warn("⚠️  Customer 2 creation failed: {}", e.getMessage());
                    }
                } else {
                    logger.info("✅ Sample users already exist ({} users), skipping user load", userCount);
                }
            } catch (Exception e) {
                logger.error("❌ Error loading sample users: {}", e.getMessage(), e);
            }
        };
    }
}
