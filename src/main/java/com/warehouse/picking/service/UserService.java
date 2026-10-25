package com.warehouse.picking.service;

import com.warehouse.picking.model.User;
import com.warehouse.picking.model.User.Role;
import com.warehouse.picking.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                user.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(
                        new SimpleGrantedAuthority(user.getRole().toString())
                )
        );
    }

    @Transactional
    public User registerCustomer(String username, String email, String password) {
        // Validate input
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Check for duplicates
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create and save user with CUSTOMER role
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, hashedPassword, Role.ROLE_CUSTOMER);
        
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> createAdminIfNotExists(String username, String email, String rawPassword) {
        // Only create if no admin exists
        long adminCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_WAREHOUSE_ADMIN)
                .count();

        if (adminCount > 0) {
            return Optional.empty();
        }

        String hashedPassword = passwordEncoder.encode(rawPassword);
        User admin = new User(username, email, hashedPassword, Role.ROLE_WAREHOUSE_ADMIN);
        
        return Optional.of(userRepository.save(admin));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
}
