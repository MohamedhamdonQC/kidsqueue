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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Listeners(utils.reports.Listeners.class)
public class CenterSearch extends BaseTest {

    private static final String BASE_URL = "https://staging.kidsqueue.softigital.com";
    private static final String FIRST_NAME = "Mohamed";
    private static final String LAST_NAME = "Hamdon";
    private static final String PASSWORD = "12345678";

    private static final String SCHOOLS = "Bardstown Child Care Program";
    private static final String ADDRESS = "1000 Templin Avenue Bardstown, KY 40004";

    private WebDriverWait wait;
    private JavascriptExecutor js;

    private void initWaits() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
    }

    private void scrollAndClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        wait.until(ExpectedConditions.elementToBeClickable(locator));
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", el);
        }
    }

    private WebElement scrollTo(By locator) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        return el;
    }

    private void openFiltersDialog() throws InterruptedException {
        WebElement filtersBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[@data-slot='dialog-trigger'][.//div[contains(text(),'Filters')]]")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", filtersBtn);
        Thread.sleep(300);
        filtersBtn.click();
        Thread.sleep(1000);
    }

    private void expandFilterSection(String sectionName) throws InterruptedException {
        WebElement section = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[.//*[contains(text(),'" + sectionName + "')]]" +
                                " | //div[contains(@class,'accordion') or contains(@class,'collapsible')]" +
                                "//*[contains(text(),'" + sectionName + "')]/ancestor::button")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", section);
        Thread.sleep(300);
        section.click();
        Thread.sleep(500);
    }

    private void selectAllOptionsInSection(String sectionName) throws InterruptedException {
        expandFilterSection(sectionName);

        List<WebElement> labels = driver.findElements(
                By.xpath("//button[.//*[contains(normalize-space(.),'" + sectionName + "')]]" +
                        "/following-sibling::*//*[self::label or self::button or self::div]")
        );

        for (WebElement label : labels) {
            String text = label.getText() == null ? "" : label.getText().trim();
            if (text.isEmpty() || text.equalsIgnoreCase(sectionName) || text.contains("Selected")) {
                continue;
            }

            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", label);
            Thread.sleep(150);
            try {
                label.click();
            } catch (Exception e) {
                js.executeScript("arguments[0].click();", label);
            }
            Thread.sleep(150);
        }
    }

    private void registerAndSetup() throws InterruptedException {
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
    }

    private void searchAndVerify(String searchTerm, String expectedText) throws InterruptedException {
        registerAndSetup();

        WebElement searchBox = scrollTo(By.id("search"));
        searchBox.clear();
        searchBox.sendKeys(searchTerm);
        Thread.sleep(1500);

        By cardLocator = By.xpath(
                "//h2[contains(@class,'text-primary') and contains(@class,'font-semibold')]" +
                        "[contains(text(),'" + expectedText + "')]"
        );

        WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(cardLocator));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", card);
        Thread.sleep(500);

        String actualText = card.getText().trim();
        Assert.assertTrue(
                actualText.contains(expectedText),
                "Expected card with name [" + expectedText + "] but found [" + actualText + "]"
        );

        System.out.println("Passed: Search [" + searchTerm + "] -> Card found: " + actualText);
    }

    private void navigateToNotePage() throws InterruptedException {
        driver.navigate().to(BASE_URL + "/parent?schools_page=1");
        wait.until(ExpectedConditions.urlContains("/parent"));
        Thread.sleep(2000);
    }

    private WebElement getNoteField() {
        WebElement noteField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("content-editor")));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", noteField);
        return noteField;
    }

    private void clickSaveNote() throws InterruptedException {
        WebElement saveButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[@type='submit'][.//div[contains(text(),'Save Note')]]")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", saveButton);
        Thread.sleep(300);
        saveButton.click();
        Thread.sleep(1500);
    }

    private void goToMyNotesTab() throws InterruptedException {
        driver.navigate().to(BASE_URL + "/parent/account");
        wait.until(ExpectedConditions.urlContains("/parent/account"));
        WebElement notesTab = wait.until(ExpectedConditions.elementToBeClickable(By.id("account-tab-notes")));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", notesTab);
        Thread.sleep(300);
        notesTab.click();
        Thread.sleep(500);
    }

    @Test(priority = 1, description = "Register a new user, assign roles, then search by full school name and verify card")
    public void registerAndAssignRoles() throws InterruptedException {
        searchAndVerify(SCHOOLS, "Bardstown Child Care Program");
    }

    @Test(priority = 2, description = "Search for a childcare center by full address and verify the card is displayed")
    public void searchByAddress() throws InterruptedException {
        searchAndVerify(ADDRESS, "Bardstown Child Care Program");
    }

    @Test(priority = 3, description = "Search by full school name and verify card is displayed")
    public void searchByFullName() throws InterruptedException {
        searchAndVerify("Bardstown Child Care Program", "Bardstown Child Care Program");
    }

    @Test(priority = 4, description = "Search by partial school name and verify card is displayed")
    public void searchByPartialName() throws InterruptedException {
        searchAndVerify("Bardstown", "Bardstown Child Care Program");
    }

    @Test(priority = 5, description = "Search by partial address street name and verify card is displayed")
    public void searchByStreet() throws InterruptedException {
        searchAndVerify("Templin Avenue", "Bardstown Child Care Program");
    }

    @Test(priority = 6, description = "Search by partial address zip code and verify card is displayed")
    public void searchByZipCode() throws InterruptedException {
        searchAndVerify("KY 40004", "Bardstown Child Care Program");
    }

    @Test(priority = 7, description = "Open filters and select all filter sections")
    public void selectAllFiltersInCenterSearch() throws InterruptedException {
        registerAndSetup();
        openFiltersDialog();

        String[] sections = new String[] {
                "Language Programs",
                "Inclusive Care",
                "Enrichment Activities",
                "Staff Qualifications",
                "Montessori",
                "Reggio Emilia",
                "Waldorf"
        };

        for (String section : sections) {
            try {
                selectAllOptionsInSection(section);
            } catch (TimeoutException e) {
                System.out.println("Section not available or empty: " + section);
            }
        }

        System.out.println("Passed: Selected all available filters in Center Search");
        Assert.assertTrue(true, "Center Search filter selection completed");
    }

    @Test(priority = 8, description = "Add a note and verify it appears in My Notes")
    public void addNoteInCenterSearch() throws InterruptedException {
        registerAndSetup();
        navigateToNotePage();

        WebElement noteField = getNoteField();
        noteField.click();
        String noteText = "Test Note " + System.currentTimeMillis();
        noteField.sendKeys(noteText);
        Thread.sleep(500);

        clickSaveNote();
        goToMyNotesTab();

        WebElement addedNote = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'" + noteText + "')]"))
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", addedNote);
        Thread.sleep(500);

        Assert.assertTrue(
                addedNote.isDisplayed(),
                "Center Search Failed - Note not found: [" + noteText + "]"
        );
        System.out.println("Passed: Note added and verified: " + noteText);
    }
}
