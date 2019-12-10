package junit.framework;

/**
 * A set of assert methods.  Messages are only displayed when an assert fails.
 *
 * @deprecated Please use {@link org.junit.Assert} instead.
 */
@Deprecated
public class Assert {
    /**
     * Protect constructor since it is a static only class
     */
    protected Assert() {
    }

    /**
     * Asserts that a condition is true. If it isn't it throws
     * an AssertionFailedError with the given message.
     */
    static public void assertTrue(String message, boolean condition) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertTrue(condition);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that a condition is true. If it isn't it throws
     * an AssertionFailedError.
     */
    static public void assertTrue(boolean condition) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertTrue(condition);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that a condition is false. If it isn't it throws
     * an AssertionFailedError with the given message.
     */
    static public void assertFalse(String message, boolean condition) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertFalse(condition);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that a condition is false. If it isn't it throws
     * an AssertionFailedError.
     */
    static public void assertFalse(boolean condition) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertFalse(condition);
        org.junit.Assert.depth--;
    }

    /**
     * Fails a test with the given message.
     */
    static public void fail(String message) {
        if (message == null) {
            throw new AssertionFailedError();
        }
        throw new AssertionFailedError(message);
    }

    /**
     * Fails a test with no message.
     */
    static public void fail() {
        fail(null);
    }

    /**
     * Asserts that two objects are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertEquals(String message, Object expected, Object actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two objects are equal. If they are not
     * an AssertionFailedError is thrown.
     */
    static public void assertEquals(Object expected, Object actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two Strings are equal.
     */
    static public void assertEquals(String message, String expected, String actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two Strings are equal.
     */
    static public void assertEquals(String expected, String actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two doubles are equal concerning a delta.  If they are not
     * an AssertionFailedError is thrown with the given message.  If the expected
     * value is infinity then the delta value is ignored.
     */
    static public void assertEquals(String message, double expected, double actual, double delta) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual,delta);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two doubles are equal concerning a delta. If the expected
     * value is infinity then the delta value is ignored.
     */
    static public void assertEquals(double expected, double actual, double delta) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual,delta);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two floats are equal concerning a positive delta. If they
     * are not an AssertionFailedError is thrown with the given message. If the
     * expected value is infinity then the delta value is ignored.
     */
    static public void assertEquals(String message, float expected, float actual, float delta) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual,delta);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two floats are equal concerning a delta. If the expected
     * value is infinity then the delta value is ignored.
     */
    static public void assertEquals(float expected, float actual, float delta) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual,delta);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two longs are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertEquals(String message, long expected, long actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two longs are equal.
     */
    static public void assertEquals(long expected, long actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two booleans are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertEquals(String message, boolean expected, boolean actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two booleans are equal.
     */
    static public void assertEquals(boolean expected, boolean actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two bytes are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertEquals(String message, byte expected, byte actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two bytes are equal.
     */
    static public void assertEquals(byte expected, byte actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two chars are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertEquals(String message, char expected, char actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two chars are equal.
     */
    static public void assertEquals(char expected, char actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two shorts are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertEquals(String message, short expected, short actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two shorts are equal.
     */
    static public void assertEquals(short expected, short actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two ints are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertEquals(String message, int expected, int actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two ints are equal.
     */
    static public void assertEquals(int expected, int actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertEquals(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that an object isn't null.
     */
    static public void assertNotNull(Object object) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertNotNull(object);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that an object isn't null. If it is
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertNotNull(String message, Object object) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertNotNull(object);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that an object is null. If it isn't an {@link AssertionError} is
     * thrown.
     * Message contains: Expected: <null> but was: object
     *
     * @param object Object to check or <code>null</code>
     */
    static public void assertNull(Object object) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertNull(object);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that an object is null.  If it is not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertNull(String message, Object object) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertNull(object);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two objects refer to the same object. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertSame(String message, Object expected, Object actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertSame(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two objects refer to the same object. If they are not
     * the same an AssertionFailedError is thrown.
     */
    static public void assertSame(Object expected, Object actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertSame(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two objects do not refer to the same object. If they do
     * refer to the same object an AssertionFailedError is thrown with the
     * given message.
     */
    static public void assertNotSame(String message, Object expected, Object actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertNotSame(expected,actual);
        org.junit.Assert.depth--;
    }

    /**
     * Asserts that two objects do not refer to the same object. If they do
     * refer to the same object an AssertionFailedError is thrown.
     */
    static public void assertNotSame(Object expected, Object actual) {
        org.junit.Assert.depth++;
        org.junit.Assert.assertNotSame(expected,actual);
        org.junit.Assert.depth--;
    }

    static public void failSame(String message) {
        String formatted = (message != null) ? message + " " : "";
        fail(formatted + "expected not same");
    }

    static public void failNotSame(String message, Object expected, Object actual) {
        String formatted = (message != null) ? message + " " : "";
        fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">");
    }

    static public void failNotEquals(String message, Object expected, Object actual) {
        fail(format(message, expected, actual));
    }

    public static String format(String message, Object expected, Object actual) {
        String formatted = "";
        if (message != null && message.length() > 0) {
            formatted = message + " ";
        }
        return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
    }
}
