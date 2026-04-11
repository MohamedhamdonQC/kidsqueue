package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RoleSelectionPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;


    private final By selectRoleButton = By.xpath("//button[normalize-space()='Select Role']");
    private final By firstRoleOption  = By.xpath("(//main//section)[1]//div/button[1]");
    private final By secondRoleOption = By.xpath("(//main//section)[2]//div/button[1]");
    private final By confirmButton    = By.xpath("/html/body/div[2]/main/div/div[2]/button");

    public RoleSelectionPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.js     = (JavascriptExecutor) driver;
    }

    public void clickSelectRole()  { scrollAndClick(selectRoleButton); }
    public void selectFirstRole()  { scrollAndClick(firstRoleOption);  }
    public void selectSecondRole() { scrollAndClick(secondRoleOption); }
    public void confirm()          { scrollAndClick(confirmButton);    }


    private void scrollAndClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        try {
            el.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", el);
        }
    }
}