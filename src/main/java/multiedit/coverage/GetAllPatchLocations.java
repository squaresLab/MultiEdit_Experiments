package multiedit.coverage;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import projects.BearsPatch;
import projects.D4JName;
import projects.Defects4JPatch;
import projects.Patch;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GetAllPatchLocations {
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

        List<Pair<String, CoverageSubset>> patches = new ArrayList<>();
        /*
          Defects4J
         */
        for (D4JName n : D4JName.values()) {
            if (n != D4JName.MOCKITO) continue;
//                if(true) continue;
            for (int i: n.bugs) {

                if (!d4jMultitestMultiedit.get(n).contains(i)) {
                    continue;
                }

                Defects4JPatch p;
                Collection<CoverageSubset> coverageFailingTests;
                try {
                    p = new Defects4JPatch(n, i);
                    patches.add(new ImmutablePair<>(n + " " + i, p.getPatchLocationsInPatched()));
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.out.println("FAILED: " + n + " " + i);
                    continue;
                }
                p.deleteDirectories();
            }
        }


        /*
          Bears
         */
        for (int i = 1; i <= BearsPatch.TOTAL_BUGS; i++) {
            if (true) continue;
            if (!bearsMultitestMultiedit.contains(i)) continue;

            try {
                BearsPatch b = new BearsPatch(i);
                patches.add(new ImmutablePair<>("Bears " + i, b.getPatchLocationsInPatched()));
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.out.println("FAILED: BEARS " + i);
                continue;
            }
        }


        for (Pair<String, CoverageSubset> p : patches) {
            System.out.println(p.getLeft());
            System.out.println(p.getRight());
            System.out.println("\n----------------\n");
        }
    }
}
