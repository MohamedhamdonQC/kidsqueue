package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.ChildcareCenterPage;
import pages.RegisterPage;
import pages.RoleSelectionPage;
import pages.dashboardPage;
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
            Thread.currentThread().interrupt();
        }
        wait.until(ExpectedConditions.elementToBeClickable(locator));
        try {
            el.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", el);
        }
    }

    private WebElement scrollTo(By locator) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollToElement(el);
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
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

    private WebElement firstVisible(List<WebElement> elements) {
        for (WebElement element : elements) {
            try {
                if (element.isDisplayed() && element.isEnabled()) {
                    return element;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void clickFirstMatching(List<WebElement> elements) {
        WebElement element = firstVisible(elements);
        if (element == null) {
            throw new RuntimeException("No visible element found to click");
        }
        scrollToElement(element);
        try {
            element.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    private void chooseRandomDropdownOption() throws InterruptedException {
        List<WebElement> options = driver.findElements(By.xpath("//div[@role='option' and not(@aria-disabled='true')]"));
        WebElement option = firstVisible(options);
        if (option != null) {
            scrollToElement(option);
            try {
                option.click();
            } catch (Exception e) {
                js.executeScript("arguments[0].click();", option);
            }
        }
        Thread.sleep(1000);
    }

    private void completeInviteToEnrollForm() throws InterruptedException {
        List<WebElement> dropdowns = driver.findElements(By.xpath(
                "//table//tbody//tr[1]//button[@data-slot='dropdown-menu-trigger'] | " +
                        "//button[@data-slot='dropdown-menu-trigger']"));
        WebElement overflowMenu = firstVisible(dropdowns);
        if (overflowMenu == null) {
            throw new RuntimeException("Invite overflow menu was not found");
        }
        scrollToElement(overflowMenu);
        try {
            overflowMenu.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", overflowMenu);
        }
        Thread.sleep(1000);

        WebElement inviteOption = null;
        for (int attempt = 0; attempt < 8 && inviteOption == null; attempt++) {
            List<WebElement> candidates = driver.findElements(By.xpath(
                    "//*[@role='menu' or @data-radix-popper-content-wrapper or contains(@id,'radix')]//*[contains(normalize-space(.),'Invite')]"));
            inviteOption = firstVisible(candidates);
            if (inviteOption == null) {
                candidates = driver.findElements(By.xpath(
                        "//*[contains(normalize-space(.),'Invite') and (self::button or self::div or self::span or self::a)]"));
                inviteOption = firstVisible(candidates);
            }
            if (inviteOption == null) {
                Thread.sleep(500);
            }
        }
        if (inviteOption == null) {
            throw new RuntimeException("Invite menu item was not found");
        }
        scrollToElement(inviteOption);
        try {
            inviteOption.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", inviteOption);
        }
        Thread.sleep(1500);

        List<WebElement> visibleButtons = driver.findElements(By.xpath(
                "//button[contains(@class,'w-full') and contains(@class,'justify-between') and not(@disabled)]"));
        if (visibleButtons.size() >= 3) {
            scrollToElement(visibleButtons.get(0));
            visibleButtons.get(0).click();
            Thread.sleep(1000);
            chooseRandomDropdownOption();

            scrollToElement(visibleButtons.get(1));
            visibleButtons.get(1).click();
            Thread.sleep(1000);
            chooseRandomDropdownOption();

            scrollToElement(visibleButtons.get(2));
            visibleButtons.get(2).click();
            Thread.sleep(1000);
            chooseRandomDropdownOption();
        }

        List<WebElement> textInputs = driver.findElements(By.xpath(
                "//input[not(@type='hidden') and not(@disabled) and " +
                        "(contains(@placeholder,'Date') or contains(@placeholder,'Amount') or @type='date' or @type='number')]"));
        for (WebElement input : textInputs) {
            if (!input.isDisplayed()) {
                continue;
            }
            scrollToElement(input);
            String type = input.getAttribute("type");
            if ("date".equalsIgnoreCase(type)) {
                input.clear();
                input.sendKeys(LocalDate.now().plusDays(new Random().nextInt(30) + 1)
                        .format(DateTimeFormatter.ISO_DATE));
            } else if ("number".equalsIgnoreCase(type)) {
                input.clear();
                input.sendKeys(String.valueOf(100 + new Random().nextInt(900)));
            }
            Thread.sleep(500);
        }

        clickFirstMatching(driver.findElements(By.xpath(
                "//button[normalize-space()='Send Invite' or contains(.,'Send Invite')]")));
        Thread.sleep(2000);
    }

    @Test(priority = 1, description = "Register a new user, add to waitlist, then verify the dashboard view")
    public void accepted_dashboard_flowaccepted_dashboard() throws InterruptedException {

        initWaits();
        hideCookieBannerArtifacts();

        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(BASE_URL + "/register");
        String email = TestDataGenerator.generateEmail("mohamed");
        registerPage.fillFirstName(FIRST_NAME);
        registerPage.fillLastName(LAST_NAME);
        registerPage.fillEmail(email);
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


        Thread.sleep(3000);
        WebElement searchBox = scrollTo(By.id("search"));
        searchBox.sendKeys(SCHOOLS);
        Thread.sleep(3000);


        scrollAndClick(By.xpath("//button[.//div[text()='View Details']]"));


        ChildcareCenterPage childcareCenterPage = new ChildcareCenterPage(driver);
        Thread.sleep(3000);
        childcareCenterPage.waitUntilLoaded();
        childcareCenterPage.addToWatchlist();


        Thread.sleep(3000);
        driver.navigate().to(BASE_URL + "/parent/dashboard");
        Thread.sleep(3000);
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.urlToBe(BASE_URL + "/parent/dashboard"));

        dashboardPage dashboardPage = new dashboardPage(driver);
        dashboardPage.clickWatching();
        Thread.sleep(3000);

        driver.navigate().to("https://staging.dashboard.kidsqueue.softigital.com/login");
        Thread.sleep(3000);

        driver.findElement(By.xpath("//input[@placeholder='Enter your email']"))
                .sendKeys("enas.gamall.salem@gmail.com");
        Thread.sleep(2000);

        driver.findElement(By.xpath("//input[@placeholder='Enter your password']"))
                .sendKeys("12345678");
        Thread.sleep(1200);

        driver.findElement(By.xpath("//button[@type='submit']")).click();
        Thread.sleep(2000);


        driver.findElement(By.xpath("//input[@class='disabled:cursor-not-allowed']"))
                .sendKeys("793421");
        Thread.sleep(1200);

        driver.findElement(By.xpath("//button[@type='submit']")).click();
        Thread.sleep(2000);


        driver.findElement(By.xpath("//button[normalize-space()='Enrollment']")).click();
        Thread.sleep(2000);

        driver.findElement(By.linkText("Waitlist")).click();
        Thread.sleep(2000);


        WebElement addToWaitlistBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Add to Waitlist']")));
        addToWaitlistBtn.click();
        Thread.sleep(2000);

        WebElement parentDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'w-full') and contains(@class,'justify-between') and contains(@class,'font-normal')]")));
        parentDropdown.click();
        Thread.sleep(1500);

        WebElement parentSearchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[contains(@id,'radix')]")));
        parentSearchInput.clear();
        parentSearchInput.sendKeys(email);
        Thread.sleep(2000);

        WebElement parentOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@role='option'][1]")));
        parentOption.click();
        Thread.sleep(1500);

        WebElement childDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//button[contains(@class,'w-full') and contains(@class,'border')])[2]")));
        childDropdown.click();
        Thread.sleep(1500);

        List<WebElement> childOptions = driver.findElements(
                By.xpath("//div[@role='option'] | //div[contains(@class,'cursor-pointer') and not(contains(.,'@'))]"));
        if (!childOptions.isEmpty()) {
            childOptions.get(0).click();
        }
        Thread.sleep(1500);


        List<WebElement> allDropdowns = driver.findElements(
                By.xpath("//button[contains(@class,'w-full') and contains(@class,'border') and contains(@class,'justify-between')]"));


        if (allDropdowns.size() >= 3) {
            allDropdowns.get(2).click();
            Thread.sleep(1500);
            List<WebElement> roomOptions = driver.findElements(By.xpath("//div[@role='option']"));
            if (!roomOptions.isEmpty()) {
                int randomIndex = new Random().nextInt(roomOptions.size());
                roomOptions.get(randomIndex).click();
            }
            Thread.sleep(1500);
        }


        allDropdowns = driver.findElements(
                By.xpath("//button[contains(@class,'w-full') and contains(@class,'border') and contains(@class,'justify-between')]"));


        if (allDropdowns.size() >= 4) {
            allDropdowns.get(3).click();
            Thread.sleep(1500);
            List<WebElement> programOptions = driver.findElements(By.xpath("//div[@role='option']"));
            if (!programOptions.isEmpty()) {
                int randomIndex = new Random().nextInt(programOptions.size());
                programOptions.get(randomIndex).click();
            }
            Thread.sleep(1500);
        }

        List<WebElement> dateInputs = driver.findElements(By.xpath("//input[@type='date']"));
        if (!dateInputs.isEmpty()) {

            LocalDate randomDate = LocalDate.now().plusDays(new Random().nextInt(30) + 1);
            String formattedDate = randomDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            dateInputs.get(0).clear();
            dateInputs.get(0).sendKeys(formattedDate);
            Thread.sleep(1000);
        }
        Thread.sleep(3000);
        driver.findElement(By.id("high-trigger")).click();
        Thread.sleep(3000);

        WebElement button = driver.findElement(By.id("waitlist-submit-trigger"));
        while (!button.isDisplayed()) {
            js.executeScript("window.scrollBy(0, 300);");
            Thread.sleep(500);
            button = driver.findElement(By.id("waitlist-submit-trigger"));
        }
        button.click();
        Thread.sleep(3000);
        completeInviteToEnrollForm();
        Thread.sleep(3000);
    }
}
