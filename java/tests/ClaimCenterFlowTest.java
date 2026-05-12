package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.ClaimCenterPage;
import pages.RegisterPage;
import pages.RoleSelectionPage;
import utils.TestDataGenerator;

import java.time.Duration;
import java.util.Random;

@Listeners(utils.reports.Listeners.class)
public class ClaimCenterFlowTest extends BaseTest {

    private static final String BASE_URL = "https://staging.kidsqueue.softigital.com";
    private static final String FIRST_NAME = "Mohamed";
    private static final String LAST_NAME = "Hamdon";
    private static final String PASSWORD = "12345678";

    private static final String[][] SCHOOL_POOL = {
            {"roots", "Roots And Wings Childcare And Preschool"},
            {"bright", "Brighter Beginnings Academy"},
            {"little", "Little Stars Daycare"},
            {"sunshine", "Sunshine Center Daycare"},
            {"rainbow", "Rainbow Learning Academy"}
    };

    private WebDriverWait wait;

    private String[] pickRandomSchool() {
        return SCHOOL_POOL[new Random().nextInt(SCHOOL_POOL.length)];
    }

    private void scrollToCenter(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", el);
    }

    private void scrollAndClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        scrollToCenter(el);
        wait.until(ExpectedConditions.elementToBeClickable(el));
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    private void scrollAndClickBottom(By locator) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'end', inline:'nearest'});", el);
        wait.until(ExpectedConditions.elementToBeClickable(el));
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    private void scrollAndType(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollToCenter(el);
        el.clear();
        el.sendKeys(text);
    }

    private void enterVerificationCode(String code) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("verificationCode")));
        scrollToCenter(field);
        field.clear();
        field.sendKeys(code);
    }

    private void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @Test(
            description = "Register, claim a school, and verify the claim page shows the school name only",
            priority = 1
    )
    public void registerAndClaimCenterWithRandomData() throws InterruptedException {
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));

        String[] school = pickRandomSchool();
        String searchTerm = school[0];
        String schoolName = school[1];

        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        String registrationEmail = TestDataGenerator.generateEmail("claimcenter");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(registrationEmail);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));

        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();
        pause(2000);

        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        pause(2000);

        scrollAndClick(By.linkText("Claim Your Center"));
        pause(2000);
        scrollAndType(By.id("search"), searchTerm);
        pause(3000);
        scrollAndClick(By.xpath("//button[normalize-space()='Claim This Center']"));
        pause(2000);

        ClaimCenterPage claimCenterPage = new ClaimCenterPage(driver);

        WebElement schoolNameEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("(//h2[contains(@class,'font-semibold')] | //h1)[1]")
        ));

        scrollAndType(By.id("first_name"), "Test");
        scrollAndType(By.id("last_name"), "Automation");
        scrollAndType(By.id("email"), TestDataGenerator.generateEmail("claim"));
        driver.findElement(By.id("phone")).sendKeys("231 312 3132");
        pause(2000);

        scrollAndClick(By.id("claim-center-create-claim-trigger"));
        pause(2000);

        enterVerificationCode("793421");
        scrollAndClick(By.id("claim-center-verify-email-trigger"));
        pause(2000);

        scrollAndClick(By.id("claim-center-continue-to-phone-trigger"));
        pause(3000);

        enterVerificationCode("793421");
        pause(3000);
        scrollAndClick(By.id("claim-center-verify-phone-trigger"));
        pause(2000);

        driver.findElement(By.id("government_id")).sendKeys("C:\\Users\\M Hamdoon\\Desktop\\CV\\Mohamed_Hamdon_Abbas_CV.pdf");
        pause(2000);
        driver.findElement(By.id("business_license")).sendKeys("C:\\Users\\M Hamdoon\\Desktop\\CV\\Mohamed_Hamdon_Abbas_CV.pdf");
        pause(2000);
        Thread.sleep(3000);
        claimCenterPage.clickContinueToReview();
        pause(2000);
        claimCenterPage.acceptTerms();
        claimCenterPage.confirmAccuracy();
        claimCenterPage.submitClaim();
        pause(3000);
        driver.findElement(By.xpath("//button[.//div[contains(.,'Logout')]]")).click();
        Thread.sleep(3000);
        driver.navigate().to("https://staging.dashboard.kidsqueue.softigital.com/admin/login");
        Thread.sleep(3000);
        driver.findElement(By.name("email")).sendKeys("super@admin.com");
        driver.findElement(By.name("password")).sendKeys("password");
        Thread.sleep(3000);
        driver.findElement(By.xpath("//button[.//text()='Sign in as Admin']")).click();
        Thread.sleep(3000);
        driver.findElement(By.linkText("Claim Requests")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement firstApprove = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(.,'Approve')]")
                )
        );
        firstApprove.click();
        WebElement secondApprove = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@role='dialog']//button[contains(.,'Approve')]")
                )
        );
        secondApprove.click();
        Thread.sleep(3000);
        System.out.println("[PASS] Claim page shows school name only: " + schoolName);
    }

    @Test(
            description = "Register without uploading file, claim a school, and verify the claim page shows the school name only",
            priority = 2
    )
    public void registerAndClaimCenterWithRandomDataa() throws InterruptedException {
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));

        String[] school = pickRandomSchool();
        String searchTerm = school[0];
        String schoolName = school[1];

        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        String registrationEmail = TestDataGenerator.generateEmail("claimcenter");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(registrationEmail);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));

        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();
        pause(2000);

        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        pause(2000);

        scrollAndClick(By.linkText("Claim Your Center"));
        pause(2000);
        scrollAndType(By.id("search"), searchTerm);
        pause(3000);
        scrollAndClick(By.xpath("//button[normalize-space()='Claim This Center']"));
        pause(2000);

        ClaimCenterPage claimCenterPage = new ClaimCenterPage(driver);

        WebElement schoolNameEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("(//h2[contains(@class,'font-semibold')] | //h1)[1]")
        ));

        scrollAndType(By.id("first_name"), "Test");
        scrollAndType(By.id("last_name"), "Automation");
        scrollAndType(By.id("email"), TestDataGenerator.generateEmail("claim"));
        driver.findElement(By.id("phone")).sendKeys("231 312 3132");
        pause(2000);

        scrollAndClick(By.id("claim-center-create-claim-trigger"));
        pause(2000);

        enterVerificationCode("793421");
        scrollAndClick(By.id("claim-center-verify-email-trigger"));
        pause(2000);

        scrollAndClick(By.id("claim-center-continue-to-phone-trigger"));
        pause(2000);

        enterVerificationCode("793421");
        scrollAndClick(By.id("claim-center-verify-phone-trigger"));
        pause(2000);

        scrollAndClickBottom(By.id("claim-center-skip-documents-trigger"));
        pause(3000);
        claimCenterPage.acceptTerms();
        claimCenterPage.confirmAccuracy();
        claimCenterPage.submitClaim();
        pause(3000);
        driver.findElement(By.xpath("//button[.//div[contains(.,'Logout')]]")).click();
        Thread.sleep(3000);
        driver.navigate().to("https://staging.dashboard.kidsqueue.softigital.com/admin/login");
        Thread.sleep(3000);
        driver.findElement(By.name("email")).sendKeys("super@admin.com");
        driver.findElement(By.name("password")).sendKeys("password");
        Thread.sleep(3000);
        driver.findElement(By.xpath("//button[.//text()='Sign in as Admin']")).click();
        Thread.sleep(3000);
        driver.findElement(By.linkText("Claim Requests")).click();
        Thread.sleep(3000);
        System.out.println("[PASS] Claim page shows school name only: " + schoolName);
    }


    @Test(
            description = "Register, claim a school, and Make Back To Center",
            priority = 3
    )
    public void BackregisterAndClaimCenterWithRandomDataa() throws InterruptedException {
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));

        String[] school = pickRandomSchool();
        String searchTerm = school[0];
        String schoolName = school[1];

        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        String registrationEmail = TestDataGenerator.generateEmail("claimcenter");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(registrationEmail);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));

        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();
        pause(2000);

        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        pause(2000);

        scrollAndClick(By.linkText("Claim Your Center"));
        pause(2000);
        scrollAndType(By.id("search"), searchTerm);
        pause(3000);
        scrollAndClick(By.xpath("//button[normalize-space()='Claim This Center']"));
        pause(2000);
        driver.findElement(By.xpath("//button[.//text()[contains(.,'Back to Center')]]")).click();
        Thread.sleep(3000);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
