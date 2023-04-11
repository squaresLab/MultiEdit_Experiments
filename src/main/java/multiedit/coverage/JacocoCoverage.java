package multiedit.coverage;

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
import projects.Patch;

public class JacocoCoverage {

    public static Collection<CoverageSubset> getCoveragePassingTests(Patch patch, Patch.Version whichVersion) {
        return getCoverageSomeTests(patch, patch.getPassingTests(), whichVersion);
    }

    public static Collection<CoverageSubset> getCoverageFailingTests(Patch patch, Patch.Version whichVersion) {
        return getCoverageSomeTests(patch, patch.getFailingTests(), whichVersion);
    }

    public static Collection<CoverageSubset> getCoverageRelevantTests(Patch patch, Patch.Version whichVersion) {
        return getCoverageSomeTests(patch, patch.getRelevantTests(), whichVersion);
    }

    public static Collection<CoverageSubset> getCoverageSomeTests(Patch p, Collection<String> whichTests, Patch.Version whichVersion) {
        List<CoverageSubset> testCaseCoverage = new ArrayList<>();

        for (String t : whichTests) {
            CoverageSubset coverageInfo = getCoverageForTest(p, t, whichVersion);
            testCaseCoverage.add(coverageInfo);

        }

        return testCaseCoverage;
    }

    public static CoverageSubset getCoverageForTest(Patch patch, String tc, Patch.Version whichVersion) {
        internalTestCase(tc, patch, whichVersion);

        Map<String, Set<Integer>> coverageInfo;
        try {
            File jacocoFile = new File("jacoco.exec");
            coverageInfo = getCoverageInfo(jacocoFile, patch, whichVersion);
            jacocoFile.delete();
            System.out.println("Jacoco file exists: " + jacocoFile.exists());
        } catch (IOException e) {
            throw new RuntimeException("Could not get coverage for " + tc, e);
        }
        CoverageSubset testCase = new CoverageSubset(tc);
        testCase.addAllClasses(coverageInfo);
        return testCase;

    }

    // code copied from genprog4java DefaultLocalization.getCoverageInfo()
    protected static Map<String, Set<Integer>> getCoverageInfo(File jacocoFile, Patch patch, Patch.Version whichVersion) throws IOException {
        Map<String, Set<Integer>> classCoverage = new HashMap<String, Set<Integer>>();

        File classFiles;

        if (whichVersion == Patch.Version.PATCHED) {
            classFiles = new File(patch.getPatchedClasses());
        } else {
            classFiles = new File(patch.getBuggyClasses());
        }

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
        analyzer.analyzeAll(classFiles);

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

            String publicClassName;
            try {
                publicClassName = cc.getPackageName() + "/" + cc.getSourceFileName().split("\\.")[0];
            } catch (NullPointerException e) {
                System.err.println("Unable to find class " + cc.toString());
                continue;
            }
            Set<Integer> existing = classCoverage.getOrDefault(publicClassName, new HashSet<>());
            existing.addAll(coveredLines);
            classCoverage.put(publicClassName, existing);
        }
        boolean noCoverage = classCoverage.entrySet().stream().allMatch(e -> e.getValue().isEmpty());
        if (noCoverage) throw new IllegalStateException("No coverage was recorded for " + patch.toString());
        return classCoverage;

    }

    protected static void internalTestCase(String thisTest, Patch patch, Patch.Version whichVersion) {

        CommandLine command = patch.getTestCommand(thisTest, whichVersion);
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
                throw new RuntimeException(exception);
//                exception.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null)
                try {
                    out.flush();
                    String output = out.toString();
                    out.reset();
                    System.out.println("OUTPUT: " + thisTest);
                    System.out.println(output);
                    out.close();
                } catch (IOException e) {
                    // you know, having to either catch or throw
                    // all exceptions is really tedious.
                }
        }
    }

}
