package tests;

import base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.*;
import utils.TestDataGenerator;
import java.time.*;
import java.time.Duration;
import org.openqa.selenium.JavascriptExecutor;

public class NoteTest extends BaseTest {

    private static final String BASE_URL   = "https://staging.kidsqueue.softigital.com";
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
    public void setupNote() {
        initWaits();
        hideCookieBannerArtifacts();
    }

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
    }

    private void navigateToNotePage() throws InterruptedException {
        driver.navigate().to(BASE_URL + "/parent?schools_page=1");
        wait.until(ExpectedConditions.urlContains("/parent"));
        Thread.sleep(2000);
    }

    private WebElement getNoteField() {
        WebElement noteField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("content-editor"))
        );
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
        WebElement notesTab = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("account-tab-notes"))
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", notesTab);
        Thread.sleep(300);
        notesTab.click();
        Thread.sleep(500);
    }

    @Test(priority = 1, description = "TC01 - Add a valid note and verify it appears in My Notes tab")
    public void addNote_HappyScenario() throws InterruptedException {

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
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(),'" + noteText + "')]")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", addedNote);
        Thread.sleep(500);

        Assert.assertTrue(
                addedNote.isDisplayed(),
                "TC01 Failed - Note not found: [" + noteText + "]"
        );
        System.out.println("✅ TC01 Passed - Note added and verified: " + noteText);
    }


    @Test(priority = 2, description = "TC02 - Click Save Note without typing anything and verify no empty note is saved")
    public void addNote_EmptyNote() throws InterruptedException {

        registerAndSetup();
        navigateToNotePage();

        getNoteField();
        clickSaveNote();
        goToMyNotesTab();

        boolean emptyNoteExists = !driver.findElements(
                By.xpath("//div[contains(@class,'note') or contains(@class,'notes-list')]//*[normalize-space(text())='']")
        ).isEmpty();

        Assert.assertFalse(
                emptyNoteExists,
                "TC02 Failed - Empty note should not be saved"
        );
        System.out.println("✅ TC02 Passed - Empty note was not saved");
    }


    @Test(priority = 3, description = "TC03 - Add a long note (500 characters) and verify it is saved correctly")
    public void addNote_LongNote() throws InterruptedException {

        registerAndSetup();
        navigateToNotePage();

        String longNote = "A".repeat(250) + " " + "B".repeat(249);

        WebElement noteField = getNoteField();
        noteField.click();
        noteField.sendKeys(longNote);
        Thread.sleep(500);

        clickSaveNote();
        goToMyNotesTab();

        WebElement addedNote = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(),'" + longNote.substring(0, 30) + "')]")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", addedNote);
        Thread.sleep(500);

        Assert.assertTrue(
                addedNote.isDisplayed(),
                "TC03 Failed - Long note not found in My Notes"
        );
        System.out.println("✅ TC03 Passed - Long note saved and verified");
    }


    @Test(priority = 4, description = "TC04 - Add a note with special characters and verify it is saved correctly")
    public void addNote_SpecialCharacters() throws InterruptedException {

        registerAndSetup();
        navigateToNotePage();

        String specialNote = "Special !@#$% Note & Test <> 123 " + System.currentTimeMillis();

        WebElement noteField = getNoteField();
        noteField.click();
        noteField.sendKeys(specialNote);
        Thread.sleep(500);

        clickSaveNote();
        goToMyNotesTab();

        WebElement addedNote = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(),'Special')]")
                )
        );
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", addedNote);
        Thread.sleep(500);

        Assert.assertTrue(
                addedNote.isDisplayed(),
                "TC04 Failed - Note with special characters not found in My Notes"
        );
        System.out.println("✅ TC04 Passed - Special characters note saved and verified");
    }
}