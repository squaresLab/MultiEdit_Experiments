package multiedit.coverage;

import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import projects.BearsPatch;
import projects.D4JName;
import projects.Defects4JPatch;
import projects.Patch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static junit.framework.TestCase.fail;

public class SpotCheckCoverage {
    JacocoCoverage jacocoCoverage = new JacocoCoverage();

    /**
     * Basically copied from DoCoverageExperiments
     * @param projectName
     * @param patchNum
     */
    public void d4jCoverage(D4JName projectName, int patchNum) {
        Defects4JPatch p;
        try {
            p = new Defects4JPatch(projectName, patchNum);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.out.println("FAILED: " + projectName + " " + patchNum);
            fail();
            return;
        }
        Collection<CoverageSubset> coverageFailingTests = jacocoCoverage.getCoverageFailingTests(p, Patch.Version.PATCHED);
        CoverageSubset patchLocation = p.getPatchLocations();

        Collection<CoverageSubset> intersectPatch = new ArrayList<>();
        coverageFailingTests.forEach(cs -> intersectPatch.add(cs.intersection(patchLocation, cs.getDescription())));

        CoverageSubset intersectAll = CoverageUtils.intersect("intersect all failing tests", intersectPatch);
        CoverageSubset aggregateAll = CoverageUtils.aggregate("aggregate all failing tests", intersectPatch);
        try {
            p.deleteDirectories();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testClosure006() {
        this.d4jCoverage(D4JName.CLOSURE, 6);
    }

    @Test
    public void testClosure011() {
        this.d4jCoverage(D4JName.CLOSURE, 11);
    }

    @Test
    public void testClosure024() {
        this.d4jCoverage(D4JName.CLOSURE, 24);
    }

    @Test
    public void testClosure103() {
        this.d4jCoverage(D4JName.CLOSURE, 103);
    }

    @Test
    public void testTime004() {
        this.d4jCoverage(D4JName.TIME, 4);
    }

    @Test
    public void testChart013() {
        this.d4jCoverage(D4JName.CHART, 13);
    }

    @Test
    public void testMockito021() {
        this.d4jCoverage(D4JName.MOCKITO, 21);
    }

    public void bearsCoverage(int i) {
        BearsPatch b = new BearsPatch(i);
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + b.getPatchName());

        Collection<CoverageSubset> coverageFailingTests = jacocoCoverage.getCoverageFailingTests(b, Patch.Version.PATCHED);
        CoverageSubset patchLocation = b.getPatchLocations();

        Collection<CoverageSubset> intersectPatch = new ArrayList<>();
        coverageFailingTests.forEach(cs -> intersectPatch.add(cs.intersection(patchLocation, cs.getDescription())));

        CoverageSubset intersectAll = CoverageUtils.intersect("intersect all failing tests", intersectPatch);
        CoverageSubset aggregateAll = CoverageUtils.aggregate("aggregate all failing tests", intersectPatch);
    }

    @Test
    public void testBears001() {
        this.bearsCoverage(1);
    }

    @Test
    public void testBears005() {
        this.bearsCoverage(5);
    }

    @Test
    public void testBears007() {
        this.bearsCoverage(7);
    }

    @Test
    public void testBears031() {
        this.bearsCoverage(31);
    }

    @Test
    public void testBears050() {
        this.bearsCoverage(50);
    }

    @Test
    public void testBears103() {
        // modules/activiti-engine/target
        this.bearsCoverage(103);
    }

    @Test
    public void testBears142() {
        // modules/activiti-engine/target
        this.bearsCoverage(142);
    }

    @Test
    public void testBears144() {
        // problem: flaky test with connecting to network
        this.bearsCoverage(144);
    }

    @Test
    public void testBears165() {
        //
        this.bearsCoverage(165);
    }

    @Test
    public void testBears149() {
        //
        this.bearsCoverage(149);
    }

    @Test
    public void testBears209() {
        //
        this.bearsCoverage(209);
    }

    @Test
    public void testFail() {
        TestCase.assertTrue(false);
    }

    public static void main(String[] args) {
        // main is necessary because part of the hack for some of
        // the bears experiments requires user input
        new SpotCheckCoverage().testBears165();
    }
}
