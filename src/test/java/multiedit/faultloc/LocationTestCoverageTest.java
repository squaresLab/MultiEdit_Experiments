package multiedit.faultloc;


import org.junit.jupiter.api.Test;
import projects.Project;
import util.TestCase;

import java.io.File;
import java.io.IOException;

import java.util.*;

import static org.junit.Assert.*;

class LocationTestCoverageTest {

    private Project smallSystemBuggy = new Project(){

        @Override
        public Collection<String> getPassingTests() {
            List<String> passingTests = Arrays.asList(
                    "broken.TriangleTest::test00",
                    "broken.TriangleTest::test01",
                    "broken.TriangleTest::test02",
                    "broken.TriangleTest::test03",
                    "broken.TriangleTest::test04",
                    "broken.TriangleTest::test08",
                    "broken.TriangleTest::test09",
                    "broken.TriangleTest::test10",
                    "broken.TriangleTest::test11",
                    "broken.TriangleTest::test12",
                    "broken.TriangleTest::test13",
                    "broken.TriangleTest::test14",
                    "broken.TriangleTest::test15",
                    "broken.TriangleTest::test16",
                    "broken.TriangleTest::test19");
            return passingTests;
        }

        @Override
        public Collection<String> getFailingTests() {
            List<String> failingTests = Arrays.asList(
                    "broken.TriangleTest::test05",
                    "broken.TriangleTest::test06",
                    "broken.TriangleTest::test07",
                    "broken.TriangleTest::test17",
                    "broken.TriangleTest::test18",
                    "broken.TriangleTest::test20",
                    "broken.TriangleTest::testCustom0",
                    "broken.TriangleTest::testCustom1",
                    "broken.TriangleTest::testCustom2",
                    "broken.TriangleTest::testCustom3",
                    "broken.TriangleTest::testCustom4");
            return failingTests;
        }

        @Override
        public String getPathToSubjectClasses() {
            return "SmallTestSystem/target/classes";
        }

        @Override
        public String getPathToTestClasses() {
            return "SmallTestSystem/target/test-classes";
        }

        @Override
        public String getClassPath() {
            return "lib/junit-4.13-rc-2.jar" + System.getProperty("path.separator")
                    + "lib/hamcrest-all-1.3.jar";
        }
    };

    private Project smallSystemCorrect = new Project() {
        @Override
        public Collection<String> getPassingTests() {
            List<String> passingTests = new ArrayList<>();
            for (int i = 0; i <= 20; i++) {
                passingTests.add(String.format("triangle.TriangleTest::test%02d", i));
            }
            passingTests.add("triangle.TriangleTest::testCustom0");
            passingTests.add("triangle.TriangleTest::testCustom1");
            passingTests.add("triangle.TriangleTest::testCustom2");
            passingTests.add("triangle.TriangleTest::testCustom3");
            passingTests.add("triangle.TriangleTest::testCustom4");
            return passingTests;
        }

        @Override
        public Collection<String> getFailingTests() {
            return new ArrayList<>();
        }

        @Override
        public String getPathToSubjectClasses() {
            return "SmallTestSystem/target/classes";
        }

        @Override
        public String getPathToTestClasses() {
            return "SmallTestSystem/target/test-classes";
        }

        @Override
        public String getClassPath() {
            return "lib/junit-4.13-rc-2.jar" + System.getProperty("path.separator")
                    + "lib/hamcrest-all-1.3.jar";
        }
    };

    @Test
    public void smallSystemTest() throws IOException {
        LocationTestCoverage locationTestCoverage = new LocationTestCoverage();
        locationTestCoverage.internalTestCase(new TestCase(TestCase.TestType.POSITIVE, "triangle.TriangleTest::test00"), smallSystemCorrect);

        File jacocoFile = new File("jacoco.exec");
        Map<String, Set<Integer>> coverage = locationTestCoverage.getCoverageInfo(jacocoFile, smallSystemCorrect);
        jacocoFile.delete();

        TreeSet<Integer> expectedOutput = new TreeSet<Integer>();
        String expectedClass = "triangle/Triangle";
        expectedOutput.addAll(Arrays.asList(3, 10, 13, 14, 17, 18, 20, 23, 30, 31, 32, 33, 34));

        assertEquals(2, coverage.size());
        assertTrue(coverage.containsKey(expectedClass));
        assertEquals(expectedOutput, coverage.get(expectedClass));
    }

    @Test
    public void testCoverageCalculatorAllPassing() {
        LocationTestCoverage locationTestCoverage = new LocationTestCoverage();


        CoverageCalculator coverageCalculator = locationTestCoverage.getCoverageAllTests(smallSystemCorrect);

        assertNotNull(coverageCalculator);

        CoverageSubset positiveTestCoverage = coverageCalculator.getPositiveTestCoverage();
        CoverageSubset negativeTestCoverage = coverageCalculator.getNegativeTestCoverage();

        assertEquals(0, negativeTestCoverage.getClassCoverageMap().size());

        // no method signatures, ending brackets, elses
        //but it does include the class signature thing
        Set<Integer> allImportantLinesInTriangle = new HashSet<>(Arrays.asList(3, 10, 11, 13, 14, 15, 17, 18, 20, 21, 23, 24, 25, 27, 30, 31, 32, 33, 34, 38));
        assertEquals(allImportantLinesInTriangle, positiveTestCoverage.getClassCoverageMap().get("triangle/Triangle"));
    }

    @Test
    public void testCoverageCalculatorSeededFault() {
        LocationTestCoverage locationTestCoverage = new LocationTestCoverage();

        System.out.println((smallSystemBuggy.getFailingTests().size() + smallSystemBuggy.getPassingTests().size())+" tests");

        CoverageCalculator coverageCalculator = locationTestCoverage.getCoverageAllTests(smallSystemBuggy);

        assertNotNull(coverageCalculator);

        CoverageSubset positiveTestCoverage = coverageCalculator.getPositiveTestCoverage();
        CoverageSubset negativeTestCoverage = coverageCalculator.getNegativeTestCoverage();

        Set<Integer> passingLinesInTriangle = new HashSet<>(Arrays.asList(3, 10, 13, 14, 15, 17, 18, 20, 21, 23, 24, 25, 27, 30, 31, 32, 33, 34, 38));
        assertEquals(passingLinesInTriangle, positiveTestCoverage.getClassCoverageMap().get("broken/Triangle"));

        Set<Integer> failingLinesInTriangle = new HashSet<>(Arrays.asList(3, 10, 11));
        assertEquals(failingLinesInTriangle, negativeTestCoverage.getClassCoverageMap().get("broken/Triangle"));
    }
}