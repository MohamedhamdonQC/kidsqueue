package tests;

import base.BaseTest;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import pages.*;
import utils.TestDataGenerator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class EventTest extends BaseTest {

    private static final String USER_EMAIL    = "admin23222@admin.com";
    private static final String USER_PASSWORD = "12345678";
    private static final String BASE_URL    = "https://staging.kidsqueue.softigital.com";
    private static final String FIRST_NAME  = "Mohamed";
    private static final String LAST_NAME   = "Hamdon";
    private static final String PASSWORD    = "12345678";
    @Test
    public void openEventsAndCreateEvent() throws InterruptedException {
        hideCookieBannerArtifacts();
        String imagePath = Paths.get(
                System.getProperty("user.dir"),
                "src", "test", "java", "tests", "resources", "images",
                "Screenshot 2026-04-09 170838.png"
        ).toString();
        driver.manage().deleteAllCookies();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        String email = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();
        Thread.sleep(3000);
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.not(
                        ExpectedConditions.urlContains("/register")
                ));
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();
        Thread.sleep(3000);
        //----2 Create Group Frist _------//
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
    }
    @Test
    public void NegtiveSenairos() throws InterruptedException {
        hideCookieBannerArtifacts();

        String imagePath = Paths.get(
                System.getProperty("user.dir"),
                "src", "test", "java", "tests", "resources", "images",
                "Screenshot 2026-04-09 170838.png"
        ).toString();
        driver.manage().deleteAllCookies();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");
        String email = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();
        Thread.sleep(3000);
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.not(
                        ExpectedConditions.urlContains("/register")
                ));
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();
        Thread.sleep(3000);
        //----2 Create Group First _------//
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
        // ── 2. Navigate to Event Taps ────────────────────────────────────────
        EventPage eventPage = new EventPage(driver);
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
       // eventPage.fillChecklistNote(3, "Verify all passports are valid for at least 6 months");
       // eventPage.fillChecklistNote(4, "Verify all passports are valid for at least 6 months");
        eventPage.fillChecklistNote(5, "Verify all passports are valid for at least 6 months");

        eventPage.clickSubmit();

        Thread.sleep(3000);

    }
    private void closeChromePasswordPopup() {
        try {
            Robot robot = new Robot();
            Thread.sleep(1000);

            // Press Enter to click OK
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
