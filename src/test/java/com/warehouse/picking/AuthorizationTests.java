package com.warehouse.picking;

import com.warehouse.picking.model.Item;
import com.warehouse.picking.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthorizationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    public void setup() {
        itemRepository.deleteAll();
        itemRepository.save(new Item("SKU-001", "Test Item", "A-01", 10, 5));
    }

    @Test
    public void testUnauthenticatedAccessToItemsRedirectsToLogin() throws Exception {
        mvc.perform(get("/items"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testCustomerCannotAccessItems() throws Exception {
        mvc.perform(get("/items"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"WAREHOUSE_ADMIN"})
    public void testWarehouseAdminCanAccessItems() throws Exception {
        mvc.perform(get("/items"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testCustomerCannotAccessStock() throws Exception {
        mvc.perform(get("/stock"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"WAREHOUSE_ADMIN"})
    public void testWarehouseAdminCanAccessStock() throws Exception {
        mvc.perform(get("/stock"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testCustomerCannotAccessWarehouseOrders() throws Exception {
        mvc.perform(get("/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"WAREHOUSE_ADMIN"})
    public void testWarehouseAdminCanAccessOrders() throws Exception {
        mvc.perform(get("/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testCustomerCanAccessCustomerCatalogue() throws Exception {
        mvc.perform(get("/customer/catalogue"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"WAREHOUSE_ADMIN"})
    public void testWarehouseAdminCannotAccessCustomerPortal() throws Exception {
        mvc.perform(get("/customer/catalogue"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testCustomerCanAccessCustomerHome() throws Exception {
        mvc.perform(get("/customer"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"WAREHOUSE_ADMIN"})
    public void testAdminCannotAccessCustomerHome() throws Exception {
        mvc.perform(get("/customer"))
                .andExpect(status().isForbidden());
    }
}
