package utils.reports;

import org.testng.ITestContext;
import org.testng.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Listeners implements ITestListener {

    private final List<Map<String, Object>> results = new ArrayList<>();

    @Override
    public void onTestStart(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        addResult(result, "PASS", null);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        addResult(result, "FAIL", result.getThrowable() == null ? null : result.getThrowable().toString());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        addResult(result, "SKIP", result.getThrowable() == null ? null : result.getThrowable().toString());
    }

    @Override
    public void onFinish(ITestContext context) {
        writeReports();
    }

    private void addResult(ITestResult result, String status, String error) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", result.getMethod().getMethodName());
        item.put("description", result.getMethod().getDescription());
        item.put("status", status);
        item.put("details", buildDetails(result, error));
        item.put("className", result.getTestClass().getName());
        item.put("startedAt", Instant.ofEpochMilli(result.getStartMillis()).toString());
        item.put("endedAt", Instant.ofEpochMilli(result.getEndMillis()).toString());
        if (error != null) {
            item.put("error", error);
        }
        results.add(item);
    }

    private void writeReports() {
        try {
            Path reportsDir = ExtentReportManager.getReportsDir();
            Files.createDirectories(reportsDir);
            Files.writeString(ExtentReportManager.getJsonReportPath(), toJson(results), StandardCharsets.UTF_8);
            Files.writeString(ExtentReportManager.getMarkdownReportPath(), toMarkdown(results), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report files", e);
        }
    }

    private String toMarkdown(List<Map<String, Object>> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Request Funds Test Report\n\n");
        builder.append("| Test | Status | Description | Details |\n");
        builder.append("|---|---|---|---|\n");
        for (Map<String, Object> item : items) {
            builder.append("| ")
                    .append(escapeMd(String.valueOf(item.get("name"))))
                    .append(" | ")
                    .append(escapeMd(String.valueOf(item.get("status"))))
                    .append(" | ")
                    .append(escapeMd(String.valueOf(item.get("description"))))
                    .append(" | ")
                    .append(escapeMd(String.valueOf(item.get("details"))))
                    .append(" |\n");
        }
        return builder.toString();
    }

    private String buildDetails(ITestResult result, String error) {
        String description = result.getMethod().getDescription();
        if (error != null && !error.isBlank()) {
            return error;
        }
        if (description != null && !description.isBlank()) {
            return description;
        }
        return result.getMethod().getMethodName();
    }

    private String toJson(List<Map<String, Object>> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"tests\":[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) builder.append(',');
            builder.append(mapToJson(items.get(i)));
        }
        builder.append("]}");
        return builder.toString();
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) builder.append(',');
            first = false;
            builder.append('"').append(escapeJson(entry.getKey())).append('"').append(':');
            Object value = entry.getValue();
            if (value == null) {
                builder.append("null");
            } else {
                builder.append('"').append(escapeJson(String.valueOf(value))).append('"');
            }
        }
        builder.append('}');
        return builder.toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String escapeMd(String value) {
        return value.replace("|", "\\|");
    }
}
