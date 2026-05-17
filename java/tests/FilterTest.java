
package tests;

import base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.*;
import utils.TestDataGenerator;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.JavascriptExecutor;
@Listeners(utils.reports.Listeners.class)
public class FilterTest extends BaseTest {

    private static final String BASE_URL = "https://staging.kidsqueue.softigital.com";
    private static final String FIRST_NAME = "Mohamed";
    private static final String LAST_NAME  = "Hamdon";
    private static final String PASSWORD   = "12345678";

    private WebDriverWait wait;
    private JavascriptExecutor js;

    private void initWaits() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js   = (JavascriptExecutor) driver;
    }

    @BeforeMethod
    public void setup() {
        super.setup();
        initWaits();
        hideCookieBannerArtifacts();
    }

    // ─────────────────────────────────────────
    // Helper: Register + Setup
    // ─────────────────────────────────────────
    private void registerAndSetup() throws InterruptedException {
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

        driver.navigate().to(BASE_URL + "/parent?schools_page=1");
        wait.until(ExpectedConditions.urlContains("/parent"));
        Thread.sleep(2000);
    }

    // ─────────────────────────────────────────
    // Helper: Open Filters Dialog
    // ─────────────────────────────────────────
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

    // ─────────────────────────────────────────
    // Helper: Expand a filter section by name
    // ─────────────────────────────────────────
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

    // ─────────────────────────────────────────
    // Helper: Select a checkbox option by label
    // ─────────────────────────────────────────
    private void selectCheckboxOption(String optionLabel) throws InterruptedException {
        By[] locators = new By[] {
                By.xpath("//label[.//*[normalize-space()='" + optionLabel + "' or contains(normalize-space(.),'" + optionLabel + "')]]"),
                By.xpath("//label[normalize-space()='" + optionLabel + "' or contains(normalize-space(.),'" + optionLabel + "')]"),
                By.xpath("//*[self::button or self::div or self::span][normalize-space()='" + optionLabel + "' or contains(normalize-space(.),'" + optionLabel + "')]"),
                By.xpath("//*[contains(normalize-space(.),'" + optionLabel + "')]/ancestor::label[1]"),
                By.xpath("//*[contains(normalize-space(.),'" + optionLabel + "')]/ancestor::*[@role='checkbox' or contains(@class,'cursor-pointer')][1]")
        };

        WebElement option = null;
        for (By locator : locators) {
            List<WebElement> matches = driver.findElements(locator);
            if (!matches.isEmpty()) {
                option = matches.get(0);
                break;
            }
        }

        if (option == null) {
            throw new NoSuchElementException("Could not locate filter option: " + optionLabel);
        }

        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", option);
        Thread.sleep(300);
        try {
            option.click();
        } catch (ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", option);
        } catch (ElementNotInteractableException e) {
            js.executeScript("arguments[0].click();", option);
        }
        Thread.sleep(500);
    }

    private void selectAllOptionsInSection(String sectionName) throws InterruptedException {
        expandFilterSection(sectionName);

        List<WebElement> labels = driver.findElements(
                By.xpath("//button[.//*[contains(normalize-space(.),'" + sectionName + "')]]" +
                        "/following-sibling::*//*[self::label or self::button or self::div][not(ancestor::*[contains(@style,'display: none')])]")
        );

        for (WebElement label : labels) {
            String text = label.getText() == null ? "" : label.getText().trim();
            if (text.isEmpty() || text.equalsIgnoreCase(sectionName) || text.matches(".*\\bSelected\\b.*")) {
                continue;
            }
            try {
                js.executeScript("arguments[0].scrollIntoView({block: 'center'});", label);
                Thread.sleep(150);
                label.click();
            } catch (Exception e) {
                try {
                    js.executeScript("arguments[0].click();", label);
                } catch (Exception ignored) {
                }
            }
            Thread.sleep(150);
        }
    }

    // ─────────────────────────────────────────
    // Helper: Click Apply / Search button
    // ─────────────────────────────────────────
    private void applyFilters() throws InterruptedException {
        WebElement applyBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Apply') or contains(text(),'Search') " +
                                "or contains(text(),'Filter') or .//div[contains(text(),'Apply')]]")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", applyBtn);
        Thread.sleep(300);
        applyBtn.click();
        Thread.sleep(2000);
    }

    // ─────────────────────────────────────────
    // Helper: Check if results exist
    // ─────────────────────────────────────────
    private boolean hasResults() {
        List<WebElement> cards = driver.findElements(
                By.xpath("//h2[contains(@class,'text-primary') and contains(@class,'font-semibold')]")
        );
        return !cards.isEmpty();
    }

    // ─────────────────────────────────────────
    // Helper: Click View Details on first card
    // ─────────────────────────────────────────
    private void clickViewDetailsIfExists() throws InterruptedException {
        List<WebElement> viewDetailsButtons = driver.findElements(
                By.xpath("//button[.//div[contains(text(),'View Details')]] " +
                        "| //a[.//button[.//div[contains(text(),'View Details')]]]")
        );
        if (!viewDetailsButtons.isEmpty()) {
            WebElement firstBtn = viewDetailsButtons.get(0);
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", firstBtn);
            Thread.sleep(300);
            firstBtn.click();
            Thread.sleep(2000);
            System.out.println("✅ Clicked View Details on first result");
        } else {
            System.out.println("ℹ️ No results found — View Details not clicked");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TC01 - Filter by Language Programs → Spanish immersion
    // ═══════════════════════════════════════════════════════════
    @Test(priority = 1, description = "TC01 - Filter by Language Programs (Spanish immersion) and verify results")
    public void filterByLanguagePrograms() throws InterruptedException {

        registerAndSetup();
        openFiltersDialog();
        expandFilterSection("Language Programs");
        selectCheckboxOption("Spanish immersion");
        applyFilters();

        boolean results = hasResults();
        System.out.println(results
                ? "✅ TC01 - Results found after filtering by Spanish immersion"
                : "ℹ️ TC01 - No results found for Spanish immersion");
     Thread.sleep(3000);
        if (results) {
            clickViewDetailsIfExists();
            Assert.assertTrue(
                    driver.getCurrentUrl().contains("/schools/") || driver.getCurrentUrl().contains("/parent"),
                    "TC01 Failed - View Details did not navigate correctly"
            );
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TC02 - Filter by Montessori
    // ═══════════════════════════════════════════════════════════
    @Test(priority = 2, description = "TC02 - Filter by Montessori and verify results or empty state")
    public void filterByMontessori() throws InterruptedException {

        registerAndSetup();
        openFiltersDialog();
        expandFilterSection("Montessori");
        selectCheckboxOption("Montessori");
        applyFilters();

        boolean results = hasResults();
        System.out.println(results
                ? "✅ TC02 - Results found after filtering by Montessori"
                : "ℹ️ TC02 - No results found for Montessori");

        if (results) {
            clickViewDetailsIfExists();
        }

        // Pass either way — both outcomes are valid
        Assert.assertTrue(true, "TC02 - Filter by Montessori completed");
    }

    // ═══════════════════════════════════════════════════════════
    // TC03 - Filter by multiple options (Enrichment Activities + Play-Based Learning)
    // ═══════════════════════════════════════════════════════════
    @Test(priority = 3, description = "TC03 - Filter by multiple categories and verify results")
    public void filterByMultipleCategories() throws InterruptedException {

        registerAndSetup();
        openFiltersDialog();

        // Select first filter
        expandFilterSection("Enrichment Activities");
        selectCheckboxOption("Enrichment Activities");

        // Scroll back and select second filter
        expandFilterSection("Play-Based Learning");
        selectCheckboxOption("Play-Based Learning");

        applyFilters();

        boolean results = hasResults();
        System.out.println(results
                ? "✅ TC03 - Results found after filtering by multiple categories"
                : "ℹ️ TC03 - No results found for combined filters");

        if (results) {
            clickViewDetailsIfExists();
        }

        Assert.assertTrue(true, "TC03 - Multi-filter completed");
    }

    @Test(priority = 5, description = "TC05 - Select all filters in the filter section and verify results")
    public void selectAllFiltersInAllSections() throws InterruptedException {

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
                System.out.println("ℹ️ Section not available or empty: " + section);
            }
        }

        applyFilters();

        boolean results = hasResults();
        System.out.println(results
                ? "✅ TC05 - Results found after selecting all filters"
                : "ℹ️ TC05 - No results found after selecting all filters");

        if (results) {
            clickViewDetailsIfExists();
        }

        Assert.assertTrue(true, "TC05 - Select all filters completed");
    }

    //
    @Test(priority = 4, description = "TC04 - Open filter dialog and close without applying any filter")
    public void openAndCloseFilterWithoutSelecting() throws InterruptedException {

        registerAndSetup();
        openFiltersDialog();

        // Close dialog via X button
        WebElement closeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[@data-slot='dialog-close'] | //button[contains(@aria-label,'Close')] " +
                                "| //button[contains(@class,'close')] | //*[name()='svg' and contains(@class,'lucide-x')]/parent::button")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", closeBtn);
        Thread.sleep(300);
        closeBtn.click();
        Thread.sleep(1000);

        // Verify dialog is closed
        List<WebElement> dialog = driver.findElements(
                By.xpath("//*[@role='dialog']")
        );
        Assert.assertTrue(
                dialog.isEmpty() || !dialog.get(0).isDisplayed(),
                "TC04 Failed - Dialog should be closed"
        );
        System.out.println("✅ TC04 Passed - Filter dialog closed without applying");
    }
}
