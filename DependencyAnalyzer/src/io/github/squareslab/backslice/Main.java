package io.github.squareslab.backslice;


import io.github.squareslab.backslice.common.Utils;
import soot.PackManager;
import soot.Transform;

public class Main
{
	static final String ANALYSIS_NAME = "jap.test";

	public static void main(String[] args)
	{
		String classpathToAnalysisTarget = args[0];
		String classToAnalyze = args[1];

		PackManager.v().getPack("jap").add(new Transform(ANALYSIS_NAME, TestPDGAnalysis.instance()));
		String[] sootArgs = Utils.getSootArgs(ANALYSIS_NAME, classpathToAnalysisTarget, classToAnalyze);
		Utils.runSoot(sootArgs);
	}
}
