package multiedit.coverage;

import projects.BearsPatch;
import projects.D4JName;
import projects.Defects4JPatch;
import projects.Patch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.BiConsumer;

public class DoCoverageExperimentsBuggy {
    static final boolean intersectPatch = false;
    public static void main(String[] args) throws IOException {
        Map<D4JName, Set<Integer>> d4jMultitestMultiedit = new HashMap<>();
        for (D4JName d4JName : D4JName.values()) {
            d4jMultitestMultiedit.put(d4JName, new HashSet<>());
        }
        Set<Integer> bearsMultitestMultiedit = new HashSet<>();
        try (Scanner relevantBugsFile = new Scanner(new File("data/multitest_multiedit.txt"))) {
            while (relevantBugsFile.hasNextLine()) {
                String[] line = relevantBugsFile.nextLine().split(":");
                if (line.length == 2) {
                    String project = line[0];
                    int number = Integer.parseInt(line[1]);
                    if (project.equals("BEARS")) {
                        bearsMultitestMultiedit.add(number);
                    } else {
                        for (D4JName d4JName : D4JName.values()) {
                            if (project.equals(d4JName.toString())) {
                                d4jMultitestMultiedit.get(d4JName).add(number);
                            }
                        }
                    }
                }
            }
        }

        System.out.println(d4jMultitestMultiedit);
        System.out.println(bearsMultitestMultiedit);
        try (PrintWriter exceptionWriter = new PrintWriter(new File("data/coverage-experiments/badOnes.data"));
             PrintWriter coverageWriter = new PrintWriter(new File("data/coverage-experiments/patchCoverage.data"));
             PrintWriter rawCoverageWriter = new PrintWriter(new File("data/coverage-experiments/rawCoverage.data"));
             PrintWriter disjointWriter = new PrintWriter(new File("data/coverage-experiments/disjoint.data"));
             PrintWriter sameWriter = new PrintWriter(new File("data/coverage-experiments/same.data"));
             PrintWriter inBetweenWriter = new PrintWriter(new File("data/coverage-experiments/inBetween.data"))) {

            /*
              Takes a patch and calculated coverage data for each failing test of the patch, calculates the intersection
              and union of the
             */
            BiConsumer<Patch, Collection<CoverageSubset>> writeAllCoverage = (p, coverageFailingTests) -> {

                CoverageSubset intersectAll = CoverageUtils.intersect("intersect all failing tests", coverageFailingTests);
                CoverageSubset aggregateAll = CoverageUtils.aggregate("aggregate all failing tests", coverageFailingTests);

                if (intersectAll.getClassCoverageMap().size() == 0) {
                    disjointWriter.println(p.getPatchName());
                } else if (intersectAll.getClassCoverageMap().equals(aggregateAll.getClassCoverageMap())) {
                    sameWriter.println(p.getPatchName());
                } else {
                    inBetweenWriter.println(p.getPatchName());
                }

                coverageWriter.println(p.getPatchName() + "-" + aggregateAll.getTotalNumLines());
                rawCoverageWriter.println("PATCH : " + p.getPatchName());
                rawCoverageWriter.println("INTERSECT");
                rawCoverageWriter.println(intersectAll);
                rawCoverageWriter.println("UNION");
                rawCoverageWriter.println(aggregateAll);
            };

            BiConsumer<Patch, Collection<CoverageSubset>> writeCoverageIntersectPatch = (p, coverageFailingTests) -> {
                CoverageSubset patchLocation = p.getPatchLocationsInBuggy();

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
            };

            /*
              Defects4J
             */
            for (D4JName n : D4JName.values()) {
                if (n == D4JName.MOCKITO) continue;
//                if(true) continue;
                for (int i : n.bugs) {
                    if (!d4jMultitestMultiedit.get(n).contains(i)) {
                        continue;
                    }

                    Defects4JPatch p;
                    Collection<CoverageSubset> coverageFailingTests;
                    try {
                        p = new Defects4JPatch(n, i);
                        coverageFailingTests = JacocoCoverage.getCoverageFailingTests(p, Patch.Version.BUGGY);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        System.out.println("FAILED: " + n + " " + i);
                        exceptionWriter.println(n + " " + i);
                        e.printStackTrace(exceptionWriter);
                        continue;
                    }
                    if (intersectPatch) {
                        writeCoverageIntersectPatch.accept(p, coverageFailingTests);
                    } else {
                        writeAllCoverage.accept(p, coverageFailingTests);
                    }

                    p.deleteDirectories();
                }
            }


            /*
              Bears
             */
            for (int i = 1; i <= BearsPatch.TOTAL_BUGS; i++) {
                if (true) continue;

//                if (i == 95 || i == 209) continue; // these have malformed test names

//                if (i != 191 && i != 192 && i != 204) continue;
//                if (!bearsMultitestMultiedit.contains(i)) continue;

                try {
                    BearsPatch b = new BearsPatch(i);
                    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + b.getPatchName());

                    Collection<CoverageSubset> coverageFailingTests = JacocoCoverage.getCoverageFailingTests(b, Patch.Version.BUGGY);
                    if (intersectPatch) {
                        writeCoverageIntersectPatch.accept(b, coverageFailingTests);
                    } else {
                        writeAllCoverage.accept(b, coverageFailingTests);
                    }
                } catch (Exception e) {
                    exceptionWriter.println("Bears Bug " + i);
                    e.printStackTrace(exceptionWriter);
                }
            }

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
