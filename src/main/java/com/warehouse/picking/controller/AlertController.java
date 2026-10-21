
package com.warehouse.picking.controller;

import com.warehouse.picking.repository.ItemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alerts")
public class AlertController {

    private final ItemRepository itemRepository;

    public AlertController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("alerts", itemRepository.findAll().stream()
                .filter(item -> item.getQuantity() == 0
                        || item.getQuantity() <= item.getReorderThreshold())
                .toList());
        return "alerts";
    }
}
