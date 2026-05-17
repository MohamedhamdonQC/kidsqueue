package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class TourRequestPage {

    private final WebDriver        driver;
    private final WebDriverWait    wait;
    private final JavascriptExecutor js;
    private final Actions          actions;

    // ── Stable locators ───────────────────────────────────────────────────────
    private final By bookTourButton       = By.xpath("//button[contains(.,'Book tour') or contains(.,'Book Tour')]");
    private final By submitRequestButton  = By.xpath("//button[contains(.,'Submit Request') or contains(.,'Submit request')]");
    private final By toursTab             = By.xpath("//*[self::button or self::a][normalize-space()='Tours' or contains(.,'Tours')]");
    private final By childrenTrigger      = By.id("children-trigger");
    private final By requestedDateTrigger = By.id("requested_date-trigger");
    private final By requestedTimeTrigger = By.id("requested_time-trigger");

    // Calendar navigation / day-cell selectors (react-day-picker v8 / shadcn)
    private final By calendarGrid = By.cssSelector("[role='grid']");

    private final By calendarCaption = By.cssSelector(
            ".rdp-caption_label, [class*='caption_label'], " +
                    "[data-slot='calendar'] [role='heading'], [aria-live='polite']");

    private final By nextMonthBtn = By.cssSelector(
            "button[name='next-month'], button[aria-label*='next month' i], " +
                    "button[aria-label*='Go to next' i], .rdp-nav_button_next, " +
                    "[data-slot='calendar-next-month']");

    private final By prevMonthBtn = By.cssSelector(
            "button[name='previous-month'], button[aria-label*='previous month' i], " +
                    "button[aria-label*='Go to previous' i], .rdp-nav_button_previous, " +
                    "[data-slot='calendar-previous-month']");

    // Time / children option selectors (Radix select)
    private final By selectOption = By.cssSelector(
            "[role='option']:not([aria-disabled='true']):not([data-disabled]), " +
                    "[data-radix-select-item]:not([data-disabled])");

    // ─────────────────────────────────────────────────────────────────────────

    public TourRequestPage(WebDriver driver) {
        this.driver  = driver;
        this.wait    = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.js      = (JavascriptExecutor) driver;
        this.actions = new Actions(driver);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void openBookTourForm() {
        forceClick(bookTourButton);
        sleep(1200);
        System.out.println("[openBookTourForm] form opened.");
    }

    public void selectChildren() {
        forceClick(childrenTrigger);
        sleep(1000);

        // Try multiple option selectors in order of specificity
        String[] optionXpaths = {
                "//*[@role='option'][not(contains(@aria-disabled,'true'))][1]",
                "//*[@role='listbox']//*[normalize-space(.)!=''][1]",
                "(//*[@data-radix-select-item])[1]",
                "(//*[@data-value])[1]",
                "//*[contains(@class,'option') and not(contains(@class,'disabled'))][normalize-space(.)!=''][1]"
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
        System.err.println("[selectChildren] WARNING: no child option found!");
    }

    /**
     * Select a date in the shadcn/react-day-picker calendar popup.
     *
     * @param dateValue ISO-8601 date string, e.g. "2026-05-16"
     */
    public void setRequestedDate(String dateValue) {
        LocalDate target = LocalDate.parse(dateValue);

        // Step 1 – open the calendar popup
        forceClick(requestedDateTrigger);
        sleep(800);

        // Step 2 – wait for the calendar grid to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(calendarGrid));

        // Step 3 – navigate to the correct month (best-effort; caption selector
        //           may not match every theme variant, so errors are non-fatal)
        navigateCalendarToMonth(YearMonth.from(target));

        // Step 4 – click the target day cell
        clickCalendarDay(target);

        // Step 5 – wait for the calendar to close on its own (auto-close on selection)
        //           then force-dismiss if it stays open, so the form re-renders
        //           the time field.
        boolean calendarClosed = false;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(4))
                    .until(ExpectedConditions.invisibilityOfElementLocated(calendarGrid));
            calendarClosed = true;
            System.out.println("[setRequestedDate] calendar closed automatically.");
        } catch (Exception ignored) {
            System.out.println("[setRequestedDate] calendar still open – forcing close.");
        }

        if (!calendarClosed) {
            // Click the date trigger again to toggle the calendar closed
            try {
                WebElement trigger = driver.findElement(requestedDateTrigger);
                js.executeScript("arguments[0].click();", trigger);
                sleep(400);
            } catch (Exception ignored) {}

            // Press Escape as a second attempt
            try { actions.sendKeys(Keys.ESCAPE).perform(); } catch (Exception ignored) {}
            sleep(300);

            // Click an element outside the calendar as a last resort
            try {
                js.executeScript(
                        "var el = document.querySelector('[data-slot=\"dialog-content\"], " +
                                "[role=\"dialog\"], form, body');" +
                                "if(el) el.dispatchEvent(new MouseEvent('mousedown',{bubbles:true}));");
            } catch (Exception ignored) {}
            sleep(400);
        }

        System.out.println("[setRequestedDate] date set → " + dateValue);
    }

    /**
     * Select a time from the Radix select dropdown.
     *
     * The time trigger goes through three states:
     *   (A) absent from DOM  – calendar is still open after day click
     *   (B) present+disabled – date not yet committed to form state
     *   (C) present+enabled  – ready to interact
     *
     * We must survive all three before clicking.
     *
     * @param timeValue display text to match, e.g. "10:00 AM"
     */
    public void setRequestedTime(String timeValue) {

        // ── Phase A: wait for the element to APPEAR in the DOM ────────────────
        System.out.println("[setRequestedTime] waiting for time trigger to appear in DOM...");
        wait.until(d -> {
            try {
                d.findElement(requestedTimeTrigger);
                return true;               // element is in DOM
            } catch (NoSuchElementException e) {
                return false;              // not yet rendered – keep polling
            }
        });

        // ── Phase B: wait for it to become ENABLED ────────────────────────────
        System.out.println("[setRequestedTime] waiting for time trigger to be enabled...");
        wait.until(d -> {
            try {
                WebElement el   = d.findElement(requestedTimeTrigger);
                boolean dis     = el.getAttribute("disabled")      != null;
                boolean ariaOff = "true".equals(el.getAttribute("aria-disabled"));
                boolean dataOff = "true".equals(el.getAttribute("data-disabled"));
                return !dis && !ariaOff && !dataOff;
            } catch (NoSuchElementException e) {
                return false;              // disappeared between polls – keep waiting
            }
        });

        // ── Phase C: open dropdown and pick a time ────────────────────────────
        forceClick(requestedTimeTrigger);
        sleep(800);

        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(selectOption, 0));
        List<WebElement> options = driver.findElements(selectOption);

        // 1st preference – exact case-insensitive match
        WebElement pick = options.stream()
                .filter(o -> o.getText().trim().equalsIgnoreCase(timeValue))
                .findFirst().orElse(null);

        // 2nd preference – same hour token ("10" from "10:00 AM")
        if (pick == null) {
            String hourToken = timeValue.split(":")[0].trim();
            pick = options.stream()
                    .filter(o -> o.getText().trim().startsWith(hourToken))
                    .findFirst().orElse(null);
        }

        // 3rd preference – first available option
        if (pick == null && !options.isEmpty()) {
            pick = options.get(0);
            System.out.println("[setRequestedTime] exact match not found; picking first available option.");
        }

        if (pick == null) {
            System.err.println("[setRequestedTime] ERROR: dropdown is empty.");
            return;
        }

        js.executeScript("arguments[0].scrollIntoView({block:'center'});", pick);
        sleep(200);
        try { pick.click(); }
        catch (Exception e) { js.executeScript("arguments[0].click();", pick); }

        System.out.println("[setRequestedTime] time selected → " + pick.getText().trim());
        sleep(500);
    }

    public void submitRequest() {
        sleep(800);
        dismissOpenPicker();

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(submitRequestButton));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        sleep(500);

        // Hide any overlapping banners/toasts
        js.executeScript(
                "document.querySelectorAll('[class*=cookie],[class*=banner],[class*=toast]')" +
                        ".forEach(el => el.style.display='none');");
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
        System.out.println("[openToursTab] Tours tab opened.");
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

    // ── Calendar helpers ──────────────────────────────────────────────────────

    /**
     * Navigate the open calendar popup to the target YearMonth.
     * Guards against infinite loops with a 24-month cap.
     */
    private void navigateCalendarToMonth(YearMonth target) {
        for (int i = 0; i < 24; i++) {
            try {
                WebElement caption = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(calendarCaption));
                YearMonth current = parseCalendarCaption(caption.getText().trim());

                if (current == null || current.equals(target)) return;

                if (current.isBefore(target)) {
                    driver.findElement(nextMonthBtn).click();
                } else {
                    driver.findElement(prevMonthBtn).click();
                }
                sleep(450);
            } catch (Exception e) {
                System.err.println("[navigateCalendarToMonth] error on attempt " + i + ": " + e.getMessage());
                return;
            }
        }
    }

    /**
     * Parse the calendar header text into a YearMonth.
     * Handles "May 2026", "May 2026 – June 2026", "May 2026 - June 2026".
     */
    private YearMonth parseCalendarCaption(String text) {
        String part = text.split("[–\\-]")[0].trim();   // take first month if range shown
        try {
            return YearMonth.parse(part,
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
        } catch (DateTimeParseException e1) {
            try {
                return YearMonth.parse(part,
                        DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));
            } catch (DateTimeParseException e2) {
                System.err.println("[parseCalendarCaption] could not parse: '" + text + "'");
                return null;
            }
        }
    }

    /**
     * Click the day button in the open calendar that matches the target LocalDate.
     *
     * Strategy 1 – exact aria-label  ("Friday, May 16, 2026")
     * Strategy 2 – day number inside the visible grid (fallback)
     */
    private void clickCalendarDay(LocalDate target) {
        // react-day-picker v8 uses aria-label "EEEE, MMMM d, yyyy"
        String ariaLabel = target.format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH));

        // Strategy 1 — aria-label
        By ariaBy = By.cssSelector(
                "button[aria-label='" + ariaLabel + "']:not([disabled]):not([aria-disabled='true'])");
        try {
            WebElement day = wait.until(ExpectedConditions.elementToBeClickable(ariaBy));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", day);
            day.click();
            System.out.println("[clickCalendarDay] clicked via aria-label: " + ariaLabel);
            return;
        } catch (Exception e) {
            System.out.println("[clickCalendarDay] aria-label strategy failed, trying day-number fallback...");
        }

        // Strategy 2 — day number inside the grid
        String dayNum = String.valueOf(target.getDayOfMonth());
        By numBy = By.xpath(
                "//td[@role='gridcell']//button[not(@disabled)][normalize-space(.)='" + dayNum + "'] | " +
                        "//*[@role='gridcell']/button[not(@disabled)][normalize-space(.)='" + dayNum + "']");
        try {
            List<WebElement> cells = driver.findElements(numBy);
            if (!cells.isEmpty()) {
                WebElement day = cells.get(0);
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", day);
                day.click();
                System.out.println("[clickCalendarDay] clicked via day-number fallback: " + dayNum);
            } else {
                System.err.println("[clickCalendarDay] ERROR: day '" + dayNum + "' not found in calendar.");
            }
        } catch (Exception e) {
            System.err.println("[clickCalendarDay] fallback also failed: " + e.getMessage());
        }
    }

    // ── Generic helpers ───────────────────────────────────────────────────────

    private void dismissOpenPicker() {
        try { actions.sendKeys(Keys.ESCAPE).perform(); } catch (Exception ignored) {}
        try { js.executeScript("document.body.click();"); }  catch (Exception ignored) {}
        sleep(600);
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