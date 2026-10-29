package com.warehouse.picking;

import com.warehouse.picking.model.User;
import com.warehouse.picking.repository.UserRepository;
import com.warehouse.picking.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void testLoginPageIsAccessible() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign in")));
    }

    @Test
    public void testRegisterPageIsAccessible() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Account")));
    }

    @Test
    public void testSuccessfulCustomerRegistration() throws Exception {
        mvc.perform(post("/register")
                .with(csrf())
                .param("username", "testuser")
                .param("email", "test@example.com")
                .param("password", "password123")
                .param("passwordConfirm", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Verify user was created with CUSTOMER role
        User user = userRepository.findByUsername("testuser").orElseThrow();
        assert user.getRole() == User.Role.ROLE_CUSTOMER;
    }

    @Test
    public void testRegistrationFailsWithMismatchedPasswords() throws Exception {
        mvc.perform(post("/register")
                .with(csrf())
                .param("username", "testuser")
                .param("email", "test@example.com")
                .param("password", "password123")
                .param("passwordConfirm", "different"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"));
    }

    @Test
    public void testUnauthenticatedAccessToHomeRedirectsToLogin() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"WAREHOUSE_ADMIN"})
    public void testAuthenticatedWarehouseAdminCanAccessHome() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("admin")))
                .andExpect(content().string(containsString("dashboard-tile")));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testAuthenticatedCustomerCanAccessHome() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("customer")))
                .andExpect(content().string(containsString("dashboard-tile")));
    }

    @Test
    public void testInvalidLoginFails() throws Exception {
        mvc.perform(post("/login")
                .with(csrf())
                .param("username", "nonexistent")
                .param("password", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
