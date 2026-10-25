
package com.warehouse.picking.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public String home(Authentication authentication, Model model) {
        // Check if user is warehouse admin or customer
        boolean isWarehouseAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_WAREHOUSE_ADMIN"));
        
        model.addAttribute("isWarehouseAdmin", isWarehouseAdmin);
        model.addAttribute("username", authentication.getName());
        
        return "home";
    }
}
