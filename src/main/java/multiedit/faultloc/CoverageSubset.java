package multiedit.faultloc;

import java.util.*;

public class CoverageSubset {

    private final String description;
    private final Map<String, Set<Integer>> classCoverageMap;

    public CoverageSubset(String desc) {
        this.description = desc;
        this.classCoverageMap = new HashMap<String, Set<Integer>>();
    }

    public void addClass(String className, Set<Integer> lineNumbers) {
        if (lineNumbers.size() == 0) {
            return;
        }
        if (this.classCoverageMap.containsKey(className)) {
            this.classCoverageMap.get(className).addAll(lineNumbers);
        } else {
            this.classCoverageMap.put(className, new HashSet<>(lineNumbers));
        }
    }

    public void addAllClasses(final Map<String, Set<Integer>> coverageInfo) {
        coverageInfo.keySet().forEach(k -> this.addClass(k, coverageInfo.get(k)));
    }

    public CoverageSubset intersection(CoverageSubset other) {
        return this.intersection(other, "intersection");
    }

    public CoverageSubset intersection(CoverageSubset other, String description) {
        CoverageSubset intersect = new CoverageSubset(description);
        Set<String> allClasses = new HashSet<>(this.classCoverageMap.keySet());
        allClasses.addAll(other.classCoverageMap.keySet());

        for (String c : allClasses) {
            Set<Integer> thisClassCoverage = new HashSet<>(this.classCoverageMap.getOrDefault(c, new HashSet<>()));
            Set<Integer> otherClassCoverage = new HashSet<>(other.classCoverageMap.getOrDefault(c, new HashSet<>()));
            thisClassCoverage.retainAll(otherClassCoverage);

            intersect.addClass(c, thisClassCoverage);
        }

        return intersect;
    }

    public Map<String, Set<Integer>> getClassCoverageMap() {
        return new HashMap<>(classCoverageMap);
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        String s = description + "\n";
        for (String c : classCoverageMap.keySet()) {
            if (classCoverageMap.get(c).size() != 0) {
                s += c + ": " + classCoverageMap.get(c) + "\n";
            }
        }
        return s;
    }

}
