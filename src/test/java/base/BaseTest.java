package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

public class BaseTest {

    protected WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setupClass() {
        if (reuseDriverAcrossTests()) {
            createDriver();
        }
    }

    @BeforeMethod
    public void setup() {
        if (driver == null) {
            createDriver();
        }
    }

    @AfterMethod
    public void tearDown() {
        if (!reuseDriverAcrossTests()) {
            quitDriver();
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (reuseDriverAcrossTests()) {
            quitDriver();
        }
    }

    protected boolean reuseDriverAcrossTests() {
        return false;
    }

    private void createDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        driver = new ChromeDriver(options);
    }

    private void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
