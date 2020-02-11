import io.github.squareslab.Main;
import io.github.squareslab.analysis.ControlDependencyAnalysis;
import io.github.squareslab.analysis.DataDependencyAnalysis;
import io.github.squareslab.common.Utils;
import org.junit.Test;
import soot.PackManager;
import soot.Transform;

import java.util.ArrayList;
import java.util.Collection;


public class TestHashMutablePDG
{
	@Test
	public void test()
	{
		String[] args = {"test-resources", "org.apache.commons.math3.util.ContinuedFraction", "170"};
		Main.main(args);
	}

	@Test
	public void testReadWriteAnalyzer()
	{
		String[] args = {"test-resources", "org.apache.commons.math3.util.ContinuedFraction", "170"};

		String classpathToAnalysisTarget = args[0];
		String classToAnalyze = args[1];
		Collection<Integer> lineNumsOfInterest = parseIntArgs(args, 2, args.length);

		PackManager.v().getPack("jap")
				.add(new Transform(DataDependencyAnalysis.ANALYSIS_NAME, new DataDependencyAnalysis(lineNumsOfInterest)));
		String[] sootArgs = Utils.getSootArgs(DataDependencyAnalysis.ANALYSIS_NAME, classpathToAnalysisTarget, classToAnalyze);
		Utils.runSoot(sootArgs);
	}

	private static Collection<Integer> parseIntArgs(String[] args, int startIndex, int endIndex)
	{
		Collection<Integer> parseOutput = new ArrayList<>(endIndex - startIndex);
		for(int i = startIndex; i < endIndex; i++)
		{
			try
			{
				int parsedInt = Integer.parseInt(args[i]);
				parseOutput.add(parsedInt);
			} catch (NumberFormatException e) {
				System.err.printf("Skipping %s since it's not a valid line number", args[i]);
			}
		}

		return parseOutput;
	}
}
