package io.github.squareslab.analysis;

import io.github.squareslab.common.DataAggregator;
import manifold.shade.org.jetbrains.annotations.Nullable;
import soot.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public class DataDependencyAnalysis extends BodyTransformer
{
	public static final String ANALYSIS_NAME = "jap.DataDependencyAnalysis";

	private Collection<Integer> lineNumsOfInterest;
	private Configuration config;

	public DataDependencyAnalysis(@Nullable Collection<Integer> lineNumbersOfInterest, Configuration configuration)
	{
		super();

		this.lineNumsOfInterest = lineNumbersOfInterest;
		this.config = configuration;
	}


	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options)
	{
		UnitGraph graph = new ExceptionalUnitGraph(body);

		DataDependencySlicer slicer = new DataDependencySlicer(graph, config);

		Map<Integer, Collection<Integer>> slices;

		if (lineNumsOfInterest == null)
			slices = slicer.getAllBackslices();
		else
			slices = slicer.getBackslices(lineNumsOfInterest);

		DataAggregator.getInstance().addDependencies(slices);
	}

	//immutable
	public static class Configuration
	{
		final boolean flowDependencies, antiDependencies, outputDependencies;

		public Configuration(boolean flowDep, boolean antiDep, boolean outputDep)
		{
			this.flowDependencies = flowDep;
			this.antiDependencies = antiDep;
			this.outputDependencies = outputDep;
		}
	}
}

