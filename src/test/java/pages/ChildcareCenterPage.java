package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ChildcareCenterPage {

    private static final String HOME_URL = "https://dev.kidsqueue.softigital.com/";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration CLICK_TIMEOUT = Duration.ofSeconds(5);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    private final By[] watchlistButtons = {
            By.xpath("//button[contains(.,'Add to Watchlist')]"),
            By.xpath("//button[contains(.,'Watchlist')]"),
            By.xpath("//a[contains(.,'Add to Watchlist')]"),
            By.xpath("//a[contains(.,'Watchlist')]"),
            By.xpath("//*[self::button or self::a][contains(@class,'watchlist')]")
    };

    public ChildcareCenterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        this.js = (JavascriptExecutor) driver;
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(HOME_URL)));
    }

    public void addToWatchlist() {
        for (By locator : watchlistButtons) {
            try {
                WebElement button = new WebDriverWait(driver, CLICK_TIMEOUT)
                        .until(ExpectedConditions.elementToBeClickable(locator));
                click(button);
                return;
            } catch (TimeoutException ignored) {
            }
        }

        throw new RuntimeException("Could not find an Add to Watchlist button on the childcare center page.");
    }

    private void click(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        try {
            element.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", element);
        }
    }
}
