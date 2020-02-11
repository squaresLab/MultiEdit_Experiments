package io.github.squareslab.analysis;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

import java.util.*;

public class DataDependencyAnalysis extends BodyTransformer
{
	public static final String ANALYSIS_NAME = "jap.DataDependencyAnalysis";

	private Collection<Integer> lineNumsOfInterest;

	DataDependencyAnalysis(Collection<Integer> lineNumbersOfInterest)
	{
		super();

		this.lineNumsOfInterest = lineNumbersOfInterest;
	}

	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options)
	{
		UnitGraph graph = new ExceptionalUnitGraph(body);

	}
}

