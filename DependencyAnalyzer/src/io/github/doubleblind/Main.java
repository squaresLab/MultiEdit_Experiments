package io.github.doubleblind;


import io.github.doubleblind.analysis.ControlDependencyAnalysis;
import io.github.doubleblind.analysis.DataDependencyAnalysis;
import io.github.doubleblind.common.DataAggregator;
import io.github.doubleblind.common.Utils;
import org.apache.commons.cli.*;
import soot.Pack;
import soot.PackManager;
import soot.Transform;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
	private static final String OPTION_LIB_PATH = "path-to-soot-libs";

	private static final String PRINT_TO_STD_OUT = "--STDOUT--";

	private static final Options OPTIONS = defineOptions();

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
				"Find control dependencies. Do not use in conjunction with data dependency analyses.");
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
				"File to write output to. Default is to print to stdout.");
		outputPath.setRequired(false);
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
				"Line(s) to back-slice from. Default is to slice all lines (which might be slow).");
		linesToAnalyze.setArgs(Option.UNLIMITED_VALUES);
		linesToAnalyze.setRequired(false);
		options.addOption(linesToAnalyze);

		Option libPathForSoot = new Option("lib", OPTION_LIB_PATH, true,
				"Path to Soot's libraries. " +
						"If you're running DependencyAnalyzer.jar, then you don't need to use this option. " +
						"Otherwise, use either jar/DependencyAnalyzer.jar (which contains all of Soot's dependencies) " +
						"or all of the libs in lib/.");
		libPathForSoot.setRequired(false);
		options.addOption(libPathForSoot);

		return options;
	}

	private static void showHelpAndExit()
	{
		new HelpFormatter().printHelp("java -jar DependencyAnalyzer.jar", OPTIONS);
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
			System.out.println(e.getMessage());
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
				System.out.printf("Skipping line %s since it's not a valid line number", intOptions[i]);
			}
		}

		return parseOutput;
	}

	private static String resolveLibPathForSoot()
	{
		Class self = Main.class;
		String myLocation = self.getProtectionDomain().getCodeSource().getLocation().getPath();
		if (myLocation.endsWith(".jar"))
			return myLocation;
		else
			throw new RuntimeException("Unable to resolve location of Soot's libs. " +
					"Please explicitly use the - "+OPTION_LIB_PATH + " option instead.");
	}

	private static void writeData(String outputPath, boolean outputMap, boolean outputExistence)
	{
		Writer writer;
		try
		{
			if (outputPath.equals(PRINT_TO_STD_OUT))
				writer = new OutputStreamWriter(System.out);
			else
				writer = new FileWriter(outputPath);
			DataAggregator.getInstance().flushDataToWriter(writer, outputMap, outputExistence);
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
		String outputPath;
		if (cmdLine.hasOption(OPTION_OUTPUT))
			outputPath = cmdLine.getOptionValue(OPTION_OUTPUT);
		else
			outputPath = PRINT_TO_STD_OUT;
		boolean outputMap = cmdLine.hasOption(OPTION_OUT_MAP);
		boolean outputExistence = cmdLine.hasOption(OPTION_OUT_EXIST);
		Collection<Integer> lineNumsOfInterest;
		if (cmdLine.hasOption(OPTION_LINES))
		{
			String[] lineNumStrings = cmdLine.getOptionValues(OPTION_LINES);
			lineNumsOfInterest = lineNumStrings.length > 0 ? parseIntOptions(lineNumStrings) : null;
		}
		else
			lineNumsOfInterest = null;
		String libPathForSoot;
		if (cmdLine.hasOption(OPTION_LIB_PATH))
			libPathForSoot = cmdLine.getOptionValue(OPTION_LIB_PATH);
		else
			libPathForSoot = resolveLibPathForSoot();

		boolean atLeastOneAnalysis = runCtrlAnalysis || runFlowAnalysis || runAntiAnalysis || runOutAnalysis;
		boolean atLeastOneOutputType = outputMap || outputExistence;
		if (! atLeastOneAnalysis || ! atLeastOneOutputType)
		{
			if (! atLeastOneAnalysis)
				System.out.printf("Must choose to run at least one type of analysis: %s, %s, %s, and/or %s\n", OPTION_CTRL_DEP, OPTION_FLOW_DEP, OPTION_ANTI_DEP, OPTION_OUT_DEP);
			if (! atLeastOneOutputType)
				System.out.printf("Must choose to run at least one type of output: %s and/or %s\n", OPTION_OUT_MAP, OPTION_OUT_EXIST);
			showHelpAndExit();
		}

		boolean runDataDependencyAnalysis = runFlowAnalysis || runAntiAnalysis || runOutAnalysis;
		if (runCtrlAnalysis && runDataDependencyAnalysis)
			throw new UnsupportedOperationException("Simultaneously running control and data dependency analyses is " +
					"currently unsupported.\nPlease run control and data dependency analyses separately.");

		String[] sootArgs;
		Pack pack = PackManager.v().getPack("jap");
		if (runCtrlAnalysis)
		{
			Transform controlDepTransform = new Transform(ControlDependencyAnalysis.ANALYSIS_NAME,
					new ControlDependencyAnalysis(lineNumsOfInterest));
			pack.add(controlDepTransform);
			sootArgs = Utils.getSootArgs(ControlDependencyAnalysis.ANALYSIS_NAME, libPathForSoot, classpathToAnalysisTarget, classToAnalyze);
			Utils.runSoot(sootArgs);
			pack.remove(ControlDependencyAnalysis.ANALYSIS_NAME);
		}

		if (runDataDependencyAnalysis)
		{
			DataDependencyAnalysis.Configuration config
					= new DataDependencyAnalysis.Configuration(runFlowAnalysis, runAntiAnalysis, runOutAnalysis);
			Transform dataDepTransform = new Transform(DataDependencyAnalysis.ANALYSIS_NAME,
					new DataDependencyAnalysis(lineNumsOfInterest, config));
			pack.add(dataDepTransform);
			sootArgs = Utils.getSootArgs(DataDependencyAnalysis.ANALYSIS_NAME, libPathForSoot, classpathToAnalysisTarget, classToAnalyze);
			Utils.runSoot(sootArgs);
			pack.remove(DataDependencyAnalysis.ANALYSIS_NAME);
		}

		writeData(outputPath, outputMap, outputExistence);
	}
}
