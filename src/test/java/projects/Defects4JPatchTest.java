package projects;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class Defects4JPatchTest {

    @Test
    public void testMath2() throws IOException {
        Defects4JPatch p = new Defects4JPatch(D4JName.MATH, 2);
        assertEquals("tmp/Math-002/buggy/target/classes", p.getPathToBuggySubjectClasses());
        assertEquals("tmp/Math-002/buggy/target/test-classes", p.getPathToBuggyTestClasses());
        assertFalse(p.getBuggyClassPath().contains("null"));
        assertEquals(1, p.getFailingTests().size());
        assertTrue(p.getFailingTests().contains("org.apache.commons.math3.distribution.HypergeometricDistributionTest::testMath1021"));
//        System.out.println(p.getFailingTests());
        p.deleteDirectories();
    }

    @Test
    public void testChart14() throws IOException {
        Defects4JPatch p = new Defects4JPatch(D4JName.CHART, 14);
        p.deleteDirectories();
    }

    @Test
    public void testMath6() throws IOException {
        Defects4JPatch p = new Defects4JPatch(D4JName.MATH, 6);
        p.deleteDirectories();
    }

    @Test
    public void testClosure134() throws IOException {
        try {
            Defects4JPatch p = new Defects4JPatch(D4JName.CLOSURE, 134);
            p.deleteDirectories();
            fail();
        } catch (RuntimeException e) {
            //pass
        }
    }

    // ***** BUGGY *****

    @Test
    public void testLang25() throws IOException {
        Defects4JPatch p = new Defects4JPatch(D4JName.LANG, 25);
        p.deleteDirectories();
    }

    @Test
    public void testLang42() throws IOException {
        Defects4JPatch p = new Defects4JPatch(D4JName.LANG, 42);
        p.deleteDirectories();
    }

    @Test
    public void testLang62() throws IOException {
        Defects4JPatch p = new Defects4JPatch(D4JName.LANG, 62);
        p.deleteDirectories();
    }

    @Test
    public void testTime12() throws IOException {
        Defects4JPatch p = new Defects4JPatch(D4JName.TIME, 12);
        p.deleteDirectories();
    }

}