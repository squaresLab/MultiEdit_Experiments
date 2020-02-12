package io.github.squareslab;


import io.github.squareslab.analysis.ControlDependencyAnalysis;
import io.github.squareslab.analysis.DataDependencyAnalysis;
import io.github.squareslab.common.Utils;
import org.apache.commons.cli.*;
import soot.PackManager;
import soot.Transform;

import java.util.ArrayList;
import java.util.Collection;

public class Main
{
	private static final String OPTION_TARGET = "target";
	private static final String OPTION_TARGET_CP = "target-classpath";
	private static final String OPTION_CTRL_DEP =  "find-control-dependencies";
	private static final String OPTION_FLOW_DEP = "find-flow-dependencies";
	private static final String OPTION_ANTI_DEP = "find-anti-dependencies";
	private static final String OPTION_OUTPUT_DEP = "find-output-dependencies";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_OUT_MAP = "output-dependency-map";
	private static final String OPTION_OUT_EXIST = "output-dependency-existence";
	private static final String OPTION_LINES = "lines-to-analyze";


	private static Options defineOptions()
	{
		Options options = new Options();

		Option classToAnalyze = new Option("t", OPTION_TARGET, true,
				"Target class to analyze.");
		classToAnalyze.setRequired(true);
		options.addOption(classToAnalyze);

		Option classpathToAnalysisTarget = new Option("tcp", OPTION_TARGET_CP, true,
				"Classpath to the analysis target.");
		classpathToAnalysisTarget.setRequired(true);
		options.addOption(classpathToAnalysisTarget);

		Option controlDependency = new Option("Dc", OPTION_CTRL_DEP, false,
				"Find control dependencies.");
		controlDependency.setRequired(false);
		options.addOption(controlDependency);

		Option flowDataDependency = new Option("Df", OPTION_FLOW_DEP, false,
				"Find flow data dependencies (read-after-write).");
		flowDataDependency.setRequired(false);
		options.addOption(flowDataDependency);

		Option antiDataDependency = new Option("Da", OPTION_ANTI_DEP, false,
				"Find anti data dependencies (write-after-read).");
		antiDataDependency.setRequired(false);
		options.addOption(antiDataDependency);

		Option outputDataDependency = new Option("Do", OPTION_OUTPUT_DEP, false,
				"Find output data dependencies (write-after-write).");
		outputDataDependency.setRequired(false);
		options.addOption(outputDataDependency);

		Option outputPath = new Option("o", OPTION_OUTPUT, true,
				"File to write output to.");
		outputPath.setRequired(true);
		options.addOption(outputPath);

		Option outputDependencyMap = new Option("Om", OPTION_OUT_MAP, false,
				"Output a line-to-lines mapping of dependencies.");
		outputDependencyMap.setRequired(false);
		options.addOption(outputDependencyMap);

		Option outputDependencyExistence = new Option("Oe", OPTION_OUT_EXIST, false,
				"Output whether there exists a dependency between specified lines");
		outputDependencyExistence.setRequired(false);
		options.addOption(outputDependencyExistence);

		Option linesToAnalyze = new Option("lines", OPTION_LINES, true,
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

	private static Collection<Integer> parseIntOptions(String[] options)
	{
		Collection<Integer> parseOutput = new ArrayList<>(options.length);
		for(int i = 0; i < options.length; i++)
		{
			try
			{
				int parsedInt = Integer.parseInt(options[i]);
				parseOutput.add(parsedInt);
			} catch (NumberFormatException e) {
				System.err.printf("Skipping line %s since it's not a valid line number", options[i]);
			}
		}

		return parseOutput;
	}

	public static void main(String[] args)
	{
		CommandLine cmdLine = parseArgs(args);

		String classToAnalyze = cmdLine.getOptionValue(OPTION_TARGET);
		String classpathToAnalysisTarget = cmdLine.getOptionValue(OPTION_TARGET_CP);
		boolean runControlDependencyAnalysis = cmdLine.hasOption(OPTION_CTRL_DEP);
		boolean runFlowDependencyAnalysis = cmdLine.hasOption(OPTION_FLOW_DEP);
		boolean runAntiDependencyAnalysis = cmdLine.hasOption(OPTION_ANTI_DEP);
		boolean runOutputDependencyAnalysis = cmdLine.hasOption(OPTION_OUTPUT_DEP);
		String outputPath = cmdLine.getOptionValue(OPTION_OUTPUT);
		boolean outputMap = cmdLine.hasOption(OPTION_OUT_MAP);
		boolean outputExistence = cmdLine.hasOption(OPTION_OUT_EXIST);
		Collection<Integer> lineNumsOfInterest = parseIntOptions(cmdLine.getOptionValues(OPTION_LINES));

		String[] sootArgs;
		if (runControlDependencyAnalysis)
		{
			PackManager.v().getPack("jap")
					.add(new Transform(ControlDependencyAnalysis.ANALYSIS_NAME, new ControlDependencyAnalysis(lineNumsOfInterest)));
			sootArgs = Utils.getSootArgs(ControlDependencyAnalysis.ANALYSIS_NAME, classpathToAnalysisTarget, classToAnalyze);
			Utils.runSoot(sootArgs);
		}

		boolean runDataDependencyAnalysis = runFlowDependencyAnalysis || runAntiDependencyAnalysis || runOutputDependencyAnalysis;
		if (runDataDependencyAnalysis)
		{
			DataDependencyAnalysis.Configuration config = new DataDependencyAnalysis.Configuration(runFlowDependencyAnalysis, runAntiDependencyAnalysis, runOutputDependencyAnalysis);
			PackManager.v().getPack("jap")
					.add(new Transform(DataDependencyAnalysis.ANALYSIS_NAME, new DataDependencyAnalysis(lineNumsOfInterest, config)));
			sootArgs = Utils.getSootArgs(DataDependencyAnalysis.ANALYSIS_NAME, classpathToAnalysisTarget, classToAnalyze);
			Utils.runSoot(sootArgs);
		}
	}
}
