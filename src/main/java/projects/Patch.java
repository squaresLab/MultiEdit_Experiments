package projects;

import multiedit.faultloc.CoverageSubset;

import java.util.Collection;

public interface Patch {

    enum Version {
        BUGGY,
        PATCHED
    }

    String getPatchName();

    Collection<String> getPassingTests();
    Collection<String> getRelevantTests();
    Collection<String> getFailingTests();

    String getPathToBuggySubjectClasses();
    String getPathToBuggyTestClasses();
    String getPathToPatchedSubjectClasses();
    String getPathToPatchedTestClasses();
    String getBuggyClassPath();
    String getPatchedClassPath();

    CoverageSubset getPatchLocations();
}
