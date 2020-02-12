package io.github.squareslab;


import io.github.squareslab.analysis.ControlDependencyAnalysis;
import io.github.squareslab.analysis.DataDependencyAnalysis;
import io.github.squareslab.common.Utils;
import org.apache.commons.cli.*;
import soot.PackManager;
import soot.Transform;

import java.util.Collection;

public class Main
{
	private static Options defineOptions()
	{

		Options options = new Options();

		Option classToAnalyze = new Option("t", "target", true,
				"Target class to analyze.");
		classToAnalyze.setRequired(true);
		options.addOption(classToAnalyze);

		Option classpathToAnalysisTarget = new Option("tcp", "target-classpath", true,
				"Classpath to the analysis target.");
		classpathToAnalysisTarget.setRequired(true);
		options.addOption(classpathToAnalysisTarget);

		Option controlDependency = new Option("Dc", "find-control-dependencies", false,
				"Find control dependencies.");
		controlDependency.setRequired(false);
		options.addOption(controlDependency);

		Option flowDataDependency = new Option("Df", "find-flow-depencencies", false,
				"Find flow data dependencies (read-after-write).");
		flowDataDependency.setRequired(false);
		options.addOption(flowDataDependency);

		Option antiDataDependency = new Option("Da", "find-anti-depencencies", false,
				"Find anti-data dependencies (write-after-read).");
		antiDataDependency.setRequired(false);
		options.addOption(antiDataDependency);

		Option writeDataDependency = new Option("Dw", "find-write-depencencies", false,
				"Find write data dependencies (write-after-write).");
		writeDataDependency.setRequired(false);
		options.addOption(writeDataDependency);

		Option outputPath = new Option("o", "output", true,
				"File to write output to.");
		outputPath.setRequired(true);
		options.addOption(outputPath);

		Option outputDependencyMap = new Option("Om", "output-dependency-map", false,
				"Output a line-to-lines mapping of dependencies.");
		outputDependencyMap.setRequired(false);
		options.addOption(outputDependencyMap);

		Option outputDependencyExistence = new Option("Oe", "output-dependency-existence", false,
				"Output whether there exists a dependency between specified lines");
		outputDependencyExistence.setRequired(false);
		options.addOption(outputDependencyExistence);

		Option linesToAnalyze = new Option("lines", "lines-to-analyze", true,
				"Line(s) to back-slice from.");
		linesToAnalyze.setArgs(Option.UNLIMITED_VALUES);
		linesToAnalyze.setRequired(true);
		options.addOption(linesToAnalyze);

		return options;
	}

	private static CommandLine parseArgs(String[] args)
	{
		Options options = defineOptions();

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);

			return cmd;
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			//todo: use something more meaningful than empty string for cmdLineSyntax
			formatter.printHelp(" ", options);
			System.exit(1);
			return null;
		}
	}

	private static DataDependencyAnalysis.Configuration getDataDepConfiguration(String analysisToRunArg)
	{
		assert analysisToRunArg.length() == 3;
		boolean flow   = analysisToRunArg.charAt(0) == '1';
		boolean anti   = analysisToRunArg.charAt(1) == '1';
		boolean output = analysisToRunArg.charAt(2) == '1';
		return new DataDependencyAnalysis.Configuration(flow, anti, output);
	}

	public static void main(String[] args)
	{
		CommandLine cmdLine = parseArgs(args);

		String classpathToAnalysisTarget = args[0];
		String classToAnalyze = args[1];
		String analysisToRun = args[2];
		Collection<Integer> lineNumsOfInterest = null;

		String[] sootArgs;
		if(analysisToRun.equals("000"))
		{
			PackManager.v().getPack("jap")
					.add(new Transform(ControlDependencyAnalysis.ANALYSIS_NAME, new ControlDependencyAnalysis(lineNumsOfInterest)));
			sootArgs = Utils.getSootArgs(ControlDependencyAnalysis.ANALYSIS_NAME, classpathToAnalysisTarget, classToAnalyze);
		}
		else
		{
			DataDependencyAnalysis.Configuration config = getDataDepConfiguration(analysisToRun);
			PackManager.v().getPack("jap")
					.add(new Transform(DataDependencyAnalysis.ANALYSIS_NAME, new DataDependencyAnalysis(lineNumsOfInterest, config)));
			sootArgs = Utils.getSootArgs(DataDependencyAnalysis.ANALYSIS_NAME, classpathToAnalysisTarget, classToAnalyze);
		}

		Utils.runSoot(sootArgs);
	}
}
