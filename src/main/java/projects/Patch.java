package projects;

import java.util.Collection;

public interface Patch {

    enum Version {
        BUGGY,
        PATCHED
    }

    Collection<String> getPassingTests();
    Collection<String> getRelevantTests();
    Collection<String> getFailingTests();

    String getPathToBuggySubjectClasses();
    String getPathToBuggyTestClasses();
    String getPathToPatchedSubjectClasses();
    String getPathToPatchedTestClasses();
    String getBuggyClassPath();
    String getPatchedClassPath();
}
