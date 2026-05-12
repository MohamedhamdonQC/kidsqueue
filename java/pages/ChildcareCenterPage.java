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

/**
 * Page Object for the Childcare Center detail page.
 *
 * Covers:
 *  • Watchlist flow
 *  • Waitlist form
 *  • Message flow  →  open modal / section  →  fill Subject + Message  →  Send
 */
public class ChildcareCenterPage {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration SHORT_TIMEOUT   = Duration.ofSeconds(5);

    // ── Infrastructure ────────────────────────────────────────────────────────
    private final WebDriver          driver;
    private final WebDriverWait      wait;
    private final WebDriverWait      shortWait;
    private final JavascriptExecutor js;

    // ── Watchlist ─────────────────────────────────────────────────────────────
    private final By[] watchlistButtons = {
            By.xpath("//button[contains(.,'Add to Watchlist')]"),
            By.xpath("//button[contains(.,'Watchlist')]"),
            By.xpath("//a[contains(.,'Add to Watchlist')]"),
            By.xpath("//a[contains(.,'Watchlist')]"),
            By.xpath("//*[self::button or self::a][contains(@class,'watchlist')]")
    };

    // ── Waitlist ──────────────────────────────────────────────────────────────
    private final By submitWaitlistButton =
            By.xpath("//button[contains(.,'Add to Waitlist') or contains(.,'Submit')]");

    // ── Message trigger  (the "Message" / "Send Message" CTA on the page) ────
    /**
     * Ordered list of selectors tried in sequence to open the message
     * compose area (modal, inline panel, or a dedicated section).
     */
    private final By[] messageOpenTriggers = {
            By.xpath("//button[normalize-space()='Message']"),
            By.xpath("//button[contains(.,'Send Message')]"),
            By.xpath("//a[normalize-space()='Message']"),
            By.xpath("//button[contains(@class,'message')]"),
            By.xpath("//*[@data-action='message' or @data-target='message']")
    };

    // ── Message compose form ──────────────────────────────────────────────────
    /**
     * Subject field – may be a plain text input or a labelled input.
     * Tried in order; first visible one wins.
     */
    private final By[] subjectLocators = {
            By.cssSelector("input#subject[name='subject']"),
            By.cssSelector("input[name='subject']"),
            By.xpath("//input[@placeholder[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'subject')]]"),
            By.xpath("//label[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'subject')]/following-sibling::input[1]"),
            By.xpath("//label[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'subject')]/..//input")
    };

    /** Message body textarea. */
    private final By messageTextarea  = By.cssSelector("textarea#message[name='message']");

    /** Send / Submit button inside the compose form. */
    private final By sendMessageButton =
            By.xpath("//button[@type='submit'][contains(.,'Send') or contains(.,'Message')]");

    // ── Success / error feedback ──────────────────────────────────────────────
    /** Toast / alert that appears after a successful send. */
    private final By successToast = By.xpath(
            "//*[contains(@class,'toast') or contains(@class,'alert') or contains(@class,'success') or contains(@role,'alert')]" +
                    "[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sent') or " +
                    " contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success') or " +
                    " contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'message')]"
    );

    // ── Constructor ───────────────────────────────────────────────────────────
    public ChildcareCenterPage(WebDriver driver) {
        this.driver    = driver;
        this.wait      = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        this.shortWait = new WebDriverWait(driver, SHORT_TIMEOUT);
        this.js        = (JavascriptExecutor) driver;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Waits for the page's document.readyState to be "complete". */
    public void waitUntilLoaded() {
        wait.until(d -> "complete".equals(
                String.valueOf(js.executeScript("return document.readyState"))
        ));
    }

    /** Clicks whichever watchlist button is present on the page. */
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

    /** Fills and submits the waitlist form. */
    public void completeWaitlistForm() {
        selectOptionByLabel("Parent/Guardian", "Choose Parent/Guardian", "Parent/Guardian");
        selectOptionByLabel("Age Group",       "Select any option",       "Age Group");
        setPreferredStartDate();
        selectOptionByLabel("Priority Tier",   "Choose high",             "High");
        clickSubmitWaitlist();
    }

    /**
     * Full message flow:
     * <ol>
     *   <li>Click the "Message" trigger to open the compose area (if present).</li>
     *   <li>Fill in the <b>Subject</b> field (if present).</li>
     *   <li>Fill in the <b>Message</b> textarea.</li>
     *   <li>Click <em>Send Message</em>.</li>
     * </ol>
     *
     * @param subject     text for the subject line (pass {@code null} or {@code ""} to skip)
     * @param messageText body of the message
     */
    public void sendMessage(String subject, String messageText) {
        // Step 1 – open the compose area (button may not exist if inline)
        openMessageCompose();

        // Step 2 – fill Subject (optional field)
        if (subject != null && !subject.isBlank()) {
            fillSubject(subject);
        }

        // Step 3 – fill Message textarea
        WebElement textarea = wait.until(
                ExpectedConditions.visibilityOfElementLocated(messageTextarea));
        scrollToCenter(textarea);
        textarea.clear();
        textarea.sendKeys(messageText);

        // Step 4 – click Send
        WebElement sendBtn = wait.until(
                ExpectedConditions.elementToBeClickable(sendMessageButton));
        click(sendBtn);
    }

    /**
     * Convenience overload – no subject field.
     *
     * @param messageText body of the message
     */
    public void sendMessage(String messageText) {
        sendMessage(null, messageText);
    }

    /**
     * Waits up to {@code DEFAULT_TIMEOUT} for the success toast/alert to appear.
     *
     * @return {@code true} if a success indicator is visible, {@code false} otherwise
     */
    public boolean isMessageSentSuccessfully() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Tries each trigger locator in {@link #messageOpenTriggers}.
     * If none are found (e.g. the compose area is always visible), this is a no-op.
     */
    private void openMessageCompose() {
        for (By locator : messageOpenTriggers) {
            try {
                WebElement trigger = new WebDriverWait(driver, SHORT_TIMEOUT)
                        .until(ExpectedConditions.elementToBeClickable(locator));
                click(trigger);
                // Small grace period for the modal/panel animation
                waitUntilLoaded();
                return;
            } catch (TimeoutException ignored) { }
        }
        // Compose area already visible – continue silently
    }

    /**
     * Fills the Subject input using the first visible locator in {@link #subjectLocators}.
     */
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
        // Subject field absent on this page variant – silently skip
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