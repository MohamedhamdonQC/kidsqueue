package tests;

import base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.*;
import utils.TestDataGenerator;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.openqa.selenium.JavascriptExecutor;
@Listeners(utils.reports.Listeners.class)
public class DashboardTest extends BaseTest {
    private static final String BASE_URL   = "https://staging.kidsqueue.softigital.com";
    private static final String FIRST_NAME = "Mohamed";
    private static final String LAST_NAME  = "Hamdon";
    private static final String PASSWORD   = "12345678";

    private static final String SCHOOLS = "Bardstown Child Care Program" ;

    private static final List<String> RANDOM_MESSAGES = Arrays.asList(
            "Hello! I would like to learn more about your childcare programs.",
            "Hi, could you please provide more details about enrollment?",
            "I'm interested in scheduling a visit to your center.",
            "Can you tell me about your availability for the upcoming semester?",
            "I'd love to know more about the curriculum you offer.",
            "What are your operating hours and fees?",
            "Do you have any openings for toddlers aged 2-3 years?",
            "I'm looking for a safe and nurturing environment for my child."
    );

    private WebDriverWait wait;
    private JavascriptExecutor js;

    private void initWaits() {
        if (wait == null) {
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        }
        if (js == null) {
            js = (JavascriptExecutor) driver;
        }
    }

    private String getRandomMessage() {
        return RANDOM_MESSAGES.get(new Random().nextInt(RANDOM_MESSAGES.size()));
    }

    private String getRequestedDate() {
        return LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_DATE);
    }

    private String getRequestedTime() {
        return LocalDateTime.now().plusHours(2)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void scrollAndClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", el);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        wait.until(ExpectedConditions.elementToBeClickable(locator));
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    private WebElement scrollTo(By locator) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", el);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        return el;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Test Case 1 – Register → Roles → Add to Watchlist → Verify in Dashboard
    // ════════════════════════════════════════════════════════════════════════
    @Test(description = "Register a new user, assign roles, then add a childcare center to watchlist and verify it")
    public void registerAndAssignRoles() throws InterruptedException {

        initWaits();
        hideCookieBannerArtifacts();
        // ── Step 1: Register ───────────────────────────────────────────────────
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");
        String email = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));

        // ── Step 2: Role Selection ─────────────────────────────────────────────
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();

        // ── Step 3: Home → Parent Portal ──────────────────────────────────────
        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        registerPage.openParentPortal();
        registerPage.clickParentNext();
        registerPage.completeChildRegistration("Hamdon", "Test Automation");

        // ── Step 4: Search ─────────────────────────────────────────────────────
        Thread.sleep(3000);
        WebElement searchBox = scrollTo(By.id("search"));
        searchBox.sendKeys(SCHOOLS);
        Thread.sleep(3000);

        // ── Step 5: View Details ───────────────────────────────────────────────
        scrollAndClick(By.xpath("//button[.//div[text()='View Details']]"));

        // ── Step 6: Add to Watchlist ───────────────────────────────────────────
        ChildcareCenterPage childcareCenterPage = new ChildcareCenterPage(driver);
        Thread.sleep(3000);
        childcareCenterPage.waitUntilLoaded();
        childcareCenterPage.addToWatchlist();

        // ── Step 7: Dashboard → Watching Tab ──────────────────────────────────
        Thread.sleep(3000);
        driver.navigate().to(BASE_URL + "/parent/dashboard");
        Thread.sleep(3000);
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.urlToBe(BASE_URL + "/parent/dashboard"));

        dashboardPage dashboardPage = new dashboardPage(driver);
        dashboardPage.clickWatching();
        Thread.sleep(3000);
        // ── Step 8: Navigate to Admin Dashboard ───────────────────────────────
        driver.navigate().to("https://staging.dashboard.kidsqueue.softigital.com/login");
        Thread.sleep(3000);

        driver.findElement(By.xpath("//input[@placeholder='Enter your email']"))
                .sendKeys("enas.gamall.salem@gmail.com");
        Thread.sleep(2000);

        driver.findElement(By.xpath("//input[@placeholder='Enter your password']"))
                .sendKeys("12345678");
        Thread.sleep(1200);

        driver.findElement(By.xpath("//button[@type='submit']")).click();
        Thread.sleep(2000);

// OTP
        driver.findElement(By.xpath("//input[@class='disabled:cursor-not-allowed']"))
                .sendKeys("793421");
        Thread.sleep(1200);

        driver.findElement(By.xpath("//button[@type='submit']")).click();
        Thread.sleep(2000);

// ── Step 9: Enrollment → Waitlist Tab ─────────────────────────────────
        driver.findElement(By.xpath("//button[normalize-space()='Enrollment']")).click();
        Thread.sleep(2000);

        driver.findElement(By.linkText("Waitlist")).click();
        Thread.sleep(2000);

// ── Step 10: Click "Add to Waitlist" Button ────────────────────────────
        WebElement addToWaitlistBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Add to Waitlist']")));
        addToWaitlistBtn.click();
        Thread.sleep(2000);
        // ── Step 11: Select Parent by Email ───────────────────────────────────
        WebElement parentDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'w-full') and contains(@class,'justify-between') and contains(@class,'font-normal')]")));
        parentDropdown.click();
        Thread.sleep(1500);

        WebElement parentSearchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[contains(@id,'radix')]")));
        parentSearchInput.clear();
        parentSearchInput.sendKeys(email);
        Thread.sleep(2000);

        WebElement parentOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@role='option'][1]")));
        parentOption.click();
        Thread.sleep(1500);
// ── Step 12: Select Child (Random – first available option) ───────────
        WebElement childDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//button[contains(@class,'w-full') and contains(@class,'border')])[2]")));
        childDropdown.click();
        Thread.sleep(1500);

        List<WebElement> childOptions = driver.findElements(
                By.xpath("//div[@role='option'] | //div[contains(@class,'cursor-pointer') and not(contains(.,'@'))]"));
        if (!childOptions.isEmpty()) {
            childOptions.get(0).click();
        }
        Thread.sleep(1500);

// ── Step 13: Select Room / Program (Random – first available option) ──
        List<WebElement> allDropdowns = driver.findElements(
                By.xpath("//button[contains(@class,'w-full') and contains(@class,'border') and contains(@class,'justify-between')]"));

// Room dropdown (index 2 if exists)
        if (allDropdowns.size() >= 3) {
            allDropdowns.get(2).click();
            Thread.sleep(1500);
            List<WebElement> roomOptions = driver.findElements(By.xpath("//div[@role='option']"));
            if (!roomOptions.isEmpty()) {
                int randomIndex = new Random().nextInt(roomOptions.size());
                roomOptions.get(randomIndex).click();
            }
            Thread.sleep(1500);
        }

// Refresh dropdown references after interactions
        allDropdowns = driver.findElements(
                By.xpath("//button[contains(@class,'w-full') and contains(@class,'border') and contains(@class,'justify-between')]"));

// Program / Age Group dropdown (index 3 if exists)
        if (allDropdowns.size() >= 4) {
            allDropdowns.get(3).click();
            Thread.sleep(1500);
            List<WebElement> programOptions = driver.findElements(By.xpath("//div[@role='option']"));
            if (!programOptions.isEmpty()) {
                int randomIndex = new Random().nextInt(programOptions.size());
                programOptions.get(randomIndex).click();
            }
            Thread.sleep(1500);
        }
// ── Step 14: Select Start Date (Random future date) ───────────────────
        List<WebElement> dateInputs = driver.findElements(By.xpath("//input[@type='date']"));
        if (!dateInputs.isEmpty()) {
            // Pick a random date within the next 30 days
            LocalDate randomDate = LocalDate.now().plusDays(new Random().nextInt(30) + 1);
            String formattedDate = randomDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            dateInputs.get(0).clear();
            dateInputs.get(0).sendKeys(formattedDate);
            Thread.sleep(1000);
        }
        Thread.sleep(3000);
        driver.findElement(By.id("high-trigger")).click();
        Thread.sleep(3000);
// ── Step 16: Submit ────────────────────────────────────────────────────
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement button = driver.findElement(By.id("waitlist-submit-trigger"));
        while (!button.isDisplayed()) {
            js.executeScript("window.scrollBy(0, 300);");
            Thread.sleep(500);
            button = driver.findElement(By.id("waitlist-submit-trigger"));
        }
        button.click();
        Thread.sleep(3000);
        driver.navigate().to("https://staging.kidsqueue.softigital.com/parent/dashboard");
        Thread.sleep(3000);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement tab = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("dashboard-tab-waitlist")
        ));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", tab
        );
        Thread.sleep(3000);
        wait.until(ExpectedConditions.elementToBeClickable(tab)).click();
        JavascriptExecutor js1 = (JavascriptExecutor) driver;
        WebElement card = null;

        while (true) {
            try {
                card = driver.findElement(By.xpath("//div[@data-slot='card-title' and text()='Bardstown Child Care Program']"));

                if (card.isDisplayed()) {
                    break;
                }
            } catch (Exception e) {
            }

            js1.executeScript("window.scrollBy(0, 500);");
            Thread.sleep(500);
        }
        Thread.sleep(3000);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", card);// ── Step 17: Verify success toast/message ─────────────────────────────
        try {
            WebElement successMsg = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(text(),'success') or contains(text(),'Success') " +
                                    "or contains(text(),'added') or contains(text(),'Added')]")));
            System.out.println("Waitlist entry added successfully: " + successMsg.getText());
        } catch (Exception e) {
            System.out.println("Could not find success message, please verify manually.");
        }
    }

    // Test Case 2 – Register → Roles → Send Message → Verify in Dashboard
    @Test(description = "Register a new user, send a message to a childcare center, " +
            "then verify it appears in the Messages dashboard")
    public void registerAndSendMessage() throws InterruptedException {

        initWaits();
        hideCookieBannerArtifacts();

        // Random content so each run produces a unique, traceable message
        String messageSubject = "Automated Subject – " + System.currentTimeMillis();
        String messageText    = getRandomMessage();

        // ── Step 1: Register ──────────────────────────────────────────────────
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        String email = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));

        // ── Step 2: Role Selection ────────────────────────────────────────────
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();

        // ── Step 3: Home → Parent Portal → Child Registration ─────────────────
        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));

        registerPage.openParentPortal();
        registerPage.clickParentNext();
        registerPage.completeChildRegistration("Hamdon", "Test Automation");

        // ── Step 4: Search for a center ───────────────────────────────────────
        Thread.sleep(3000);
        WebElement searchBox = waitAndScrollTo(By.id("search"));
        searchBox.sendKeys(SCHOOLS);
        Thread.sleep(3000);
        // Wait for search results to appear (no raw sleep)
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[.//div[text()='View Details']]")));

        // ── Step 5: Open center details ───────────────────────────────────────
        scrollAndClick(By.xpath("//button[.//div[text()='View Details']]"));

        // ── Step 6: Send message (subject + body) ─────────────────────────────
        ChildcareCenterPage centerPage = new ChildcareCenterPage(driver);
        Thread.sleep(2000);
        centerPage.sendMessage(messageSubject, messageText);

        // Soft verification: success toast should appear before we navigate away
        boolean sentConfirmed = centerPage.isMessageSentSuccessfully();
        System.out.println(">>> Message sent. Success toast visible: " + sentConfirmed);

        // ── Step 7: Navigate to Messages Dashboard ────────────────────────────
        driver.navigate().to(BASE_URL + "/parent/dashboard?messages_page=1");
        wait.until(ExpectedConditions.urlContains("/parent/dashboard"));

        // ── Step 8: Ensure the messages view is ready ─────────────────────────
        Thread.sleep(2000);
        dashboardPage dashboardPage = new dashboardPage(driver);
        dashboardPage.scrollToMessagesBottom();
        Thread.sleep(2000);
        // ── Step 9: Assert sent message is visible ────────────────────────────
        Assert.assertTrue(
                dashboardPage.isMessageVisible(messageText),
                "Sent message '" + messageText + "' was NOT found in the Messages dashboard!"
        );
    }

    // ── Helpers (could live in BaseTest) ─────────────────────────────────────
    private WebElement waitAndScrollTo(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        return element;
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════
    // Test Case 3 – Register → View Details → Book Tour → Verify in Dashboard Tours Tab
    // ═══════════════════════════════════════════════════════════════════════════════════════
    @Test(description = "Register a parent, open a childcare center, book a tour, then verify the request in the Tours dashboard tab")
    public void registerAndBookTour() throws InterruptedException {

        initWaits();
        hideCookieBannerArtifacts();

        String requestedDate = getRequestedDate();
        String requestedTime = getRequestedTime();

        // ── Register ──────────────────────────────────────────────────────────
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        String email = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));

        // ── Role selection ────────────────────────────────────────────────────
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();

        // ── Complete child registration ───────────────────────────────────────
        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        registerPage.openParentPortal();
        registerPage.clickParentNext();
        registerPage.completeChildRegistration("Hamdon", "Test Automation");

        // ── Search for a school and open its details ──────────────────────────
        Thread.sleep(3000);
        WebElement searchBox = scrollTo(By.id("search"));
        searchBox.sendKeys(SCHOOLS);
        Thread.sleep(3000);
        scrollAndClick(By.xpath("//button[.//div[text()='View Details']]"));

        // ── Book a tour ───────────────────────────────────────────────────────
        TourRequestPage tourRequestPage = new TourRequestPage(driver);

        tourRequestPage.openBookTourForm();
        tourRequestPage.selectChildren();
        tourRequestPage.setRequestedDate(requestedDate);
        tourRequestPage.setRequestedTime(requestedTime);
        tourRequestPage.submitRequest();

        // ── Verify in the Tours dashboard tab ─────────────────────────────────
        driver.navigate().to(BASE_URL + "/parent/dashboard?messages_page=1");
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.urlContains("/parent/dashboard"));

        tourRequestPage.openToursTab();

        Assert.assertTrue(
                tourRequestPage.isTextVisible(requestedDate),
                "Requested date '" + requestedDate + "' was not found in the Tours tab."
        );
        Assert.assertTrue(
                tourRequestPage.isTextVisible(requestedTime),
                "Requested time '" + requestedTime + "' was not found in the Tours tab."
        );
        Assert.assertTrue(
                tourRequestPage.isTextVisible(SCHOOLS),
                "School '" + SCHOOLS + "' was not found in the Tours tab."
        );
    }


    @Test(description = "End-to-End: Register user, assign roles, create group and event, then verify event on dashboard")    public void registerAndEventRoles() throws InterruptedException {
        initWaits();
        hideCookieBannerArtifacts();
        // ── Step 1: Register ───────────────────────────────────────────────────
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");
        String email = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));
        // ── Step 2: Role Selection ─────────────────────────────────────────────
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();

       //------Step3 : ---//
        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        registerPage.openParentPortal();
        registerPage.clickParentNext();
        registerPage.completeChildRegistration("Hamdon", "Test Automation");
      //--------------Step 4 --- //
        String imagePath = Paths.get(
                System.getProperty("user.dir"),
                "src", "test", "java", "tests", "resources", "images",
                "Screenshot 2026-04-09 170838.png"
        ).toString();
        GroupPage groupPage = new GroupPage(driver);
        driver.navigate().to("https://staging.kidsqueue.softigital.com/parent/groups?page=1");
        groupPage.CreateGroup();
        groupPage.nameofgroup("Test Automation Group");
        groupPage.Description("Description of the Group page Automation");
        groupPage.MemberShip();
        groupPage.Anyone();
        groupPage.Image(imagePath);
        groupPage.ClicktoCreateGroup();
        Thread.sleep(3000);
        // ── 2. Navigate to Groups page ────────────────────────────────────────
        EventPage eventPage = new EventPage(driver);
        driver.navigate().to("https://staging.kidsqueue.softigital.com/parent/groups?page=1");
        // ── 3. Switch to Events tab ───────────────────────────────────────────
        eventPage.clickEventsTab();
        Thread.sleep(3000);
        // ── 4. Open Create Event dialog ───────────────────────────────────────
        eventPage.clickCreateEvent();
        // ── 5. Build test data ────────────────────────────────────────────────
        long ts = System.currentTimeMillis();
        String eventName        = "Automation Event "       + ts;
        String eventDescription = "Automation description " + ts;

        // Calendar day strings (day-of-month only, as shown in the picker)

        ZoneId cairo = ZoneId.of("Africa/Cairo");
        String startDay = String.valueOf(LocalDate.now(cairo).plusDays(1).getDayOfMonth());
        String endDay   = String.valueOf(LocalDate.now(cairo).plusDays(2).getDayOfMonth());

        // Time in "HHmm" – typeTime() strips non-digits before sending
        String startTime = LocalTime.of(20, 37).format(DateTimeFormatter.ofPattern("hhmma"));
        String endTime   = LocalTime.of(17, 41).format(DateTimeFormatter.ofPattern("hhmma"));

        String location = "Cairo";

        // ── 6. Fill the form ──────────────────────────────────────────────────
        eventPage.fillEventName(eventName);
        eventPage.fillEventDescription(eventDescription);

        // Group/Audience: click combobox → pick first result
        eventPage.chooseFirstAudienceResult();

        // Visibility: click dropdown → pick "Public"
        eventPage.choosePublicVisibility();

        // Dates: open calendar picker and click the correct day cell
        Thread.sleep(3000);
        eventPage.selectStartDate(startDay);
        eventPage.selectEndDate(endDay);

        // Times: native <input type="time">
        eventPage.fillStartTime(startTime);
        eventPage.fillEndTime(endTime);

        // Location
        eventPage.fillLocation(location);

        // ── 7. Proceed to next step ───────────────────────────────────────────
        Thread.sleep(2000); // Give form time to validate

        // Scroll to bottom of modal first
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelector('[role=\"dialog\"]')?.scrollTo(0, 9999);"
        );
        eventPage.clickNext();
        eventPage.clickInvitesbuttom();

        eventPage.fillChecklistNote(0, "Verify all passports are valid for at least 6 months");
        eventPage.fillChecklistNote(1, "Verify all passports are valid for at least 6 months");
        eventPage.fillChecklistNote(2, "Verify all passports are valid for at least 6 months");
        eventPage.fillChecklistNote(3, "Verify all passports are valid for at least 6 months");
        eventPage.fillChecklistNote(4, "Verify all passports are valid for at least 6 months");
        eventPage.fillChecklistNote(5, "Verify all passports are valid for at least 6 months");
        eventPage.clickSubmit();
        Thread.sleep(3000);
        driver.navigate().to("https://staging.kidsqueue.softigital.com/parent/dashboard");
        Thread.sleep(3000);
        WebElement eventsTab = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("dashboard-tab-events"))
        );
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", eventsTab
        );

        Thread.sleep(500);

        try {
            eventsTab.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", eventsTab);
        }

        // ── Step: Scroll to the created event and verify its name ─────────────────
// Wait for the event card to appear by its name
        WebElement eventCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h3[contains(@class,'font-semibold') and normalize-space(text())='" + eventName + "']")
        ));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                eventCard
        );
        Thread.sleep(1000);

        Assert.assertTrue(
                eventCard.isDisplayed(),
                "Event name '" + eventName + "' should be visible on the dashboard"
        );
        Assert.assertEquals(
                eventCard.getText().trim(),
                eventName,
                "Event name on dashboard does not match the created event!"
        );
        System.out.println("✅ Event verified on dashboard: " + eventCard.getText());
    }
}
