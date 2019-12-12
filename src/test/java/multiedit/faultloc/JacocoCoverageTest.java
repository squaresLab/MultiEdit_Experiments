package multiedit.faultloc;


import org.junit.jupiter.api.Test;
import projects.Patch;
import util.TestCase;

import java.io.File;
import java.io.IOException;

import java.util.*;

import static org.junit.Assert.*;

class JacocoCoverageTest {
    private static final List<String> allTests = new ArrayList<>();
    static {
        for (int i = 0; i <= 20; i++) {
            allTests.add(String.format("triangle.TriangleTest::test%02d", i));
        }
        allTests.add("triangle.TriangleTest::testCustom0");
        allTests.add("triangle.TriangleTest::testCustom1");
        allTests.add("triangle.TriangleTest::testCustom2");
        allTests.add("triangle.TriangleTest::testCustom3");
        allTests.add("triangle.TriangleTest::testCustom4");
    }

    private Patch smallSystemBuggy = new Patch(){

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
        public Collection<String> getRelevantTests() {
            return allTests;
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
        public String getPathToBuggySubjectClasses() {
            return "SmallTestSystem/target/classes";
        }

        @Override
        public String getPathToBuggyTestClasses() {
            return "SmallTestSystem/target/test-classes";
        }

        @Override
        public String getPathToPatchedSubjectClasses() {
            return "SmallTestSystem/target/classes";
        }

        @Override
        public String getPathToPatchedTestClasses() {
            return "SmallTestSystem/target/test-classes";
        }

        @Override
        public String getBuggyClassPath() {
            return "lib/junit-4.13-rc-2.jar" + System.getProperty("path.separator")
                    + "lib/hamcrest-all-1.3.jar";
        }

        @Override
        public String getPatchedClassPath() {
            return getBuggyClassPath();
        }
    };

    @Test
    public void smallSystemTest() throws IOException {
        JacocoCoverage jacocoCoverage = new JacocoCoverage();
        jacocoCoverage.internalTestCase(new TestCase(TestCase.TestType.POSITIVE, "triangle.TriangleTest::test00"), smallSystemBuggy, Patch.Version.PATCHED);

        File jacocoFile = new File("jacoco.exec");
        Map<String, Set<Integer>> coverage = jacocoCoverage.getCoverageInfo(jacocoFile, smallSystemBuggy);
        jacocoFile.delete();

        TreeSet<Integer> expectedOutput = new TreeSet<Integer>();
        String expectedClass = "triangle/Triangle";
        expectedOutput.addAll(Arrays.asList(3, 10, 13, 14, 17, 18, 20, 23, 30, 31, 32, 33, 34));

        assertEquals(2, coverage.size());
        assertTrue(coverage.containsKey(expectedClass));
        assertEquals(expectedOutput, coverage.get(expectedClass));
    }

    @Test
    public void testCoverageAllPassing() {
        JacocoCoverage jacocoCoverage = new JacocoCoverage();


        Collection<CoverageSubset> relevantTests = jacocoCoverage.getCoverageRelevantTests(smallSystemBuggy, Patch.Version.PATCHED);
        assertEquals(26, relevantTests.size());
        CoverageSubset relevantTestCoverage = CoverageUtils.aggregate("All Tests", relevantTests);

        // no method signatures, ending brackets, elses
        //but it does include the class signature thing
        Set<Integer> allImportantLinesInTriangle = new HashSet<>(Arrays.asList(3, 10, 11, 13, 14, 15, 17, 18, 20, 21, 23, 24, 25, 27, 30, 31, 32, 33, 34, 38));
        assertEquals(allImportantLinesInTriangle, relevantTestCoverage.getClassCoverageMap().get("triangle/Triangle"));
    }

    @Test
    public void testCoverageSeededFault() {
        JacocoCoverage jacocoCoverage = new JacocoCoverage();

        System.out.println((smallSystemBuggy.getFailingTests().size() + smallSystemBuggy.getPassingTests().size())+" tests");

        Collection<CoverageSubset> passing = jacocoCoverage.getCoveragePassingTests(smallSystemBuggy, Patch.Version.BUGGY);
        Collection<CoverageSubset> failing = jacocoCoverage.getCoverageFailingTests(smallSystemBuggy, Patch.Version.BUGGY);

        CoverageSubset positiveTestCoverage = CoverageUtils.aggregate("All Positive Tests", passing);
        CoverageSubset negativeTestCoverage = CoverageUtils.aggregate("All Negative Tests", failing);

        Set<Integer> passingLinesInTriangle = new HashSet<>(Arrays.asList(3, 10, 13, 14, 15, 17, 18, 20, 21, 23, 24, 25, 27, 30, 31, 32, 33, 34, 38));
        assertEquals(passingLinesInTriangle, positiveTestCoverage.getClassCoverageMap().get("broken/Triangle"));

        Set<Integer> failingLinesInTriangle = new HashSet<>(Arrays.asList(3, 10, 11));
        assertEquals(failingLinesInTriangle, negativeTestCoverage.getClassCoverageMap().get("broken/Triangle"));
        assertEquals(failingLinesInTriangle, CoverageUtils.intersect("Intersect neg tests", failing).getClassCoverageMap().get("broken/Triangle"));
    }
}