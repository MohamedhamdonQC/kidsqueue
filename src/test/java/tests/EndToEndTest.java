package tests;

import base.BaseTest;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.ChildcareCenterPage;
import pages.RegisterPage;
import pages.RoleSelectionPage;
import pages.Search;
import utils.TestDataGenerator;
import pages.dashboardPage ;
import java.time.Duration;

@Listeners(utils.reports.Listeners.class)
public class EndToEndTest extends BaseTest {

    private static final String BASE_URL    = "https://dev.kidsqueue.softigital.com";
    private static final String FIRST_NAME  = "Mohamed";
    private static final String LAST_NAME   = "Hamdon";
    private static final String PASSWORD    = "12345678";
    private static final String SEARCH_TERM = "Roots and Wings Childcare and Preschool";

    @Test(description = "Register a new user, assign roles, then search for a childcare center")
    public void registerAndAssignRoles() throws InterruptedException {

        // ── Step 1: Register ─────────────────────────────────────────────────
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        String email = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();

        // ── Wait: leave /register ────────────────────────────────────────────
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.not(
                        ExpectedConditions.urlContains("/register")
                ));

        // ── Step 2: Select Roles ─────────────────────────────────────────────
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();

        // ── Step 3: Navigate to Home ─────────────────────────────────────────
        driver.navigate().to(BASE_URL + "/");
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.urlToBe(BASE_URL + "/"));

        // ── Step 4: Search and Select ────────────────────────────────────────
        Search searchPage = new Search(driver);
        searchPage.searchFor(SEARCH_TERM);
        searchPage.searchSelect();

        ChildcareCenterPage childcareCenterPage = new ChildcareCenterPage(driver);
        childcareCenterPage.waitUntilLoaded();
        childcareCenterPage.addToWatchlist();

        driver.navigate().to(BASE_URL + "/parent/dashboard");
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.urlToBe(BASE_URL + "/parent/dashboard"));
// Final Test End To End
        dashboardPage dashboardPage = new dashboardPage(driver);
        dashboardPage.clickWatching();
        Thread.sleep(3000);
        Assert.assertTrue(
                dashboardPage.isSchoolVisible(SEARCH_TERM),
                "School not found in Watching tab!"
        );



    }
}
