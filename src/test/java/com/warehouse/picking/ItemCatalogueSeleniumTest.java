
package com.warehouse.picking;

import com.warehouse.picking.model.User;
import com.warehouse.picking.model.User.Role;
import com.warehouse.picking.model.Item;
import com.warehouse.picking.repository.UserRepository;
import com.warehouse.picking.repository.ItemRepository;
import com.warehouse.picking.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ItemCatalogueSeleniumTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ItemRepository itemRepository;

    private static final String TEST_USERNAME = "warehouse_test_user";
    private static final String TEST_EMAIL = "warehouse_test@example.com";
    private static final String TEST_PASSWORD = "TestPassword123!";

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @RegisterExtension
    final FailureScreenshotExtension failureScreenshots =
            new FailureScreenshotExtension();

    @BeforeEach
    void setUp() {
        // Create a test user with WAREHOUSE_ADMIN role
        if (!userRepository.existsByUsername(TEST_USERNAME)) {
            User testUser = new User(
                TEST_USERNAME,
                TEST_EMAIL,
                passwordEncoder.encode(TEST_PASSWORD),
                Role.ROLE_WAREHOUSE_ADMIN
            );
            userRepository.save(testUser);
        }

        // Load sample items if not already present
        if (itemRepository.count() == 0) {
            itemRepository.save(new Item("SKU-1001", "Wireless Mouse", "A-01", 50, 10));
            itemRepository.save(new Item("SKU-1002", "Mechanical Keyboard", "A-02", 35, 8));
            itemRepository.save(new Item("SKU-1003", "USB-C Cable", "B-01", 80, 15));
            itemRepository.save(new Item("SKU-1004", "Laptop Stand", "B-02", 25, 5));
            itemRepository.save(new Item("SKU-1005", "HDMI Adapter", "C-01", 40, 10));
        }

        // Initialize Selenium WebDriver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1365,900");
        options.addArguments("--disable-gpu");

        driver = new ChromeDriver(options);

        // Login via Selenium before running tests
        loginTestUser();
    }

    private void loginTestUser() {
        // Navigate to login page
        driver.get(baseUrl() + "/login");

        // Wait for page load and enter credentials
        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        usernameField.sendKeys(TEST_USERNAME);
        passwordField.sendKeys(TEST_PASSWORD);
        loginButton.click();

        // Wait for redirect to home page (or items page) to complete
        // This ensures the session cookie is set before navigating to protected routes
        try {
            Thread.sleep(2000); // Give page time to load after login
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void itemCatalogueLoadsSuccessfully() throws InterruptedException {
        driver.get(baseUrl() + "/items");

        // Wait for page to fully load
        Thread.sleep(1000);

        assertTrue(driver.getTitle().contains("Item Catalogue"));
        assertEquals("Item Catalogue",
                driver.findElement(By.tagName("h2")).getText());
        assertTrue(driver.findElement(By.name("search")).isDisplayed());
        
        // Wait for table rows to be present
        java.util.List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        int rowCount = rows.size();
        
        // Should have at least 1 row (could be more if sample data was loaded)
        assertTrue(rowCount >= 1, "Expected at least 1 item row, but found: " + rowCount);
    }

    @Test
    void searchFindsMatchingItem() {
        driver.get(baseUrl() + "/items");

        WebElement searchBox = driver.findElement(By.name("search"));
        searchBox.sendKeys("HDMI Adapter");

        driver.findElement(
                By.xpath("//button[normalize-space()='Search']")
        ).click();

        assertTrue(driver.getPageSource().contains("HDMI Adapter"));
        assertFalse(driver.getPageSource().contains("Mechanical Keyboard"));
    }

    @Test
    void addItemFormOpens() {
        driver.get(baseUrl() + "/items/new");

        assertTrue(driver.findElement(By.tagName("h2"))
                .getText().contains("Add New Item"));
        assertTrue(driver.findElement(By.id("sku")).isDisplayed());
        assertTrue(driver.findElement(By.id("name")).isDisplayed());
        assertTrue(driver.findElement(By.id("save-item")).isDisplayed());
    }

    private void captureFailureScreenshot(String testName) {
        if (driver == null) {
            return;
        }

        try {
            Path directory = Path.of("target", "selenium-screenshots");
            Files.createDirectories(directory);

            String safeName = testName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path destination = directory.resolve(safeName + ".png");

            Files.copy(
                    ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE)
                            .toPath(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.err.println(
                    "Failure screenshot saved to: "
                            + destination.toAbsolutePath()
            );
        } catch (Exception screenshotError) {
            System.err.println(
                    "Could not save failure screenshot: "
                            + screenshotError.getMessage()
            );
        }
    }

    class FailureScreenshotExtension
            implements TestExecutionExceptionHandler {

        @Override
        public void handleTestExecutionException(
                ExtensionContext context,
                Throwable throwable
        ) throws Throwable {
            captureFailureScreenshot(
                    context.getRequiredTestMethod().getName()
            );
            throw throwable;
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
