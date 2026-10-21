package com.warehouse.picking.config;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.repository.ItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleDataLoader {

    @Bean
    CommandLineRunner loadSampleData(ItemRepository repository) {
        return args -> {

            if (repository.count() == 0) {

                repository.save(createItem(
                        "SKU-1001",
                        "Wireless Mouse",
                        "A-01",
                        50,
                        10
                ));

                repository.save(createItem(
                        "SKU-1002",
                        "Mechanical Keyboard",
                        "A-02",
                        35,
                        8
                ));

                repository.save(createItem(
                        "SKU-1003",
                        "USB-C Cable",
                        "B-01",
                        80,
                        15
                ));

                repository.save(createItem(
                        "SKU-1004",
                        "Laptop Stand",
                        "B-02",
                        25,
                        5
                ));

                repository.save(createItem(
                        "SKU-1005",
                        "HDMI Adapter",
                        "C-01",
                        40,
                        10
                ));
            }
        };
    }

    private Item createItem(
            String sku,
            String name,
            String location,
            int quantity,
            int reorderThreshold) {

        Item item = new Item();
        item.setSku(sku);
        item.setName(name);
        item.setLocation(location);
        item.setQuantity(quantity);
        item.setReorderThreshold(reorderThreshold);

        return item;
    }
}