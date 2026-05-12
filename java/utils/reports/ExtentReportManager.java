package utils.reports;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtentReportManager {

    public static Path getReportsDir() {
        return Paths.get(System.getProperty("user.dir"), "reports");
    }

    public static Path getJsonReportPath() {
        return getReportsDir().resolve("request-funds-report.json");
    }

    public static Path getMarkdownReportPath() {
        return getReportsDir().resolve("request-funds-report.md");
    }
}
