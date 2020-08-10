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

    public String pathToBears = System.getProperty("user.home") + "/bears-benchmark";
    private static final Set<Integer> unconventionalBugs = new HashSet<>(Arrays.asList(142, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 186, 187, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 205, 206, 207, 208, 210, 211, 212, 213, 214, 215, 218, 219, 220, 221, 222, 223, 224, 226, 227, 228, 229, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 251));
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
    private CoverageSubset patchLocationsInPatched;
    private CoverageSubset patchLocationsInBuggy;
    private final Scanner sysin = new Scanner(System.in);

    public BearsPatch(int bugNumber) {
        this.bugNumber = bugNumber;
        this.branchName = bugBranches.get(bugNumber);
    }

    @Override
    public String getPatchName() {
        return String.format("BEARS:%03d", bugNumber);
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
            if (version == Version.PATCHED) {
                checkoutPatched();
            } else {
                checkoutBuggy();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.bugNumber == 165) {
            modifyXML(this.pathToBears + "/dhis-2", "pom.xml");
        } else {
            modifyXML(this.pathToBears, "pom.xml");
        }

        CommandLine command = CommandLine.parse("sh");

        command.addArgument("src/main/bash/runBears.sh");
        command.addArgument(pathToBears);
        command.addArgument(test);
        return command;
    }

    private void modifyXML(String pathToPom, String pomName) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc;
            try {
                File xmlFile = new File(pathToPom + "/" + pomName);
                System.out.println(xmlFile.toPath());
                doc = builder.build(xmlFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            Element rootNode = doc.getRootElement();

            Element build = rootNode.getChild("build", rootNode.getNamespace());

            if (build == null) {
                build = new Element("build", rootNode.getNamespace());
                rootNode.addContent(build);
            }


            Element plugins = build.getChild("plugins", build.getNamespace());
            if (plugins == null && build.getChild("pluginManagement", rootNode.getNamespace()) != null) {
                build = build.getChild("pluginManagement", rootNode.getNamespace());
                plugins = build.getChild("plugins", build.getNamespace());
            }
            if (plugins == null) {
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
            xmlOutput.output(doc, new FileWriter(pathToPom + "/" + pomName));

            if (build.getName().contains("pluginManagement")) {
                // idk how to generalize this. maybe case by case is the best policy
                if (this.bugNumber == 191 || this.bugNumber == 192){
                    modifyXML(pathToPom + "/src/server", "pom.xml");
                } else {
                    modifyXML(pathToPom + "/dhis-api", "pom.xml");
                }
            }

        } catch (IOException | JDOMException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CoverageSubset getPatchLocationsInPatched() {
        if (patchLocationsInPatched == null) {
            setPatchLocations();
        }

        return this.patchLocationsInPatched;
    }

    @Override
    public CoverageSubset getPatchLocationsInBuggy() {
        if (patchLocationsInBuggy == null) {
            setPatchLocations();
        }

        return this.patchLocationsInBuggy;
    }

    private void setPatchLocations() {
        List<String[]> repairedFiles = patchFiles.get(bugNumber);

        CoverageSubset locationsInPatched = new CoverageSubset(branchName + " patch");
        CoverageSubset locationsInBuggy = new CoverageSubset(branchName + " buggy");
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
                System.out.println("Path: " + path);
                if (branchName.startsWith("traccar-traccar")) {
                    String filename = path.split("/src/")[1];
                    className = filename.substring(0, filename.length() - 5);
                } else {
                    className = path.split(".java.?")[1]; // should remove the "src/main/java/ and ".java"
                }

                System.out.println("Classname: " + className);

                CoverageSubset classDiff = PatchDiffUtils.getPatchLineNumbersTarget(
                        buggySources.get(originalFiles.get(i)),
                        patchedSources.get(patchedFiles.get(i)),
                        className);
                locationsInPatched.addAllClasses(classDiff.getClassCoverageMap());

                classDiff = PatchDiffUtils.getPatchLineNumbersSource(
                        buggySources.get(originalFiles.get(i)),
                        patchedSources.get(patchedFiles.get(i)),
                        className);
                locationsInBuggy.addAllClasses(classDiff.getClassCoverageMap());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.patchLocationsInPatched = locationsInPatched;
        this.patchLocationsInBuggy = locationsInBuggy;
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

    @Override
    public String toString() {
        return this.getPatchName();
    }

}
