package multiedit.faultloc;

import org.junit.jupiter.api.Test;
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
    public void coverage(D4JName projectName, int patchNum) {
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
        this.coverage(D4JName.CLOSURE, 6);
    }

    @Test
    public void testClosure011() {
        this.coverage(D4JName.CLOSURE, 11);
    }

    @Test
    public void testClosure024() {
        this.coverage(D4JName.CLOSURE, 24);
    }

    @Test
    public void testClosure103() {
        this.coverage(D4JName.CLOSURE, 103);
    }

    @Test
    public void testTime004() {
        this.coverage(D4JName.TIME, 4);
    }

    @Test
    public void testChart013() {
        this.coverage(D4JName.CHART, 13);
    }

    @Test
    public void testMockito021() {
        this.coverage(D4JName.MOCKITO, 21);
    }
}
