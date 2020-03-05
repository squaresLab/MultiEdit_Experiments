package projects;

import multiedit.coverage.CoverageSubset;
import org.apache.commons.exec.CommandLine;
import util.CommandLineRunner;
import util.PatchDiffUtils;

import java.io.*;
import java.util.*;

public class Defects4JPatch implements Patch {

    private D4JName projectName;
    private int bugNumber;
    private String d4jWorkingDir;
    private Collection<String> passingTests, failingTests, relevantTests;
    private String pathToBuggySubjectClasses, pathToBuggyTestClasses, pathToPatchedSubjectClasses, pathToPatchedTestClasses;
    private String buggyClassPath, patchedClassPath;
    private CoverageSubset patchLocations;

    public Defects4JPatch(D4JName projectName, int bugNumber) {
        if (bugNumber <= 0 || bugNumber > projectName.numBugs) {
            throw new IllegalArgumentException("Invalid bug number " + bugNumber + " for " + projectName);
        }

        String d4jWorkingDir = String.format("tmp/%s-%03d", projectName.getID(), bugNumber);

        this.projectName = projectName;
        this.bugNumber = bugNumber;
        this.d4jWorkingDir = d4jWorkingDir;

        try {
            checkoutD4JProject();
            parseFields();
        } catch (Exception e) {
            throw new RuntimeException("Failed to checkout D4J Project: " + projectName + " " + bugNumber, e);
        }
    }

    @Override
    public String getPatchName() {
        return String.format("%s:%03d", projectName, bugNumber);
    }

    @Override
    public Collection<String> getPassingTests() {
        // TODO: maybe this should do the right thing one day
        // the contents of `this.passingTests` is empty
        // there is no way to get only passing tests out of the box in D4J, apparently
        return this.passingTests;
    }

    @Override
    public Collection<String> getRelevantTests() {
        return relevantTests;
    }

    @Override
    public Collection<String> getFailingTests() {
        return failingTests;
    }

    @Override
    public String getBuggyClasses() {
        return pathToBuggySubjectClasses;
    }

    public String getPathToBuggyTestClasses() {
        return pathToBuggyTestClasses;
    }

    @Override
    public String getPatchedClasses() {
        return pathToPatchedSubjectClasses;
    }

    public String getPathToPatchedTestClasses() {
        return pathToPatchedTestClasses;
    }

    public String getBuggyClassPath() {
        return buggyClassPath;
    }

    public String getPatchedClassPath() {
        return patchedClassPath;
    }

    @Override
    public CommandLine getTestCommand(String test, Version version) {
        // read in the test files to get a list of test class names
        // store it in the testcase object, which will be the name
        // this is a little strange because each test class has multiple
        // test cases in it. I think we can actually change this behavior
        // through various
        // hacks on StackOverflow, but for the time being I just want something
        // that works at all
        // rather than a perfect implementation. One thing at a time.
        CommandLine command = CommandLine.parse("java");
        String outputDir = "target/classes";

        String classPath = outputDir;

        if (version == Patch.Version.BUGGY) {
            classPath += System.getProperty("path.separator")
                    + this.getBuggyClasses() + System.getProperty("path.separator")
                    + this.getPathToBuggyTestClasses() + System.getProperty("path.separator")
                    + this.getBuggyClassPath();
        } else if (version == Patch.Version.PATCHED) {
            classPath += System.getProperty("path.separator")
                    + this.getPatchedClasses() + System.getProperty("path.separator")
                    + this.getPathToPatchedTestClasses()+ System.getProperty("path.separator")
                    + this.getPatchedClassPath();
        }

        command.addArgument("-classpath");
        command.addArgument(classPath);

        command.addArgument("-Xmx1024m");
        command.addArgument("-javaagent:" + "lib/jacocoagent.jar"
                + "=excludes=org.junit.*,append=false");

        command.addArgument("util.JUnitTestRunner");

        command.addArgument(test);
        return command;
    }

    @Override
    public CoverageSubset getPatchLocations() {
        return patchLocations;
    }

    public void deleteDirectories() throws IOException {
        CommandLine command = new CommandLine("rm");
        command.addArgument("-rf");
        command.addArgument(d4jWorkingDir);
        System.out.println(command);
        CommandLineRunner.runCommand(command);
    }

    private void checkoutD4JProject() throws IOException {
        CommandLine command = new CommandLine("src/main/bash/checkoutD4J.sh");
        command.addArgument(projectName.getID());
        command.addArgument(String.valueOf(bugNumber));
        command.addArgument(d4jWorkingDir);
        System.out.println(String.join(" ", command.toStrings()));
        if (projectName == D4JName.MOCKITO || (projectName == D4JName.CHART && bugNumber == 5)) {
            // defects4j commands are super flaky through the command line runner, but work perfectly fine in my shell
            try {
                Thread.sleep(300_000); // run the damn thing yourself.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            int tries = 0;
            while (true) {
                try {
                    CommandLineRunner.runCommand(command);
                } catch (Exception e) {
                    if (tries >= 5) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(++tries);
                    continue;
                }
                break;
            }
        }

    }

    private void parseFields() throws Exception {
        File configFile = new File(d4jWorkingDir + "/defects4j.config");
        Map<String, String> properties = new HashMap<>();

        CommandLineRunner.lineIterator(configFile, s -> {
            String[] prop = s.split("=");
            properties.put(prop[0], prop[1]);
        });

        this.pathToBuggySubjectClasses = properties.get("buggyClassFolder");
        this.pathToBuggyTestClasses = properties.get("buggyTestFolder");
        this.pathToPatchedSubjectClasses = properties.get("patchedClassFolder");
        this.pathToPatchedTestClasses = properties.get("patchedTestFolder");
        this.buggyClassPath = properties.get("buggySrcClassPath") + System.getProperty("path.separator")
                + properties.get("buggyTestClassPath") + System.getProperty("path.separator")
                + "lib/junit-4.13-rc-2.jar";
        this.patchedClassPath = properties.get("patchedSrcClassPath") + System.getProperty("path.separator")
                + properties.get("patchedTestClassPath") + System.getProperty("path.separator")
                + "lib/junit-4.13-rc-2.jar";

        // test names
        this.passingTests = new ArrayList<>();

        this.relevantTests = new ArrayList<>();
        CommandLineRunner.lineIterator(new File(properties.get("relevantTests")), s -> relevantTests.add(s));

        this.failingTests = new ArrayList<>();
        CommandLineRunner.lineIterator(new File(properties.get("negativeTests")), s -> failingTests.add(s));

        // calculate diff
        Collection<String> modifiedClasses = Arrays.asList(properties.get("modifiedClasses").split(":"));
        
        this.patchLocations = PatchDiffUtils.getPatchLineNumbers(properties.get("buggySource"), properties.get("patchedSource"), modifiedClasses);

    }

}
