package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class EventPage {

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(30);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    // ─────────────────────────────────────────────────────────────────────────
    // TAB NAVIGATION  (Groups page)
    // ─────────────────────────────────────────────────────────────────────────

    /** "Events" tab – Radix TabsTrigger whose id ends with '-trigger-events' */
    private final By eventsTab = By.id("groups-tab-events");

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE EVENT BUTTON  (inside Events tab)
    // The button contains a <svg> plus sign and the text "Create Event"
    // ─────────────────────────────────────────────────────────────────────────
    private final By createEventButton = By.id("create-event-open-trigger") ;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE EVENT FORM FIELDS
    // These live inside a modal/dialog opened by the Create Event button.
    // The app uses Next.js + Radix UI components – inputs are <input> tags
    // identified most reliably by their placeholder text.
    // ─────────────────────────────────────────────────────────────────────────

    /** Event Name – text input */
    private final By eventNameInput = By.xpath(
            "//input[@placeholder='Enter event name']"
    );

    /** Event Description – input or textarea */
    private final By eventDescriptionInput = By.xpath(
            "//*[self::input or self::textarea][@placeholder='Enter event description']"
    );
    private final By ClicknextCal = By.xpath(
            "//button[@aria-label='Go to the Next Month']"
    );

    // ── Group/Audience combobox ───────────────────────────────────────────────
    /**
     * The Group/Audience field renders as a Radix combobox trigger button
     * whose visible text is "Choose group/audience".
     */
    private final By audienceComboboxTrigger = By.xpath(
            "//button[@role='combobox' and contains(normalize-space(.),'Choose group/audience')]"
    );

    /**
     * After clicking the combobox, options appear in a listbox.
     * We pick the first visible option item.
     */
    private final By audienceFirstOption = By.xpath(
            "(//*[@role='option' or @role='listitem' or contains(@class,'command-item')])[1]"
    );

    // ── Visibility dropdown ───────────────────────────────────────────────────
    /**
     * Visibility is a Select / combobox whose placeholder text is
     * "Choose visibility".
     */
    private final By visibilityTrigger = By.xpath(
            "//button[@role='combobox' and contains(normalize-space(.),'Choose visibility')]"
    );

    /** The "Public" option inside the visibility dropdown */
    private final By visibilityPublicOption = By.xpath(
            "//*[@role='option' and normalize-space(.)='Public']"
    );

    // ── Date pickers ──────────────────────────────────────────────────────────
    /**
     * Start date – the calendar trigger button.
     * Both date pickers share the same placeholder text "Pick a date";
     * we target by label proximity using Radix 'data-slot' or by index.
     */
    private final By startDateTrigger = By.xpath(
            "(//button[contains(normalize-space(.),'Pick a date')])[1]"
    );

    private final By endDateTrigger = By.xpath(
            "//button[@id='end_date-trigger']"
    );

    /**
     * After a date picker opens, individual day cells are rendered as
     * <td role="gridcell"> or <button> inside the calendar grid.
     * We use a generic day-cell selector and pick the target day by text.
     */
    private final String dayInCalendar =
            "//td[@role='gridcell' and not(@aria-disabled='true') and normalize-space(.)='%s']" +
                    "| //button[@role='gridcell' and not(@disabled) and normalize-space(.)='%s']";

    // ── Time inputs ───────────────────────────────────────────────────────────
    /**
     * Start/End time fields render as native <input type="time">.
     * Target by type + label proximity (first = start, second = end).
     */
    private final By startTimeInput = By.xpath(
            "(//input[@type='time'])[1]"
    );

    private final By endTimeInput = By.xpath(
            "(//input[@type='time'])[2]"
    );

    // ── Location ──────────────────────────────────────────────────────────────
    private final By locationInput = By.xpath(
            "//input[@placeholder='Enter location']"
    );

    // ── Navigation buttons ────────────────────────────────────────────────────

    private final By nextButton = By.xpath(
            "//button[@data-slot='button' and normalize-space()='Next']"
    );
    private final By nextButtonFallback = By.xpath(
            "//button[contains(@class,'bg-primary') and normalize-space()='Next']"
    );

    private final By Invites = By.xpath(
            "//button[@id='invite-all']"
    );

    private final By passportNotes        = By.id("checklist.0.notes");
    private final By travelInsuranceNotes = By.id("checklist.1.notes");
    private final By emergencyContactNotes= By.id("checklist.2.notes");
    private final By chaperonesNotes      = By.id("checklist.3.notes");
    private final By firstAidKitNotes     = By.id("checklist.4.notes");
    private final By groupPhotoCardNotes  = By.id("checklist.5.notes");
    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────

    public EventPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, WAIT_TIMEOUT);
        this.js     = (JavascriptExecutor) driver;
    }
    public void openGroupsPage() {
        driver.get("https://kidsqueue.softigital.com/parent/groups?page=1");
    }

    /** Click the "Events" tab to switch to the events view */
    public void clickEventsTab() {
        click(eventsTab);
    }
    public void ClicknextCal() {
        click(ClicknextCal);
    }

    /** Click the "+ Create Event" button to open the creation dialog */
    public void clickCreateEvent() {
        click(createEventButton);
    }

    /** Type into the Event Name field */
    public void fillEventName(String eventName) {
        type(eventNameInput, eventName);
    }

    /** Type into the Event Description field */
    public void fillEventDescription(String description) {
        type(eventDescriptionInput, description);
    }

    /**
     * Open Group/Audience combobox and click the first available option.
     * If your test needs a specific group, call {@link #chooseAudienceByName(String)} instead.
     */
    public void chooseFirstAudienceResult() {
        click(audienceComboboxTrigger);
        clickFirstVisible(audienceFirstOption);
    }

    /** Open Group/Audience combobox and select by exact option text */
    public void chooseAudienceByName(String groupName) {
        click(audienceComboboxTrigger);
        By option = By.xpath(
                String.format(
                        "//*[@role='option' and normalize-space(.)='%s']" +
                                "| //*[contains(@class,'command-item') and normalize-space(.)='%s']",
                        groupName, groupName)
        );
        click(option);
    }

    /**
     * Open the Visibility dropdown and select "Public".
     * Extend similarly for "Private" etc.
     */
    public void choosePublicVisibility() {
        click(visibilityTrigger);
        click(visibilityPublicOption);
    }
    public void fillChecklistNote(int index, String notes) {
        type(By.id("checklist." + index + ".notes"), notes);
    }

    private final By submitButton = By.xpath(
            "//button[contains(@type,'submit')]"
    );
    /**
     * Open the Visibility dropdown and select any option by label text.
     * E.g. chooseVisibility("Private")
     */
    public void chooseVisibility(String visibilityLabel) {
        click(visibilityTrigger);
        By option = By.xpath(
                String.format("//*[@role='option' and normalize-space(.)='%s']", visibilityLabel)
        );
        click(option);
    }

    /**
     * Click the Start Date picker and then click the correct day.
     *
     * @param day  day of month as shown in the calendar, e.g. "15"
     */
    public void selectStartDate(String day) {
        click(startDateTrigger);
        ClicknextCal();
        clickCalendarDay(day);
    }

    /**
     * Click the End Date picker and then click the correct day.
     *
     * @param day  day of month as shown in the calendar, e.g. "16"
     */
    public void selectEndDate(String day) {
        click(endDateTrigger);
        ClicknextCal();
        clickCalendarDay(day);
    }

    /**
     * Fill the Start Time native input.
     * Format must match the browser locale – typically "HH:mm" (24-h) or "hh:mm AM/PM".
     */
    public void fillStartTime(String startTime) {
        typeTime(startTimeInput, startTime);
    }

    /** Fill the End Time native input. */
    public void fillEndTime(String endTime) {
        typeTime(endTimeInput, endTime);
    }

    /** Type into the Location field */
    public void fillLocation(String location) {
        type(locationInput, location);
    }

    /** Click the "Next" button at the bottom of the form */
    public void clickNext() {
        try {
            // Wait until dialog is fully rendered
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        // Try 3 strategies
        if (!tryClickNext()) {
            throw new AssertionError("Failed to click Next button after all attempts.");
        }
    }

    public void clickInvitesbuttom() {
        click(Invites);
    }

    public void clickSubmit() {
        click(submitButton);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void click(By locator) {
        try {
            WebElement el = getVisibleElement(locator);
            scrollToCenter(el);
            try {
                el.click();
            } catch (ElementClickInterceptedException e) {
                js.executeScript("arguments[0].click();", el);
            }
        } catch (TimeoutException e) {
            throw new AssertionError("Could not click element: " + locator, e);
        }
    }

    private WebElement getVisibleElement(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        List<WebElement> elements = driver.findElements(locator);
        for (WebElement el : elements) {
            if (el.isDisplayed()) return el;
        }
        // Fallback: return last element even if not yet visible
        return elements.get(elements.size() - 1);
    }

    private void scrollToCenter(WebElement el) {
        js.executeScript("arguments[0].scrollIntoView({block:'center',inline:'nearest'});", el);
    }

    /**
     * Standard text input: clear then sendKeys.
     * Falls back to JS value injection + synthetic events for React-controlled inputs.
     */
    private void type(By locator, String value) {
        WebElement el = getVisibleElement(locator);
        scrollToCenter(el);
        el.clear();
        el.sendKeys(value);

        // Ensure React state is updated
        js.executeScript(
                "arguments[0].dispatchEvent(new Event('input',  {bubbles:true}));" +
                        "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                el
        );
    }

    /**
     * Fills a native <input type="time"> reliably across browsers.
     * Sends the value character-by-character to avoid browser auto-formatting issues.
     */
    private void typeTime(By locator, String timeValue) {
        WebElement el = getVisibleElement(locator);
        scrollToCenter(el);
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        // Send digits only – browser fills the colon/AM-PM separators automatically
        String digits = timeValue.replaceAll("[^0-9AaPpMm]", "");
        el.sendKeys(digits);
        js.executeScript(
                "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));", el
        );
    }

    /**
     * After a date-picker calendar is open, click the day cell matching {@code day}.
     */
    private void clickCalendarDay(String day) {
        By dayLocator = By.xpath(
                String.format(dayInCalendar, day, day)
        );
        click(dayLocator);
    }

    private void clickFirstVisible(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        List<WebElement> elements = driver.findElements(locator);
        for (WebElement el : elements) {
            if (!el.isDisplayed()) continue;
            scrollToCenter(el);
            try {
                el.click();
            } catch (ElementClickInterceptedException e) {
                js.executeScript("arguments[0].click();", el);
            }
            return;
        }
        throw new TimeoutException("No visible element found for: " + locator);
    }
    private boolean tryClickNext() {
        // Strategy 1: Normal click with explicit wait
        try {
            WebElement btn = wait.until(
                    ExpectedConditions.elementToBeClickable(nextButton)
            );
            scrollToCenter(btn);
            btn.click();
            return true;
        } catch (Exception e1) {
            System.out.println("Strategy 1 failed: " + e1.getMessage());
        }

        // Strategy 2: JavaScript click
        try {
            WebElement btn = driver.findElement(nextButton);
            scrollToCenter(btn);
            js.executeScript("arguments[0].click();", btn);
            return true;
        } catch (Exception e2) {
            System.out.println("Strategy 2 failed: " + e2.getMessage());
        }

        // Strategy 3: Fallback locator + JS click
        try {
            WebElement btn = driver.findElement(nextButtonFallback);
            js.executeScript(
                    "arguments[0].scrollIntoView({block:'center'});" +
                            "arguments[0].click();", btn
            );
            return true;
        } catch (Exception e3) {
            System.out.println("Strategy 3 failed: " + e3.getMessage());
        }

        return false;
    }

}
