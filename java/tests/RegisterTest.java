package tests;

import base.BaseTest;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.RegisterPage;
import pages.RoleSelectionPage;
import utils.TestDataGenerator;

import java.time.Duration;


@Listeners(utils.reports.Listeners.class)
public class RegisterTest extends BaseTest {

    
    private static final String BASE_URL         = "https://dev.kidsqueue.softigital.com";
    private static final String REGISTER_URL     = BASE_URL + "/register";
    private static final String FIRST_NAME       = "Mohamed";
    private static final String LAST_NAME        = "Hamdon";
    private static final String VALID_PASSWORD   = "12345678";
    private static final String EXISTING_EMAIL   = "Test@Test.com"; 

    private static final int    SHORT_WAIT       = 3_000;   
    private static final int    DRIVER_TIMEOUT   = 20;      

    

    
    private RegisterPage openRegisterPage() {
        RegisterPage page = new RegisterPage(driver);
        page.open(REGISTER_URL);
        return page;
    }

    
    private void waitForUrlChange(String segmentToLeave) {
        new WebDriverWait(driver, Duration.ofSeconds(DRIVER_TIMEOUT))
                .until(ExpectedConditions.not(
                        ExpectedConditions.urlContains(segmentToLeave)));
    }

    
    private void waitForExactUrl(String expectedUrl) {
        new WebDriverWait(driver, Duration.ofSeconds(DRIVER_TIMEOUT))
                .until(ExpectedConditions.urlToBe(expectedUrl));
    }

    
    private void assertValidationMessage(RegisterPage page, String message) {
        Assert.assertTrue(
                page.isValidationMessageDisplayed(message),
                "Expected validation message not found: [" + message + "]"
        );
    }

    private void assertStillOnRegisterPage() {
        Assert.assertTrue(
                driver.getCurrentUrl().contains("/register"),
                "Expected to stay on /register but navigated away to: " + driver.getCurrentUrl()
        );
    }

    
    @Test(description = "TC01 - Register with valid data successfully")
    public void registerWithValidData() throws InterruptedException {
        RegisterPage registerPage = openRegisterPage();
        String email = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.fillConfirmPassword(VALID_PASSWORD);
        registerPage.submit();
        Thread.sleep(SHORT_WAIT);

        
        waitForUrlChange("/register");

        
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();
        Thread.sleep(SHORT_WAIT);

        
        driver.navigate().to(BASE_URL + "/");
        waitForExactUrl(BASE_URL + "/");

        registerPage.openParentPortal();
        registerPage.clickParentNext();
        Thread.sleep(SHORT_WAIT);

        registerPage.completeChildRegistration("Hamdon", "Test Automation");
        Thread.sleep(SHORT_WAIT);
    }


    
    
    @Test(description = "TC02 - Submit empty form shows all required field errors")
    public void registerWithEmptyForm() {
        RegisterPage registerPage = openRegisterPage();
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "First name is required");
        assertValidationMessage(registerPage, "Last name is required");
        assertValidationMessage(registerPage, "Email is required");
        assertValidationMessage(registerPage, "Password is required");
        assertValidationMessage(registerPage, "Password is required");
    }

    
    
    @Test(description = "TC03 - Invalid email format shows email validation error",
            dataProvider = "invalidEmails",
            dataProviderClass = utils.DataProviders.class)
    public void registerWithInvalidEmailFormat(String invalidEmail) {
        RegisterPage registerPage = openRegisterPage();

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(invalidEmail);
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.fillConfirmPassword(VALID_PASSWORD);
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Enter a valid email address");
    }

    
    
    @Test(description = "TC04 - Already registered email shows duplicate email error")
    public void registerWithExistingEmail() {
        RegisterPage registerPage = openRegisterPage();

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(EXISTING_EMAIL);
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.fillConfirmPassword(VALID_PASSWORD);
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Email is already registered");
    }

    
    
    @Test(description = "TC05 - Missing first name shows required field error")
    public void registerWithMissingFirstName() {
        RegisterPage registerPage = openRegisterPage();
        String email = TestDataGenerator.generateEmail("no_firstname");

        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.fillConfirmPassword(VALID_PASSWORD);
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "First name is required");
    }

    
    
    @Test(description = "TC06 - Missing last name shows required field error")
    public void registerWithMissingLastName() {
        RegisterPage registerPage = openRegisterPage();
        String email = TestDataGenerator.generateEmail("no_lastname");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.fillConfirmPassword(VALID_PASSWORD);
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Last name is required");
    }

    
    
    @Test(description = "TC07 - Missing email shows required field error")
    public void registerWithMissingEmail() {
        RegisterPage registerPage = openRegisterPage();

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.fillConfirmPassword(VALID_PASSWORD);
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Email is required");
    }

    
    
    @Test(description = "TC08 - Password shorter than 8 characters shows length error")
    public void registerWithShortPassword() {
        RegisterPage registerPage = openRegisterPage();
        String email = TestDataGenerator.generateEmail("short_pw");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword("123");          
        registerPage.fillConfirmPassword("123");
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Password must be at least 8 characters");
    }

    
    
    @Test(description = "TC09 - Missing password shows required field error")
    public void registerWithMissingPassword() {
        RegisterPage registerPage = openRegisterPage();
        String email = TestDataGenerator.generateEmail("no_password");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillConfirmPassword(VALID_PASSWORD);
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Password is required");
    }

    
    
    @Test(description = "TC10 - Missing confirm password shows required field error")
    public void registerWithMissingConfirmPassword() {
        RegisterPage registerPage = openRegisterPage();
        String email = TestDataGenerator.generateEmail("no_confirm");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Password is required");
    }

    
    
    @Test(description = "TC11 - Mismatched passwords shows mismatch error")
    public void registerWithMismatchedPasswords() {
        RegisterPage registerPage = openRegisterPage();
        String email = TestDataGenerator.generateEmail("mismatch_pw");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.fillConfirmPassword("different99");
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Passwords do not match");
    }

    
    
    @Test(description = "TC12 - Numeric-only password shows complexity error")
    public void registerWithNumericOnlyPassword() {
        RegisterPage registerPage = openRegisterPage();
        String email = TestDataGenerator.generateEmail("numeric_pw");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword("12345678");
        registerPage.fillConfirmPassword("12345678");
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Password must contain at least one letter");
    }

    
    
    @Test(description = "TC13 - First name with special characters shows invalid format error")
    public void registerWithSpecialCharactersInFirstName() {
        RegisterPage registerPage = openRegisterPage();
        String email = TestDataGenerator.generateEmail("spec_first");

        registerPage.fillFirstName("M@h#med!");
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.fillConfirmPassword(VALID_PASSWORD);
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "First name must contain only letters");
    }

    
    
    @Test(description = "TC14 - Last name with special characters shows invalid format error")
    public void registerWithSpecialCharactersInLastName() {
        RegisterPage registerPage = openRegisterPage();
        String email = TestDataGenerator.generateEmail("spec_last");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName("H@md$n#");
        registerPage.fillEmail(email);
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.fillConfirmPassword(VALID_PASSWORD);
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Last name must contain only letters");
    }

    
    
    @Test(description = "TC15 - Email with spaces shows invalid email format error")
    public void registerWithEmailContainingSpaces() {
        RegisterPage registerPage = openRegisterPage();

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail("mo hamed@test.com");  
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.fillConfirmPassword(VALID_PASSWORD);
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Enter a valid email address");
    }

    
    
    @Test(description = "TC16 - Extremely long inputs are rejected or truncated with an error")
    public void registerWithExtremelyLongInputs() {
        RegisterPage registerPage = openRegisterPage();

        String longString  = "A".repeat(256);        
        String longEmail   = "a".repeat(245) + "@t.com";

        registerPage.fillFirstName(longString);
        registerPage.fillLastName(longString);
        registerPage.fillEmail(longEmail);
        registerPage.fillPassword(longString);
        registerPage.fillConfirmPassword(longString);
        registerPage.submit();

        assertStillOnRegisterPage();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
