package utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TestDataGenerator {

    private TestDataGenerator() {
    }

    public static String generateEmail(String prefix) {
        String safePrefix = prefix == null || prefix.isBlank() ? "user" : prefix.trim().replaceAll("\\s+", "");
        return safePrefix + "." + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    public static String randomAlphanumeric(int length) {
        if (length <= 0) {
            return "";
        }

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length());
            builder.append(chars.charAt(index));
        }
        return builder.toString();
    }

    public static String randomSentence(int wordCount) {
        if (wordCount <= 0) {
            return "";
        }

        String[] words = {
                "bright", "safe", "happy", "play", "learn", "grow", "care",
                "fun", "child", "smile", "young", "smart", "kind", "active"
        };

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            String word = words[ThreadLocalRandom.current().nextInt(words.length)];
            builder.append(word);
        }

        builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
        builder.append('.');
        return builder.toString();
    }

    public static String randomDigits(int length) {
        if (length <= 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ThreadLocalRandom.current().nextInt(10));
        }
        return builder.toString();
    }
}
