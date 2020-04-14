package hmcts.example;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class LibraryTest {
    @Test public void testSomeLibraryMethod() {
        Library classUnderTest = new Library();
        assertNotNull("someLibraryMethod should not be null", classUnderTest != null);
    }
}
