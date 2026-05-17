package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class ChildcareCenterPage {

    
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration SHORT_TIMEOUT   = Duration.ofSeconds(5);

    
    private final WebDriver          driver;
    private final WebDriverWait      wait;
    private final WebDriverWait      shortWait;
    private final JavascriptExecutor js;

    
    private final By[] watchlistButtons = {
            By.xpath("//button[contains(.,'Add to Watchlist')]"),
            By.xpath("//button[contains(.,'Watchlist')]"),
            By.xpath("//a[contains(.,'Add to Watchlist')]"),
            By.xpath("//a[contains(.,'Watchlist')]"),
            By.xpath("//*[self::button or self::a][contains(@class,'watchlist')]")
    };

    
    private final By submitWaitlistButton =
            By.xpath("//button[contains(.,'Add to Waitlist') or contains(.,'Submit')]");

    
    
    private final By[] messageOpenTriggers = {
            By.xpath("//button[normalize-space()='Message']"),
            By.xpath("//button[contains(.,'Send Message')]"),
            By.xpath("//a[normalize-space()='Message']"),
            By.xpath("//button[contains(@class,'message')]"),
            By.xpath("//*[@data-action='message' or @data-target='message']")
    };

    
    
    private final By[] subjectLocators = {
            By.cssSelector("input#subject[name='subject']"),
            By.cssSelector("input[name='subject']"),
            By.xpath("//input[@placeholder[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'subject')]]"),
            By.xpath("//label[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'subject')]/following-sibling::input[1]"),
            By.xpath("//label[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'subject')]/..//input")
    };

    
    private final By messageTextarea  = By.cssSelector("textarea#message[name='message']");

    
    private final By sendMessageButton =
            By.xpath("//button[@type='submit'][contains(.,'Send') or contains(.,'Message')]");

    
    
    private final By successToast = By.xpath(
            "//*[contains(@class,'toast') or contains(@class,'alert') or contains(@class,'success') or contains(@role,'alert')]" +
                    "[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sent') or " +
                    " contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success') or " +
                    " contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'message')]"
    );

    
    public ChildcareCenterPage(WebDriver driver) {
        this.driver    = driver;
        this.wait      = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        this.shortWait = new WebDriverWait(driver, SHORT_TIMEOUT);
        this.js        = (JavascriptExecutor) driver;
    }

    

    
    public void waitUntilLoaded() {
        wait.until(d -> "complete".equals(
                String.valueOf(js.executeScript("return document.readyState"))
        ));
    }

    
    public void addToWatchlist() {
        for (By locator : watchlistButtons) {
            try {
                WebElement btn = new WebDriverWait(driver, SHORT_TIMEOUT)
                        .until(ExpectedConditions.elementToBeClickable(locator));
                click(btn);
                return;
            } catch (TimeoutException ignored) { }
        }
        throw new RuntimeException(
                "Could not find an 'Add to Watchlist' button on the childcare centre page.");
    }

    
    public void completeWaitlistForm() {
        selectOptionByLabel("Parent/Guardian", "Choose Parent/Guardian", "Parent/Guardian");
        selectOptionByLabel("Age Group",       "Select any option",       "Age Group");
        setPreferredStartDate();
        selectOptionByLabel("Priority Tier",   "Choose high",             "High");
        clickSubmitWaitlist();
    }

    
    public void sendMessage(String subject, String messageText) {
        
        openMessageCompose();

        
        if (subject != null && !subject.isBlank()) {
            fillSubject(subject);
        }

        
        WebElement textarea = wait.until(
                ExpectedConditions.visibilityOfElementLocated(messageTextarea));
        scrollToCenter(textarea);
        textarea.clear();
        textarea.sendKeys(messageText);

        
        WebElement sendBtn = wait.until(
                ExpectedConditions.elementToBeClickable(sendMessageButton));
        click(sendBtn);
    }

    
    public void sendMessage(String messageText) {
        sendMessage(null, messageText);
    }

    
    public boolean isMessageSentSuccessfully() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    

    
    private void openMessageCompose() {
        for (By locator : messageOpenTriggers) {
            try {
                WebElement trigger = new WebDriverWait(driver, SHORT_TIMEOUT)
                        .until(ExpectedConditions.elementToBeClickable(locator));
                click(trigger);
                
                waitUntilLoaded();
                return;
            } catch (TimeoutException ignored) { }
        }
        
    }

    
    private void fillSubject(String subject) {
        for (By locator : subjectLocators) {
            try {
                WebElement input = new WebDriverWait(driver, SHORT_TIMEOUT)
                        .until(ExpectedConditions.visibilityOfElementLocated(locator));
                scrollToCenter(input);
                input.clear();
                input.sendKeys(subject);
                return;
            } catch (TimeoutException ignored) { }
        }
        
        System.out.println("[INFO] Subject field not found – skipping.");
    }

    private void selectOptionByLabel(String labelText, String optionText, String fieldHint) {
        String lLabel  = labelText.toLowerCase();
        String lOption = optionText.toLowerCase();
        String lHint   = fieldHint.toLowerCase();

        By trigger = By.xpath(
                "//*[self::button or self::div or self::span or self::input]" +
                        "[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lLabel + "') or " +
                        " contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lLabel + "') or " +
                        " contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lOption + "')]"
        );
        try { click(wait.until(ExpectedConditions.elementToBeClickable(trigger))); }
        catch (Exception ignored) { }

        By option = By.xpath(
                "//*[self::div or self::button or self::span or self::li]" +
                        "[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lOption + "') or " +
                        " contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lHint  + "')]"
        );
        try { click(wait.until(ExpectedConditions.elementToBeClickable(option))); }
        catch (Exception ignored) { }
    }

    private void setPreferredStartDate() {
        String futureDate = LocalDate.now().plusDays(3).format(DateTimeFormatter.ISO_DATE);
        By dateInput = By.xpath(
                "//input[contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'start') or " +
                        "contains(translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'start') or " +
                        "contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'date') or " +
                        "@type='date']"
        );
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(dateInput));
        click(input);
        input.clear();
        input.sendKeys(futureDate);
    }

    private void clickSubmitWaitlist() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(submitWaitlistButton));
        click(button);
    }

    private void scrollToCenter(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    private void click(WebElement element) {
        scrollToCenter(element);
        try {
            element.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", element);
        }
    }
}