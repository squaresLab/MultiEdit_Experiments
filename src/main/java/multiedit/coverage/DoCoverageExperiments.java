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
import java.util.function.BiConsumer;

public class DoCoverageExperiments {
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
            BiConsumer<Patch, Collection<CoverageSubset>> writeCoverage = (p, coverageFailingTests) -> {
                CoverageSubset patchLocation = p.getPatchLocationsInPatched();

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
                if (n == D4JName.MOCKITO) continue; // needs some manual intervention
                for (int i: n.bugs) {

                    if (!d4jMultitestMultiedit.get(n).contains(i)) {
                        continue;
                    }

                    Defects4JPatch p;
                    Collection<CoverageSubset> coverageFailingTests;
                    try {
                        p = new Defects4JPatch(n, i);
                        coverageFailingTests = JacocoCoverage.getCoverageFailingTests(p, Patch.Version.PATCHED);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        System.out.println("FAILED: " + n + " " + i);
                        continue;
                    }
                    writeCoverage.accept(p, coverageFailingTests);

                    p.deleteDirectories();
                }
            }


            /*
              Bears
             */
            for (int i = 1; i <= BearsPatch.TOTAL_BUGS; i++) {
                if (!bearsMultitestMultiedit.contains(i)) continue;

                try {
                    BearsPatch b = new BearsPatch(i);
                    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + b.getPatchName());

                    Collection<CoverageSubset> coverageFailingTests = JacocoCoverage.getCoverageFailingTests(b, Patch.Version.PATCHED);
                    writeCoverage.accept(b, coverageFailingTests);
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
