package util;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import multiedit.coverage.CoverageSubset;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class PatchDiffUtils {

    public static Map<String, List<String>> getSourceCodeForClasses(String pathToSourceDir, Collection<String> classes) throws IOException {
        Map<String, List<String>> sourceCode = new HashMap<>();

        for (String c : classes) {
            String pathToClass = c.replace('.', '/');

            List<String> source;
            try {
                source = Files.readAllLines(new File(pathToSourceDir + "/" + pathToClass + ".java").toPath(), StandardCharsets.ISO_8859_1);
            } catch (NoSuchFileException e) {
                source = new ArrayList<>();
            }
            sourceCode.put(c, source);
        }
        return sourceCode;
    }

    public static Map<String, List<String>> getSourceCodeForClasses(Collection<String> paths) throws IOException {
        Map<String, List<String>> sourceCode = new HashMap<>();

        for (String p : paths) {
            List<String> source;
            try {
                 source = Files.readAllLines(new File(p).toPath(), StandardCharsets.ISO_8859_1);
            } catch (NoSuchFileException e) {
                source = new ArrayList<>();
            }
            sourceCode.put(p, source);
        }
        return sourceCode;
    }

    public static CoverageSubset getPatchLineNumbersTarget(String pathToBuggySource, String pathToPatchedSource,
                                                           Collection<String> modifiedClasses) throws Exception {
        CoverageSubset patchLines = new CoverageSubset("Patch");

        for (String c : modifiedClasses) {
            String pathToClass = c.replace('.', '/');

            //build simple lists of the lines of the two testfiles
            List<String> original = Files.readAllLines(new File(pathToBuggySource + "/" + pathToClass + ".java").toPath(), StandardCharsets.ISO_8859_1);
            List<String> revised = Files.readAllLines(new File(pathToPatchedSource+ "/" + pathToClass + ".java").toPath(), StandardCharsets.ISO_8859_1);

            CoverageSubset classDiff = getPatchLineNumbersTarget(original, revised, pathToClass);
            patchLines.addAllClasses(classDiff.getClassCoverageMap());
        }

        return patchLines;
    }

    public static CoverageSubset getPatchLineNumbersTarget(List<String> originalSource, List<String> patchedSource, String className) throws DiffException {
        CoverageSubset patchLines = new CoverageSubset("Patch");
        Patch<String> patch = DiffUtils.diff(originalSource, patchedSource);

        List<Integer> lineNumbers = new ArrayList<>();
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            int start = delta.getTarget().getPosition();
            if (delta.getType().equals(DeltaType.DELETE)) {
                lineNumbers.add(delta.getTarget().getPosition());
                lineNumbers.add(delta.getTarget().getPosition() + 1);
            } else {
                for (int i = 1; i <= delta.getTarget().size(); i++) {
                    lineNumbers.add(start + i);
                }
            }
        }
        patchLines.addClass(className, new HashSet<>(lineNumbers));
        return patchLines;
    }

    public static CoverageSubset getPatchLineNumbersSource(String pathToBuggySource, String pathToPatchedSource,
                                                           Collection<String> modifiedClasses) throws Exception {
        CoverageSubset patchLines = new CoverageSubset("Patch");

        for (String c : modifiedClasses) {
            String pathToClass = c.replace('.', '/');

            //build simple lists of the lines of the two testfiles
            List<String> original = Files.readAllLines(new File(pathToBuggySource + "/" + pathToClass + ".java").toPath(), StandardCharsets.ISO_8859_1);
            List<String> revised = Files.readAllLines(new File(pathToPatchedSource+ "/" + pathToClass + ".java").toPath(), StandardCharsets.ISO_8859_1);

            CoverageSubset classDiff = getPatchLineNumbersSource(original, revised, pathToClass);
            patchLines.addAllClasses(classDiff.getClassCoverageMap());
        }

        return patchLines;
    }


    public static CoverageSubset getPatchLineNumbersSource(List<String> originalSource, List<String> patchedSource, String className) throws DiffException {
        CoverageSubset patchLines = new CoverageSubset("Patch");
        Patch<String> patch = DiffUtils.diff(originalSource, patchedSource);

        List<Integer> lineNumbers = new ArrayList<>();
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            int start = delta.getSource().getPosition();
            if (delta.getType().equals(DeltaType.INSERT)) {
                lineNumbers.add(delta.getSource().getPosition());
                lineNumbers.add(delta.getSource().getPosition() + 1);
            } else {
                for (int i = 1; i <= delta.getSource().size(); i++) {
                    lineNumbers.add(start + i);
                }
            }
        }
        patchLines.addClass(className, new HashSet<>(lineNumbers));
        return patchLines;
    }
}
