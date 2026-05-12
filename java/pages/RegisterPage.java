package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RegisterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;
    private final Random random = new Random();

    // ── Locators ──────────────────────────────────────────────────────────────
    private final By firstNameInput   = By.cssSelector("form > div:nth-child(1) input");
    private final By lastNameInput    = By.cssSelector("form > div:nth-child(2) input");
    private final By emailInput       = By.cssSelector("form > div:nth-child(3) input");
    private final By passwordInput    = By.cssSelector("form > div:nth-child(4) input");
    private final By confirmPassInput = By.cssSelector("form > div:nth-child(5) input");
    private final By submitButton     = By.cssSelector("form button[type='submit']");
    private final By childFirstName   = By.id("children.0.first_name");
    private final By childLastName    = By.id("children.0.last_name");
    private final By birthMonthTrigger = By.id("children.0.birth_month-trigger");
    private final By birthYearTrigger  = By.id("children.0.birth_year-trigger");
    private final By relationTrigger   = By.id("children.0.relation_to_child-trigger");
    private final By bothButton        = By.id("bt-0");
    private final By parentPortalButton = By.xpath("//button[@class=\"focus-visible:border-ring aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive shrink-0 cursor-pointer justify-center whitespace-nowrap transition-all outline-none focus-visible:ring-[3px] disabled:pointer-events-none disabled:cursor-default disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4 focus-visible:ring-primary/20 dark:focus-visible:ring-primary/40 dark:bg-secondary border shadow-xs h-9 px-4 py-2 has-[>svg]:px-3 flex items-center gap-2 rounded-lg border-amber-300 bg-white text-sm font-medium text-amber-600 hover:bg-amber-100\"]");
    private final By parentNextButton = By.id("parent-portal-parent-next-trigger");
    private final By childrenNext      = By.id("parent-portal-children-next-trigger");
    private final By preferencesNext   = By.id("parent-portal-preferences-next-trigger");
    private final By reviewSubmit      = By.id("parent-portal-review-submit-trigger");

    private static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

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
    public void openParentPortal()                { scrollAndClick(parentPortalButton); }
    public void clickParentNext()                 { scrollAndClick(parentNextButton); }
    public boolean isValidationMessageDisplayed(String message) {
        return !driver.findElements(By.xpath(
                "//div[contains(@class,'text-red-600') and contains(normalize-space(),'" + message + "')]"
        )).isEmpty();
    }

    public void completeChildRegistration(String firstName, String lastName) {
        type(childFirstName, firstName);
        type(childLastName, lastName);
        chooseOption(birthMonthTrigger, MONTHS[random.nextInt(MONTHS.length)]);
        chooseOption(birthYearTrigger, String.valueOf(2010 + random.nextInt(16)));
        chooseOption(relationTrigger, "Both");
        scrollAndClick(bothButton);
        scrollAndClick(childrenNext);
        scrollAndClick(preferencesNext);
        scrollAndClick(By.xpath("//button[@value='on']"));
        scrollAndClick(reviewSubmit);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    private void chooseOption(By triggerLocator, String ignoredOptionText) {
        for (int attempt = 0; attempt < 2; attempt++) {
            WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(triggerLocator));
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", trigger);
            try {
                trigger.click();
            } catch (Exception e) {
                js.executeScript("arguments[0].click();", trigger);
            }

            List<WebElement> options = new ArrayList<>(
                    driver.findElements(By.xpath("//*[@role='option' and not(@aria-hidden='true')]"))
            );
            options.removeIf(option -> !option.isDisplayed() || !option.isEnabled());
            if (options.isEmpty()) {
                continue;
            }

            WebElement option = options.get(random.nextInt(options.size()));
            try {
                js.executeScript("arguments[0].click();", option);
                return;
            } catch (StaleElementReferenceException stale) {
                if (attempt == 1) {
                    throw stale;
                }
            }
        }

        throw new RuntimeException("No stable dropdown option could be selected");
    }

    private void scrollAndClick(By locator) {
        for (int attempt = 0; attempt < 2; attempt++) {
            // Force the browser window to the bottom before clicking.
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            try {
                el.click();                                      // Try native click first
                return;
            } catch (org.openqa.selenium.ElementClickInterceptedException e) {
                js.executeScript("arguments[0].click();", el);  // Fallback: JS click
                return;
            } catch (StaleElementReferenceException stale) {
                if (attempt == 1) {
                    throw stale;
                }
            }
        }
    }
}
