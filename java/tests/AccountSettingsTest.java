package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.AccountSettingsPage;
import pages.LoginPage;
import pages.RegisterPage;
import pages.RoleSelectionPage;
import utils.TestDataGenerator;

import javax.imageio.plugins.tiff.TIFFImageReadParam;
import java.time.Duration;

public class AccountSettingsTest extends BaseTest {

    private static final String BASE_URL = "https://staging.kidsqueue.softigital.com/";
    private static final String EMAIL = "Test@Leave.com";
    private static final String PASSWORD = "Password";
    private static final String NEW_FIRST_NAME = "TestFirst";
    private static final String NEW_LAST_NAME = "TestLast";
    private static final String CHILD_FIRST_NAME = "Child Test";
    private static final String CHILD_LAST_NAME = "Last name Test";
    private static final String CHILD_MONTH = "January";
    private static final String CHILD_YEAR = "2020";
    private static final String CHILD_RELATION = "Father";
    private static final String FIRST_NAME  = "Mohamed";
    private static final String LAST_NAME   = "Hamdon";
    @Test
    public void updateParentAccountSettings() throws InterruptedException {

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
        Thread.sleep(2000);
        driver.findElement(
                By.xpath("//button[.//span[normalize-space()='Toggle menu']]")
        ).click();
        driver.findElement(By.linkText("Settings")).click();
        Thread.sleep(3000);

        AccountSettingsPage accountSettingsPage = new AccountSettingsPage(driver);
        accountSettingsPage.openSettingsTab();
        accountSettingsPage.openSettingsSection();

        Thread.sleep(3000);

        accountSettingsPage.updateFirstAndLastName(NEW_FIRST_NAME, NEW_LAST_NAME);
        accountSettingsPage.setEmail("TestAutomation@gmail.com");

        Thread.sleep(3000);

        accountSettingsPage.openAddChildDialog();
        accountSettingsPage.fillChildFirstName(CHILD_FIRST_NAME);
        accountSettingsPage.fillChildLastName(CHILD_LAST_NAME);

        accountSettingsPage.openBirthMonth();
        accountSettingsPage.chooseOptionByText(CHILD_MONTH);

        accountSettingsPage.openBirthYear();
        accountSettingsPage.chooseOptionByText(CHILD_YEAR);

        accountSettingsPage.openRelationDropdown();
        accountSettingsPage.chooseOptionByText(CHILD_RELATION);

        accountSettingsPage.clickChildCheckbox();
        accountSettingsPage.confirmAddChild();

        Thread.sleep(3000);

        accountSettingsPage.saveChanges();

        Thread.sleep(3000);

        driver.findElement(
                By.xpath("//button[.//span[normalize-space()='Toggle menu']]")
        ).click();

        Thread.sleep(2000);

        driver.findElement(By.linkText("Center Search")).click();

        Thread.sleep(3000);

        

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        WebElement userNameElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//span[contains(@class,'text-primary-500') and contains(text(),'Hello')]")
                )
        );

        String actualText = userNameElement.getText(); 

        String actualName = actualText.replace("Hello,", "").trim();
        String expectedName = (NEW_FIRST_NAME + " " + NEW_LAST_NAME).trim();

        Assert.assertTrue(
                actualName.equalsIgnoreCase(expectedName),
                " Name mismatch! Expected: " + expectedName + " but found: " + actualName
        );
    }
}
