package io.github.squareslab.analysis;

import io.github.squareslab.analysis.DataDependencyAnalysis.Configuration;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public class DataDependencySlicer
{
	Map<Integer, Collection<Unit>> linesToUnitsMap;
	UnitGraph graph;
	Map<Unit, ReadWriteSets<Object>> dataflowMap;
	Configuration config;
	Map<Unit, Integer> unitToLineMap;

	public DataDependencySlicer(Map<Integer, Collection<Unit>> linesToUnitsMapping,
								UnitGraph unitGraph,
								Map<Unit, ReadWriteSets<Object>> dataflowMapping,
								Configuration configuration)
	{
		this.linesToUnitsMap = linesToUnitsMapping;
		this.graph = unitGraph;
		this.dataflowMap = dataflowMapping;
		this.config = configuration;

		this.unitToLineMap = getUnitToLineMap(this.linesToUnitsMap);
	}

	private static Map<Unit, Integer> getUnitToLineMap(Map<Integer, Collection<Unit>> linesToUnitsMapping)
	{
		Map<Unit, Integer> unitToLineMap = new HashMap<>();

		for(int line : linesToUnitsMapping.keySet())
		{
			for(Unit unit : linesToUnitsMapping.get(line))
			{
				unitToLineMap.put(unit, line);
			}
		}

		return unitToLineMap;
	}

	private boolean isDependent
			(Unit pred, Set<Object> alreadyRead, Set<Object> alreadyWritten)
	{
		if (dataflowMap.containsKey(pred))
		{
			ReadWriteSets<Object> predDataflow = dataflowMap.get(pred);

			if(config.flowDependencies)
			{
				for (Object readVar : alreadyRead)
				{
					if (predDataflow.hasWrittenTo(readVar)) //write-before-read
						return true;
				}
			}

			if(config.antiDependencies)
			{
				for (Object writtenVar : alreadyWritten)
				{
					if (predDataflow.hasRead(writtenVar)) //read-before-write
						return true;
				}
			}

			if(config.outputDependencies)
			{
				for (Object writtenVar : alreadyWritten)
				{
					if (predDataflow.hasWrittenTo(writtenVar)) //write-before-write
						return true;
				}
			}
		}
		else
		{
			System.err.println("Slicing error: predecessor unit not found in dataflowMap.");
		}

		return false;
	}

	/**
	 *
	 * @param start
	 * @return backslice
	 */
	private LinkedHashSet<Unit> getBackslice(Unit start)
	{
		List<Unit> predecessorsList = graph.getPredsOf(start);
		//deduplicate predecessors while maintaining order; a LinkedHashSet maintains insertion-order w/ a linked list
		LinkedHashSet<Unit> predecessors = new LinkedHashSet<>(predecessorsList);

		LinkedHashSet<Unit> dependencyBackslice = new LinkedHashSet<>();

		//keep track of all read variables in the backslice (+ the starting unit)
		Set<Object> alreadyRead = new HashSet<>();
		Set<Object> alreadyWritten = new HashSet<>();

		//add starting unit's reads to the list of relevant reads
		if (dataflowMap.containsKey(start))
		{
			ReadWriteSets<Object> startDataflow = dataflowMap.get(start);
			alreadyRead.add(startDataflow.readsIterator());
			alreadyWritten.add(startDataflow.writesIterator());
		}
		else
		{
			System.err.println("Slicing error: start unit not found in dataflowMap.");
		}

		for (Unit pred : predecessors)
		{
			if (isDependent(pred, alreadyRead, alreadyWritten))
			{
				dependencyBackslice.add(pred);
				ReadWriteSets<Object> predDataflow = dataflowMap.get(pred);
				//reads & writes of this predecessor unit are now relevant
				alreadyRead.add(predDataflow.readsIterator());
				alreadyWritten.add(predDataflow.writesIterator());
			}
		}

		return dependencyBackslice;
	}

	/**
	 *
	 * @param units units to slice from
	 * @return merged backslices in arbitrary order
	 */
	private Collection<Unit> getBacksliceUnits(Collection<Unit> units)
	{
		//don't need to keep units ordered, line sorting will take care of ordering
		Collection<Unit> backslices = new HashSet<>();

		for(Unit unit : units)
			backslices.addAll(getBackslice(unit));

		return backslices;
	}

	/**
	 *
	 * @param lineToSliceFrom line to slice from
	 * @return lines in backwards dependency slice
	 */
	public Collection<Integer> getBackslice(int lineToSliceFrom)
	{
		//todo implement
	}

	/**
	 *
	 * @param linesToSliceFrom lines to slice from
	 * @return map: line -> lines in backwards dependency slice
	 */
	public Map<Integer, Collection<Integer>> getBackslices(Collection<Integer> linesToSliceFrom)
	{
		Map<Integer, Collection<Integer>> backslices = new HashMap<>();

		for (int line : linesToSliceFrom)
		{
			Collection<Integer> slice = getBackslice(line);
			backslices.put(line, slice);
		}

		return backslices;
	}
}
