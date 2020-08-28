package io.github.doubleblind.analysis;

import io.github.doubleblind.common.DataAggregator;
import manifold.shade.org.jetbrains.annotations.Nullable;
import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.ProgramDependenceGraph;

import java.util.*;

public class ControlDependencyAnalysis extends BodyTransformer
{
	public static final String ANALYSIS_NAME = "jap.ControlDependencyAnalysis";

	private Collection<Integer> lineNumsOfInterest;

	public ControlDependencyAnalysis(@Nullable Collection<Integer> lineNumbersOfInterest)
	{
		super();

		this.lineNumsOfInterest = lineNumbersOfInterest;
	}

	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options)
	{

		SootMethod method = body.getMethod();
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		ProgramDependenceGraph pdg = new HashMutablePDG(unitGraph);

		ControlDependencySlicer slicer = new ControlDependencySlicer(pdg, unitGraph);

		Map<Integer, Collection<Integer>> slices;

		if (lineNumsOfInterest == null)
			slices = slicer.getAllBackslices();
		else
			slices = slicer.getBackslices(lineNumsOfInterest);

		DataAggregator.getInstance().addDependencies(slices);
	}
}
