package util;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import multiedit.faultloc.CoverageSubset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class PatchDiffUtils {
    public static CoverageSubset getPatchLineNumbers(String pathToBuggySource, String pathToPatchedSource,
                                                     Collection<String> modifiedClasses) throws Exception {
        CoverageSubset patchLines = new CoverageSubset("Patch");

        for (String c : modifiedClasses) {
            String pathToClass = c.replace('.', '/');

            //build simple lists of the lines of the two testfiles
            List<String> original = Files.readAllLines(new File(pathToBuggySource + "/" + pathToClass + ".java").toPath());
            List<String> revised = Files.readAllLines(new File(pathToPatchedSource+ "/" + pathToClass + ".java").toPath());

            //compute the patch: this is the diffutils part
            Patch<String> patch = DiffUtils.diff(original, revised);

            List<Integer> lineNumbers = new ArrayList<>();
            for (AbstractDelta<String> delta : patch.getDeltas()) {
                int start = delta.getTarget().getPosition();
                for (int i = 0; i < delta.getTarget().size(); i++) {
                    lineNumbers.add(start + i);
                }
            }
            patchLines.addClass(pathToClass, new HashSet<>(lineNumbers));
        }

        return patchLines;
    }
}
