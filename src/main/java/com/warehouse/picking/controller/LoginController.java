package com.warehouse.picking.controller;

import com.warehouse.picking.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute RegistrationForm form,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate input
            if (form.getUsername() == null || form.getUsername().isBlank()) {
                throw new IllegalArgumentException("Username is required");
            }
            if (form.getEmail() == null || form.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (form.getPassword() == null || form.getPassword().isBlank()) {
                throw new IllegalArgumentException("Password is required");
            }
            if (form.getPasswordConfirm() == null || form.getPasswordConfirm().isBlank()) {
                throw new IllegalArgumentException("Password confirmation is required");
            }
            if (!form.getPassword().equals(form.getPasswordConfirm())) {
                throw new IllegalArgumentException("Passwords do not match");
            }
            if (form.getPassword().length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters");
            }

            userService.registerCustomer(form.getUsername(), form.getEmail(), form.getPassword());
            redirectAttributes.addFlashAttribute("message", 
                    "Registration successful. Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    public static class RegistrationForm {
        @NotBlank
        private String username;
        
        @NotBlank
        private String email;
        
        @NotBlank
        private String password;
        
        @NotBlank
        private String passwordConfirm;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getPasswordConfirm() { return passwordConfirm; }
        public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
    }
}
