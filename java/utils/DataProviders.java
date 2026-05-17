package utils;

import org.testng.annotations.DataProvider;


public class DataProviders {

    
    @DataProvider(name = "invalidEmails")
    public static Object[][] invalidEmails() {
        return new Object[][] {
                { "plainaddress"          },   
                { "@missinglocal.com"     },   
                { "missing@domain"        },   
                { "missing.domain@"       },   
                { "two@@signs.com"        },   
                { "has space@domain.com"  },   
                { "comma,test@domain.com" },   
                { ".leading@domain.com"   },   
                { "trailing.@domain.com"  },   
                { "test@.domain.com"      },   
                { "test@domain..com"      },   
                { ""                      },   
        };
    }
}