package multiedit.faultloc;


import org.junit.jupiter.api.Test;
import util.TestCase;

import java.io.IOException;

class LocationTestCoverageTest {

    @Test
    public void smallSystemTest() throws IOException {
        LocationTestCoverage locationTestCoverage = new LocationTestCoverage();
        locationTestCoverage.internalTestCase("","", new TestCase(TestCase.TestType.POSITIVE, "triangle.TriangleTest::test00"), true);
        locationTestCoverage.getCoverageInfo();
    }
}