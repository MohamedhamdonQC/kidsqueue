package tests;

import base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.LoginPage;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.Duration;
public class NoteTest extends BaseTest {

    private static final String BASE_URL      = "https://kidsqueue.softigital.com/";
    private static final String USER_EMAIL    = "ddd@dd.com";
    private static final String USER_PASSWORD = "123456789";

    WebDriverWait wait;

    @BeforeMethod
    public void setupNote() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        hideCookieBannerArtifacts();
    }

    @Test
    public void addNote_HappyScenario() {

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(BASE_URL + "login");
        loginPage.login(USER_EMAIL, USER_PASSWORD);
        closeChromePasswordPopup();
        driver.navigate().to(BASE_URL + "parent?schools_page=1");
        WebElement noteField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("content-editor"))
        );
        String noteText = "Test Note " + System.currentTimeMillis();
        noteField.sendKeys(noteText);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit']")
        )).click();
        driver.navigate().to(BASE_URL + "parent/account");
        wait.until(ExpectedConditions.elementToBeClickable(
        By.id("account-tab-notes")
        )).click();
        WebElement addedNote = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(),'" + noteText + "')]")
                )
        );
        Assert.assertTrue(addedNote.isDisplayed(), "Note was not added successfully!");
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

    @Test
    public void addEmptyNote_ShouldFail() {

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(BASE_URL + "login");
        loginPage.login(USER_EMAIL, USER_PASSWORD);
        closeChromePasswordPopup();
        driver.navigate().to(BASE_URL + "parent?schools_page=1");

        WebElement noteField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("content-editor"))
        );
        noteField.clear();

        driver.findElement(By.xpath("//button[@type='submit']")).click();

        boolean isErrorDisplayed = driver.getPageSource().contains("required");

        Assert.assertTrue(isErrorDisplayed, "Empty note was accepted!");
    }

    @Test
    public void addSpacesOnlyNote_ShouldFail() {

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(BASE_URL + "login");
        loginPage.login(USER_EMAIL, USER_PASSWORD);
        closeChromePasswordPopup();
        driver.navigate().to(BASE_URL + "parent?schools_page=1");

        WebElement noteField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("content-editor"))
        );

        noteField.sendKeys("     ");

        driver.findElement(By.xpath("//button[@type='submit']")).click();

        boolean isErrorDisplayed = driver.getPageSource().contains("required");

        Assert.assertTrue(isErrorDisplayed, "Spaces-only note was accepted!");
    }

    @Test
    public void addVeryLongNote_ShouldHandleProperly() {

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(BASE_URL + "login");
        loginPage.login(USER_EMAIL, USER_PASSWORD);

        driver.navigate().to(BASE_URL + "parent?schools_page=1");

        WebElement noteField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("content-editor"))
        );
        String longText = "A".repeat(5000);
        noteField.sendKeys(longText);
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        Assert.assertTrue(driver.getCurrentUrl().contains("parent"));
    }

}
