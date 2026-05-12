package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class TourRequestPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;
    private final Actions actions;

    private final By bookTourButton       = By.xpath("//button[contains(.,'Book tour') or contains(.,'Book Tour')]");
    private final By submitRequestButton  = By.xpath("//button[contains(.,'Submit Request') or contains(.,'Submit request')]");
    private final By toursTab             = By.xpath("//*[self::button or self::a][normalize-space()='Tours' or contains(.,'Tours')]");
    private final By childrenTrigger      = By.id("children-trigger");
    private final By requestedDateTrigger = By.id("requested_date-trigger");
    private final By requestedTimeTrigger = By.id("requested_time-trigger");

    public TourRequestPage(WebDriver driver) {
        this.driver  = driver;
        this.wait    = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.js      = (JavascriptExecutor) driver;
        this.actions = new Actions(driver);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    public void openBookTourForm() {
        forceClick(bookTourButton);
        sleep(1200);
    }

    public void selectChildren() {
        forceClick(childrenTrigger);
        sleep(1500);

        String[] optionXpaths = {
                "//*[@role='option'][not(contains(@aria-disabled,'true'))][1]",
                "//*[@role='listbox']//*[normalize-space(.)!=''][1]",
                "(//*[@data-radix-select-item])[1]",
                "(//*[@data-value])[1]",
                "//*[contains(@class,'option') and not(contains(@class,'disabled'))][normalize-space(.)!=''][1]",
                "//*[contains(@class,'item') and not(contains(@class,'disabled'))][normalize-space(.)!=''][1]"
        };

        for (String xpath : optionXpaths) {
            try {
                List<WebElement> opts = driver.findElements(By.xpath(xpath));
                if (!opts.isEmpty()) {
                    WebElement opt = opts.get(0);
                    js.executeScript("arguments[0].scrollIntoView({block:'center'});", opt);
                    sleep(300);
                    try { opt.click(); }
                    catch (Exception e) { js.executeScript("arguments[0].click();", opt); }
                    System.out.println("[selectChildren] clicked via: " + xpath);
                    sleep(600);
                    return;
                }
            } catch (Exception ignored) {}
        }
        System.err.println("[selectChildren] WARNING: no option found!");
    }

    public void setRequestedDate(String dateValue) {
        // XPaths ordered by specificity — date inputs only, no time inputs
        String[] dateXpaths = {
                "//input[@id='requested_date-trigger']",
                "//input[contains(@id,'requested_date')]",
                "//input[contains(@name,'requested_date')]",
                "//input[@type='date']"
        };
        setFieldValue(requestedDateTrigger, dateXpaths, dateValue);
        dismissOpenPicker(); // close the date picker before touching time
    }

    public void setRequestedTime(String timeValue) {
        // XPaths ordered by specificity — time inputs only, no date inputs
        String[] timeXpaths = {
                "//input[@id='requested_time-trigger']",
                "//input[contains(@id,'requested_time')]",
                "//input[contains(@name,'requested_time')]",
                "//input[@type='time']"
        };
        setFieldValue(requestedTimeTrigger, timeXpaths, timeValue);
        dismissOpenPicker();
    }

    public void submitRequest() {
        sleep(800);
        dismissOpenPicker(); // make sure nothing is covering the button

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(submitRequestButton));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        sleep(500);

        // Hide any overlays (cookie banners, toasts, modals) blocking the click
        js.executeScript(
                "document.querySelectorAll('[class*=cookie],[class*=banner],[class*=toast]')" +
                        ".forEach(el => el.style.display='none');"
        );
        sleep(300);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(submitRequestButton)).click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", btn);
        }
        System.out.println("[submitRequest] clicked.");
    }

    public void openToursTab() {
        forceClick(toursTab);
        sleep(1500);
    }

    public boolean isTextVisible(String text) {
        By locator = By.xpath("//*[contains(normalize-space(.),'" + text + "')]");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return driver.getPageSource().contains(text);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Clicks the trigger, then tries each XPath in order until it finds
     * a visible input, sets its value via sendKeys, and falls back to
     * React synthetic event injection if sendKeys doesn't work.
     */
    private void setFieldValue(By triggerLocator, String[] inputXpaths, String value) {
        forceClick(triggerLocator);
        sleep(800);

        WebElement input = findVisibleInput(inputXpaths);
        if (input == null) {
            System.err.println("[setFieldValue] ERROR: no input found for value: " + value);
            return;
        }

        js.executeScript("arguments[0].scrollIntoView({block:'center'});", input);
        sleep(300);

        // Strategy 1 — sendKeys
        try {
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.DELETE);
            input.clear();
            input.sendKeys(value);
            sleep(400);
            input.sendKeys(Keys.ESCAPE); // close picker without submitting
            System.out.println("[setFieldValue] sendKeys OK → " + value);
            return;
        } catch (Exception e) {
            System.out.println("[setFieldValue] sendKeys failed, trying JS injection...");
        }

        // Strategy 2 — React synthetic event injection
        try {
            injectReactValue(input, value);
            System.out.println("[setFieldValue] JS injection OK → " + value);
        } catch (Exception e) {
            System.err.println("[setFieldValue] JS injection failed: " + e.getMessage());
        }
    }

    /**
     * Closes any open date/time picker overlay by pressing Escape and
     * clicking the document body, so subsequent elements become clickable.
     */
    private void dismissOpenPicker() {
        try {
            actions.sendKeys(Keys.ESCAPE).perform();
        } catch (Exception ignored) {}
        try {
            js.executeScript("document.body.click();");
        } catch (Exception ignored) {}
        sleep(600);
    }

    private WebElement findVisibleInput(String[] xpaths) {
        for (String xpath : xpaths) {
            try {
                List<WebElement> found = driver.findElements(By.xpath(xpath));
                for (WebElement el : found) {
                    if (el.isDisplayed()) {
                        System.out.println("[findVisibleInput] found via: " + xpath);
                        return el;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void injectReactValue(WebElement input, String value) {
        js.executeScript(
                "var el  = arguments[0];" +
                        "var val = arguments[1];" +
                        "var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "setter.call(el, val);" +
                        "el.dispatchEvent(new Event('input',  {bubbles:true}));" +
                        "el.dispatchEvent(new Event('change', {bubbles:true}));" +
                        "el.dispatchEvent(new Event('blur',   {bubbles:true}));",
                input, value
        );
    }

    private void forceClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        sleep(300);
        try { el.click(); }
        catch (Exception e) { js.executeScript("arguments[0].click();", el); }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}