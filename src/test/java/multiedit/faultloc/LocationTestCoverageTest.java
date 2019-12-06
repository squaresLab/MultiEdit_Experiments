package multiedit.faultloc;


import org.junit.jupiter.api.Test;
import util.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

class LocationTestCoverageTest {

    @Test
    public void smallSystemTest() throws IOException {
        LocationTestCoverage locationTestCoverage = new LocationTestCoverage();
        locationTestCoverage.internalTestCase(new TestCase(TestCase.TestType.POSITIVE, "triangle.TriangleTest::test00"), "SmallTestSystem/target/classes", "SmallTestSystem/target/test-classes");

        File jacocoFile = new File("jacoco.exec");
        Map<String, Set<Integer>> coverage = locationTestCoverage.getCoverageInfo(jacocoFile, "SmallTestSystem/target/classes");
        jacocoFile.delete();

        TreeSet<Integer> expectedOutput = new TreeSet<Integer>();
        String expectedClass = "triangle/Triangle";
        expectedOutput.addAll(Arrays.asList(3, 10, 13, 14, 17, 18, 20, 23, 30, 31, 32, 33, 34));

        assertEquals(1, coverage.size());
        assertTrue(coverage.containsKey(expectedClass));
        assertEquals(expectedOutput, coverage.get(expectedClass));
    }

    @Test
    public void testCoverageCalculatorAllPassing() {
        LocationTestCoverage locationTestCoverage = new LocationTestCoverage();
        List<String> passingTests = new ArrayList<String>();
        for (int i = 0; i <= 20; i++) {
            passingTests.add(String.format("triangle.TriangleTest::test%02d", i));
        }
        passingTests.add("triangle.TriangleTest::testCustom0");
        passingTests.add("triangle.TriangleTest::testCustom1");
        passingTests.add("triangle.TriangleTest::testCustom2");
        passingTests.add("triangle.TriangleTest::testCustom3");
        passingTests.add("triangle.TriangleTest::testCustom4");

        CoverageCalculator coverageCalculator = locationTestCoverage.getCoverageAllTests(passingTests, new ArrayList<String>(), "SmallTestSystem/target/classes", "SmallTestSystem/target/test-classes");

        assertNotNull(coverageCalculator);

        CoverageSubset positiveTestCoverage = coverageCalculator.getPositiveTestCoverage();
        CoverageSubset negativeTestCoverage = coverageCalculator.getNegativeTestCoverage();

        assertEquals(0, negativeTestCoverage.getClassCoverageMap().size());
        assertEquals(1, positiveTestCoverage.getClassCoverageMap().size());
        // no method signatures, ending brackets, elses
        //but it does include the class signature thing
        Set<Integer> allImportantLinesInTriangle = new HashSet<>(Arrays.asList(3, 10, 11, 13, 14, 15, 17, 18, 20, 21, 23, 24, 25, 27, 30, 31, 32, 33, 34, 38));
        assertEquals(allImportantLinesInTriangle, positiveTestCoverage.getClassCoverageMap().get("triangle/Triangle"));
    }
}