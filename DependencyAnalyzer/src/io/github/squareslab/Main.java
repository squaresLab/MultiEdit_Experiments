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
	private static final String OPTION_OUT_DEP = "find-output-dependencies";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_OUT_MAP = "output-dependency-map";
	private static final String OPTION_OUT_EXIST = "output-dependency-existence";
	private static final String OPTION_LINES = "lines-to-analyze";

	private static final Options OPTIONS;

	static
	{
		OPTIONS = defineOptions();
	}

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

		Option outputDataDependency = new Option("Do", OPTION_OUT_DEP, false,
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

	private static void showHelpAndExit()
	{
		//todo: use something more meaningful than empty string for cmdLineSyntax
		new HelpFormatter().printHelp(" ", OPTIONS);
		System.exit(1);
	}

	private static CommandLine parseArgs(String[] args)
	{
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(OPTIONS, args);

			return cmd;
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			System.err.flush();
			showHelpAndExit();
			return null; //not returned due to exit
		}
	}

	private static Collection<Integer> parseIntOptions(String[] intOptions)
	{
		Collection<Integer> parseOutput = new ArrayList<>(intOptions.length);
		for(int i = 0; i < intOptions.length; i++)
		{
			try
			{
				int parsedInt = Integer.parseInt(intOptions[i]);
				parseOutput.add(parsedInt);
			} catch (NumberFormatException e) {
				System.err.printf("Skipping line %s since it's not a valid line number", intOptions[i]);
			}
		}

		return parseOutput;
	}

	public static void main(String[] args)
	{
		CommandLine cmdLine = parseArgs(args);

		String classToAnalyze = cmdLine.getOptionValue(OPTION_TARGET);
		String classpathToAnalysisTarget = cmdLine.getOptionValue(OPTION_TARGET_CP);
		boolean runCtrlAnalysis = cmdLine.hasOption(OPTION_CTRL_DEP);
		boolean runFlowAnalysis = cmdLine.hasOption(OPTION_FLOW_DEP);
		boolean runAntiAnalysis = cmdLine.hasOption(OPTION_ANTI_DEP);
		boolean runOutAnalysis = cmdLine.hasOption(OPTION_OUT_DEP);
		String outputPath = cmdLine.getOptionValue(OPTION_OUTPUT);
		boolean outputMap = cmdLine.hasOption(OPTION_OUT_MAP);
		boolean outputExistence = cmdLine.hasOption(OPTION_OUT_EXIST);
		Collection<Integer> lineNumsOfInterest = parseIntOptions(cmdLine.getOptionValues(OPTION_LINES));

		boolean atLeastOneAnalysis = runCtrlAnalysis || runFlowAnalysis || runAntiAnalysis || runOutAnalysis;
		boolean atLeastOneOutputType = outputMap || outputExistence;


		if (! atLeastOneAnalysis || ! atLeastOneOutputType)
		{
			if (! atLeastOneAnalysis)
			{
				System.err.printf("Must choose to run at least one type of analysis: %s, %s, %s, and/or %s\n",
						OPTION_CTRL_DEP, OPTION_FLOW_DEP, OPTION_ANTI_DEP, OPTION_OUT_DEP);
				System.err.flush();
			}
			if (! atLeastOneOutputType)
			{
				System.err.printf("Must choose to run at least one type of output: %s and/or %s\n",
						OPTION_OUT_MAP, OPTION_OUT_EXIST);
				System.err.flush();
			}
			showHelpAndExit();
		}

		String[] sootArgs;
		if (runCtrlAnalysis)
		{
			PackManager.v().getPack("jap")
					.add(new Transform(ControlDependencyAnalysis.ANALYSIS_NAME, new ControlDependencyAnalysis(lineNumsOfInterest)));
			sootArgs = Utils.getSootArgs(ControlDependencyAnalysis.ANALYSIS_NAME, classpathToAnalysisTarget, classToAnalyze);
			Utils.runSoot(sootArgs);
		}

		boolean runDataDependencyAnalysis = runFlowAnalysis || runAntiAnalysis || runOutAnalysis;
		if (runDataDependencyAnalysis)
		{
			DataDependencyAnalysis.Configuration config
					= new DataDependencyAnalysis.Configuration(runFlowAnalysis, runAntiAnalysis, runOutAnalysis);
			PackManager.v().getPack("jap")
					.add(new Transform(DataDependencyAnalysis.ANALYSIS_NAME, new DataDependencyAnalysis(lineNumsOfInterest, config)));
			sootArgs = Utils.getSootArgs(DataDependencyAnalysis.ANALYSIS_NAME, classpathToAnalysisTarget, classToAnalyze);
			Utils.runSoot(sootArgs);
		}
	}
}
