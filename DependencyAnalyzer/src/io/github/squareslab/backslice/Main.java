package io.github.squareslab.backslice;


import io.github.squareslab.backslice.common.Utils;
import soot.PackManager;
import soot.Transform;

import java.util.ArrayList;
import java.util.Collection;

public class Main
{
	private static final String ANALYSIS_NAME = "jap.pdg";

	/**
	 *
	 * @param args source array
	 * @param startIndex starting index (inclusive)
	 * @param endIndex ending index (exclusive)
	 * @return a collection of integers parsed from args from the starting index to the ending index
	 */
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

	public static void main(String[] args)
	{
		if (args.length < 3)
		{
			System.err.println("Usage: 0th argument: classpath to the analysis target (containing the class to analyze)");
			System.err.println("1st argument: the class to analyze, a .class file");
			System.err.println("subsequent argument(s): the source code line numbers to analyze");
			System.exit(1);
		}

		String classpathToAnalysisTarget = args[0];
		String classToAnalyze = args[1];
		Collection<Integer> lineNumsOfInterest = parseIntArgs(args, 2, args.length);

		PackManager.v().getPack("jap").add(new Transform(ANALYSIS_NAME, new IntraproceduralPDGAnalysis(lineNumsOfInterest)));
		String[] sootArgs = Utils.getSootArgs(ANALYSIS_NAME, classpathToAnalysisTarget, classToAnalyze);
		Utils.runSoot(sootArgs);
	}
}
