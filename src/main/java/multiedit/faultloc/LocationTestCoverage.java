package multiedit.faultloc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import org.apache.commons.exec.*;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import util.TestCase;

public class LocationTestCoverage {

    // possible TODO: assert that passing tests pass and failing tests fail
    public CoverageCalculator getCoverageAllTests(Collection<String> passingTests, Collection<String> failingTests, String pathToSubjectClasses, String pathToTestClasses) {
        CoverageCalculator coverageCalculator = new CoverageCalculator();

        for (String t : passingTests) {
            TestCase tc = new TestCase(TestCase.TestType.POSITIVE, t);
            internalTestCase(tc, pathToSubjectClasses, pathToTestClasses);

            Map<String, Set<Integer>> coverageInfo;
            try {
                File jacocoFile = new File("jacoco.exec");
                coverageInfo = getCoverageInfo(jacocoFile, pathToSubjectClasses);
                jacocoFile.delete();
            } catch (IOException e) {
                throw new RuntimeException("Could not get coverage for " + t, e);
            }

            coverageCalculator.addTestCoverage(tc, coverageInfo);
        }

        for (String t : failingTests) {
            TestCase tc = new TestCase(TestCase.TestType.NEGATIVE, t);
            internalTestCase(tc, pathToSubjectClasses, pathToTestClasses);

            Map<String, Set<Integer>> coverageInfo;
            try {
                File jacocoFile = new File("jacoco.exec");
                coverageInfo = getCoverageInfo(jacocoFile, pathToSubjectClasses);
                jacocoFile.delete();
            } catch (IOException e) {
                throw new RuntimeException("Could not get coverage for " + t, e);
            }

            coverageCalculator.addTestCoverage(tc, coverageInfo);
        }

        return coverageCalculator;
    }

    // code copied from genprog4java DefaultLocalization.getCoverageInfo()
    public Map<String, Set<Integer>> getCoverageInfo(File jacocoFile, String pathToSubjectClasses) throws IOException {
        Map<String, Set<Integer>> classCoverage = new HashMap<String, Set<Integer>>();

        ExecutionDataStore executionData = new ExecutionDataStore();

        final FileInputStream in = new FileInputStream(jacocoFile);
        final ExecutionDataReader reader = new ExecutionDataReader(in);
        reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
            public void visitSessionInfo(final SessionInfo info) {
            }
        });
        reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
            public void visitClassExecution(final ExecutionData data) {
                executionData.put(data);
            }
        });

        reader.read();
        in.close();

        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData,
                coverageBuilder);
        File file = new File(pathToSubjectClasses);
        analyzer.analyzeAll(file);

        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            TreeSet<Integer> coveredLines = new TreeSet<Integer>();

            for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
                boolean covered = false;
                switch (cc.getLine(i).getStatus()) {
                    case ICounter.PARTLY_COVERED:
                    case ICounter.FULLY_COVERED:
                        covered = true;
                        break;

                    case ICounter.NOT_COVERED:
                    case ICounter.EMPTY:
                    default:
                        break;
                }
                if (covered) {
                    coveredLines.add(i);
                }
            }
//            System.out.println(cc.getName());
//            System.out.println("covered lines?");
//            System.out.println(coveredLines);

            classCoverage.put(cc.getName(), coveredLines);
        }
        return classCoverage;

    }

    public void internalTestCase(TestCase thisTest, String pathToSubjectClasses, String pathToTestClasses) {

        CommandLine command = internalTestCaseCommand(
                thisTest, pathToSubjectClasses, pathToTestClasses);
        System.out.println(command);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(96000);
        DefaultExecutor executor = new DefaultExecutor();
        String workingDirectory = System.getProperty("user.dir");
        executor.setWorkingDirectory(new File(workingDirectory));
        executor.setWatchdog(watchdog);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        executor.setExitValue(0);

        executor.setStreamHandler(new PumpStreamHandler(out));

        try {
            try {
                executor.execute(command);
            } catch (ExecuteException exception) {
                exception.printStackTrace();
            }
            out.flush();
            String output = out.toString();
            out.reset();
            System.out.println("OUTPUT: " + thisTest);
            System.out.println(output);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    // you know, having to either catch or throw
                    // all exceptions is really tedious.
                }
        }
    }

    public CommandLine internalTestCaseCommand(TestCase test, String pathToSubjectClasses, String pathToTestClasses) {
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

        String classPath = outputDir + System.getProperty("path.separator")
                + pathToSubjectClasses + System.getProperty("path.separator")
                + pathToTestClasses + System.getProperty("path.separator")
                + "lib/junit-4.13-rc-2.jar" + System.getProperty("path.separator")
                + "lib/hamcrest-all-1.3.jar";

        // Positive tests
        command.addArgument("-classpath");
        command.addArgument(classPath);

        command.addArgument("-Xmx1024m");
        command.addArgument("-javaagent:" + "lib/jacocoagent.jar"
                + "=excludes=org.junit.*,append=false");

        command.addArgument("util.JUnitTestRunner");

        command.addArgument(test.toString());
        return command;

    }
}
