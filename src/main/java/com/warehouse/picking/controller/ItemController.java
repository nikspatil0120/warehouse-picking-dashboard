
package com.warehouse.picking.controller;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.service.ItemService;
import com.warehouse.picking.repository.ItemRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/items")
public class ItemController {

    private final ItemService service;
    private final ItemRepository repository;

    public ItemController(ItemService service, ItemRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            Model model) {

        List<Item> items = service.findAll();

        if (search != null && !search.isBlank()) {
            String query = search.toLowerCase(Locale.ROOT);
            items = items.stream()
                    .filter(item ->
                            (item.getSku() != null &&
                                    item.getSku().toLowerCase(Locale.ROOT)
                                            .contains(query))
                            || (item.getName() != null &&
                                    item.getName().toLowerCase(Locale.ROOT)
                                            .contains(query)))
                    .toList();
        }

        model.addAttribute("items", items);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("totalItems", repository.count());
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

        if (!result.hasFieldErrors("sku")
                && service.skuExists(item.getSku())) {
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
