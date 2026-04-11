package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RegisterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    // ── Locators ──────────────────────────────────────────────────────────────
    private final By firstNameInput   = By.cssSelector("form > div:nth-child(1) input");
    private final By lastNameInput    = By.cssSelector("form > div:nth-child(2) input");
    private final By emailInput       = By.cssSelector("form > div:nth-child(3) input");
    private final By passwordInput    = By.cssSelector("form > div:nth-child(4) input");
    private final By confirmPassInput = By.cssSelector("form > div:nth-child(5) input");
    private final By submitButton     = By.cssSelector("form button[type='submit']");

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.js     = (JavascriptExecutor) driver;
    }

    public void open(String url)                  { driver.get(url); }
    public void fillFirstName(String value)       { type(firstNameInput,   value); }
    public void fillLastName(String value)        { type(lastNameInput,    value); }
    public void fillEmail(String value)           { type(emailInput,       value); }
    public void fillPassword(String value)        { type(passwordInput,    value); }
    public void fillConfirmPassword(String value) { type(confirmPassInput, value); }
    public void submit()                          { scrollAndClick(submitButton); }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    private void scrollAndClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        // Scroll element to center of viewport to avoid sticky header/footer overlap
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        try {
            el.click();                                      // Try native click first
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", el);  // Fallback: JS click
        }
    }
}