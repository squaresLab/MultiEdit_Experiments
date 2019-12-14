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
        assertEquals("/home/serenach/MultiEdit_Experiments/tmp/Math-002/buggy/target/classes:/home/serenach/MultiEdit_Experiments/tmp/Math-002/buggy/target/classes:/home/serenach/MultiEdit_Experiments/tmp/Math-002/buggy/target/test-classes:/home/serenach/tooling/defects4j/framework/projects/lib/junit-4.11.jar:/home/serenach/MultiEdit_Experiments/tmp/Math-002/buggy/lib/junit-4.8.2.jar",
                p.getBuggyClassPath());
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

}