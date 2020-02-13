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
             PrintWriter disjointWriter = new PrintWriter(new File("data/coverage-experiments/disjoint.data"));
             PrintWriter sameWriter = new PrintWriter(new File("data/coverage-experiments/same.data"));
             PrintWriter inBetweenWriter = new PrintWriter(new File("data/coverage-experiments/inBetween.data"))) {
//            for (D4JName n : D4JName.values()) {
//                for (int i = 1; i <= n.numBugs; i++) {
//
//                    Defects4JPatch p;
//                    try {
//                        p = new Defects4JPatch(n, i);
//                    } catch (Exception e) {
//                        e.printStackTrace(System.err);
//                        System.out.println("FAILED: " + n + " " + i);
//                        continue;
//                    }
//                    Collection<CoverageSubset> coverageFailingTests = jacocoCoverage.getCoverageFailingTests(p, Patch.Version.PATCHED);
//                    CoverageSubset patchLocation = p.getPatchLocations();
//
//                    Collection<CoverageSubset> intersectPatch = new ArrayList<>();
//                    coverageFailingTests.forEach(cs -> intersectPatch.add(cs.intersection(patchLocation, cs.getDescription())));
//
//                    CoverageSubset intersectAll = CoverageUtils.intersect("intersect all failing tests", intersectPatch);
//                    CoverageSubset aggregateAll = CoverageUtils.aggregate("aggregate all failing tests", intersectPatch);
//
//                    if (intersectAll.getClassCoverageMap().size() == 0) {
//                        disjointWriter.println(p.getPatchName());
//                    } else if (intersectAll.getClassCoverageMap().equals(aggregateAll.getClassCoverageMap())) {
//                        sameWriter.println(p.getPatchName());
//                    } else {
//                        inBetweenWriter.println(p.getPatchName());
//                    }
//
//                    coverageWriter.println(p.getPatchName() + "-" + getPercentCoverage(patchLocation, aggregateAll));
//
//                    p.deleteDirectories();
//                }
//            }


            Set<Integer> redo = new HashSet<>(Arrays.asList(142, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251));
            for (int i = 1; i <= BearsPatch.TOTAL_BUGS; i++) {
//                if (i == 95 || i == 209) continue; // these have malformed test names

                if (!redo.contains(i)) continue;

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
