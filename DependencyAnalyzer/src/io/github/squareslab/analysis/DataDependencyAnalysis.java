package io.github.squareslab.analysis;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public class DataDependencyAnalysis extends BodyTransformer
{
	public static final String ANALYSIS_NAME = "jap.DataDependencyAnalysis";

	private Collection<Integer> lineNumsOfInterest;
	private Configuration config;

	public DataDependencyAnalysis(Collection<Integer> lineNumbersOfInterest, Configuration configuration)
	{
		super();

		this.lineNumsOfInterest = lineNumbersOfInterest;
		this.config = configuration;
	}

	private Map<Integer, Collection<Unit>> getLinesToUnitsMap(Iterable<Unit> units)
	{
		Map<Integer, Collection<Unit>> map = new HashMap<>();

		for (Unit unit : units)
		{
			int lineNum = unit.getJavaSourceStartLineNumber();

			if (lineNum == -1)
				continue;

			//if lineNum is previously unseen, add an empty value set to map
			if (! map.containsKey(lineNum))
			{
				Collection<Unit> unitsForLineNum = new ArrayList<>();
				map.put(lineNum, unitsForLineNum);
			}

			map.get(lineNum).add(unit);
		}

		return map;
	}


	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options)
	{
		UnitGraph graph = new ExceptionalUnitGraph(body);

		Map<Unit, ReadWriteSets<Object>> dataflowMap = ReadWriteAnalysis.getReadWriteSets(graph);

		return;
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

