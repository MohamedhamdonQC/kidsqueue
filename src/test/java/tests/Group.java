package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import pages.GroupPage;
import pages.RegisterPage;
import pages.RoleSelectionPage;
import utils.TestDataGenerator;

import java.nio.file.Paths;

public class Group extends BaseTest {

    @Test
    public void createGroupAfterRegister() throws InterruptedException {
        RegisterPage Resgister = new RegisterPage(driver);
        Resgister.open("https://dev.kidsqueue.softigital.com/register");
        Resgister.fillFirstName("Mohamed");
        Resgister.fillLastName("Hamdon");
        Resgister.fillEmail("mo" + TestDataGenerator.generateEmail("123"));
        Resgister.fillPassword("12345678");
        Resgister.fillConfirmPassword("12345678");
        Resgister.submit();

        RoleSelectionPage role = new RoleSelectionPage(driver);
        role.clickSelectRole();
        role.selectFirstRole();
        role.selectSecondRole();
        role.confirm();

        Thread.sleep(10000);

        String imagePath = Paths.get(
                System.getProperty("user.dir"),
                "src",
                "test",
                "java",
                "tests",
                "resources",
                "images",
                "Screenshot 2026-04-09 170838.png"
        ).toString();

        GroupPage groupPage = new GroupPage(driver);
        groupPage.open("https://dev.kidsqueue.softigital.com/parent/groups?page=1");
        groupPage.CreateGroup();
        groupPage.nameofgroup("Test Automation Group");
        groupPage.Description("Description of the Group page Automation");
        groupPage.MemberShip();
        groupPage.Anyone();
        groupPage.Image(imagePath);
        groupPage.ClicktoCreateGroup();

        Thread.sleep(10000);
    }
}
