package com.warehouse.picking.service;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository repository;

    @InjectMocks
    private ItemService service;

    @Test
    void findAllReturnsItemsSortedByName() {
        Item item1 = new Item();
        item1.setSku("SKU-1001");
        item1.setName("Mouse");

        Item item2 = new Item();
        item2.setSku("SKU-1002");
        item2.setName("Keyboard");

        when(repository.findAll())
                .thenReturn(List.of(item1, item2));

        List<Item> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Keyboard", result.get(0).getName());
        assertEquals("Mouse", result.get(1).getName());

        verify(repository).findAll();
    }

    @Test
    void skuExistsReturnsTrueWhenSkuExists() {
        when(repository.existsBySku("SKU-1001"))
                .thenReturn(true);

        assertTrue(service.skuExists("SKU-1001"));

        verify(repository).existsBySku("SKU-1001");
    }

    @Test
    void skuExistsReturnsFalseWhenSkuDoesNotExist() {
        when(repository.existsBySku("SKU-9999"))
                .thenReturn(false);

        assertFalse(service.skuExists("SKU-9999"));

        verify(repository).existsBySku("SKU-9999");
    }

    @Test
    void addSavesAndReturnsItem() {
        Item item = new Item();
        item.setSku("SKU-1003");
        item.setName("Monitor");
        item.setQuantity(25);

        when(repository.save(item))
                .thenReturn(item);

        Item result = service.add(item);

        assertSame(item, result);

        verify(repository).save(item);
    }
}