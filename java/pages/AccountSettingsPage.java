package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AccountSettingsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    public AccountSettingsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.js = (JavascriptExecutor) driver;
    }

    

    public void openAccountPage(String baseUrl) {
        driver.get(baseUrl + "/parent/account");
    }

    public void openSettingsTab() {
        clickByVisibleText("Settings");
    }

    

    public void updateFirstAndLastName(String firstName, String lastName) {
        setInputValue(firstNameLocator(), firstName);
        setInputValue(lastNameLocator(), lastName);
    }

    public void saveChanges() {
        clickSaveChanges();
    }

    public void openSettingsSection() {
        clickById("account-tab-settings");
    }

    public void setEmail(String email) {
        setInputValue(By.id("email"), email);
    }

    public void openAddChildDialog() {
        clickAddChildButton();
    }

    public void fillChildFirstName(String value) {
        setInputValue(By.id("firstName"), value);
    }

    public void fillChildLastName(String value) {
        setInputValue(By.id("lastName"), value);
    }

    public void openBirthMonth() {
        clickById("birth_month-trigger");
    }

    public void openBirthYear() {
        clickById("birth_year-trigger");
    }

    public void openRelationDropdown() {
        clickById("relation-trigger");
    }

    public void confirmAddChild() {
        clickById("add-child-confirm-trigger");
    }

    public void chooseOptionByText(String optionText) {
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@role='option'][normalize-space()='" + optionText + "']")
        ));
        js.executeScript("arguments[0].click();", option);
    }

    public void clickChildCheckbox() {
        clickById("bt");
    }

    

    private By firstNameLocator() {
        return firstMatch(
                By.id("first_name")
        );
    }

    private By lastNameLocator() {
        return firstMatch(
                By.id("last_name")
        );
    }

    

    private void setInputValue(By locator, String value) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

        js.executeScript("arguments[0].scrollIntoView({block:'center'});", input);

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
    }

    private void clickByVisibleText(String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[self::button or self::a or self::div][contains(normalize-space(),'" + text + "')]")
        ));

        js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);

        try {
            element.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    private void clickById(String id) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.id(id)));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        try {
            element.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    private void clickAddChildButton() {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-child-open-trigger")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        wait.until(ExpectedConditions.elementToBeClickable(element));
        try {
            element.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    

    private void clickSaveChanges() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

        WebElement button = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[contains(normalize-space(),'Save Changes') or .//*[contains(normalize-space(),'Save Changes')]]")
        ));

        js.executeScript("arguments[0].scrollIntoView({block:'end', inline:'nearest'});", button);
        js.executeScript("window.scrollBy(0, 250);");

        wait.until(ExpectedConditions.elementToBeClickable(button));

        try {
            button.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", button);
        }
    }

    

    private By firstMatch(By... locators) {
        for (By locator : locators) {
            if (!driver.findElements(locator).isEmpty()) {
                return locator;
            }
        }
        throw new RuntimeException("Could not resolve a matching locator");
    }
}
