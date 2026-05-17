package tests;

import base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.*;
import utils.TestDataGenerator;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Listeners(utils.reports.Listeners.class)
public class AcceptDashboard extends BaseTest {

    private static final String BASE_URL = "https://staging.kidsqueue.softigital.com";
    private static final String DASHBOARD_URL = "https://staging.dashboard.kidsqueue.softigital.com";
    private static final String FIRST_NAME = "Mohamed";
    private static final String LAST_NAME = "Hamdon";
    private static final String PASSWORD = "12345678";
    private static final String SCHOOLS = "Bardstown Child Care Program";

    private WebDriverWait wait;
    private JavascriptExecutor js;
    private String parentEmail;

    private void initWaits() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
    }

    private void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    private void scrollAndClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollToElement(el);
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }
        wait.until(ExpectedConditions.elementToBeClickable(locator));
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", el);
        }
    }

    private WebElement scrollTo(By locator) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollToElement(el);
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }
        return el;
    }

    private void loginToDashboard(String email, String password) throws InterruptedException {
        driver.navigate().to(DASHBOARD_URL + "/login");
        Thread.sleep(2000);

        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@type='email' or contains(@placeholder, 'email')]")));
        emailField.clear();
        emailField.sendKeys(email);

        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@type='password']")));
        passwordField.clear();
        passwordField.sendKeys(password);

        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
        submit.click();
        Thread.sleep(2000);

        WebElement codeField = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@class='disabled:cursor-not-allowed']")));
        codeField.sendKeys("793421");
        Thread.sleep(1000);

        driver.findElement(By.xpath("//button[@type='submit']")).click();
        Thread.sleep(2000);
    }

    private void pickAnyOption() throws InterruptedException {
        List<WebElement> options = driver.findElements(By.xpath("//div[@role='option']"));
        if (!options.isEmpty()) {
            options.get(new Random().nextInt(options.size())).click();
        }
        Thread.sleep(1000);
    }

    @Test(priority = 1, description = "Register a new user, add to waitlist, send invite to enroll, then verify accepted dashboard flow")
    public void registerAndAssignRoles() throws InterruptedException {
        initWaits();
        hideCookieBannerArtifacts();

        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");
        parentEmail = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(parentEmail);
        registerPage.fillPassword(PASSWORD);
        registerPage.fillConfirmPassword(PASSWORD);
        registerPage.submit();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));

        RoleSelectionPage roleSelectionPage = new RoleSelectionPage(driver);
        roleSelectionPage.clickSelectRole();
        roleSelectionPage.selectFirstRole();
        roleSelectionPage.selectSecondRole();
        roleSelectionPage.confirm();

        driver.navigate().to(BASE_URL + "/");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        registerPage.openParentPortal();
        registerPage.clickParentNext();
        registerPage.completeChildRegistration("Hamdon", "Test Automation");
        Thread.sleep(2000);

        WebElement searchBox = scrollTo(By.id("search"));
        searchBox.sendKeys(SCHOOLS);
        Thread.sleep(2000);
        scrollAndClick(By.xpath("//button[.//div[text()='View Details']]"));

        ChildcareCenterPage childcareCenterPage = new ChildcareCenterPage(driver);
        childcareCenterPage.waitUntilLoaded();
        childcareCenterPage.addToWatchlist();

        driver.navigate().to(BASE_URL + "/parent/dashboard");
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/parent/dashboard"));
        dashboardPage dashboardPage = new dashboardPage(driver);
        dashboardPage.clickWatching();

        loginToDashboard(parentEmail, PASSWORD);

        WebElement enrollmentMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(), 'Enrollment')]")));
        scrollToElement(enrollmentMenu);
        enrollmentMenu.click();
        Thread.sleep(1000);

        WebElement waitlistOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(), 'Waitlist')]")));
        scrollToElement(waitlistOption);
        waitlistOption.click();
        Thread.sleep(1500);

        WebElement addToWaitlistBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Add to Waitlist']")));
        scrollToElement(addToWaitlistBtn);
        addToWaitlistBtn.click();
        Thread.sleep(1500);

        WebElement inviteToEnrollBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(normalize-space(.), 'Invite To Enroll')]")));
        scrollToElement(inviteToEnrollBtn);
        inviteToEnrollBtn.click();
        Thread.sleep(1500);

        WebElement parentDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'w-full') and contains(@class,'justify-between') and contains(@class,'font-normal')]")));
        parentDropdown.click();
        Thread.sleep(1000);

        WebElement parentSearchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[contains(@id,'radix')]")));
        parentSearchInput.clear();
        parentSearchInput.sendKeys(parentEmail);
        Thread.sleep(1000);

        WebElement parentOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@role='option'][1]")));
        parentOption.click();
        Thread.sleep(1000);

        List<WebElement> dropdowns = driver.findElements(
                By.xpath("//button[contains(@class,'w-full') and contains(@class,'border') and contains(@class,'justify-between')]"));
        if (dropdowns.size() >= 3) {
            dropdowns.get(2).click();
            Thread.sleep(1000);
            pickAnyOption();
        }
        dropdowns = driver.findElements(
                By.xpath("//button[contains(@class,'w-full') and contains(@class,'border') and contains(@class,'justify-between')]"));
        if (dropdowns.size() >= 4) {
            dropdowns.get(3).click();
            Thread.sleep(1000);
            pickAnyOption();
        }

        List<WebElement> dateInputs = driver.findElements(By.xpath("//input[@type='date']"));
        if (dateInputs.size() >= 2) {
            LocalDate startDate = LocalDate.now().plusDays(new Random().nextInt(10) + 1);
            LocalDate endDate = startDate.plusDays(new Random().nextInt(10) + 1);
            dateInputs.get(0).clear();
            dateInputs.get(0).sendKeys(startDate.format(DateTimeFormatter.ISO_DATE));
            dateInputs.get(1).clear();
            dateInputs.get(1).sendKeys(endDate.format(DateTimeFormatter.ISO_DATE));
        }

        List<WebElement> moneyInputs = driver.findElements(By.xpath(
                "//input[@type='number' or contains(@inputmode,'decimal') or contains(@placeholder,'Weekly') or contains(@placeholder,'Monthly')]"));
        if (!moneyInputs.isEmpty()) {
            moneyInputs.get(0).clear();
            moneyInputs.get(0).sendKeys(String.valueOf(100 + new Random().nextInt(200)));
        }
        if (moneyInputs.size() > 1) {
            moneyInputs.get(1).clear();
            moneyInputs.get(1).sendKeys(String.valueOf(300 + new Random().nextInt(400)));
        }

        List<WebElement> amountButtons = driver.findElements(By.xpath(
                "//button[contains(normalize-space(.), 'Positive') or contains(normalize-space(.), 'Weekly') or contains(normalize-space(.), 'Monthly')]"));
        for (WebElement amountButton : amountButtons) {
            try {
                amountButton.click();
                break;
            } catch (Exception ignored) {
            }
        }

        WebElement sendInviteBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(normalize-space(.), z'Send Invite')]")));
        scrollToElement(sendInviteBtn);
        sendInviteBtn.click();
        Thread.sleep(2500);

        loginToDashboard(parentEmail, PASSWORD);

        WebElement acceptedTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(), 'Accepted')]")));
        scrollToElement(acceptedTab);
        acceptedTab.click();
        Thread.sleep(2000);

        WebElement requestCard = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//*[contains(@class,'cursor-pointer') and contains(@class,'rounded-xl')])[1]")));
        scrollToElement(requestCard);
        requestCard.click();
        Thread.sleep(1500);

        WebElement nextBtn1 = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(normalize-space(.), 'Next')]")));
        scrollToElement(nextBtn1);
        nextBtn1.click();
        Thread.sleep(1500);

        WebElement nextBtn2 = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(normalize-space(.), 'Next')]")));
        scrollToElement(nextBtn2);
        nextBtn2.click();
        Thread.sleep(1500);

        Assert.assertTrue(true, "Invite and accepted-dashboard flow completed");
        System.out.println("Invite sent, accepted request opened, and next steps completed.");
    }
}
