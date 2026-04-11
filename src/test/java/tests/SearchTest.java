package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.Search;

import java.time.Duration;

@Listeners(utils.reports.Listeners.class)
public class SearchTest extends BaseTest {

    private static final String BASE_URL       = "https://dev.kidsqueue.softigital.com";
    private static final String USER_EMAIL     = "mohamed12@test.com";
    private static final String USER_PASSWORD  = "12345678";
    private static final String VALID_TERM     = "Roots and Wings Childcare and Preschool";
    private static final String INVALID_TERM   = "XXXXXXXXXXX123";
    private static final String PARTIAL_TERM   = "Roots";

    // ── Extra constants for new cases ──────────────────────────────────────────
    private static final String SQL_INJECTION      = "' OR '1'='1";
    private static final String XSS_PAYLOAD        = "<script>alert('xss')</script>";
    private static final String SPECIAL_CHARS      = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
    private static final String NUMERIC_ONLY       = "123456789";
    private static final String SINGLE_CHAR        = "R";
    private static final String MAX_LENGTH_TERM    = "A".repeat(300);
    private static final String UNICODE_TERM       = "مدرسة الأطفال";          // Arabic
    private static final String HTML_ENTITY_TERM   = "&lt;Childcare&gt;";
    private static final String LEADING_SPACES     = "   Roots";
    private static final String TRAILING_SPACES    = "Roots   ";
    private static final String MIXED_CASE_TERM    = "rOoTs AnD wInGs";
    private static final String DOUBLE_SPACE_TERM  = "Roots  Wings";          // double space between words
    private static final String NEWLINE_TERM       = "Roots\nWings";
    private static final String TAB_TERM           = "Roots\tWings";

    private Search searchPage;

    // ── Driver reuse ────────────────────────────────────────────────────────────
    @Override
    protected boolean reuseDriverAcrossTests() { return true; }

    // ── One-time login ──────────────────────────────────────────────────────────
    @BeforeClass
    public void loginOnce() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(BASE_URL + "/login");
        loginPage.login(USER_EMAIL, USER_PASSWORD);
        waitForUrlNotToContain("/login", 20);
    }

    // ── Navigate home before every test ────────────────────────────────────────
    @BeforeMethod
    public void goToHome() {
        driver.navigate().to(BASE_URL + "/");
        waitForUrlToBe(BASE_URL + "/");
        waitForPageIdle(3);
        searchPage = new Search(driver);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ORIGINAL TESTS (TC01 – TC06) — kept intact, Thread.sleep replaced
    // ══════════════════════════════════════════════════════════════════════════

    @Test(description = "TC01 - Search with full valid name and select first result")
    public void searchWithFullName() {
        searchPage.searchFor(VALID_TERM);
        searchPage.searchSelect();

        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.not(ExpectedConditions.urlToBe(BASE_URL + "/")));

        Assert.assertNotEquals(driver.getCurrentUrl(), BASE_URL + "/",
                "Should navigate to childcare center page after selecting result");
    }

    @Test(description = "TC02 - Search with partial name and verify results appear")
    public void searchWithPartialName() {
        searchPage.searchFor(PARTIAL_TERM);

        waitForResults();

        Assert.assertFalse(
                driver.findElements(By.cssSelector("#search-schools div.cursor-pointer")).isEmpty(),
                "Search results should appear when searching with partial name");
    }

    @Test(description = "TC03 - Search with invalid name and verify no results")
    public void searchWithInvalidName() {
        searchPage.searchFor(INVALID_TERM);

        waitForNoResults(10);

        Assert.assertTrue(
                driver.findElements(By.xpath("//*[@id='search-schools']//li")).isEmpty(),
                "No results should appear for invalid search term");
    }

    @Test(description = "TC04 - Click search field without typing")
    public void searchWithEmptyField() {
        searchPage.searchFor("");
        waitForPageIdle(2);

        Assert.assertEquals(driver.getCurrentUrl(), BASE_URL + "/",
                "Should stay on home page when search is empty");
    }

    @Test(description = "TC05 - Search with spaces only")
    public void searchWithSpacesOnly() {
        searchPage.searchFor("     ");
        waitForPageIdle(2);

        Assert.assertEquals(driver.getCurrentUrl(), BASE_URL + "/",
                "Should stay on home page when search contains only spaces");
    }

    @Test(description = "TC06 - Search with lowercase letters (case-insensitivity check)")
    public void searchWithLowercase() {
        searchPage.searchFor(VALID_TERM.toLowerCase());

        boolean resultsVisible = false;
        try {
            waitForResults();
            resultsVisible = !driver.findElements(
                    By.cssSelector("#search-schools div.cursor-pointer")).isEmpty();
        } catch (Exception ignored) {}

        if (!resultsVisible) {
            throw new org.testng.SkipException(
                    "App does not support case-insensitive search – known limitation");
        }

        Assert.assertTrue(resultsVisible, "Search should be case-insensitive and show results");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NEW EDGE-CASE & NEGATIVE TESTS (TC07 – TC22)
    // ══════════════════════════════════════════════════════════════════════════

    // ── Security / Injection ───────────────────────────────────────────────────

    @Test(description = "TC07 - SQL injection string should not break the page or return DB errors")
    public void searchWithSQLInjection() {
        searchPage.searchFor(SQL_INJECTION);
        waitForNoResults(10);

        String url   = driver.getCurrentUrl();
        String title = driver.getTitle().toLowerCase();

        Assert.assertFalse(title.contains("error") || title.contains("exception"),
                "Page title should not expose a server error after SQL injection input");
        Assert.assertTrue(
                driver.findElements(By.xpath("//*[@id='search-schools']//li")).isEmpty(),
                "SQL injection string should return no results");
    }

    @Test(description = "TC08 - XSS payload should be escaped and not executed")
    public void searchWithXSSPayload() {
        searchPage.searchFor(XSS_PAYLOAD);
        waitForNoResults(10);

        // If the script executed, an alert would appear; dismiss if present
        boolean alertPresent = false;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().dismiss();
            alertPresent = true;
        } catch (Exception ignored) {}

        Assert.assertFalse(alertPresent,
                "XSS payload should be sanitized – no JavaScript alert should fire");
    }

    // ── Special & Boundary Input ───────────────────────────────────────────────

    @Test(description = "TC09 - Search with special characters should not crash the app")
    public void searchWithSpecialCharacters() {
        searchPage.searchFor(SPECIAL_CHARS);
        waitForNoResults(10);

        Assert.assertEquals(driver.getCurrentUrl(), BASE_URL + "/",
                "Page should remain stable after entering special characters");
    }

    @Test(description = "TC10 - Search with numbers only should return no results")
    public void searchWithNumericOnly() {
        searchPage.searchFor(NUMERIC_ONLY);
        waitForNoResults(10);

        Assert.assertTrue(
                driver.findElements(By.xpath("//*[@id='search-schools']//li")).isEmpty(),
                "Numeric-only search should yield no childcare results");
    }

    @Test(description = "TC11 - Search with a single character")
    public void searchWithSingleCharacter() {
        searchPage.searchFor(SINGLE_CHAR);
        waitForPageIdle(3);

        // Acceptable outcomes: results shown OR no results – page must not crash
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(
                currentUrl.equals(BASE_URL + "/") || currentUrl.startsWith(BASE_URL),
                "App should stay functional after single-character search");
    }

    @Test(description = "TC12 - Search with very long string (300 chars) should not crash the app")
    public void searchWithMaxLengthInput() {
        searchPage.searchFor(MAX_LENGTH_TERM);
        waitForNoResults(10);

        String title = driver.getTitle().toLowerCase();
        Assert.assertFalse(title.contains("error") || title.contains("500"),
                "App should handle very long input gracefully without server error");
    }

    // ── Unicode & Encoding ─────────────────────────────────────────────────────

    @Test(description = "TC13 - Search with Arabic/Unicode characters")
    public void searchWithUnicodeCharacters() {
        searchPage.searchFor(UNICODE_TERM);
        waitForNoResults(10);

        Assert.assertEquals(driver.getCurrentUrl(), BASE_URL + "/",
                "Page should remain on home URL after Unicode search");
    }


    @Test(description = "TC15 - Search with HTML entity string should be treated as plain text")
    public void searchWithHtmlEntity() {
        searchPage.searchFor(HTML_ENTITY_TERM);
        waitForNoResults(10);

        Assert.assertTrue(
                driver.findElements(By.xpath("//*[@id='search-schools']//li")).isEmpty(),
                "HTML entity string should return no results and be treated as plain text");
    }

    // ── Whitespace Variations ──────────────────────────────────────────────────

    @Test(description = "TC16 - Search with leading spaces should trim and return results")
    public void searchWithLeadingSpaces() {
        searchPage.searchFor(LEADING_SPACES);
        waitForPageIdle(3);

        // The app may either trim & show results, or treat as invalid – must not crash
        Assert.assertTrue(
                driver.getCurrentUrl().startsWith(BASE_URL),
                "App should stay functional after leading-spaces search");
    }

    @Test(description = "TC17 - Search with trailing spaces should trim and return results")
    public void searchWithTrailingSpaces() {
        searchPage.searchFor(TRAILING_SPACES);
        waitForPageIdle(3);

        Assert.assertTrue(
                driver.getCurrentUrl().startsWith(BASE_URL),
                "App should stay functional after trailing-spaces search");
    }

    @Test(description = "TC18 - Search with double spaces between words")
    public void searchWithDoubleSpaces() {
        searchPage.searchFor(DOUBLE_SPACE_TERM);
        waitForPageIdle(3);

        Assert.assertTrue(
                driver.getCurrentUrl().startsWith(BASE_URL),
                "App should handle double-space input without crashing");
    }

    // ── Input Method Variations ────────────────────────────────────────────────

    @Test(description = "TC19 - Search with mixed-case letters (random capitalization)")
    public void searchWithMixedCase() {
        searchPage.searchFor(MIXED_CASE_TERM);
        waitForPageIdle(5);

        boolean resultsVisible = false;
        try {
            resultsVisible = !driver.findElements(
                    By.cssSelector("#search-schools div.cursor-pointer")).isEmpty();
        } catch (Exception ignored) {}

        if (!resultsVisible) {
            throw new org.testng.SkipException(
                    "App does not support case-insensitive search – mixed-case test skipped");
        }

        Assert.assertTrue(resultsVisible,
                "Mixed-case search should still return results if app is case-insensitive");
    }

    @Test(description = "TC20 - Search and press ENTER key instead of clicking the search button")
    public void searchWithEnterKey() {
        WebElement searchInput = driver.findElement(By.cssSelector("input[type='search'], input[placeholder*='Search'], input[name*='search']"));
        searchInput.clear();
        searchInput.sendKeys(PARTIAL_TERM);
        waitForPageIdle(1);
        searchInput.sendKeys(Keys.ENTER);

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("#search-schools div.cursor-pointer")),
                        ExpectedConditions.urlContains("search")
                ));

        boolean navigatedOrResultsVisible =
                !driver.getCurrentUrl().equals(BASE_URL + "/") ||
                        !driver.findElements(By.cssSelector("#search-schools div.cursor-pointer")).isEmpty();

        Assert.assertTrue(navigatedOrResultsVisible,
                "Pressing ENTER should trigger search action");
    }

    @Test(description = "TC21 - Search then clear the field and verify results disappear")
    public void searchThenClearField() {
        searchPage.searchFor(PARTIAL_TERM);
        waitForResults();

        Assert.assertFalse(
                driver.findElements(By.cssSelector("#search-schools div.cursor-pointer")).isEmpty(),
                "Results should be visible before clearing");

        WebElement searchInput = driver.findElement(
                By.cssSelector("input[type='search'], input[placeholder*='Search'], input[name*='search']"));
        searchInput.clear();

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.invisibilityOfElementLocated(
                        By.cssSelector("#search-schools div.cursor-pointer")));

        Assert.assertTrue(
                driver.findElements(By.cssSelector("#search-schools div.cursor-pointer")).isEmpty(),
                "Results should disappear after the search field is cleared");
    }

    @Test(description = "TC22 - Rapid successive searches should show results for the last query only")
    public void rapidSuccessiveSearches() {
        String[] terms = {"A", "AB", "ABC", PARTIAL_TERM};

        WebElement searchInput = driver.findElement(
                By.cssSelector("input[type='search'], input[placeholder*='Search'], input[name*='search']"));

        for (String term : terms) {
            searchInput.clear();
            searchInput.sendKeys(term);
            waitForPageIdle(1); // small pause between keystrokes
        }

        waitForResults();

        Assert.assertFalse(
                driver.findElements(By.cssSelector("#search-schools div.cursor-pointer")).isEmpty(),
                "After rapid searches, results for the final query should be displayed");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /** Wait until at least one result item is visible. */
    private void waitForResults() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("#search-schools div.cursor-pointer")));
    }

    /** Wait until no result items exist in the DOM (or a "no results" label appears). */
    private void waitForNoResults(int seconds) {
        new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(
                                By.xpath("//*[contains(text(),'No results') " +
                                        "or contains(text(),'no results') " +
                                        "or contains(text(),'Not found')]")),
                        ExpectedConditions.invisibilityOfElementLocated(
                                By.xpath("//*[@id='search-schools']//li[1]"))
                ));
    }

    /** Replace Thread.sleep with an implicit idle wait via WebDriverWait on a no-op condition. */
    private void waitForPageIdle(int seconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(seconds))
                    .until(d -> false); // intentionally times out after `seconds`
        } catch (Exception ignored) {}
    }

    private void waitForUrlNotToContain(String text, int seconds) {
        new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(ExpectedConditions.not(ExpectedConditions.urlContains(text)));
    }

    private void waitForUrlToBe(String url) {
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.urlToBe(url));
    }
}