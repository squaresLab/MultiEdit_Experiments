package projects;

import org.apache.commons.exec.CommandLine;

import multiedit.coverage.CoverageSubset;
import util.CommandLineRunner;
import util.PatchDiffUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class BearsPatch implements Patch {

    public static final String pathToBears = System.getProperty("user.home") + "/bears-benchmark";
    private static Map<Integer, String> bugBranches = new HashMap<>();
    private static Map<Integer, List<String[]>> patchFiles = new HashMap<>();
    private static Map<Integer, List<String>> failingTests = new HashMap<>();
    public static final int TOTAL_BUGS = 251;
    static {
        try {
            CommandLineRunner.lineIterator(new File("src/main/resources/bears/bug_id_and_branch.txt"), line -> {
                String[] items = line.split(",");
                bugBranches.put(Integer.parseInt(items[0]), items[1]);
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

            CommandLineRunner.lineIterator(new File("src/main/resources/bears/bearstests.txt"), line -> {
                List<String> items = new ArrayList<>(Arrays.asList(line.split(",")));
                int bugId = Integer.parseInt(items.remove(0));
                failingTests.put(bugId, items);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int bugNumber;
    private String branchName;
    private CoverageSubset patchLocations;

    public BearsPatch(int bugNumber) {
        this.bugNumber = bugNumber;
        this.branchName = bugBranches.get(bugNumber);
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
        return failingTests.get(bugNumber);
    }

    @Override
    public String getBuggyClasses() {
        try {
            checkoutBuggy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pathToBears + "/target/classes";
    }

    @Override
    public String getPatchedClasses() {
        try {
            checkoutPatched();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pathToBears + "/target/classes";
    }

    @Override
    public CommandLine getTestCommand(String test, Version version) {
//        try {
//            checkoutPatched();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        CommandLine command = CommandLine.parse("java");
//
//        String classPath = "target/classes" + System.getProperty("path.separator")
//                + pathToBears + "/target/classes" + System.getProperty("path.separator")
//                + pathToBears + "/target/test-classes" + System.getProperty("path.separator")
//                + System.getProperty("user.home") + "/.m2/repository";
//
////        if (version == Patch.Version.BUGGY) {
////            classPath += System.getProperty("path.separator")
////                    + this.getBuggyClasses() + System.getProperty("path.separator")
////                    + this.getPathToBuggyTestClasses() + System.getProperty("path.separator")
////                    + this.getBuggyClassPath();
////        } else if (version == Patch.Version.PATCHED) {
//
////        }
//
//        command.addArgument("-classpath");
//        command.addArgument(classPath);
//
//        command.addArgument("-Xmx1024m");
//        command.addArgument("-javaagent:" + "lib/jacocoagent.jar"
//                + "=excludes=org.junit.*,append=false");
//
//        command.addArgument("util.JUnitTestRunner");
//
//        test = test.replace("#", "::");
//        command.addArgument(test);
//        return command;
//    }
//
//    public void help() {
        try {
            checkoutPatched();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CommandLine command = CommandLine.parse("sh");

        command.addArgument("src/main/bash/runBears.sh");
        command.addArgument(pathToBears);
        command.addArgument(test);
        return command;
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
