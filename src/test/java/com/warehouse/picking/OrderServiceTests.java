package com.warehouse.picking;

import com.warehouse.picking.model.CustomerOrder;
import com.warehouse.picking.model.Item;
import com.warehouse.picking.model.User;
import com.warehouse.picking.repository.CustomerOrderRepository;
import com.warehouse.picking.repository.ItemRepository;
import com.warehouse.picking.repository.UserRepository;
import com.warehouse.picking.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class OrderServiceTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerOrderRepository orderRepository;

    private Item item;
    private User customer;

    @BeforeEach
    public void setup() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        orderRepository.deleteAll();

        item = new Item("SKU-TEST-" + System.nanoTime(), "Test Item", "A-01", 100, 10);
        itemRepository.save(item);

        customer = new User("customer_test_" + System.nanoTime(), "customer_test_" + System.nanoTime() + "@example.com", "hash", User.Role.ROLE_CUSTOMER);
        userRepository.save(customer);
    }

    @Test
    public void testCreateOrderForCustomer() {
        CustomerOrder order = orderService.createOrderForCustomer(customer, item.getId(), 10);

        assertNotNull(order);
        assertEquals(customer.getId(), order.getCustomer().getId());
        assertEquals(item.getId(), order.getItem().getId());
        assertEquals(10, order.getQuantity());
        assertEquals(CustomerOrder.OrderStatus.PENDING, order.getStatus());
    }

    @Test
    public void testCreateOrderDeductsStock() {
        int initialQuantity = item.getQuantity();
        orderService.createOrderForCustomer(customer, item.getId(), 10);

        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(initialQuantity - 10, updatedItem.getQuantity());
    }

    @Test
    public void testCreateOrderFailsWithInsufficientStock() {
        assertThrows(IllegalArgumentException.class, 
                () -> orderService.createOrderForCustomer(customer, item.getId(), 200));
    }

    @Test
    public void testCreateOrderFailsWithNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrderForCustomer(customer, item.getId(), -5));
    }

    @Test
    public void testCancelOrderReleasesStock() {
        int initialQuantity = item.getQuantity();
        CustomerOrder order = orderService.createOrderForCustomer(customer, item.getId(), 10);
        int stockAfterOrder = itemRepository.findById(item.getId()).orElseThrow().getQuantity();

        orderService.cancelOrder(order.getId(), customer);

        Item finalItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(initialQuantity, finalItem.getQuantity());
        
        // Reload the order from repository to get the updated status
        CustomerOrder cancelledOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(CustomerOrder.OrderStatus.CANCELLED, cancelledOrder.getStatus());
    }

    @Test
    public void testCancelOrderFailsIfNotPending() {
        CustomerOrder order = orderService.createOrderForCustomer(customer, item.getId(), 10);
        orderService.updateOrderStatus(order.getId(), CustomerOrder.OrderStatus.PICKING, customer);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.cancelOrder(order.getId(), customer));
    }

    @Test
    public void testGetOrdersByCustomer() {
        orderService.createOrderForCustomer(customer, item.getId(), 5);
        orderService.createOrderForCustomer(customer, item.getId(), 10);

        var orders = orderService.getOrdersByCustomer(customer);
        assertEquals(2, orders.size());
    }

    @Test
    public void testUpdateOrderStatusValidTransition() {
        CustomerOrder order = orderService.createOrderForCustomer(customer, item.getId(), 10);

        orderService.updateOrderStatus(order.getId(), CustomerOrder.OrderStatus.PICKING, customer);
        CustomerOrder updated = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(CustomerOrder.OrderStatus.PICKING, updated.getStatus());

        orderService.updateOrderStatus(order.getId(), CustomerOrder.OrderStatus.COMPLETED, customer);
        updated = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(CustomerOrder.OrderStatus.COMPLETED, updated.getStatus());
    }

    @Test
    public void testUpdateOrderStatusInvalidTransition() {
        CustomerOrder order = orderService.createOrderForCustomer(customer, item.getId(), 10);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(order.getId(), CustomerOrder.OrderStatus.COMPLETED, customer));
    }

    @Test
    public void testNoDoubleDeductionOnConcurrentOrders() throws InterruptedException {
        // Create multiple orders and verify stock is correctly deducted
        int initialQuantity = item.getQuantity();
        int orderQuantity = 30;
        
        orderService.createOrderForCustomer(customer, item.getId(), orderQuantity);
        orderService.createOrderForCustomer(customer, item.getId(), orderQuantity);

        Item finalItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(initialQuantity - (2 * orderQuantity), finalItem.getQuantity());
    }
}
