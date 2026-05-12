package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.time.Duration;

public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    // ───────── Setup ─────────
    @BeforeMethod
    public void setup() {
        createDriver();
        driver.manage().deleteAllCookies();
        handleCookiesIfPresent(); // ✅ Auto-called before every test
    }

    @AfterMethod
    public void tearDown() {
        quitDriver();
    }

    // ───────── SAFE CLICK (IMPROVED) ─────────
    protected void safeClick(By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        waitForPageReady();

        WebElement element = wait.until(
                ExpectedConditions.elementToBeClickable(locator)
        );

        ((JavascriptExecutor) driver)
                .executeScript("document.activeElement.blur();");

        ((JavascriptExecutor) driver)
                .executeScript(
                        "arguments[0].scrollIntoView({block:'center', inline:'center'});",
                        element
                );

        try { Thread.sleep(500); } catch (Exception ignored) {}

        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", element);
        }
    }

    // ───────── HANDLE COOKIES ─────────
    protected void handleCookiesIfPresent() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            By acceptBtn = By.xpath(
                    "//button[contains(normalize-space(.),'Accept') or " +
                            "contains(normalize-space(.),'Agree') or " +
                            "contains(normalize-space(.),'Allow') or " +
                            "contains(normalize-space(.),'I agree')]"
            );

            WebElement btn = wait.until(
                    ExpectedConditions.elementToBeClickable(acceptBtn)
            );

            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'});", btn);

            try {
                btn.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            }

            hideCookieBannerArtifacts();

        } catch (Exception ignored) {
        }
    }

    protected void hideCookieBannerArtifacts() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "Array.from(document.querySelectorAll('body *')).forEach(function(el) {" +
                            "  var id = (el.id || '').toLowerCase();" +
                            "  var cls = (el.className || '').toString().toLowerCase();" +
                            "  var text = (el.innerText || '').toLowerCase();" +
                            "  var matches = /cookie|consent/.test(id) || /cookie|consent/.test(cls) || /cookie|consent/.test(text);" +
                            "  if (matches) {" +
                            "    el.style.display = 'none';" +
                            "    el.style.visibility = 'hidden';" +
                            "    el.style.pointerEvents = 'none';" +
                            "  }" +
                            "});"
            );
        } catch (Exception ignored) {
        }
    }

    // ───────── PAGE READY ─────────
    protected void waitForPageReady() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(webDriver ->
                ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")
                        .equals("complete")
        );
    }

    // ───────── DRIVER ─────────
    private void createDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--incognito");

        driver = new ChromeDriver(options);
    }

    private void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
