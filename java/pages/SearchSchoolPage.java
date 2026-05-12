package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class SearchSchoolPage {

    private static final String SEARCH_SCHOOL_URL = "https://kidsqueue.softigital.com/";

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    private final By schoolNameInput = By.cssSelector(
            "input[type='search'], input[placeholder*='School'], input[placeholder*='Search'], input[name*='school'], input[name*='search']"
    );
    private final By searchButton = By.xpath(
            "//button[normalize-space()='Search' or contains(.,'Search')] | //input[@type='submit' and (@value='Search' or contains(@value,'Search'))]"
    );
    private final By resultCards = By.cssSelector(
            "[data-testid*='school'], #search-schools div.cursor-pointer, #search-schools li, a[href*='school']"
    );
    private final By noResultsState = By.xpath("//*[contains(.,'No results') or contains(.,'Not found')]");

    public SearchSchoolPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.js = (JavascriptExecutor) driver;
    }

    public void open() {
        driver.get(SEARCH_SCHOOL_URL);
    }

    public void enterSchoolName(String schoolName) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(schoolNameInput));
        input.clear();
        input.sendKeys(schoolName);
    }

    public void clickSearch() {
        scrollAndClick(searchButton);
    }

    public void searchForSchool(String schoolName) {
        enterSchoolName(schoolName);
        clickSearch();
    }


    public boolean isNoResultsStateVisible() {
        try {
            return wait.withTimeout(Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(noResultsState))
                    .isDisplayed();
        } catch (TimeoutException ignored) {
            return false;
        } finally {
            wait.withTimeout(Duration.ofSeconds(20));
        }
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
