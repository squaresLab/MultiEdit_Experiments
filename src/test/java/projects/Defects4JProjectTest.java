package projects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Defects4JProjectTest {

    @Test
    public void testMath2() {
        Defects4JProject p = new Defects4JProject(D4JName.MATH, 2);
        assertEquals("tmp/Math-002/target/classes", p.getPathToSubjectClasses());
        assertEquals("tmp/Math-002/target/test-classes", p.getPathToTestClasses());
        assertEquals("/home/serenach/MultiEdit_Experiments/tmp/Math-002/target/classes:/home/serenach/MultiEdit_Experiments/tmp/Math-002/target/classes:/home/serenach/MultiEdit_Experiments/tmp/Math-002/target/test-classes:/home/serenach/tooling/defects4j/framework/projects/lib/junit-4.11.jar:/home/serenach/MultiEdit_Experiments/tmp/Math-002/lib/junit-4.8.2.jar",
                p.getClassPath());
        assertEquals(1, p.getFailingTests().size());
        assertTrue(p.getFailingTests().contains("org.apache.commons.math3.distribution.HypergeometricDistributionTest::testMath1021"));
//        System.out.println(p.getFailingTests());
    }

}