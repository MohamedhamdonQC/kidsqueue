package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.Duration;

public class MyDocumentsettings extends BaseTest {
    private static final String BASE_URL      = "https://kidsqueue.softigital.com/";
    private static final String USER_EMAIL    = "ddd@dd.com";
    private static final String USER_PASSWORD = "123456789";

    WebDriverWait wait;

    @BeforeMethod
    public void setup() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        hideCookieBannerArtifacts();
    }
    @Test()
    public void adddocument_HappyScenario() throws InterruptedException {
        LoginPage loginPage = new LoginPage(driver) ;
        loginPage.open(BASE_URL + "login");
        loginPage.login(USER_EMAIL, USER_PASSWORD);
        closeChromePasswordPopup();
        driver.navigate().to(BASE_URL+"parent/account");
        Thread.sleep(3000);
        driver.findElement(By.id(" account-tab-documents")).click();
        Thread.sleep(3000);
        WebElement checkbox = driver.findElement(By.xpath("//button[@role='checkbox']"));
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block: 'center'});", checkbox);
        Thread.sleep(500);
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", checkbox);
        Thread.sleep(3000);

        WebElement addButton = driver.findElement(
                By.xpath("//button[.//div[normalize-space()='Add Other Document']]")
        );
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", addButton);
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", addButton);
        Thread.sleep(3000);

        driver.findElement(By.id("documentName")).sendKeys("Test decounment ");
        Thread.sleep(2000);

        String filePath = "C:\\Users\\M Hamdoon\\Desktop\\CV\\Mohamed_Hamdon_Abbas_CV.pdf";
        WebElement upload = driver.findElement(By.id("file-trigger"));
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].style.display='block';", upload);

        upload.sendKeys(filePath);
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
