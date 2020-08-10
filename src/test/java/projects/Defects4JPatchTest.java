package projects;

import multiedit.coverage.CoverageSubset;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class Defects4JPatchTest {

    @Test
    public void testMath2() throws IOException {
        Defects4JPatch p = new Defects4JPatch(D4JName.MATH, 2);
        assertEquals("tmp/Math-002/buggy/target/classes", p.getBuggyClasses());
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

        CoverageSubset actualLocationsInPatch = new CoverageSubset("Patch");
        actualLocationsInPatch.addClass(
                "org/apache/commons/math3/optim/BaseOptimizer",
                new HashSet<>(Arrays.asList(51)));
        actualLocationsInPatch.addClass(
                "org/apache/commons/math3/optim/nonlinear/scalar/gradient/NonLinearConjugateGradientOptimizer",
                new HashSet<>(Arrays.asList(213, 214, 216, 222, 276)));
        actualLocationsInPatch.addClass(
                "org/apache/commons/math3/optim/nonlinear/scalar/noderiv/CMAESOptimizer",
                new HashSet<>(Arrays.asList(388)));
        actualLocationsInPatch.addClass(
                "org/apache/commons/math3/optim/nonlinear/scalar/noderiv/PowellOptimizer",
                new HashSet<>(Arrays.asList(190, 191, 192, 226)));
        actualLocationsInPatch.addClass(
                "org/apache/commons/math3/optim/nonlinear/scalar/noderiv/SimplexOptimizer",
                new HashSet<>(Arrays.asList(158, 175)));
        actualLocationsInPatch.addClass(
                "org/apache/commons/math3/optim/nonlinear/vector/jacobian/GaussNewtonOptimizer",
                new HashSet<>(Arrays.asList(105, 106, 107, 159)));
        actualLocationsInPatch.addClass(
                "org/apache/commons/math3/optim/nonlinear/vector/jacobian/LevenbergMarquardtOptimizer",
                new HashSet<>(Arrays.asList(321, 322, 324, 325, 489)));

        assertEquals(actualLocationsInPatch, p.getPatchLocationsInPatched());

        CoverageSubset actualLocationsInBuggy = new CoverageSubset("Patch");
        actualLocationsInBuggy.addClass(
                "org/apache/commons/math3/optim/BaseOptimizer",
                new HashSet<>(Arrays.asList(51)));
        actualLocationsInBuggy.addClass(
                "org/apache/commons/math3/optim/nonlinear/scalar/gradient/NonLinearConjugateGradientOptimizer",
                new HashSet<>(Arrays.asList(214, 217, 223, 277)));
        actualLocationsInBuggy.addClass(
                "org/apache/commons/math3/optim/nonlinear/scalar/noderiv/CMAESOptimizer",
                new HashSet<>(Arrays.asList(387, 388)));
        actualLocationsInBuggy.addClass(
                "org/apache/commons/math3/optim/nonlinear/scalar/noderiv/PowellOptimizer",
                new HashSet<>(Arrays.asList(191, 193, 227)));
        actualLocationsInBuggy.addClass(
                "org/apache/commons/math3/optim/nonlinear/scalar/noderiv/SimplexOptimizer",
                new HashSet<>(Arrays.asList(158, 175)));
        actualLocationsInBuggy.addClass(
                "org/apache/commons/math3/optim/nonlinear/vector/jacobian/GaussNewtonOptimizer",
                new HashSet<>(Arrays.asList(106, 108, 160)));
        actualLocationsInBuggy.addClass(
                "org/apache/commons/math3/optim/nonlinear/vector/jacobian/LevenbergMarquardtOptimizer",
                new HashSet<>(Arrays.asList(322, 325, 489)));

        assertEquals(actualLocationsInBuggy, p.getPatchLocationsInBuggy());

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
    public void testLang64() throws IOException {
        Defects4JPatch p = new Defects4JPatch(D4JName.LANG, 64);
        p.deleteDirectories();
    }

    @Test
    public void testTime12() throws IOException {
        Defects4JPatch p = new Defects4JPatch(D4JName.TIME, 12);
        p.deleteDirectories();
    }

    @Test
    public void testTime3() throws IOException {
        Defects4JPatch p = new Defects4JPatch(D4JName.TIME, 3);
        p.deleteDirectories();
    }


}