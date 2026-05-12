package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

public class ClaimProcessPage {

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(25);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    private final By documentsTab = By.id("account-tab-documents");
    private final By documentsTabFallback = By.xpath("//*[normalize-space()='Documents' or contains(normalize-space(),'Documents')]");
    private final By uploadCheckbox = By.xpath("//button[@role='checkbox' or @aria-checked]");
    private final By addOtherDocumentButton = By.xpath("//button[.//div[normalize-space()='Add Other Document'] or contains(normalize-space(),'Add Other Document')]");
    private final By documentNameInput = By.id("documentName");
    private final By fileInput = By.id("file-trigger");
    private final By nextButton = By.xpath("//button[contains(normalize-space(.),'Next') or contains(normalize-space(.),'Continue')]");
    private final By submitButton = By.xpath("//button[contains(normalize-space(.),'Submit') or contains(normalize-space(.),'Review')]");
    private final By errorMessage = By.xpath("//*[contains(normalize-space(.),'didn') and contains(normalize-space(.),'upload')] | //*[contains(normalize-space(.),'upload documents')]");
    private final By successState = By.xpath("//*[contains(normalize-space(.),'submitted') or contains(normalize-space(.),'success') or contains(normalize-space(.),'claim')] ");

    public ClaimProcessPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        this.js = (JavascriptExecutor) driver;
    }

    public void open(String url) {
        driver.get(url);
    }

    public void openDocumentsTab() {
        clickAny(documentsTab, documentsTabFallback);
    }

    public void selectUploadCheckbox() {
        click(uploadCheckbox);
    }

    public void clickAddOtherDocument() {
        click(addOtherDocumentButton);
    }

    public void fillDocumentName(String value) {
        type(documentNameInput, value);
    }

    public void uploadFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file.getAbsolutePath());
        }

        WebElement upload = wait.until(ExpectedConditions.presenceOfElementLocated(fileInput));
        js.executeScript(
                "arguments[0].style.display='block';" +
                        "arguments[0].style.visibility='visible';" +
                        "arguments[0].style.opacity='1';",
                upload
        );
        upload.sendKeys(file.getAbsolutePath());
    }

    public void clickNext() {
        click(nextButton);
    }

    public void clickSubmit() {
        click(submitButton);
    }

    public boolean isValidationErrorVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isSuccessStateVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(successState)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void type(By locator, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        el.clear();
        el.sendKeys(value);
    }

    private void clickAny(By... locators) {
        TimeoutException last = null;
        for (By locator : locators) {
            try {
                click(locator);
                return;
            } catch (TimeoutException e) {
                last = e;
            }
        }
        if (last != null) {
            throw last;
        }
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
