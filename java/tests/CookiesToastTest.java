package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class CookiesToastTest {

    private static final String BASE_URL = "https://staging.kidsqueue.softigital.com/";

    private WebDriver driver;
    private WebDriverWait wait;

    private final By acceptCookiesButton = By.xpath(
            "//button[contains(normalize-space(.),'Accept') and contains(normalize-space(.),'Cookies')] | " +
                    "//button[contains(normalize-space(.),'Accept All')] | " +
                    "//button[contains(normalize-space(.),'Accept')] | " +
                    "//button[contains(normalize-space(.),'Agree')] | " +
                    "//button[contains(normalize-space(.),'Allow')]"
    );

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--incognito");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(description = "Cookie toast appears and can be accepted")
    public void acceptCookieToast() {
        driver.get(BASE_URL);

        WebElement acceptButton = wait.until(
                ExpectedConditions.visibilityOfElementLocated(acceptCookiesButton)
        );
        Assert.assertTrue(acceptButton.isDisplayed(), "Expected cookie toast accept button to be visible");

        try {
            acceptButton.click();
        } catch (Exception e) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", acceptButton);
        }

        wait.until(ExpectedConditions.invisibilityOfElementLocated(acceptCookiesButton));
        Assert.assertTrue(
                driver.findElements(acceptCookiesButton).isEmpty(),
                "Cookie toast should disappear after acceptance"
        );
    }
}
