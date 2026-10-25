package com.warehouse.picking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF protection not needed for H2/dev console (which we removed)
                .headers(headers -> headers.frameOptions(frameOptions ->
                        frameOptions.disable()
                ))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // Customer endpoints
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")

                        // Warehouse admin endpoints
                        .requestMatchers("/items/**", "/stock/**", "/alerts/**").hasRole("WAREHOUSE_ADMIN")
                        .requestMatchers("/orders").hasRole("WAREHOUSE_ADMIN")
                        .requestMatchers("/orders/**").hasRole("WAREHOUSE_ADMIN")

                        // Home requires authentication (either role)
                        .requestMatchers("/").authenticated()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }
}
