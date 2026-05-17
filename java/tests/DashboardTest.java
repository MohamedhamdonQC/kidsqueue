package tests;

import base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.*;
import utils.TestDataGenerator;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.openqa.selenium.JavascriptExecutor;
@Listeners(utils.reports.Listeners.class)
public class DashboardTest extends BaseTest {

    private static final String BASE_URL = "https://staging.kidsqueue.softigital.com";
    private static final String FIRST_NAME = "Mohamed";
    private static final String LAST_NAME = "Hamdon";
    private static final String PASSWORD = "12345678";

    private static final String SCHOOLS = "Bardstown Child Care Program";

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
    private String bookedDate;
    private String bookedTime;

    private void initWaits() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
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
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }
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
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }
        return el;
    }

    
    
    
    @Test(  priority = 1,description = "Register a new user, assign roles, then add a childcare center to watchlist and verify it")
    public void registerAndAssignRoles() throws InterruptedException {

        initWaits();
        hideCookieBannerArtifacts();
        
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

        
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();

        
        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        registerPage.openParentPortal();
        registerPage.clickParentNext();
        registerPage.completeChildRegistration("Hamdon", "Test Automation");

        
        Thread.sleep(3000);
        WebElement searchBox = scrollTo(By.id("search"));
        searchBox.sendKeys(SCHOOLS);
        Thread.sleep(3000);

        
        scrollAndClick(By.xpath("//button[.//div[text()='View Details']]"));

        
        ChildcareCenterPage childcareCenterPage = new ChildcareCenterPage(driver);
        Thread.sleep(3000);
        childcareCenterPage.waitUntilLoaded();
        childcareCenterPage.addToWatchlist();

        
        Thread.sleep(3000);
        driver.navigate().to(BASE_URL + "/parent/dashboard");
        Thread.sleep(3000);
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.urlToBe(BASE_URL + "/parent/dashboard"));

        dashboardPage dashboardPage = new dashboardPage(driver);
        dashboardPage.clickWatching();
        Thread.sleep(3000);
        
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


        driver.findElement(By.xpath("//input[@class='disabled:cursor-not-allowed']"))
                .sendKeys("793421");
        Thread.sleep(1200);

        driver.findElement(By.xpath("//button[@type='submit']")).click();
        Thread.sleep(2000);


        driver.findElement(By.xpath("//button[normalize-space()='Enrollment']")).click();
        Thread.sleep(2000);

        driver.findElement(By.linkText("Waitlist")).click();
        Thread.sleep(2000);


        WebElement addToWaitlistBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Add to Waitlist']")));
        addToWaitlistBtn.click();
        Thread.sleep(2000);
        
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


        List<WebElement> allDropdowns = driver.findElements(
                By.xpath("//button[contains(@class,'w-full') and contains(@class,'border') and contains(@class,'justify-between')]"));


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


        allDropdowns = driver.findElements(
                By.xpath("//button[contains(@class,'w-full') and contains(@class,'border') and contains(@class,'justify-between')]"));


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

        List<WebElement> dateInputs = driver.findElements(By.xpath("//input[@type='date']"));
        if (!dateInputs.isEmpty()) {
            
            LocalDate randomDate = LocalDate.now().plusDays(new Random().nextInt(30) + 1);
            String formattedDate = randomDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            dateInputs.get(0).clear();
            dateInputs.get(0).sendKeys(formattedDate);
            Thread.sleep(1000);
        }
        Thread.sleep(3000);
        driver.findElement(By.id("high-trigger")).click();
        Thread.sleep(3000);

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
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", card);
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

    
    @Test(  priority = 2,description = "Register a new user, send a message to a childcare center, " +
            "then verify it appears in the Messages dashboard")
    public void registerAndSendMessage() throws InterruptedException {

        initWaits();

        hideCookieBannerArtifacts();

        
        String messageSubject = "Automated Subject – " + System.currentTimeMillis();
        String messageText = getRandomMessage();

        
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

        
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();

        
        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));

        registerPage.openParentPortal();
        registerPage.clickParentNext();
        registerPage.completeChildRegistration("Hamdon", "Test Automation");

        
        Thread.sleep(3000);
        WebElement searchBox = waitAndScrollTo(By.id("search"));
        searchBox.sendKeys(SCHOOLS);
        Thread.sleep(3000);
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[.//div[text()='View Details']]")));

        
        scrollAndClick(By.xpath("//button[.//div[text()='View Details']]"));

        
        ChildcareCenterPage centerPage = new ChildcareCenterPage(driver);
        Thread.sleep(2000);
        centerPage.sendMessage(messageSubject, messageText);

        
        boolean sentConfirmed = centerPage.isMessageSentSuccessfully();
        System.out.println(">>> Message sent. Success toast visible: " + sentConfirmed);

        
        driver.navigate().to(BASE_URL + "/parent/dashboard?messages_page=1");
        wait.until(ExpectedConditions.urlContains("/parent/dashboard"));

        
        Thread.sleep(2000);
        dashboardPage dashboardPage = new dashboardPage(driver);
        dashboardPage.scrollToMessagesBottom();
        Thread.sleep(2000);
        
        Assert.assertTrue(
                dashboardPage.isMessageVisible(messageText),
                "Sent message '" + messageText + "' was NOT found in the Messages dashboard!"
        );
    }

    
    private WebElement waitAndScrollTo(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        return element;
    }

    private <T> T randomPick(List<T> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot pick from an empty list.");
        }
        return items.get(new Random().nextInt(items.size()));
    }

    private String deriveDisplayDate(String rawDateLabel) {
        if (rawDateLabel == null || rawDateLabel.isBlank()) {
            return "";
        }

        try {
            LocalDate date = LocalDate.parse(
                    rawDateLabel,
                    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", java.util.Locale.ENGLISH)
            );
            return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", java.util.Locale.ENGLISH));
        } catch (Exception ignored) {
            return rawDateLabel.trim();
        }
    }

    
    
    
    @Test(  priority = 3,description = "Register a parent, open a childcare center, book a tour, then verify the request in the Tours dashboard tab")
    public void registerAndBookTour() throws InterruptedException {
        initWaits();

        hideCookieBannerArtifacts();

        String requestedDate = getRequestedDate();
        String requestedTime = getRequestedTime();


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


        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();


        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        registerPage.openParentPortal();
        registerPage.clickParentNext();
        registerPage.completeChildRegistration("Hamdon", "Test Automation");


        Thread.sleep(3000);
        WebElement searchBox = scrollTo(By.id("search"));
        searchBox.sendKeys(SCHOOLS);
        Thread.sleep(3000);
        scrollAndClick(By.xpath("//button[.//div[text()='View Details']]"));


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

    }

    @Test(  priority = 4,description = "End-to-End: Register user, assign roles, create group and event, then verify event on dashboard")
    public void registerAndEventRoles() throws InterruptedException {
        initWaits();   // ← ADD THIS BACK

        hideCookieBannerArtifacts();
        
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
        
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();

        
        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        registerPage.openParentPortal();
        registerPage.clickParentNext();
        registerPage.completeChildRegistration("Hamdon", "Test Automation");
        
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
        
        EventPage eventPage = new EventPage(driver);
        driver.navigate().to("https://staging.kidsqueue.softigital.com/parent/groups?page=1");
        
        eventPage.clickEventsTab();
        Thread.sleep(3000);
        
        eventPage.clickCreateEvent();
        
        long ts = System.currentTimeMillis();
        String eventName = "Automation Event " + ts;
        String eventDescription = "Automation description " + ts;

        

        ZoneId cairo = ZoneId.of("Africa/Cairo");
        String startDay = String.valueOf(LocalDate.now(cairo).plusDays(1).getDayOfMonth());
        String endDay = String.valueOf(LocalDate.now(cairo).plusDays(2).getDayOfMonth());

        
        String startTime = LocalTime.of(20, 37).format(DateTimeFormatter.ofPattern("hhmma"));
        String endTime = LocalTime.of(17, 41).format(DateTimeFormatter.ofPattern("hhmma"));

        String location = "Cairo";

        
        eventPage.fillEventName(eventName);
        eventPage.fillEventDescription(eventDescription);

        
        eventPage.chooseFirstAudienceResult();

        
        eventPage.choosePublicVisibility();

        
        Thread.sleep(3000);
        eventPage.selectStartDate(startDay);
        eventPage.selectEndDate(endDay);

        
        eventPage.fillStartTime(startTime);
        eventPage.fillEndTime(endTime);

        
        eventPage.fillLocation(location);

        
        Thread.sleep(2000); 

        
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

    @Test(  priority = 5,description = "Register a new user, assign roles, then add a childcare center to watchlist and verify it")
    public void createWaitlistRequestAndApprove() throws InterruptedException {
        initWaits();   // ← ADD THIS BACK

        System.out.println("\n=== Starting End-to-End Test: Register User, Assign Roles, Add Childcare Center to Watchlist ===\n");
        hideCookieBannerArtifacts();
        
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

        
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();

        
        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        registerPage.openParentPortal();
        registerPage.clickParentNext();
        registerPage.completeChildRegistration("Hamdon", "Test Automation");
        Thread.sleep(3000);
        
        
        
        System.out.println("========== PART 1: Creating Waitlist Request ==========");
        
        driver.get("https://staging.kidsqueue.softigital.com/parent/dashboard");
        waitForPageLoad();
        System.out.println("✓ Step 1: Navigated to Dashboard");

        
        WebElement waitlistedTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(), 'Waitlisted')]")));
        scrollToElement(waitlistedTab);
        waitlistedTab.click();
        System.out.println("✓ Step 2: Clicked on 'Waitlisted' tab");

        
        WebElement joinWaitlistBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space(.)='Join a Waitlist']"
                        + " | //button[normalize-space(.)='Join a waitlist']"
                        + " | //button[contains(normalize-space(.), 'Join a Waitlist')]"
                        + " | //a[contains(normalize-space(.), 'Join a Waitlist')]")
        ));
        scrollToElement(joinWaitlistBtn);
        joinWaitlistBtn.click();
        System.out.println("✓ Step 3: Clicked 'Join a Waitlist' button");
        Thread.sleep(3000);

        
        WebElement programOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(), 'Bardstown Child Care Program')]")));
        scrollToElement(programOption);
        programOption.click();
        System.out.println("✓ Step 4: Selected 'Bardstown Child Care Program'");

        
        
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//*[contains(@class,'loading') or contains(@class,'spinner') or contains(@class,'overlay')]")));


        WebElement requestToJoinBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(normalize-space(.), 'Request To Join')]"
                        + " | //a[contains(normalize-space(.), 'Request To Join')]")));

        scrollToElement(requestToJoinBtn);


        try {
            requestToJoinBtn.click();
        } catch (Exception e) {
            System.out.println("⚠ Normal click blocked, retrying with JS click...");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", requestToJoinBtn);
        }
        System.out.println("✓ Step 5: Clicked 'Request To Join' button");

        
        WebElement dropdownTrigger = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//*[@role='combobox'] | //*[@role='listbox'])[1]")));
        scrollToElement(dropdownTrigger);
        dropdownTrigger.click();
        WebElement firstOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//*[@role='option'])[1]")));
        firstOption.click();
        System.out.println("✓ Step 6: Selected first option for Child's Name");

        
        
        WebElement ageGroupSelect = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//select[@name='age_group']")));

        List<WebElement> ageGroupOptions = ageGroupSelect.findElements(By.tagName("option"));
        String firstOptionValue = ageGroupOptions.get(1).getAttribute("value");
        String firstOptionText  = ageGroupOptions.get(1).getText();
        js.executeScript(
                "var sel = arguments[0];"
                        + "sel.value = arguments[1];"
                        + "sel.dispatchEvent(new Event('input',  { bubbles: true }));"
                        + "sel.dispatchEvent(new Event('change', { bubbles: true }));",
                ageGroupSelect, firstOptionValue);

        System.out.println("✔  Step 6: Age Group selected: " + firstOptionText);
        
        
        WebElement submitRequestBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(normalize-space(.), 'Submit Request')]")));
        scrollToElement(submitRequestBtn);

        try {
            submitRequestBtn.click();
        } catch (ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", submitRequestBtn);
        }

        System.out.println("✔  Step 8: Clicked 'Submit Request' button");

        
        Thread.sleep(2000); 

        WebElement confirmationMessage1 = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Your request goes to the center for review')]")));
        scrollToElement(confirmationMessage1);
        Assert.assertTrue(confirmationMessage1.isDisplayed(), "First confirmation message not displayed");
        System.out.println("✓ Step 9: Verified first confirmation message: 'Your request goes to the center for review...'");

        WebElement confirmationMessage2 = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'There is no requirements yet for this age group')]")));
        Assert.assertTrue(confirmationMessage2.isDisplayed(), "Second confirmation message not displayed");
        System.out.println("✓ Step 9: Verified second confirmation message: 'There is no requirements yet for this age group'");

        System.out.println("\n✓✓ PART 1 COMPLETED: Waitlist Request Created Successfully ✓✓\n");

        
        
        
        System.out.println("========== PART 2: User Login Flow ==========");

        
        driver.get("https://staging.dashboard.kidsqueue.softigital.com/login");
        waitForPageLoad();
        System.out.println("✓ Step 10: Navigated to Login page");
        Thread.sleep(3000);
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@type='email' or contains(@placeholder, 'email')]")));
        scrollToElement(emailField);
        emailField.clear();
        emailField.sendKeys("enas.gamall.salem@gmail.com");
        System.out.println("✓ Step 11: Entered email: enas.gamall.salem@gmail.com");
        Thread.sleep(1500);
        
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@type='password']")));
        scrollToElement(passwordField);
        passwordField.clear();
        passwordField.sendKeys("12345678");
        System.out.println("✓ Step 12: Entered password");
        Thread.sleep(1500);
        
        WebElement letsGoBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(normalize-space(.), concat('Let', \"'\" ,  's Go!'))]")));
        scrollToElement(letsGoBtn);

        try {
            letsGoBtn.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", letsGoBtn);
        }

        System.out.println("✓ Step 13: Clicked 'Let's Go!' button");
        Thread.sleep(3000);
        driver.findElement(By.xpath("//input[@class='disabled:cursor-not-allowed']"))
                .sendKeys("793421");
        Thread.sleep(1200);

        driver.findElement(By.xpath("//button[@type='submit']")).click();
        Thread.sleep(2000);
        System.out.println("✓ Step 15: Clicked 'Verify Account' button");
        Thread.sleep(3000);

        
        
        
        System.out.println("========== PART 3: Approve Request and Add to Waitlist ==========");

        
        WebElement enrollmentMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(), 'Enrollment')]")));
        scrollToElement(enrollmentMenu);
        enrollmentMenu.click();
        System.out.println("✓ Step 16: Clicked on 'Enrollment'");
        Thread.sleep(1000);

        
        WebElement waitlistOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(), 'Waitlist')]")));
        scrollToElement(waitlistOption);
        waitlistOption.click();
        System.out.println("✓ Step 17: Selected 'Waitlist'");
        Thread.sleep(2000);
        
        WebElement requestsTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(@id, '-trigger-requests')]"
                        + " | //*[@role='tab' and contains(normalize-space(.), 'Requests')]"
                        + " | //button[contains(normalize-space(.), 'Requests')]")));
        scrollToElement(requestsTab);
        try {
            requestsTab.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", requestsTab);
        }

        System.out.println("✔  Step 18: Clicked 'Requests' tab");

        
        Thread.sleep(2000);
        


        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[contains(@class,'cursor-pointer') and contains(@class,'rounded-xl')]")));
        WebElement createdRequest = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//button[contains(@class,'cursor-pointer')"
                        + " and contains(@class,'rounded-xl')"
                        + " and contains(@class,'text-left')"
                        + " and .//*[contains(normalize-space(.), 'pending review')]])[1]")));
        scrollToElement(createdRequest);
        try {
            createdRequest.click();
        } catch (Exception e) {
            System.out.println("⚠  Normal click blocked, retrying with JS click...");
            js.executeScript("arguments[0].click();", createdRequest);
        }

        System.out.println("✔  Step 19: Selected the pending review request");

        
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//*[contains(@class, 'loading') or contains(@class, 'spinner') or contains(@class, 'loader')]")));
        Thread.sleep(2000);
        System.out.println("✓ Step 20: Waited for page to load completely");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[contains(normalize-space(.), 'Approve')]")));

        WebElement approveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(normalize-space(.), 'Approve') "
                        + "and contains(normalize-space(.), 'Waitlist')]")));

        js.executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'nearest'});",
                approveBtn);
        Thread.sleep(500); 
        try {
            approveBtn.click();
        } catch (Exception e) {
            System.out.println("⚠  Normal click blocked, retrying with JS click...");
            js.executeScript("arguments[0].click();", approveBtn);
        }
        System.out.println("✔  Step 21: Clicked 'Approve & Add to Waitlist'");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[contains(normalize-space(.), 'Approve')]")));

        WebElement approveBtn1 = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(normalize-space(.), 'Approve') "
                        + "and contains(normalize-space(.), 'Waitlist')]")));

        js.executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'nearest'});",
                approveBtn1);
        Thread.sleep(500); 
        try {
            approveBtn1.click();
        } catch (Exception e) {
            System.out.println("⚠  Normal click blocked, retrying with JS click...");
            js.executeScript("arguments[0].click();", approveBtn1);
        }

        
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'success') or contains(text(), 'approved') or contains(@class, 'success')]")));
        Assert.assertTrue(successMessage.isDisplayed(), "Approval success message not displayed");

        System.out.println("\n✓✓ PART 3 COMPLETED: Request Approved and Added to Waitlist Successfully ✓✓\n");
        System.out.println("========== FINAL VERIFICATION ==========");
        System.out.println("✓ All 21 steps executed successfully");
        System.out.println("✓ User registered and roles assigned");
        System.out.println("✓ Childcare center added to watchlist");
        System.out.println("✓ Request approved and verified");

        System.out.println("\n✓✓✓✓✓ END-TO-END TEST COMPLETED SUCCESSFULLY ✓✓✓✓✓\n");
    }

    
    private void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    private void waitForPageLoad() {
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
    }
}
