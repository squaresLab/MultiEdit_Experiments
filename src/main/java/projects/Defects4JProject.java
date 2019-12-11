package projects;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class Defects4JProject implements Project {

    private D4JName projectName;
    private int bugNumber;
    private String d4jWorkingDir;
    private Collection<String> passingTests, failingTests;
    private String pathToSubjectClasses, pathToTestClasses;
    private String classPath;

    public Defects4JProject(D4JName projectName, int bugNumber) {
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
        } catch (IOException e) {
            throw new RuntimeException("Failed to checkout D4J Project: " + projectName + " " + bugNumber, e);
        }
    }

    private void checkoutD4JProject() throws IOException {
        String workingDirectory = System.getProperty("user.dir");

        CommandLine command = new CommandLine("src/main/bash/checkoutD4J.sh");
        command.addArgument(projectName.getID());
        command.addArgument(String.valueOf(bugNumber));
        command.addArgument(d4jWorkingDir);
        System.out.println(command);

        ExecuteWatchdog watchdog = new ExecuteWatchdog(96000);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(workingDirectory));
        executor.setWatchdog(watchdog);

        executor.setExitValue(0);

        executor.execute(command);
    }

    private void parseFields() throws IOException {
        File configFile = new File(d4jWorkingDir + "/defects4j.config");
        Map<String, String> properties = new HashMap<>();
        lineIterator(configFile, s -> {
            String[] prop = s.split("=");
            properties.put(prop[0], prop[1]);
        });

        this.pathToSubjectClasses = properties.get("classFolder");
        this.pathToTestClasses = properties.get("testFolder");
        this.classPath = properties.get("srcClassPath") + System.getProperty("path.separator") + properties.get("testClassPath");

        this.passingTests = new ArrayList<>();
        lineIterator(new File(properties.get("relevantTests")), s -> passingTests.add(s));

        this.failingTests = new ArrayList<>();
        lineIterator(new File(properties.get("negativeTests")), s -> failingTests.add(s));
    }

    private void lineIterator(File f, Consumer<String> eachLine) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            eachLine.accept(line);
        }
        bufferedReader.close();
    }

    @Override
    public Collection<String> getPassingTests() {
        // TODO: maybe this should do the right thing one day
        // the contents of `this.passingTests` is all the tests that load the affected classes,
        // both passing and failing tests
        // there is no way to get only passing tests out of the box in D4J, apparently
        return new ArrayList<>();
    }

    @Override
    public Collection<String> getFailingTests() {
        return failingTests;
    }

    @Override
    public String getPathToSubjectClasses() {
        return pathToSubjectClasses;
    }

    @Override
    public String getPathToTestClasses() {
        return pathToTestClasses;
    }

    @Override
    public String getClassPath() {
        return classPath;
    }
}
