package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import pages.*;
import utils.TestDataGenerator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.file.Paths;
import java.time.Duration;

public class GroupTest extends BaseTest {
    private static final String BASE_URL    = "https://staging.kidsqueue.softigital.com";
    private static final String FIRST_NAME  = "Mohamed";
    private static final String LAST_NAME   = "Hamdon";
    private static final String PASSWORD    = "12345678";

    String postTitle   = TestDataGenerator.randomAlphanumeric(8);

    String postContent = TestDataGenerator.randomSentence(8);
    @Test
    public void createGroupAfterLogin() throws InterruptedException {
        hideCookieBannerArtifacts();
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
        driver.navigate().refresh();
        Thread.sleep(3000);
        groupPage.scrollToBottom();
        Thread.sleep(2000);
        groupPage.Viewdetails();
        Thread.sleep(3000);
        groupPage.addpost();
        Thread.sleep(5000);
        groupPage.Title(postTitle);
        Thread.sleep(1000);
        groupPage.Contect(postContent);
        Thread.sleep(1000);
        groupPage.CLickonUploadPost();
        Thread.sleep(3000);
        driver.navigate().to("https://staging.kidsqueue.softigital.com/parent/groups?page=1");
        Thread.sleep(3000);
        groupPage.PostPage();
        Thread.sleep(3000);


    }

    @Test
    public void TestLogoutFromtheGroup() throws InterruptedException {
        hideCookieBannerArtifacts();
        driver.manage().deleteAllCookies();
        LoginPage loginPage = new LoginPage(driver);
        driver.navigate().to("https://staging.kidsqueue.softigital.com/login");
        loginPage.login("Test@Leave.com", "Password");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("otp_value"))).sendKeys("793421");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']"))).click();
        Thread.sleep(3000);
        driver.navigate().to("https://staging.kidsqueue.softigital.com/parent/groups?page=1");
        WebElement firstLeaveBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//button[normalize-space()='Leave'])[1]")
                )
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", firstLeaveBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstLeaveBtn);

        WebElement popupLeaveBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@role='dialog']//button[normalize-space()='Leave']")
                )
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", popupLeaveBtn);

        Thread.sleep(3000);
    }



    private void closeChromePasswordPopup() {
        try {
            Robot robot = new Robot();
            Thread.sleep(1000);

            
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}