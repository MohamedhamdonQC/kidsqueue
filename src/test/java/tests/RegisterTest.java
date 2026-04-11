package tests;

import base.BaseTest;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.RegisterPage;
import utils.TestDataGenerator;

import java.time.Duration;

@Listeners(utils.reports.Listeners.class)
public class RegisterTest extends BaseTest {

    private static final String BASE_URL   = "https://dev.kidsqueue.softigital.com";
    private static final String FIRST_NAME = "Mohamed";
    private static final String LAST_NAME  = "Hamdon";
    private static final String PASSWORD   = "12345678";

    @Test(description = "TC01 - Register with valid data successfully")
    public void registerWithValidData() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        String email = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();

        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.not(
                        ExpectedConditions.urlContains("/register")
                ));

        Assert.assertFalse(driver.getCurrentUrl().contains("/register"),
                "Should redirect away from /register after successful registration");
    }


    @Test(description = "TC02 - Register with already existing email")
    public void registerWithExistingEmail() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail("existing@gmail.com");
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();

        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                "Should stay on /register when email already exists");
    }

    @Test(description = "TC03 - Register with mismatched passwords")
    public void registerWithMismatchedPasswords() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(TestDataGenerator.generateEmail("test"));
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword("wrongpassword");
        registerPage.submit();

        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                "Should stay on /register when passwords do not match");
    }

    @Test(description = "TC04 - Register with empty fields")
    public void registerWithEmptyFields() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        registerPage.submit();

        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                "Should stay on /register when fields are empty");
    }

    @Test(description = "TC05 - Register with invalid email format")
    public void registerWithInvalidEmail() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail("not-an-email");
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();

        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                "Should stay on /register when email format is invalid");
    }

    @Test(description = "TC06 - Register with short password")
    public void registerWithShortPassword() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");

        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(TestDataGenerator.generateEmail("test"));
        registerPage.fillPassword("123");
        registerPage.fillConfirmPassword("123");
        registerPage.submit();

        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                "Should stay on /register when password is too short");
    }
}
