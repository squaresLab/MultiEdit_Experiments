package io.github.doubleblind.common;

import io.github.doubleblind.analysis.LineMappingAlgorithms;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * A singleton for aggregating and writing data.
 *
 * Intended workflow:
 * 1. One or more threads update the DataAggregator with new data on dependencies.
 * 2. Once all analysis is finished, one thread flushes the data to a Writer.
 */
public class DataAggregator
{
	//TreeMap maintains order of keys, TreeSet maintains order of values for each key
	//I'm using Collection<Integer> as the value type argument instead because Java's Generic type inheritence is bad.
	//TreeMap<Integer, TreeSet<Integer>> is the real type;
	TreeMap<Integer, Collection<Integer>> lineDependencyMap;

	private DataAggregator()
	{
		lineDependencyMap = new TreeMap<>();
	}

	public synchronized void addDependencies(Map<Integer, Collection<Integer>> newDependencies)
	{
		for(int line : newDependencies.keySet())
		{
			if(! lineDependencyMap.containsKey(line))
			{
				TreeSet<Integer> setOfLines = new TreeSet<>();
				lineDependencyMap.put(line, setOfLines);
			}

			Collection<Integer> dependentLines = newDependencies.get(line);
			lineDependencyMap.get(line).addAll(dependentLines);
		}
	}

	private void outputDependencyExistece(Writer writer) throws IOException
	{
		if (LineMappingAlgorithms.containsDependencyAmongKeys(lineDependencyMap))
			writer.append("true\n");
		else
			writer.append("false\n");
	}

	private void flushMap(Writer writer) throws IOException
	{
		for(int line : lineDependencyMap.keySet())
		{
			StringBuilder lineAndDependencies = new StringBuilder(line + ",");
			for(int depLine : lineDependencyMap.get(line))
			{
				lineAndDependencies.append(depLine).append(',');
			}
			//truncate the last dangling comma
			lineAndDependencies.deleteCharAt(lineAndDependencies.length() - 1);
			//add a newline
			lineAndDependencies.append('\n');

			//write a line of CSV
			writer.append(lineAndDependencies);
		}
	}


	public void flushDataToWriter(Writer writer, boolean outputMap, boolean outputExistence) throws IOException
	{
		if (outputExistence)
			outputDependencyExistece(writer);
		if (outputMap)
			flushMap(writer);
	}


	private static DataAggregator instance = new DataAggregator();
	public static DataAggregator getInstance() {return instance;}
}
