package projects;

import multiedit.faultloc.CoverageSubset;
import util.CommandLineRunner;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BearsPatch implements Patch {

    private static final String pathToBears = System.getProperty("user.home") + "/bears-benchmark";
    public static Map<Integer, String> allBugs = new HashMap<>();
    static {
        try {
            CommandLineRunner.lineIterator(new File("src/main/resources/bears/bug_id_and_branch.txt"), line -> {
                String[] items = line.split(",");
                allBugs.put(Integer.parseInt(items[0]), items[1]);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int bugNumber;
    private String branchName;
    List<String> failingTests;

    public BearsPatch(int bugNumber) {
        this.bugNumber = bugNumber;
        this.branchName = allBugs.get(bugNumber);
        loadPatch();
    }

    @Override
    public String getPatchName() {
        return String.format("%s:%03d", branchName, bugNumber);
    }

    /**
     * Unimplemented
     * @return
     */
    @Override
    public Collection<String> getPassingTests() {
        return null;
    }

    /**
     * Unimplemented
     * @return
     */
    @Override
    public Collection<String> getRelevantTests() {
        return null;
    }

    @Override
    public Collection<String> getFailingTests() {
        return null;
    }

    @Override
    public String getPathToBuggySubjectClasses() {
        return null;
    }

    @Override
    public String getPathToBuggyTestClasses() {
        return null;
    }

    @Override
    public String getPathToPatchedSubjectClasses() {
        return null;
    }

    @Override
    public String getPathToPatchedTestClasses() {
        return null;
    }

    @Override
    public String getBuggyClassPath() {
        return null;
    }

    @Override
    public String getPatchedClassPath() {
        return null;
    }

    @Override
    public CoverageSubset getPatchLocations() {
        return null;
    }


    private void loadPatch() {

    }

}
