package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Random;

public class ParentPortalPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    
    private final By FirstNameChildname = By.id("children.0.first_name");
    private final By LastNameChildname  = By.id("children.0.last_name");
    private final By BirthMonth        = By.id("children.0.birth_month-trigger");
    private final By BirthYear         = By.id("children.0.birth_year-trigger");
    private final By RelationtoChild   = By.id("children.0.relation_to_child-trigger");
    private final By Both              = By.id("bt-0");
    private final By childcare         = By.id("children.0.timeline-trigger");

    private static final String[] TIMELINES = {" 3 Months"};
    private static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };
    private final Random random = new Random();

    public ParentPortalPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.js     = (JavascriptExecutor) driver;
    }

    

    public void clickChildrenInfoNext() throws InterruptedException {
        
        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
        Thread.sleep(600);

        
        WebElement nextBtn = (WebElement) js.executeScript(
                "return Array.from(document.querySelectorAll('button')).find(function(btn) {" +
                        "  var text = btn.innerText.trim();" +
                        "  return text.includes('Children Info')" +
                        "      && !btn.disabled" +
                        "      && !btn.getAttribute('aria-haspopup');" +
                        "});"
        );

        if (nextBtn == null) {
            String allBtns = (String) js.executeScript(
                    "return Array.from(document.querySelectorAll('button'))" +
                            ".map(function(b){ return '[' + b.innerText.trim() + ']'; }).join(', ');"
            );
            throw new RuntimeException("'Children Info' button not found! Buttons: " + allBtns);
        }

        js.executeScript("arguments[0].click();", nextBtn);
        Thread.sleep(500);
    }

    

    public void clickSkip() throws InterruptedException {
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            Thread.sleep(500);
        } catch (Exception ignored) {}

        js.executeScript(
                "var el = document.querySelector('[id*=\"zsiq\"]');" +
                        "if(el){ el.style.display='none'; }"
        );

        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));

        WebElement skipBtn = (WebElement) js.executeScript(
                "return Array.from(document.querySelectorAll('button')).find(function(btn) {" +
                        "  return btn.innerText.trim() === 'Skip' && !btn.getAttribute('aria-haspopup');" +
                        "});"
        );

        if (skipBtn == null) {
            String allBtns = (String) js.executeScript(
                    "return Array.from(document.querySelectorAll('button'))" +
                            ".map(function(b){ return '[' + b.innerText.trim() + ']'; }).join(', ');"
            );
            throw new RuntimeException("Skip button not found! Buttons: " + allBtns);
        }

        js.executeScript("arguments[0].click();", skipBtn);
    }

    

    public void fillChildFirstName(String value) {
        type(FirstNameChildname, value);
    }

    public void fillChildLastName(String value) {
        type(LastNameChildname, value);
    }

    public void selectBirthMonth(String month) throws InterruptedException {
        selectDropdownOption(BirthMonth, month);
    }

    public void selectBirthYear(String year) throws InterruptedException {
        selectDropdownOption(BirthYear, year);
    }

    public void selectRelationToChild(String relation) throws InterruptedException {
        selectDropdownOption(RelationtoChild, relation);
    }

    public void selectChildBirthAndRelation() throws InterruptedException {
        selectDropdownOption(BirthMonth, MONTHS[random.nextInt(MONTHS.length)]);
        selectDropdownOption(BirthYear, String.valueOf(getRandomYear()));
        selectDropdownOption(RelationtoChild, "Father");
    }

    public void clickBoth() throws InterruptedException {
        WebElement bothBtn = wait.until(ExpectedConditions.elementToBeClickable(Both));
        js.executeScript("arguments[0].click();", bothBtn);
        Thread.sleep(300);
    }

    public void selectChildcareTimeline() throws InterruptedException {
        String timeline = TIMELINES[random.nextInt(TIMELINES.length)];
        selectDropdownOption(childcare, timeline);
    }

    public void clickNextStep() throws InterruptedException {
        WebElement nextBtn = (WebElement) js.executeScript(
                "return Array.from(document.querySelectorAll('button')).find(function(btn) {" +
                        "  var text = btn.innerText.trim();" +
                        "  return (text.includes('Next') || text.includes('Center Preferences') || text.includes('Continue'))" +
                        "      && !btn.getAttribute('aria-haspopup');" +
                        "});"
        );

        if (nextBtn == null) throw new RuntimeException("Next step button not found!");

        js.executeScript("arguments[0].click();", nextBtn);
        Thread.sleep(500);
    }

    

    private void selectDropdownOption(By triggerLocator, String optionText)
            throws InterruptedException {
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(triggerLocator));
        js.executeScript("arguments[0].click();", trigger);
        Thread.sleep(400);

        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@role='option'][normalize-space()='" + optionText + "']")
        ));
        js.executeScript("arguments[0].click();", option);
        Thread.sleep(300);
    }

    private void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    private int getRandomYear() {
        return 2010 + random.nextInt(16);
    }
}
