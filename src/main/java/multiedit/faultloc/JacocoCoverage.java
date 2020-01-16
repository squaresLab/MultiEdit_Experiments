package multiedit.faultloc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
import projects.Patch;
import util.TestCase;

public class JacocoCoverage {

    public Collection<CoverageSubset> getCoveragePassingTests(Patch patch, Patch.Version whichVersion) {
        return getCoverageSomeTests(patch, patch.getPassingTests(), whichVersion);
    }

    public Collection<CoverageSubset> getCoverageFailingTests(Patch patch, Patch.Version whichVersion) {
        return getCoverageSomeTests(patch, patch.getFailingTests(), whichVersion);
    }

    public Collection<CoverageSubset> getCoverageRelevantTests(Patch patch, Patch.Version whichVersion) {
        return getCoverageSomeTests(patch, patch.getRelevantTests(), whichVersion);
    }

    public Collection<CoverageSubset> getCoverageSomeTests(Patch p, Collection<String> whichTests, Patch.Version whichVersion) {
        List<CoverageSubset> testCaseCoverage = new ArrayList<>();

        for (String t : whichTests) {
            TestCase tc = new TestCase(TestCase.TestType.POSITIVE, t);
            CoverageSubset coverageInfo = getCoverageForTest(p, tc, whichVersion);
            testCaseCoverage.add(coverageInfo);

        }

        return testCaseCoverage;
    }

    public CoverageSubset getCoverageForTest(Patch patch, TestCase tc, Patch.Version whichVersion) {
        internalTestCase(tc, patch, whichVersion);

        Map<String, Set<Integer>> coverageInfo;
        try {
            File jacocoFile = new File("jacoco.exec");
            coverageInfo = getCoverageInfo(jacocoFile, patch, whichVersion);
            jacocoFile.delete();
        } catch (IOException e) {
            throw new RuntimeException("Could not get coverage for " + tc.getTestName(), e);
        }
        CoverageSubset testCase = new CoverageSubset(tc.getTestName());
        testCase.addAllClasses(coverageInfo);
        return testCase;

    }

    // code copied from genprog4java DefaultLocalization.getCoverageInfo()
    protected Map<String, Set<Integer>> getCoverageInfo(File jacocoFile, Patch patch, Patch.Version whichVersion) throws IOException {
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
        File file;

        if (whichVersion == Patch.Version.PATCHED) {
            file = new File(patch.getPathToPatchedSubjectClasses());
        } else {
            file = new File(patch.getPathToBuggySubjectClasses());
        }
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

            String publicClassName = cc.getPackageName() + "/" + cc.getSourceFileName().split("\\.")[0];
            Set<Integer> existing = classCoverage.getOrDefault(publicClassName, new HashSet<>());
            existing.addAll(coveredLines);
            classCoverage.put(publicClassName, existing);
        }
        return classCoverage;

    }

    protected void internalTestCase(TestCase thisTest, Patch patch, Patch.Version whichVersion) {

        CommandLine command = internalTestCaseCommand(
                thisTest, patch, whichVersion);
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

    protected CommandLine internalTestCaseCommand(TestCase test, Patch patch, Patch.Version whichVersion) {
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

        if (whichVersion == Patch.Version.BUGGY) {
            classPath += System.getProperty("path.separator")
                    + patch.getPathToBuggySubjectClasses() + System.getProperty("path.separator")
                    + patch.getPathToBuggyTestClasses() + System.getProperty("path.separator")
                    + patch.getBuggyClassPath();
        } else if (whichVersion == Patch.Version.PATCHED) {
            classPath += System.getProperty("path.separator")
                    + patch.getPathToPatchedSubjectClasses() + System.getProperty("path.separator")
                    + patch.getPathToPatchedTestClasses()+ System.getProperty("path.separator")
                    + patch.getPatchedClassPath();
        }

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
