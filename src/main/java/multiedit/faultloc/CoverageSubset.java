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
        if (this.classCoverageMap.containsKey(className)) {
            this.classCoverageMap.get(className).addAll(lineNumbers);
        } else {
            this.classCoverageMap.put(className, new HashSet<>(lineNumbers));
        }
    }

    public void addAllClasses(final Map<String, Set<Integer>> coverageInfo) {
        coverageInfo.keySet().forEach(k -> this.addClass(k, coverageInfo.get(k)));
    }

    public Map<String, Set<Integer>> getClassCoverageMap() {
        return classCoverageMap;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        String s = description + "\n";
        for (String c : classCoverageMap.keySet()) {
            s += c + ": " + classCoverageMap.get(c) + "\n";
        }
        return s;
    }

}
