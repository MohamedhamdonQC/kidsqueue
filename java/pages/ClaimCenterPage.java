package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.TestDataGenerator;

import java.time.Duration;
import java.util.List;

public class ClaimCenterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    private final By searchInput = By.cssSelector("#search-schools input, input[type='search'], input[placeholder*='Search']");
    private final By resultItem = By.cssSelector("#search-schools div.cursor-pointer, #search-schools li, a[href*='school']");
    private final By claimCenterButton = By.xpath(
            "//button[contains(normalize-space(.),'Claim This Center') or contains(normalize-space(.),'Claim this Center')]"
    );
    private final By submitButton = By.xpath(
            "//button[@type='submit' or contains(normalize-space(.),'Submit') or contains(normalize-space(.),'Continue')]"
    );
    private final By continueToReviewButton = By.id("claim-center-continue-to-review-trigger");
    private final By acceptTermsCheckbox = By.id("accept_terms");
    private final By confirmAccuracyCheckbox = By.id("confirm_accuracy");
    private final By submitClaimButton = By.id("claim-center-submit-claim-trigger");

    public ClaimCenterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.js = (JavascriptExecutor) driver;
    }

    public void open(String url) {
        driver.get(url);
    }

    public void searchForSchool(String schoolName) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        input.clear();
        input.sendKeys(schoolName);
    }

    public void selectFirstSchoolResult() {
        WebElement item = wait.until(ExpectedConditions.elementToBeClickable(resultItem));
        click(item);
    }

    public void clickClaimThisCenter() {
        click(wait.until(ExpectedConditions.elementToBeClickable(claimCenterButton)));
    }

    public void fillRandomClaimForm() {
        fillVisibleRequiredTextInputs();
        fillVisibleRequiredTextAreas();
        fillVisibleRequiredSelects();
        toggleVisibleRequiredOptions();
    }

    public void submit() {
        clickAfterScrollDown(wait.until(ExpectedConditions.presenceOfElementLocated(submitButton)));
    }

    public void clickContinueToReview() {
        clickAfterScrollDown(wait.until(ExpectedConditions.presenceOfElementLocated(continueToReviewButton)));
    }

    public void acceptTerms() {
        clickAfterScrollDown(wait.until(ExpectedConditions.presenceOfElementLocated(acceptTermsCheckbox)));
    }

    public void confirmAccuracy() {
        clickAfterScrollDown(wait.until(ExpectedConditions.presenceOfElementLocated(confirmAccuracyCheckbox)));
    }

    public void submitClaim() {
        clickAfterScrollDown(wait.until(ExpectedConditions.presenceOfElementLocated(submitClaimButton)));
    }

    private void fillVisibleRequiredTextInputs() {
        List<WebElement> inputs = driver.findElements(By.cssSelector("input"));
        for (WebElement input : inputs) {
            if (!isVisibleAndEnabled(input)) {
                continue;
            }
            if (!isRequired(input)) {
                continue;
            }

            String type = safeAttribute(input, "type").toLowerCase();
            if (type.equals("hidden") || type.equals("submit") || type.equals("button") || type.equals("checkbox") || type.equals("radio") || type.equals("file")) {
                continue;
            }

            String value;
            switch (type) {
                case "email":
                    value = TestDataGenerator.generateEmail("claim");
                    break;
                case "tel":
                case "number":
                    value = TestDataGenerator.randomDigits(10);
                    break;
                case "date":
                    value = "2026-05-06";
                    break;
                default:
                    value = TestDataGenerator.randomSentence(2).replace(".", "");
                    break;
            }

            setValue(input, value);
        }
    }

    private void fillVisibleRequiredTextAreas() {
        List<WebElement> textareas = driver.findElements(By.cssSelector("textarea"));
        for (WebElement textarea : textareas) {
            if (!isVisibleAndEnabled(textarea)) {
                continue;
            }
            if (!isRequired(textarea)) {
                continue;
            }
            setValue(textarea, TestDataGenerator.randomSentence(8));
        }
    }

    private void fillVisibleRequiredSelects() {
        List<WebElement> selects = driver.findElements(By.cssSelector("select"));
        for (WebElement selectElement : selects) {
            if (!isVisibleAndEnabled(selectElement)) {
                continue;
            }
            if (!isRequired(selectElement)) {
                continue;
            }
            try {
                Select select = new Select(selectElement);
                if (select.getOptions().size() > 1) {
                    select.selectByIndex(1);
                }
            } catch (Exception ignored) {
                // Leave custom selects alone.
            }
        }
    }

    private void toggleVisibleRequiredOptions() {
        List<WebElement> controls = driver.findElements(By.cssSelector("input[type='checkbox'], input[type='radio']"));
        for (WebElement control : controls) {
            if (!isVisibleAndEnabled(control)) {
                continue;
            }
            if (!isRequired(control)) {
                continue;
            }
            try {
                if (!control.isSelected()) {
                    click(control);
                }
            } catch (StaleElementReferenceException ignored) {
            }
        }
    }

    private void setValue(WebElement element, String value) {
        click(element);
        element.clear();
        element.sendKeys(value);
    }

    private void click(WebElement element) {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        js.executeScript("arguments[0].scrollIntoView({block:'end', inline:'nearest'});", element);
        try {
            element.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    private void clickAfterScrollDown(WebElement element) {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        js.executeScript("arguments[0].scrollIntoView({block:'end', inline:'nearest'});", element);
        wait.until(ExpectedConditions.elementToBeClickable(element));
        try {
            element.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    private boolean isVisibleAndEnabled(WebElement element) {
        try {
            return element.isDisplayed() && element.isEnabled();
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    private boolean isRequired(WebElement element) {
        String required = safeAttribute(element, "required");
        String ariaRequired = safeAttribute(element, "aria-required");
        return "true".equalsIgnoreCase(required)
                || "required".equalsIgnoreCase(required)
                || "true".equalsIgnoreCase(ariaRequired);
    }

    private String safeAttribute(WebElement element, String name) {
        try {
            String value = element.getAttribute(name);
            return value == null ? "" : value;
        } catch (NoSuchElementException e) {
            return "";
        }
    }
}
