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
    public String getPathToBuggySubjectClasses() {
        return pathToBuggySubjectClasses;
    }

    @Override
    public String getPathToBuggyTestClasses() {
        return pathToBuggyTestClasses;
    }

    @Override
    public String getPathToPatchedSubjectClasses() {
        return pathToPatchedSubjectClasses;
    }

    @Override
    public String getPathToPatchedTestClasses() {
        return pathToPatchedTestClasses;
    }

    @Override
    public String getBuggyClassPath() {
        return buggyClassPath;
    }

    @Override
    public String getPatchedClassPath() {
        return patchedClassPath;
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
        System.out.println(command);
        CommandLineRunner.runCommand(command);
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
