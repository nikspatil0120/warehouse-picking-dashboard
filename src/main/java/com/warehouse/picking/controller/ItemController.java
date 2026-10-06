package com.warehouse.picking.controller;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", service.findAll());
        return "items/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("item", new Item());
        return "items/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("item") Item item,
            BindingResult result,
            RedirectAttributes ra) {

        if (!result.hasFieldErrors("sku") && service.skuExists(item.getSku())) {
            result.rejectValue("sku", "duplicate", "SKU already exists");
        }

        if (result.hasErrors()) {
            return "items/form";
        }

        service.add(item);
        ra.addFlashAttribute("message", "Item added successfully");

        return "redirect:/items";
    }
}