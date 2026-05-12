package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RequestFundsPage {

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(25);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    private final By openRequestFundsButton = By.xpath(
            "//button[contains(normalize-space(.),'Request Funds') or contains(.,'Request Funds')]"
    );
    private final By modalDialog = By.xpath(
            "//*[@role='dialog' or contains(@class,'dialog') or contains(@class,'modal')]"
    );
    private final By recipientEmailInput = By.xpath(
            "//input[@type='email' or contains(translate(@name,'EMAIL','email'),'email') or " +
                    "contains(translate(@placeholder,'EMAIL','email'),'email')]"
    );
    private final By amountInput = By.xpath(
            "//input[@type='number' or contains(translate(@name,'AMOUNT','amount'),'amount') or " +
                    "contains(translate(@placeholder,'AMOUNT','amount'),'amount')]"
    );
    private final By notesInput = By.xpath(
            "//textarea | //input[contains(translate(@name,'NOTE','note'),'note')] | " +
                    "//input[contains(translate(@placeholder,'NOTE','note'),'note')]"
    );
    private final By childDropdownTrigger = By.id("child_id-trigger");
    private final By childOptionList = By.xpath("//*[@role='option' or @role='listbox' or @data-slot='select-content']");
    private final By noResultsState = By.xpath(
            "//*[contains(normalize-space(.),'No results found') or contains(normalize-space(.),'No results')]"
    );
    private final By addNewChildButton = By.xpath(
            "//button[contains(normalize-space(.),'+ Add New Child') or contains(normalize-space(.),'Add New Child')]"
    );
    private final By createRequestButton = By.xpath(
            "//button[@type='submit' and contains(normalize-space(.),'Create Request')] | " +
                    "//button[contains(normalize-space(.),'Create Request')]"
    );
    private final By successToast = By.xpath(
            "//*[contains(normalize-space(.),'Fund request sent successfully')]"
    );
    private final By pendingState = By.xpath(
            "//*[normalize-space(.)='Pending' or contains(normalize-space(.),'Pending')]"
    );
    public RequestFundsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        this.js = (JavascriptExecutor) driver;
    }

    public void open(String url) {
        driver.get(url);
    }

    public void openRequestFundsModal() {
        click(openRequestFundsButton);
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalDialog));
    }

    public void fillRecipientEmail(String email) {
        type(recipientEmailInput, email);
    }

    public void fillAmount(String amount) {
        type(amountInput, amount);
    }

    public void fillNotes(String notes) {
        type(notesInput, notes);
    }

    public void openChildDropdown() {
        click(childDropdownTrigger);
        wait.until(ExpectedConditions.visibilityOfElementLocated(childOptionList));
    }

    public boolean isNoChildrenEmptyStateVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(noResultsState)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isAddNewChildVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(addNewChildButton)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void clickAddNewChild() {
        click(addNewChildButton);
    }

    public void submitRequest() {
        click(createRequestButton);
    }

    public boolean isSuccessToastVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(successToast)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isModalClosed() {
        return driver.findElements(modalDialog).isEmpty();
    }

    public boolean isPendingVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(pendingState)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isRequestVisibleForEmail(String email) {
        By row = By.xpath(
                "//*[contains(normalize-space(.),'" + email + "')]"
        );
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(row)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getEmailFieldValue() {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(recipientEmailInput));
        return input.getAttribute("value");
    }

    private void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        el.click();
        el.clear();
        el.sendKeys(text);
    }

    private void click(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", el);
        }
    }
}
