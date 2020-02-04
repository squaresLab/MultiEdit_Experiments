package io.github.squareslab.backslice;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;

import java.util.Map;

public class TestPDGAnalysis extends BodyTransformer
{

	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options)
	{

		SootMethod method = body.getMethod();
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		HashMutablePDG pdg = new HashMutablePDG(unitGraph);
	}

	private static TestPDGAnalysis theInstance = new TestPDGAnalysis();
	public static TestPDGAnalysis instance() {return theInstance;}
}
