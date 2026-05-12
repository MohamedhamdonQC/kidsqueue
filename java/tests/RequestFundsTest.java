package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.RegisterPage;
import pages.RequestFundsPage;
import pages.RoleSelectionPage;
import utils.TestDataGenerator;

import java.time.Duration;

@Listeners(utils.reports.Listeners.class)
public class RequestFundsTest extends BaseTest {

    private static final String BASE_URL = "https://staging.kidsqueue.softigital.com";
    private static final String USER_EMAIL = "Test@Leave.com";
    private static final String USER_PASSWORD = "Password";
    private static final String FIRST_NAME  = "Mohamed";
    private static final String LAST_NAME   = "Hamdon";
    private static final String PASSWORD    = "12345678";
    @Test(description = "Verify the parent can submit a fund request successfully from the Request Funds modal")
    public void createFundRequest() throws InterruptedException {

        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");
        hideCookieBannerArtifacts();
        hideCookieBannerArtifacts();
        String email = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();
        Thread.sleep(3000);

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
        Thread.sleep(3000);
        // ── Step 3: Navigate to Home ─────────────────────────────────────────
        driver.navigate().to(BASE_URL + "/");
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        registerPage.openParentPortal();
        registerPage.clickParentNext();
        Thread.sleep(3000);
        registerPage.completeChildRegistration("Hamdon", "Test Atuomation");
        Thread.sleep(3000);
        // ── Step 4: Search and Select ────────────────────────────────────────
        Thread.sleep(3000);
        driver.findElement( By.xpath("//button[@aria-haspopup='menu']")).click();
        Thread.sleep(1500);
        driver.findElement(By.linkText("Wallet & Payments")).click();
        Thread.sleep(2000);
        RequestFundsPage requestFundsPage = new RequestFundsPage(driver);
        requestFundsPage.openRequestFundsModal();
        String recipientEmail = TestDataGenerator.generateEmail("funds-recipient");
        String amount = "150";
        String notes = "Automation request " + System.currentTimeMillis();
        requestFundsPage.openChildDropdown();
        requestFundsPage.fillRecipientEmail(recipientEmail);
        requestFundsPage.fillAmount(amount);
        requestFundsPage.fillNotes(notes);
        requestFundsPage.submitRequest();
        Assert.assertTrue(
                requestFundsPage.isSuccessToastVisible(),
                "Expected success toast with 'Fund request sent successfully'"
        );
        Assert.assertTrue(
                requestFundsPage.isModalClosed(),
                "Expected request funds modal to close after submit"
        );
        Assert.assertTrue(
                requestFundsPage.isPendingVisible(),
                "Expected the created request to appear in Pending state"
        );
        Assert.assertTrue(
                requestFundsPage.isRequestVisibleForEmail(recipientEmail),
                "Expected the request row to include the written recipient email"
        );
    }

    @Test(description = "BUG - Request Funds empty child dropdown should show Add New Child")
    public void requestFundsShowsAddNewChildWhenNoChildrenExist() throws InterruptedException {
        hideCookieBannerArtifacts();
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        String email = TestDataGenerator.generateEmail("nochildren");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();
        Thread.sleep(3000);

        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));

        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();
        Thread.sleep(1500);
        driver.findElement(By.xpath("//button[@aria-haspopup='menu']")).click();
        Thread.sleep(1500);
        driver.findElement(By.linkText("Wallet & Payments")).click();
        Thread.sleep(2000);
        hideCookieBannerArtifacts();
        RequestFundsPage requestFundsPage = new RequestFundsPage(driver);
        requestFundsPage.openRequestFundsModal();
        requestFundsPage.openChildDropdown();

        Assert.assertTrue(
                requestFundsPage.isNoChildrenEmptyStateVisible(),
                "Expected the child dropdown to show an empty state when no children exist"
        );
        Assert.assertTrue(
                requestFundsPage.isAddNewChildVisible(),
                "BUG: + Add New Child is missing from the Request Funds empty child state"
        );
    }
}
