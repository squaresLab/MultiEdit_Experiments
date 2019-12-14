package multiedit.faultloc;

import projects.D4JName;
import projects.Defects4JPatch;
import projects.Patch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class DoCoverageExperiments {
    public static void main(String[] args) throws IOException {
        JacocoCoverage jacocoCoverage = new JacocoCoverage();
        Scanner in = new Scanner(System.in);

        Map<String, Double> patchCoverage = new TreeMap<>();
        List<String> disjoint = new ArrayList<>();
        List<String> same = new ArrayList<>();
        List<String> inBetween = new ArrayList<>();

        for (D4JName n : D4JName.values()) {
            for (int i = 1; i <= n.numBugs; i++) {
                if (n == D4JName.CLOSURE && i == 134) {
                    continue;
                }

                Defects4JPatch p = new Defects4JPatch(n, i);
                Collection<CoverageSubset> coverageFailingTests = jacocoCoverage.getCoverageFailingTests(p, Patch.Version.PATCHED);
                CoverageSubset patchLocation = p.getPatchLocations();

                Collection<CoverageSubset> intersectPatch = new ArrayList<>();
                coverageFailingTests.forEach(cs -> intersectPatch.add(cs.intersection(patchLocation, cs.getDescription())));

                CoverageSubset intersectAll = CoverageUtils.intersect("intersect all failing tests", intersectPatch);
                CoverageSubset aggregateAll = CoverageUtils.aggregate("aggregate all failing tests", intersectPatch);

                if (intersectAll.getClassCoverageMap().size() == 0) {
                    disjoint.add(p.getPatchName());
                } else if (intersectAll.getClassCoverageMap().equals(aggregateAll.getClassCoverageMap())) {
                    same.add(p.getPatchName());
                } else {
                    inBetween.add(p.getPatchName());
                }

                patchCoverage.put(p.getPatchName(), getPercentCoverage(patchLocation, aggregateAll));

                p.deleteDirectories();
            }
        }

        PrintWriter writer = new PrintWriter(new File("data/coverage-experiments/patchCoverage.data"));
        for (String name : patchCoverage.keySet()) {
            writer.println(name + "-" + patchCoverage.get(name));
        }
        writer.close();

        writer = new PrintWriter(new File("data/coverage-experiments/disjoint.data"));
        for (String name : disjoint) {
            writer.println(name);
        }
        writer.close();

        writer = new PrintWriter(new File("data/coverage-experiments/same.data"));
        for (String name : same) {
            writer.println(name);
        }
        writer.close();

        writer = new PrintWriter(new File("data/coverage-experiments/inBetween.data"));
        for (String name : inBetween) {
            writer.println(name);
        }
        writer.close();
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
