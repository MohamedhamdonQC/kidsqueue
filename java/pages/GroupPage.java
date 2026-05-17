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

public class GroupPage {
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration CLICK_TIMEOUT = Duration.ofSeconds(8);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    private final By createGroup = By.xpath("//div[@class='flex items-center justify-between']//button[@type='button']//div[1]");
    private final By groupName = By.xpath("//input[@placeholder='Enter group name...']");
    private final By description = By.cssSelector("textarea[placeholder='Describe the purpose of this group...']");
    private final By[] membership = new By[] {
            By.xpath("//form//*[self::button or self::div][contains(normalize-space(.), 'Membership') or contains(normalize-space(.), 'membership')]"),
            By.xpath("//form//*[contains(normalize-space(.), 'Member')]")
    };
    private final By autoApprovedToggle = By.xpath(
            "//label[contains(normalize-space(.), 'Auto') and contains(normalize-space(.), 'Approv')]" +
                    " | //button[@role='switch'][contains(@aria-label, 'auto') or contains(@id, 'auto')]" +
                    " | //form//*[@role='switch']"
    );

    private final By postFileInput = By.xpath(
            "//div[contains(@class,'dialog') or contains(@class,'modal') or contains(@role,'dialog')]" +
                    "//input[@type='file']"
    );

    private final By[] anyone = new By[] {
            By.xpath("//form//*[self::label or self::button or self::div][contains(normalize-space(.), 'Anyone') or contains(normalize-space(.), 'anyone')]"),
            By.xpath("//form//*[contains(normalize-space(.), 'Public')]")
    };
    private final By submit = By.cssSelector("button[type='submit']");
    private final By imageInput = By.cssSelector("input[type='file']");
    By Details = By.xpath("//button[contains(.,'Details')]");

    private final By post = By.xpath("//button[normalize-space()='Add Post']") ;

    private final By Title = By.xpath("//input[@id='title']") ;
    private final By Contect = By.xpath("//textarea[@id='content']") ;

    private final By UploadPost = By.id("add-post-trigger") ;

    private final By PostPage = By.id("groups-tab-messages") ;

    public GroupPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        this.js = (JavascriptExecutor) driver;
    }

    public void open(String url) { driver.get(url); }

    public void CreateGroup() { scrollAndClick(createGroup); }

    public void nameofgroup(String value) { type(groupName, value); }
    public void Title(String value) { type(Title, value); }
    public void Contect(String value) { type(Contect, value); }
    public void Description(String value) { type(description, value); }

    public void MemberShip() { scrollAndClick(membership); }

    public void Viewdetails() { scrollAndClick(Details); }

    public void Anyone() { scrollAndClick(anyone); }
    public void addpost() { scrollAndClick(post); }

    public void ClicktoCreateGroup() { scrollAndClick(submit); }
    public void CLickonUploadPost() {scrollAndClick(UploadPost);}

    public void PostPage() { scrollAndClick(PostPage); }

    public void Image(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }

        File file = new File(imagePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Image file not found: " + file.getAbsolutePath());
        }

        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(imageInput));
        js.executeScript(
                "arguments[0].style.display='block';" +
                        "arguments[0].style.visibility='visible';" +
                        "arguments[0].style.opacity='1';",
                fileInput
        );
        fileInput.sendKeys(file.getAbsolutePath());
    }

    private void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    private void scrollAndClick(By... locators) {
        TimeoutException lastException = null;

        for (By locator : locators) {
            try {
                WebElement el = new WebDriverWait(driver, CLICK_TIMEOUT)
                        .until(ExpectedConditions.elementToBeClickable(locator));
                js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
                try {
                    el.click();
                } catch (ElementClickInterceptedException e) {
                    js.executeScript("arguments[0].click();", el);
                }
                return;
            } catch (TimeoutException e) {
                lastException = e;
            }
        }

        if (lastException != null) {
            throw lastException;
        }
    }
    public void scrollToBottom() throws InterruptedException {
        wait.until(ExpectedConditions.jsReturnsValue(
                "return document.readyState === 'complete'"
        ));

        
        long totalHeight = (long) js.executeScript("return document.body.scrollHeight");
        long currentPos = 0;
        long step = totalHeight / 5; 

        while (currentPos < totalHeight) {
            currentPos += step;
            js.executeScript("window.scrollTo(0, " + currentPos + ");");
            Thread.sleep(500);
        }

        
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(1000);
    }
}
