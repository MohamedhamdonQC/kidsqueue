# KQ — Automated Test Suite

> End-to-end and functional test automation for the **KidsQueue** web platform using **Selenium WebDriver + Java + TestNG**

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Selenium](https://img.shields.io/badge/Selenium-4.14.0-43B02A?style=flat-square&logo=selenium&logoColor=white)](https://www.selenium.dev/)
[![TestNG](https://img.shields.io/badge/TestNG-7.9.0-FF6C37?style=flat-square)](https://testng.org/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=flat-square&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![ExtentReports](https://img.shields.io/badge/ExtentReports-5.1.1-7F77DD?style=flat-square)](https://www.extentreports.com/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

---

## 📋 Table of Contents

- [About](#about)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Test Suites](#test-suites)
- [Test Results](#test-results)
- [Prerequisites](#prerequisites)
- [How to Run](#how-to-run)
- [Reports](#reports)
- [Author](#author)

---

## About

**KQ** is a Selenium-based test automation framework built to validate the [KidsQueue](https://dev.kidsqueue.softigital.com) platform — a childcare center discovery and management web application.

The framework follows the **Page Object Model (POM)** design pattern, integrates **ExtentReports** for rich HTML test reports, and uses **WebDriverManager** to handle browser driver setup automatically.

---

## Tech Stack

| Tool / Library | Version | Purpose |
|---|---|---|
| Java | 17 | Test scripting language |
| Selenium WebDriver | 4.14.0 | Browser automation |
| TestNG | 7.9.0 | Test execution & assertions |
| Apache Maven | — | Build & dependency management |
| ExtentReports | 5.1.1 | HTML test reporting |
| WebDriverManager | 5.5.3 | Auto browser driver setup |
| Page Object Model | — | Test design pattern |

---

## Project Structure

```
KQ/
├── src/
│   └── test/
│       └── java/
│           ├── base/
│           │   └── BaseTest.java          # Driver setup, teardown, shared utilities
│           ├── pages/
│           │   ├── RegisterPage.java      # Register page POM
│           │   ├── LoginPage.java         # Login page POM
│           │   ├── Search.java            # Search component POM
│           │   ├── RoleSelectionPage.java # Role selection POM
│           │   ├── ChildcareCenterPage.java
│           │   ├── dashboardPage.java
│           │   └── GroupPage.java
│           ├── tests/
│           │   ├── SearchTest.java        # 22 search test cases
│           │   ├── RegisterTest.java      # 6 registration test cases
│           │   ├── EndToEndTest.java      # Full user journey test
│           │   └── Group.java             # Group creation test
│           └── utils/
│               ├── TestDataGenerator.java # Dynamic test data factory
│               └── reports/
│                   └── Listeners.java     # ExtentReports listener
├── test-output/
│   └── index.html                         # ExtentReports HTML report
├── pom.xml
└── README.md
```

---

## Test Suites

### 🔍 SearchTest — `22 test cases`
Tests all search scenarios on the KidsQueue home page after login.

| Test Case | Description | Type |
|---|---|---|
| `searchWithFullName` | Search using a full valid school name | Positive |
| `searchWithPartialName` | Search using partial keyword | Positive |
| `searchWithLowercase` | Case-insensitive search | Positive |
| `searchWithMixedCase` | Mixed-case input like `rOoTs AnD wInGs` | Boundary |
| `searchWithEnterKey` | Submit search via Enter key | Positive |
| `searchWithInvalidName` | Search term with no results | Negative |
| `searchWithEmptyField` | Submit empty search field | Negative |
| `searchWithSpacesOnly` | Search with only whitespace | Negative |
| `searchWithLeadingSpaces` | Leading spaces before a valid term | Boundary |
| `searchWithTrailingSpaces` | Trailing spaces after a valid term | Boundary |
| `searchWithDoubleSpaces` | Double space between words | Boundary |
| `searchWithSingleCharacter` | Single character input | Boundary |
| `searchWithMaxLengthInput` | 300-character input string | Boundary |
| `searchWithNumericOnly` | Numeric-only search term | Negative |
| `searchWithSpecialCharacters` | Special characters `!@#$%^&*()` | Negative |
| `searchWithSQLInjection` | SQL injection payload `' OR '1'='1` | Security |
| `searchWithXSSPayload` | XSS payload `<script>alert()</script>` | Security |
| `searchWithUnicodeCharacters` | Arabic text `مدرسة الأطفال` | Boundary |
| `searchWithHtmlEntity` | HTML entity input `&lt;Childcare&gt;` | Boundary |
| `searchWithEmoji` | Emoji character input | Negative |
| `searchThenClearField` | Search then clear the input field | Positive |
| `rapidSuccessiveSearches` | Multiple rapid successive searches | Stress |

---

### 📝 RegisterTest — `6 test cases`
Tests user registration flow at `/register`.

| Test Case | Description | Type |
|---|---|---|
| `registerWithValidData` | Register with valid unique data → redirect | Positive |
| `registerWithExistingEmail` | Register with an already-used email | Negative |
| `registerWithMismatchedPasswords` | Password and confirm password don't match | Negative |
| `registerWithEmptyFields` | Submit the form with all fields empty | Negative |
| `registerWithInvalidEmail` | Invalid email format like `not-an-email` | Negative |
| `registerWithShortPassword` | Password shorter than the minimum length | Negative |

---

### 🔄 EndToEndTest — `1 test case`
Full user journey from registration to watchlist verification.

```
Register → Select Roles → Search for Childcare Center
→ Add to Watchlist → Navigate to Dashboard → Verify School in Watching Tab
```

---

### 👥 Group — `1 test case`
Tests group creation after a full registration flow.

```
Register → Select Roles → Navigate to Groups Page
→ Create Group (name, description, membership type, image)
```

---

## Test Results

> Last run: **Apr 7, 2026 — 08:06 PM**

| Metric | Result |
|---|---|
| ✅ Tests Passed | 20 |
| ❌ Tests Failed | 2 |
| ⏭️ Tests Skipped | 0 |
| 📊 Pass Rate | **90%** |
| ⏱️ Duration | ~2 min 25 sec |

### Failed Tests

| Test | Error | Root Cause |
|---|---|---|
| `searchThenClearField` | `TimeoutException` — dropdown not hidden within 5s | UI state timing issue |
| `searchWithEmoji` | `WebDriverException` — input not interactable | Emoji input not supported by field |

---

## Prerequisites

Make sure you have the following installed:

- [Java 17+](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/download.cgi)
- [Google Chrome](https://www.google.com/chrome/) (WebDriverManager handles ChromeDriver automatically)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) *(recommended)*

---

## How to Run

### Clone the repository
```bash
git clone https://github.com/mohamedhamdontester-lab/KQ.git
cd KQ
```

### Run all tests
```bash
mvn test
```

### Run a specific test class
```bash
mvn test -Dtest=SearchTest
mvn test -Dtest=RegisterTest
mvn test -Dtest=EndToEndTest
```

### Run via IntelliJ
Right-click any test class or method → **Run**

---

## Reports

After each test run, an HTML report is generated automatically at:

```
test-output/index.html
```

Open it in any browser to view a full breakdown of:
- Pass / Fail status per test
- Execution timestamps and duration
- Full stack traces for failed tests
- Timeline chart of all test executions
- System / environment info

---

## Author

**Mohamed Hamdon Abbas**
Software Testing Engineer · Cairo, Egypt

[![LinkedIn](https://img.shields.io/badge/LinkedIn-mohamedhamdon-0077B5?style=flat-square&logo=linkedin)](https://www.linkedin.com/in/mohamedhamdon)
[![GitHub](https://img.shields.io/badge/GitHub-mohamedhamdontester--lab-181717?style=flat-square&logo=github)](https://github.com/mohamedhamdontester-lab)
[![Gmail](https://img.shields.io/badge/Gmail-mohamedhamdontester@gmail.com-D14836?style=flat-square&logo=gmail)](mailto:mohamedhamdontester@gmail.com)
