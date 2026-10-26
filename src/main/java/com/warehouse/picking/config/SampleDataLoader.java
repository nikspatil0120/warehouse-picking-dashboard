package com.warehouse.picking.config;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleDataLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(SampleDataLoader.class);

    @Bean
    CommandLineRunner loadSampleData(ItemRepository repository) {
        return args -> {
            
            logger.info("Checking sample data: current item count = {}", repository.count());

            // Define sample items
            String[][] items = {
                {"SKU-1001", "Wireless Mouse", "A-01", "50", "10"},
                {"SKU-1002", "Mechanical Keyboard", "A-02", "35", "8"},
                {"SKU-1003", "USB-C Cable", "B-01", "80", "15"},
                {"SKU-1004", "Laptop Stand", "B-02", "25", "5"},
                {"SKU-1005", "HDMI Adapter", "C-01", "40", "10"}
            };
            
            int itemsCreated = 0;
            
            // Check and create each item if it doesn't exist
            for (String[] itemData : items) {
                String sku = itemData[0];
                if (!repository.existsBySku(sku)) {
                    Item item = createItem(
                            sku,
                            itemData[1],
                            itemData[2],
                            Integer.parseInt(itemData[3]),
                            Integer.parseInt(itemData[4])
                    );
                    repository.save(item);
                    itemsCreated++;
                    logger.debug("Created item: {}", sku);
                }
            }
            
            if (itemsCreated > 0) {
                logger.info("✅ {} sample items created successfully", itemsCreated);
            } else {
                logger.info("✅ All sample items already exist in database");
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