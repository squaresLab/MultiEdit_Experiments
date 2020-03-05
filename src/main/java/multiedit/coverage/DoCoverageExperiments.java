package multiedit.coverage;

import projects.BearsPatch;
import projects.D4JName;
import projects.Defects4JPatch;
import projects.Patch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

public class DoCoverageExperiments {
    public static void main(String[] args) throws IOException {
        JacocoCoverage jacocoCoverage = new JacocoCoverage();



        try (PrintWriter exceptionWriter = new PrintWriter(new File("data/coverage-experiments/badOnes.data"));
             PrintWriter coverageWriter = new PrintWriter(new File("data/coverage-experiments/patchCoverage.data"));
             PrintWriter rawCoverageWriter = new PrintWriter(new File("data/coverage-experiments/rawCoverage.data"));
             PrintWriter disjointWriter = new PrintWriter(new File("data/coverage-experiments/disjoint.data"));
             PrintWriter sameWriter = new PrintWriter(new File("data/coverage-experiments/same.data"));
             PrintWriter inBetweenWriter = new PrintWriter(new File("data/coverage-experiments/inBetween.data"))) {

            Map<D4JName, Set<Integer>> d4jMultitestMultiedit = new HashMap<>();
            d4jMultitestMultiedit.put(D4JName.CHART, new HashSet<>(Arrays.asList(2, 5, 7, 14, 15, 16, 18, 19, 21, 22, 25)));
            d4jMultitestMultiedit.put(D4JName.LANG, new HashSet<>(Arrays.asList(1, 3, 4, 7, 10, 12, 13, 15, 17, 19, 20, 22, 23, 27, 31, 30, 32, 34, 35, 36, 41, 42, 46, 47, 50, 53, 56, 60, 62, 63, 64, 65)));
            d4jMultitestMultiedit.put(D4JName.MATH, new HashSet<>(Arrays.asList(1, 4, 7, 14, 16, 18, 21, 23, 24, 26, 28, 29, 35, 36, 37, 38, 40, 43, 44, 46, 47, 49, 51, 52, 54, 55, 62, 65, 67, 68, 72, 74, 76, 81, 83, 86, 93, 95, 98, 99, 100, 102, 106, 6, 8, 12, 15, 64, 66, 79, 84, 88, 92)));
            d4jMultitestMultiedit.put(D4JName.TIME, new HashSet<>(Arrays.asList(1, 2, 3, 7, 10, 12, 23, 26, 5, 6, 8, 9, 13, 17, 20, 21, 22)));
            d4jMultitestMultiedit.put(D4JName.CLOSURE, new HashSet<>());
            d4jMultitestMultiedit.put(D4JName.MOCKITO, new HashSet<>(Arrays.asList(3, 4, 11, 20, 35, 2, 25, 37)));

            for (D4JName n : D4JName.values()) {
                if (true) continue;
                if (n != D4JName.MOCKITO) continue;
                for (int i = 1; i <= n.numBugs; i++) {
                    if (!d4jMultitestMultiedit.get(n).contains(i)) {
                        continue;
                    }

                    Defects4JPatch p;
                    Collection<CoverageSubset> coverageFailingTests;
                    try {
                        p = new Defects4JPatch(n, i);
                        coverageFailingTests = jacocoCoverage.getCoverageFailingTests(p, Patch.Version.PATCHED);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        System.out.println("FAILED: " + n + " " + i);
                        continue;
                    }
                    CoverageSubset patchLocation = p.getPatchLocations();

                    Collection<CoverageSubset> intersectPatch = new ArrayList<>();
                    coverageFailingTests.forEach(cs -> intersectPatch.add(cs.intersection(patchLocation, cs.getDescription())));

                    CoverageSubset intersectAll = CoverageUtils.intersect("intersect all failing tests", intersectPatch);
                    CoverageSubset aggregateAll = CoverageUtils.aggregate("aggregate all failing tests", intersectPatch);

                    if (intersectAll.getClassCoverageMap().size() == 0) {
                        disjointWriter.println(p.getPatchName());
                    } else if (intersectAll.getClassCoverageMap().equals(aggregateAll.getClassCoverageMap())) {
                        sameWriter.println(p.getPatchName());
                    } else {
                        inBetweenWriter.println(p.getPatchName());
                    }

                    coverageWriter.println(p.getPatchName() + "-" + getPercentCoverage(patchLocation, aggregateAll));
                    rawCoverageWriter.println("PATCH : " + p.getPatchName());
                    rawCoverageWriter.println("INTERSECT");
                    rawCoverageWriter.println(intersectAll);
                    rawCoverageWriter.println("UNION");
                    rawCoverageWriter.println(aggregateAll);

                    p.deleteDirectories();
                }
            }


//            Set<Integer> redo = new HashSet<>(Arrays.asList(149, 150, 151, 152, 155, 156, 157, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 172, 174, 176, 177, 178, 179, 180, 181, 182, 183, 186, 187, 190, 191));
//            Set<Integer> redo = new HashSet<>(Arrays.asList(150, 162, 164, 165, 167, 168, 170, 172, 179, 191));
//             there are only 15 of these for bears, so might as well just test these
            Set<Integer> bearsMultitestMultiedit = new HashSet<>(Arrays.asList(5, 7, 10, 12, 13, 18, 20, 23, 24, 31, 33,
                    40, 41, 50, 51, 54, 64, 66, 79, 80, 83, 96, 97, 101, 102, 103, 104, 105, 106, 107, 111, 112, 113,
                    114, 115, 116, 117, 123, 126, 128, 131, 134, 135, 138, 140, 143, 185, 207, 209, 213, 215, 216, 219,
                    221, 224, 230, 250, 99, 118, 231, 11, 62, 65, 141, 1, 8, 14, 15, 16, 17, 21, 28, 29, 30, 34, 35, 37,
                    39, 45, 48, 52, 55, 57, 58, 59, 63, 67, 68, 71, 72, 75, 77, 82, 84, 86, 90, 91, 92, 93, 94, 122,
                    189, 190, 191, 192, 194, 204, 223, 225, 235, 243, 247));
            for (int i = 1; i <= BearsPatch.TOTAL_BUGS; i++) {

//                if (i == 95 || i == 209) continue; // these have malformed test names

                if (i != 191 && i != 192 && i != 204) continue;
                if (!bearsMultitestMultiedit.contains(i)) continue;

                try {
                    BearsPatch b = new BearsPatch(i);
                    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + b.getPatchName());

                    Collection<CoverageSubset> coverageFailingTests = jacocoCoverage.getCoverageFailingTests(b, Patch.Version.PATCHED);
                    CoverageSubset patchLocation = b.getPatchLocations();

                    Collection<CoverageSubset> intersectPatch = new ArrayList<>();
                    coverageFailingTests.forEach(cs -> intersectPatch.add(cs.intersection(patchLocation, cs.getDescription())));

                    CoverageSubset intersectAll = CoverageUtils.intersect("intersect all failing tests", intersectPatch);
                    CoverageSubset aggregateAll = CoverageUtils.aggregate("aggregate all failing tests", intersectPatch);

                    if (intersectAll.getClassCoverageMap().size() == 0) {
                        disjointWriter.println(b.getPatchName());
                    } else if (intersectAll.getClassCoverageMap().equals(aggregateAll.getClassCoverageMap())) {
                        sameWriter.println(b.getPatchName());
                    } else {
                        inBetweenWriter.println(b.getPatchName());
                    }

                    coverageWriter.println(b.getPatchName() + "-" + getPercentCoverage(patchLocation, aggregateAll));
                    rawCoverageWriter.println("PATCH : " + b.getPatchName());
                    rawCoverageWriter.println("INTERSECT");
                    rawCoverageWriter.println(intersectAll);
                    rawCoverageWriter.println("UNION");
                    rawCoverageWriter.println(aggregateAll);
                } catch (Exception e) {
                    exceptionWriter.println("Bears Bug " + i);
                    e.printStackTrace(exceptionWriter);
                }
            }

        }

        try (PrintWriter writer = new PrintWriter("data/coverage-experiments/date.txt")) {
            writer.println(LocalDateTime.now().toString());
        }

    }

    private static double getPercentCoverage(CoverageSubset all, CoverageSubset subset) {
        double numLineNumbers = 0.0;
        double numContainedLineNumbers = 0.0;

        for (String c : all.getClassCoverageMap().keySet()) {
            Set<Integer> allLineNumbers = all.getClassCoverageMap().get(c);
            Set<Integer> subsetLineNumbers = subset.getClassCoverageMap().getOrDefault(c, new HashSet<>());

            numLineNumbers += allLineNumbers.size();
            numContainedLineNumbers += subsetLineNumbers.size();
        }

        return numContainedLineNumbers / numLineNumbers;
    }
}
