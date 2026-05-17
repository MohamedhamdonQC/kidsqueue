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

    
    
    

    
    private final By eventsTab = By.id("groups-tab-events");

    
    
    
    
    private final By createEventButton = By.id("create-event-open-trigger") ;

    
    
    
    
    
    

    
    private final By eventNameInput = By.xpath(
            "//input[@placeholder='Enter event name']"
    );

    
    private final By eventDescriptionInput = By.xpath(
            "//*[self::input or self::textarea][@placeholder='Enter event description']"
    );
    private final By ClicknextCal = By.xpath(
            "//button[@aria-label='Go to the Next Month']"
    );

    
    
    private final By audienceComboboxTrigger = By.xpath(
            "//button[@role='combobox' and contains(normalize-space(.),'Choose group/audience')]"
    );

    
    private final By audienceFirstOption = By.xpath(
            "(//*[@role='option' or @role='listitem' or contains(@class,'command-item')])[1]"
    );

    
    
    private final By visibilityTrigger = By.xpath(
            "//button[@role='combobox' and contains(normalize-space(.),'Choose visibility')]"
    );

    
    private final By visibilityPublicOption = By.xpath(
            "//*[@role='option' and normalize-space(.)='Public']"
    );

    
    
    private final By startDateTrigger = By.xpath(
            "(//button[contains(normalize-space(.),'Pick a date')])[1]"
    );

    private final By endDateTrigger = By.xpath(
            "//button[@id='end_date-trigger']"
    );

    
    private final String dayInCalendar =
            "//td[@role='gridcell' and not(@aria-disabled='true') and normalize-space(.)='%s']" +
                    "| //button[@role='gridcell' and not(@disabled) and normalize-space(.)='%s']";

    
    
    private final By startTimeInput = By.xpath(
            "(//input[@type='time'])[1]"
    );

    private final By endTimeInput = By.xpath(
            "(//input[@type='time'])[2]"
    );

    
    private final By locationInput = By.xpath(
            "//input[@placeholder='Enter location']"
    );

    

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
    
    
    

    public EventPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, WAIT_TIMEOUT);
        this.js     = (JavascriptExecutor) driver;
    }
    public void openGroupsPage() {
        driver.get("https://kidsqueue.softigital.com/parent/groups?page=1");
    }

    
    public void clickEventsTab() {
        click(eventsTab);
    }
    public void ClicknextCal() {
        click(ClicknextCal);
    }

    
    public void clickCreateEvent() {
        click(createEventButton);
    }

    
    public void fillEventName(String eventName) {
        type(eventNameInput, eventName);
    }

    
    public void fillEventDescription(String description) {
        type(eventDescriptionInput, description);
    }

    
    public void chooseFirstAudienceResult() {
        click(audienceComboboxTrigger);
        clickFirstVisible(audienceFirstOption);
    }

    
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
    
    public void chooseVisibility(String visibilityLabel) {
        click(visibilityTrigger);
        By option = By.xpath(
                String.format("//*[@role='option' and normalize-space(.)='%s']", visibilityLabel)
        );
        click(option);
    }

    
    public void selectStartDate(String day) {
        click(startDateTrigger);
        ClicknextCal();
        clickCalendarDay(day);
    }

    
    public void selectEndDate(String day) {
        click(endDateTrigger);
        ClicknextCal();
        clickCalendarDay(day);
    }

    
    public void fillStartTime(String startTime) {
        typeTime(startTimeInput, startTime);
    }

    
    public void fillEndTime(String endTime) {
        typeTime(endTimeInput, endTime);
    }

    
    public void fillLocation(String location) {
        type(locationInput, location);
    }

    
    public void clickNext() {
        try {
            
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        
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
        
        return elements.get(elements.size() - 1);
    }

    private void scrollToCenter(WebElement el) {
        js.executeScript("arguments[0].scrollIntoView({block:'center',inline:'nearest'});", el);
    }

    
    private void type(By locator, String value) {
        WebElement el = getVisibleElement(locator);
        scrollToCenter(el);
        el.clear();
        el.sendKeys(value);

        
        js.executeScript(
                "arguments[0].dispatchEvent(new Event('input',  {bubbles:true}));" +
                        "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                el
        );
    }

    
    private void typeTime(By locator, String timeValue) {
        WebElement el = getVisibleElement(locator);
        scrollToCenter(el);
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        
        String digits = timeValue.replaceAll("[^0-9AaPpMm]", "");
        el.sendKeys(digits);
        js.executeScript(
                "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));", el
        );
    }

    
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

        
        try {
            WebElement btn = driver.findElement(nextButton);
            scrollToCenter(btn);
            js.executeScript("arguments[0].click();", btn);
            return true;
        } catch (Exception e2) {
            System.out.println("Strategy 2 failed: " + e2.getMessage());
        }

        
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
