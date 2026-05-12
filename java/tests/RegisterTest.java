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

/**
 * RegisterTest
 *
 * Covers:
 *   Positive  – TC01 : Full happy-path registration
 *   Negative  – TC02 : Empty form submission
 *             – TC03 : Invalid e-mail format
 *             – TC04 : Already-registered e-mail
 *             – TC05 : First name missing
 *             – TC06 : Last name missing
 *             – TC07 : E-mail field missing
 *             – TC08 : Password too short (< 8 chars)
 *             – TC09 : Password missing
 *             – TC10 : Confirm password missing
 *             – TC11 : Passwords do not match
 *             – TC12 : Password contains only numbers
 *             – TC13 : First name with special characters
 *             – TC14 : Last name with special characters
 *             – TC15 : E-mail with spaces
 *             – TC16 : Extremely long input in all fields
 */
@Listeners(utils.reports.Listeners.class)
public class RegisterTest extends BaseTest {

    // ── Constants ────────────────────────────────────────────────────────────
    private static final String BASE_URL         = "https://dev.kidsqueue.softigital.com";
    private static final String REGISTER_URL     = BASE_URL + "/register";
    private static final String FIRST_NAME       = "Mohamed";
    private static final String LAST_NAME        = "Hamdon";
    private static final String VALID_PASSWORD   = "12345678";
    private static final String EXISTING_EMAIL   = "Test@Test.com"; // pre-seeded account

    private static final int    SHORT_WAIT       = 3_000;   // ms  – kept for parity; prefer explicit waits
    private static final int    DRIVER_TIMEOUT   = 20;      // seconds

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Navigate to /register and return a fresh page object. */
    private RegisterPage openRegisterPage() {
        RegisterPage page = new RegisterPage(driver);
        page.open(REGISTER_URL);
        return page;
    }

    /** Generic wait: URL must no longer contain the given path segment. */
    private void waitForUrlChange(String segmentToLeave) {
        new WebDriverWait(driver, Duration.ofSeconds(DRIVER_TIMEOUT))
                .until(ExpectedConditions.not(
                        ExpectedConditions.urlContains(segmentToLeave)));
    }

    /** Wait until URL equals the given value exactly. */
    private void waitForExactUrl(String expectedUrl) {
        new WebDriverWait(driver, Duration.ofSeconds(DRIVER_TIMEOUT))
                .until(ExpectedConditions.urlToBe(expectedUrl));
    }

    /**
     * Assert that a field-level validation message is visible on the page.
     *
     * @param page    the RegisterPage object
     * @param message the exact (or partial) text expected in the error element
     */
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

    /**
     * TC01 – Register with valid data successfully.
     * Full end-to-end flow: registration → role selection → home → parent portal.
     */
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

        // Leave /register
        waitForUrlChange("/register");

        // Step 2 – Role selection
        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();
        Thread.sleep(SHORT_WAIT);

        // Step 3 – Navigate home
        driver.navigate().to(BASE_URL + "/");
        waitForExactUrl(BASE_URL + "/");

        registerPage.openParentPortal();
        registerPage.clickParentNext();
        Thread.sleep(SHORT_WAIT);

        registerPage.completeChildRegistration("Hamdon", "Test Automation");
        Thread.sleep(SHORT_WAIT);
    }


    // ── TC02 : Empty form ────────────────────────────────────────────────────
    /**
     * TC02 – Submit a completely empty registration form.
     * All required-field errors should appear; the user should stay on /register.
     */
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

    // ── TC03 : Invalid e-mail format ─────────────────────────────────────────
    /**
     * TC03 – Various malformed e-mail addresses.
     * The field validator should reject each one individually.
     */
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

    // ── TC04 : Already-registered e-mail ─────────────────────────────────────
    /**
     * TC04 – Attempt to register with an e-mail that already exists in the system.
     * Expects a server-side error message about duplicate registration.
     */
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

    // ── TC05 : Missing first name ─────────────────────────────────────────────
    /**
     * TC05 – Leave the First Name field blank; fill all others correctly.
     */
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

    // ── TC06 : Missing last name ──────────────────────────────────────────────
    /**
     * TC06 – Leave the Last Name field blank; fill all others correctly.
     */
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

    // ── TC07 : Missing e-mail ─────────────────────────────────────────────────
    /**
     * TC07 – Leave the Email field blank; fill all others correctly.
     */
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

    // ── TC08 : Password too short ─────────────────────────────────────────────
    /**
     * TC08 – Enter a password shorter than the minimum required length (8 chars).
     */
    @Test(description = "TC08 - Password shorter than 8 characters shows length error")
    public void registerWithShortPassword() {
        RegisterPage registerPage = openRegisterPage();
        String email = TestDataGenerator.generateEmail("short_pw");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword("123");          // 3 chars – too short
        registerPage.fillConfirmPassword("123");
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Password must be at least 8 characters");
    }

    // ── TC09 : Missing password ───────────────────────────────────────────────
    /**
     * TC09 – Leave the Password field blank.
     */
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

    // ── TC10 : Missing confirm password ──────────────────────────────────────
    /**
     * TC10 – Leave the Confirm Password field blank.
     */
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

    // ── TC11 : Passwords do not match ────────────────────────────────────────
    /**
     * TC11 – Password and Confirm Password fields contain different values.
     */
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

    // ── TC12 : Password contains only numbers ────────────────────────────────
    /**
     * TC12 – Password is numerics-only (e.g. "12345678").
     * Many apps require at least one letter; adjust expected message as needed.
     */
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

    // ── TC13 : First name with special characters ─────────────────────────────
    /**
     * TC13 – First name contains special characters that should not be allowed.
     */
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

    // ── TC14 : Last name with special characters ──────────────────────────────
    /**
     * TC14 – Last name contains special characters that should not be allowed.
     */
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

    // ── TC15 : E-mail with spaces ─────────────────────────────────────────────
    /**
     * TC15 – E-mail field contains an embedded space, making it syntactically invalid.
     */
    @Test(description = "TC15 - Email with spaces shows invalid email format error")
    public void registerWithEmailContainingSpaces() {
        RegisterPage registerPage = openRegisterPage();

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail("mo hamed@test.com");  // space inside local part
        registerPage.fillPassword(VALID_PASSWORD);
        registerPage.fillConfirmPassword(VALID_PASSWORD);
        registerPage.submit();

        assertStillOnRegisterPage();
        assertValidationMessage(registerPage, "Enter a valid email address");
    }

    // ── TC16 : Extremely long input in all fields ─────────────────────────────
    /**
     * TC16 – All text fields receive input that exceeds a reasonable maximum length.
     * Expects either a truncation/max-length enforcement or an explicit error.
     */
    @Test(description = "TC16 - Extremely long inputs are rejected or truncated with an error")
    public void registerWithExtremelyLongInputs() {
        RegisterPage registerPage = openRegisterPage();

        String longString  = "A".repeat(256);        // 256 characters
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
