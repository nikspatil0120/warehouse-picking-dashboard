package com.warehouse.picking;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

class ItemCatalogueSeleniumTest {

    private WebDriver driver;
    private final String baseUrl = "http://localhost:8081";

    @RegisterExtension
    final FailureScreenshotExtension failureScreenshots =
            new FailureScreenshotExtension();

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1365,900");
        options.addArguments("--disable-gpu");
        driver = new ChromeDriver(options);
    }

    @Test
    void itemCatalogueLoadsSuccessfully() {
        driver.get(baseUrl + "/items");

        assertTrue(driver.getTitle().contains("Item Catalogue"));
        assertEquals("Item Catalogue",
                driver.findElement(By.tagName("h2")).getText());
        assertTrue(driver.findElement(By.name("search")).isDisplayed());
        assertEquals(5,
                driver.findElements(By.cssSelector("tbody tr")).size());
	org.junit.jupiter.api.Assertions.fail(
    "Intentional Task 10 quality-gate demonstration"
);
    }

    @Test
    void searchFindsMatchingItem() {
        driver.get(baseUrl + "/items");

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
        driver.get(baseUrl + "/items/new");

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
                    "Failure screenshot saved to: " + destination.toAbsolutePath()
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