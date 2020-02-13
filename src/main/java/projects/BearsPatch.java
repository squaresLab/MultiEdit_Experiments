package projects;

import org.apache.commons.exec.CommandLine;

import multiedit.coverage.CoverageSubset;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.util.IteratorIterable;
import util.CommandLineRunner;
import util.PatchDiffUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class BearsPatch implements Patch {

    public static final String pathToBears = System.getProperty("user.home") + "/bears-benchmark";
    private static final Set<Integer> unconventionalBugs = new HashSet<>(Arrays.asList(142, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 210, 211, 212, 213, 214, 215, 216, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251));
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
    private final Scanner sysin = new Scanner(System.in);

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
        if (unconventionalBugs.contains(this.bugNumber)) {
            System.out.println("Bears Bug " + bugNumber);
            System.out.print("Path to classes (Move jacoco file over): ");
            System.out.flush();
            return pathToBears + sysin.nextLine() + "/target/classes";
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
        if (unconventionalBugs.contains(this.bugNumber)) {
            System.out.println("Bears Bug " + bugNumber);
            System.out.print("Path to classes (Move jacoco file over): ");
            System.out.flush();
            return pathToBears + sysin.nextLine() + "/target/classes";
        }
        return pathToBears + "/target/classes";
    }

    @Override
    public CommandLine getTestCommand(String test, Version version) {
        try {
            checkoutPatched();

            SAXBuilder builder = new SAXBuilder();
            File xmlFile = new File(pathToBears+"/pom.xml");

            Document doc = builder.build(xmlFile);
            Element rootNode = doc.getRootElement();

            Element build = rootNode.getChild("build", rootNode.getNamespace());

//            if (!buildDescendents.hasNext()) {
//                throw new IllegalStateException("no build field");
//            } else {
//                Element build = buildDescendents.next();
//                if (buildDescendents.hasNext()) {
//                    throw new IllegalStateException("Should only be one build element");
//                }

                Element plugins = build.getChild("plugins", build.getNamespace());
                if (plugins == null) {
                    System.out.println("what");
                    plugins = new Element("plugins", build.getNamespace());
                    build.addContent(plugins);
                }

                /*
                <plugin>
                    <groupId>org.jacoco</groupId>
                    <artifactId>jacoco-maven-plugin</artifactId>
                    <version>0.8.2</version>
                    <executions>
                        <execution>
                            <goals>
                                <goal>prepare-agent</goal>
                            </goals>
                        </execution>
                        <!-- attached to Maven test phase -->
                        <execution>
                            <id>report</id>
                            <phase>test</phase>
                            <goals>
                                <goal>report</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
                 */
                Element jacocoPlugin = new Element("plugin", build.getNamespace());
                jacocoPlugin.addContent(new Element("groupId", build.getNamespace()).setText("org.jacoco"));
                jacocoPlugin.addContent(new Element("artifactId", build.getNamespace()).setText("jacoco-maven-plugin"));
                jacocoPlugin.addContent(new Element("version", build.getNamespace()).setText("0.8.5"));
                Element executions = new Element("executions", build.getNamespace());
                executions.addContent(
                        new Element("execution", build.getNamespace()).addContent(
                                new Element("goals", build.getNamespace()).addContent(
                                        new Element("goal", build.getNamespace()).setText("prepare-agent")
                                )
                        )
                );
                executions.addContent(
                        new Element("execution", build.getNamespace())
                                .addContent(new Element("id", build.getNamespace()).setText("report"))
                                .addContent(new Element("phase", build.getNamespace()).setText("test"))
                                .addContent(
                                    new Element("goals", build.getNamespace()).addContent(
                                            new Element("goal", build.getNamespace()).setText("report")
                                    )
                                )
                );
                jacocoPlugin.addContent(executions);

                // I hate this solution
                IteratorIterable<Element> groupIds = plugins.getDescendants(Filters.element("groupId", build.getNamespace()));
                boolean[] hasJacoco = new boolean[]{false};
                groupIds.forEach(e -> hasJacoco[0] = hasJacoco[0] | e.getText().equals("org.jacoco"));

                if (!hasJacoco[0]) plugins.addContent(jacocoPlugin);

                XMLOutputter xmlOutput = new XMLOutputter();

                // display nice nice
                xmlOutput.setFormat(Format.getPrettyFormat());
                xmlOutput.output(doc, new FileWriter(pathToBears+"/pom.xml"));
//            }

        } catch (IOException | JDOMException e) {
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
