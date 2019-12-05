package multiedit.faultloc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TreeSet;

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

    private ExecutionDataStore executionData = null;



    // code copied from genprog4java DefaultLocalization.getCoverageInfo()
    public TreeSet<Integer> getCoverageInfo() throws IOException {
        TreeSet<Integer> atoms = new TreeSet<Integer>();

        //             class      source code in file
//        for (Map.Entry<ClassInfo, String> ele : source.entrySet()) {
//            ClassInfo targetClassInfo = ele.getKey();
//            String pathToCoverageClass = Configuration.outputDir + File.separator
//                    + "coverage/coverage.out" + File.separator + targetClassInfo.pathToClassFile();
//            File compiledClass = new File(pathToCoverageClass);
//            if(!compiledClass.exists()) {
//                pathToCoverageClass = Configuration.classSourceFolder + File.separator + targetClassInfo.pathToClassFile();
//                compiledClass = new File(pathToCoverageClass);
//            }

            if (executionData == null) {
                executionData = new ExecutionDataStore();
            }

            final FileInputStream in = new FileInputStream(new File(
                    "jacoco.exec"));
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
            File file = new File("/home/serenach/MultiEdit_Experiments/SmallTestSystem/target/test-classes");
            analyzer.analyzeAll(file);

            TreeSet<Integer> coveredLines = new TreeSet<Integer>();
            for (final IClassCoverage cc : coverageBuilder.getClasses()) {
                for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
                    boolean covered = false;
                    switch (cc.getLine(i).getStatus()) {
                        case ICounter.PARTLY_COVERED:
                            covered = true;
                            break;
                        case ICounter.FULLY_COVERED:
                            covered = true;
                            break;
                        case ICounter.NOT_COVERED:
                            break;
                        case ICounter.EMPTY:
                            break;
                        default:
                            break;
                    }
                    if (covered) {
                        coveredLines.add(i);
                    }
                }
            }
        System.out.println(coveredLines);
//            for (int line : coveredLines) {
//                ArrayList<Integer> atomIds = original.atomIDofSourceLine(line);
//                if (atomIds != null && atomIds.size() >= 0) {
//                    atoms.addAll(atomIds);
//                }
//            }
//        }
        return atoms;
    }

    public void internalTestCase(String sanityExename,
                                            String sanityFilename, TestCase thisTest, boolean doingCoverage) throws IOException {

        CommandLine command = internalTestCaseCommand(sanityExename,
                sanityFilename, thisTest, doingCoverage);
        System.out.println(command);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(96000);
        DefaultExecutor executor = new DefaultExecutor();
        String workingDirectory = System.getProperty("user.dir");
        executor.setWorkingDirectory(new File(workingDirectory));
        executor.setWatchdog(watchdog);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        executor.setExitValue(0);

        executor.setStreamHandler(new PumpStreamHandler(out));
//        FitnessValue posFit = new FitnessValue();

        try {
            executor.execute(command);
            out.flush();
            String output = out.toString();
            out.reset();
            System.out.println("OUTPUT");
            System.out.println(output);
//            posFit = CachingRepresentation.parseTestResults(
//                    thisTest.getTestName(), output);

        } catch (ExecuteException exception) {
            exception.printStackTrace();
            out.flush();
            String output = out.toString();
            out.reset();
            System.out.println("OUTPUT");
            System.out.println(output);
//            posFit.setAllPassed(false);
        } catch (Exception e) {
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
//        return posFit;
    }

    public CommandLine internalTestCaseCommand(String exeName,
                                               String fileName, TestCase test, boolean doingCoverage) {
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
                + "SmallTestSystem/target/classes" + System.getProperty("path.separator")
                + "SmallTestSystem/target/test-classes" + System.getProperty("path.separator")
                + "SmallTestSystem/target/SmallTestSystem-1.0-SNAPSHOT-tests.jar" + System.getProperty("path.separator")
                + "lib/junit-4.13-rc-2.jar" + System.getProperty("path.separator")
                + "lib/hamcrest-all-1.3.jar";

        // Positive tests
        command.addArgument("-classpath");
        command.addArgument(classPath);

        if (doingCoverage) {

            command.addArgument("-Xmx1024m");
            command.addArgument("-javaagent:" + "lib/jacocoagent.jar"
                    + "=excludes=org.junit.*,append=false");
        } else {
            command.addArgument("-Xms128m");
            command.addArgument("-Xmx256m");
            command.addArgument("-client");
        }

        command.addArgument("util.JUnitTestRunner");

        command.addArgument(test.toString());
        //logger.info("Command: " + command.toString());
        return command;

    }
}
