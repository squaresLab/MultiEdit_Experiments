package projects;

import java.util.Collection;

public interface Project {

    Collection<String> getPassingTests();
    Collection<String> getFailingTests();
    String getPathToSubjectClasses();
    String getPathToTestClasses();
    String getClassPath();
}
