package com.warehouse.picking.config;

import com.warehouse.picking.repository.UserRepository;
import com.warehouse.picking.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Production-safe administrator bootstrap.
 * 
 * Creates an initial administrator account ONLY when:
 * 1. The active profile is "prod"
 * 2. No users exist in the database
 * 3. Environment variables ADMIN_USERNAME, ADMIN_EMAIL, and ADMIN_PASSWORD are set
 * 
 * This prevents accidental deployment with default credentials and ensures
 * the initial admin is intentionally provisioned.
 */
@Configuration
@Profile("prod")
public class AdminBootstrap {

    @Bean
    CommandLineRunner bootstrapAdmin(UserRepository userRepository, UserService userService) {
        return args -> {
            // Only run if no users exist
            if (userRepository.count() > 0) {
                return;
            }

            // Read credentials from environment variables
            String adminUsername = System.getenv("ADMIN_USERNAME");
            String adminEmail = System.getenv("ADMIN_EMAIL");
            String adminPassword = System.getenv("ADMIN_PASSWORD");

            // If any are missing, do not create a default admin
            if (adminUsername == null || adminEmail == null || adminPassword == null) {
                System.out.println(
                    "WARNING: No users found in database and ADMIN_USERNAME, ADMIN_EMAIL, or ADMIN_PASSWORD " +
                    "environment variables are not set. No default administrator will be created. " +
                    "Please provision an administrator manually before using the application in production."
                );
                return;
            }

            // Create the administrator from environment variables
            try {
                userService.createAdminIfNotExists(adminUsername, adminEmail, adminPassword);
                System.out.println("Administrator account '" + adminUsername + "' created from environment variables.");
            } catch (Exception e) {
                System.err.println("Failed to create administrator account: " + e.getMessage());
            }
        };
    }
}
