package utils;

public class TestDataGenerator {

    private TestDataGenerator() {}

    public static String generateEmail(String prefix) {
        return prefix + System.currentTimeMillis() + "@gmail.com";
    }
}