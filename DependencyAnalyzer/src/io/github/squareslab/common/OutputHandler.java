package io.github.squareslab.common;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class OutputHandler
{
	//TreeMap maintains order of keys, TreeSet maintains order of values for each key
	TreeMap<Integer, TreeSet<Integer>> lineDependencyMap;
	Writer writer = null;

	private OutputHandler()
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

	public void registerWriter(Writer w)
	{
		writer = w;
	}

	public void flushDataToOutput() throws IOException
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

		writer.close();
	}


	private static OutputHandler instance = new OutputHandler();
	public static OutputHandler getInstance() {return instance;}
}
