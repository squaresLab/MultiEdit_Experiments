package projects;

import org.apache.commons.exec.CommandLine;

import multiedit.coverage.CoverageSubset;
import util.CommandLineRunner;
import util.PatchDiffUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class BearsPatch implements Patch {

    private static final String pathToBears = System.getProperty("user.home") + "/bears-benchmark";
    public static Map<Integer, String> allBugs = new HashMap<>();
    public static Map<Integer, List<String[]>> patchFiles = new HashMap<>();
    static {
        try {
            CommandLineRunner.lineIterator(new File("src/main/resources/bears/bug_id_and_branch.txt"), line -> {
                String[] items = line.split(",");
                allBugs.put(Integer.parseInt(items[0]), items[1]);
            });
            CommandLineRunner.lineIterator(new File("src/main/resources/bears/modifiedfiles.txt"), line -> {
                String[] items = line.split(",");
                List<String[]> gitDiffFiles = new ArrayList<>();
                for (int i = 1; i < items.length; i++) {
                    // ["diff", "--git", "a/<patched>", "b/<original>"]
                    String[] diffItems = items[i].split(" ");
                    String patched = diffItems[2].substring(2); //remove "a/"
                    String original = diffItems[3].substring(2); // lots of magic numbers; apologies :/
                    gitDiffFiles.add(new String[]{original, patched});
                }

                patchFiles.put(Integer.parseInt(items[0]), gitDiffFiles);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int bugNumber;
    private String branchName;
    List<String> failingTests;
    private CoverageSubset patchLocations;

    public BearsPatch(int bugNumber) {
        this.bugNumber = bugNumber;
        this.branchName = allBugs.get(bugNumber);
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
        if (patchLocations == null) {
            List<String[]> repairedFiles = patchFiles.get(bugNumber);

            CoverageSubset patch = new CoverageSubset(branchName);
            try {
                checkoutBuggy();
                List<String> originalFiles = repairedFiles.stream().map(files -> pathToBears + "/" + files[0]).collect(Collectors.toList());
                Map<String, List<String>> buggySources = PatchDiffUtils.getSourceCodeForClasses(originalFiles);
                checkoutPatched();
                List<String> patchedFiles = repairedFiles.stream().map(files -> pathToBears + "/" + files[1]).collect(Collectors.toList());
                Map<String, List<String>> patchedSources = PatchDiffUtils.getSourceCodeForClasses(patchedFiles);

                for (int i = 0; i < originalFiles.size(); i++) {
                    String className;
                    String path = originalFiles.get(i);
                    if (branchName.startsWith("traccar-traccar")) {
                        className = path.substring(4, path.length() - 5);
                    } else {
                        className = path.split(".java.?")[1]; // should remove the "src/main/java/ and ".java"
                    }
                    CoverageSubset classDiff = PatchDiffUtils.getPatchLineNumbers(
                            buggySources.get(originalFiles.get(i)),
                            patchedSources.get(patchedFiles.get(i)),
                            className);
                    patch.addAllClasses(classDiff.getClassCoverageMap());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.patchLocations = patch;
        }

        return this.patchLocations;
    }

    private void checkoutBuggy() throws IOException {
        CommandLine command = CommandLine.parse("src/main/bash/checkoutBearsBuggy.sh");
        command.addArgument(branchName);
        command.addArgument(pathToBears);
        System.out.println(command);
        CommandLineRunner.runCommand(command);
    }

    private void checkoutPatched() throws IOException {
        CommandLine command = CommandLine.parse("src/main/bash/checkoutBearsPatched.sh");
        command.addArgument(branchName);
        command.addArgument(pathToBears);
        System.out.println(command);
        CommandLineRunner.runCommand(command);
    }

}
