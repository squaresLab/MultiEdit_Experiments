package io.github.squareslab;


import io.github.squareslab.analysis.ControlDependencyAnalysis;
import io.github.squareslab.analysis.DataDependencyAnalysis;
import io.github.squareslab.common.Utils;
import soot.PackManager;
import soot.Transform;

import java.util.ArrayList;
import java.util.Collection;

public class Main
{
	private static void printUsageMessage()
	{
		System.err.println("Usage: 0th argument: classpath to the analysis target (containing the class to analyze)");
		System.err.println("1st argument: the class to analyze, a .class file");
		System.err.println("2nd argument: the analysis to run");
			System.err.println("\t000 for control dependency analysis");
			System.err.println("\t100 for data flow dependency analysis");
			System.err.println("\t010 for data anti dependency analysis");
			System.err.println("\t001 for data output dependency analysis");
			System.err.println("\t110 for data flow+anti dependency analysis");
			System.err.println("\t011 for data anti+output dependency analysis");
			System.err.println("\t111 for data flow+anti+output dependency analysis");
		System.err.println("subsequent argument(s): the source code line numbers to analyze");
	}

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

	private static DataDependencyAnalysis.Configuration getConfiguration(String analysisToRunArg)
	{
		assert analysisToRunArg.length() == 3;
		boolean flow   = analysisToRunArg.charAt(0) == '1';
		boolean anti   = analysisToRunArg.charAt(1) == '1';
		boolean output = analysisToRunArg.charAt(2) == '1';
		return new DataDependencyAnalysis.Configuration(flow, anti, output);
	}

	public static void main(String[] args)
	{
		//todo: add option to select ctrl dependency or data dependency analysis
		if (args.length < 4)
		{
			printUsageMessage();
			System.exit(1);
		}

		String classpathToAnalysisTarget = args[0];
		String classToAnalyze = args[1];
		String analysisToRun = args[2];
		Collection<Integer> lineNumsOfInterest = parseIntArgs(args, 3, args.length);

		String[] sootArgs;
		if(analysisToRun.equals("000"))
		{
			PackManager.v().getPack("jap")
					.add(new Transform(ControlDependencyAnalysis.ANALYSIS_NAME, new ControlDependencyAnalysis(lineNumsOfInterest)));
			sootArgs = Utils.getSootArgs(ControlDependencyAnalysis.ANALYSIS_NAME, classpathToAnalysisTarget, classToAnalyze);
		}
		else
		{
			DataDependencyAnalysis.Configuration config = getConfiguration(analysisToRun);
			PackManager.v().getPack("jap")
					.add(new Transform(DataDependencyAnalysis.ANALYSIS_NAME, new DataDependencyAnalysis(lineNumsOfInterest, config)));
			sootArgs = Utils.getSootArgs(DataDependencyAnalysis.ANALYSIS_NAME, classpathToAnalysisTarget, classToAnalyze);
		}

		Utils.runSoot(sootArgs);
	}
}
