package io.github.squareslab.analysis;

import io.github.squareslab.common.DataAggregator;
import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;
import soot.toolkits.graph.pdg.ProgramDependenceGraph;

import java.util.*;

public class ControlDependencyAnalysis extends BodyTransformer
{
	public static final String ANALYSIS_NAME = "jap.ControlDependencyAnalysis";

	private Collection<Integer> lineNumsOfInterest;

	public ControlDependencyAnalysis(Collection<Integer> lineNumbersOfInterest)
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

		ControlDependencySlicer slicer = new ControlDependencySlicer(pdg);

		Map<Integer, Collection<Integer>> slices = slicer.getBackslices(lineNumsOfInterest);

		DataAggregator.getInstance().addDependencies(slices);
	}
}
