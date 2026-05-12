package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class dashboardPage {

    private final WebDriver          driver;
    private final WebDriverWait      wait;
    private final JavascriptExecutor js;

    private final By watching  = By.id("dashboard-tab-watching");
    private final By checkName = By.xpath("//div[@data-slot='card-title']");

    public dashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.js     = (JavascriptExecutor) driver;
    }

    public void open(String url) {
        driver.get(url);
    }

    public void clickWatching() {
        scrollToBottom();
        scrollAndClick(watching);
    }

    /** Returns true if a card with the given school name is visible. */
    public boolean isSchoolVisible(String name) {
        By locator = By.xpath(
                "//div[@data-slot='card-title' and contains(text(),'" + name + "')]");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether the sent message text appears anywhere on the messages page.
     * Tries an exact-text XPath first; falls back to a page-source contains check.
     */
    public boolean isMessageVisible(String messageText) {
        By locator = By.xpath("//*[contains(text(),'" + messageText + "')]");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            // Fallback: raw page source check
            return driver.getPageSource().contains(messageText);
        }
    }

    /**
     * Scrolls the page fully to the bottom smoothly,
     * used after navigating to the messages dashboard to reveal sent messages.
     */
    public void scrollToMessagesBottom() {
        js.executeScript(
                "window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private void scrollToBottom() {
        js.executeScript("window.scrollBy(0, 400);");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    private void scrollAndClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        try {
            el.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", el);
        }
    }
}