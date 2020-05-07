package projects;

import multiedit.coverage.CoverageSubset;
import org.apache.commons.exec.CommandLine;

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

    String getBuggyClasses();
    String getPatchedClasses();



    CommandLine getTestCommand(String test, Version version);

    CoverageSubset getPatchLocationsInPatched();
    CoverageSubset getPatchLocationsInBuggy();
}
