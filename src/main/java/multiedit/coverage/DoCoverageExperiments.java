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
            d4jMultitestMultiedit.put(D4JName.CHART, new HashSet<>(Arrays.asList(2, 14, 16, 18, 19, 22, 25)));
            d4jMultitestMultiedit.put(D4JName.LANG, new HashSet<>(Arrays.asList(20, 22,  30, 34, 41, 47, 50, 12, 15, 19, 36)));
            d4jMultitestMultiedit.put(D4JName.MATH, new HashSet<>(Arrays.asList(1, 4, 16, 29, 35, 36, 37, 98, 99, 21, 43, 46, 47, 68, 76, 86, 102)));
            d4jMultitestMultiedit.put(D4JName.TIME, new HashSet<>(Arrays.asList(12, 5, 6,  21, 22)));
            d4jMultitestMultiedit.put(D4JName.CLOSURE, new HashSet<>());
            d4jMultitestMultiedit.put(D4JName.MOCKITO, new HashSet<>(Arrays.asList(3, 4, 11, 20, 23, 35)));
            for (D4JName n : D4JName.values()) {

                if (n == D4JName.MOCKITO) continue;
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
            Set<Integer> bearsMultitestMultiedit = new HashSet<>(Arrays.asList(7, 12, 31, 40, 41, 62, 79, 80, 103, 123, 140, 141, 209, 216, 250));
            for (int i = 1; i <= BearsPatch.TOTAL_BUGS; i++) {
                if (true) continue;

//                if (i == 95 || i == 209) continue; // these have malformed test names

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
