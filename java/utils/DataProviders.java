package utils;

import org.testng.annotations.DataProvider;

/**
 * Central TestNG data providers shared across test classes.
 */
public class DataProviders {

    /**
     * Provides a set of syntactically invalid e-mail addresses for TC03.
     * Each row is passed as a single String argument to the test method.
     */
    @DataProvider(name = "invalidEmails")
    public static Object[][] invalidEmails() {
        return new Object[][] {
                { "plainaddress"          },   // no @ or domain
                { "@missinglocal.com"     },   // no local part
                { "missing@domain"        },   // no TLD
                { "missing.domain@"       },   // no domain at all
                { "two@@signs.com"        },   // double @
                { "has space@domain.com"  },   // space in local part
                { "comma,test@domain.com" },   // comma in local part
                { ".leading@domain.com"   },   // leading dot
                { "trailing.@domain.com"  },   // trailing dot before @
                { "test@.domain.com"      },   // dot immediately after @
                { "test@domain..com"      },   // consecutive dots in domain
                { ""                      },   // empty string (treated as missing)
        };
    }
}