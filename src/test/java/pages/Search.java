package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Search {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    private final By searchContainer = By.id("search-schools");
    private final By searchInput     = By.xpath("//*[@id='search-schools']//input");

    private final By firstResult = By.cssSelector("#search-schools div.cursor-pointer");


    public Search(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.js     = (JavascriptExecutor) driver;
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    public void searchFor(String keyword) {
        scrollAndClick(searchContainer);
        WebElement input = wait.until(
                ExpectedConditions.visibilityOfElementLocated(searchInput)
        );
        input.clear();
        input.sendKeys(keyword);
    }

    public void searchSelect() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstResult));
        scrollAndClick(firstResult);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
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