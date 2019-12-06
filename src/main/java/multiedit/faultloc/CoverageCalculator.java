package multiedit.faultloc;

import util.TestCase;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CoverageCalculator {

    private Set<CoverageSubset> positiveTests;
    private Set<CoverageSubset> negativeTests;

    public CoverageCalculator() {
        this.positiveTests = new HashSet<>();
        this.negativeTests = new HashSet<>();
    }

    public void addTestCoverage(TestCase testCase, Map<String, Set<Integer>> coverageInfo) {
        CoverageSubset testCoverage = new CoverageSubset(testCase.getTestName());
        testCoverage.addAllClasses(coverageInfo);

        if (testCase.getPosOrNeg() == TestCase.TestType.POSITIVE) {
            positiveTests.add(testCoverage);
        } else {
            negativeTests.add(testCoverage);
        }
    }

    public CoverageSubset getPositiveTestCoverage() {
        CoverageSubset posTests = new CoverageSubset("Coverage of Pos Tests");
        for (CoverageSubset cov : positiveTests) {
            posTests.addAllClasses(cov.getClassCoverageMap());
        }
        return posTests;
    }

    public CoverageSubset getNegativeTestCoverage() {
        CoverageSubset negTests = new CoverageSubset("Coverage of Neg Tests");
        for (CoverageSubset cov : negativeTests) {
            negTests.addAllClasses(cov.getClassCoverageMap());
        }
        return negTests;
    }

    public CoverageSubset getIntersectionNegTests() {
        CoverageSubset intersection = null;

        for (CoverageSubset cov : negativeTests) {
            if (intersection == null) {
                intersection = cov;
            } else {
                intersection = intersection.intersection(cov, "Intersection of Neg Tests");
            }
        }

        return intersection;
    }

}
