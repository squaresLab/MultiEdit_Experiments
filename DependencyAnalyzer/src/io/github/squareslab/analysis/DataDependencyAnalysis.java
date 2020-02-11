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

	public DataDependencyAnalysis(Collection<Integer> lineNumbersOfInterest)
	{
		super();

		this.lineNumsOfInterest = lineNumbersOfInterest;
	}



	/**
	 *
	 * @param graph
	 * @param start
	 * @param dataflowMap
	 * @return
	 */
	private List<Unit> getFlowDependencyBackslice
			(UnitGraph graph,
			 Unit start,
			 Map<Unit, ReadWriteSets<Object>> dataflowMap)
	{
		List<Unit> predecessorsList = graph.getPredsOf(start);
		//deduplicate predecessors while maintaining order; a LinkedHashSet maintains insertion-order w/ a linked list
		LinkedHashSet<Unit> predecessors = new LinkedHashSet<>(predecessorsList);

		List<Unit> dependencyBackslice = new ArrayList<>();

		//keep track of all read variables in the backslice (+ the starting unit)
		Set<Object> alreadyRead = new HashSet<>();

		//add starting unit's reads to the list of relevant reads
		if (dataflowMap.containsKey(start))
		{
			ReadWriteSets<Object> startDataflow = dataflowMap.get(start);
			alreadyRead.add(startDataflow.readsIterator());
		}
		else
		{
			System.err.println("Slicing error: start unit not found in dataflowMap.");
		}

		for (Unit pred : predecessors)
		{
			if (dataflowMap.containsKey(pred))
			{
				ReadWriteSets<Object> predDataflow = dataflowMap.get(pred);
				for (Object readVar : alreadyRead)
				{
					if (predDataflow.hasWrittenTo(readVar)) //write-before-read
					{
						dependencyBackslice.add(pred);
						//reads of this predecessor unit are now relevant
						alreadyRead.add(predDataflow.readsIterator());
					}
				}
			}
			else
			{
				System.err.println("Slicing error: predecessor unit not found in dataflowMap.");
			}
		}

		return dependencyBackslice;
	}

	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options)
	{
		UnitGraph graph = new ExceptionalUnitGraph(body);

		Map<Unit, ReadWriteSets<Object>> dataflowMap = ReadWriteAnalysis.getReadWriteSets(graph);

		return;
	}
}

