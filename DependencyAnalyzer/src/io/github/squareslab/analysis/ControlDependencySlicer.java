package io.github.squareslab.analysis;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.*;

import java.util.*;
import java.util.stream.Collectors;

public class ControlDependencySlicer
{
	private ProgramDependenceGraph pdg;
	private Map<Integer, Collection<PDGNode>> linesToNodesMap;
	private Map<PDGNode, Integer> nodeToLineMap;
	private Map<Unit, Integer> unitToLineMap;

	private Map<Integer, Collection<Integer>> forwardSlices;
	private Map<Integer, Collection<Integer>> backSlices;

	public ControlDependencySlicer(ProgramDependenceGraph programDependenceGraph, UnitGraph cfg)
	{
		pdg = programDependenceGraph;

		linesToNodesMap = LineMappingAlgorithms.getLineToPDGNodesMap(pdg);
		nodeToLineMap = LineMappingAlgorithms.getPDGNodeToLineMap(linesToNodesMap);
		unitToLineMap = LineMappingAlgorithms.getUnitToLineMap(LineMappingAlgorithms.getLinesToUnitsMap(cfg));

		forwardSlices = generateAllForwardSlices();
		backSlices = LineMappingAlgorithms.flipMap(forwardSlices);
	}

	/**
	 *
	 * @param line line of interest
	 * @return
	 */
	private Collection<PDGNode> getDependents(int line)
	{
		//if we don't have any pdg nodes for that line, return an empty collection
		if( ! linesToNodesMap.containsKey(line))
			return Collections.emptyList();

		Collection<PDGNode> dependents = new ArrayList<>();

		for (PDGNode node : linesToNodesMap.get(line))
			dependents.addAll(node.getDependents());

		return dependents;
	}

	/**
	 *
	 * @param node PDGNode which may or may not be in the keys of nodeToLineMap
	 * @return line numbers corresponding to node
	 */
	private Collection<Integer> getLineNumbers(PDGNode node)
	{
		Collection<Integer> lineNumbers = new HashSet<>(); //don't count duplicates

		//easy case: we already know its line number
		if (nodeToLineMap.containsKey(node))
		{
			lineNumbers.add(nodeToLineMap.get(node));
		}
		else if (node.getNode() instanceof IRegion)
		{
			Collection<Unit> constituentUnits = ((IRegion) node.getNode()).getUnits();
			for (Unit unitOfNode : constituentUnits)
			{
				if (unitToLineMap.containsKey(unitOfNode))
					lineNumbers.add(unitToLineMap.get(unitOfNode));
				else if (unitOfNode.getJavaSourceStartLineNumber() == -1); //we know this is a case to skip
				else
					System.out.println("I don't know what to do with " + unitOfNode.toString());
			}
		}
		else
		{
			//i don't know what to do
			System.out.println("I don't know what to do with " + node.toString());
		}

		return lineNumbers;
	}

	/**
	 * @param lineToSliceFrom line to slice from
	 * @return lines in backwards dependency slice
	 */
	private Collection<Integer> generateForwardSlice(int lineToSliceFrom)
	{
		Collection<Integer> backsliceLines = new HashSet<>(); //don't count duplicates

		Collection<PDGNode> dependents = getDependents(lineToSliceFrom);
		for(PDGNode dep : dependents)
		{
			Collection<Integer> depLineNums = getLineNumbers(dep);
			backsliceLines.addAll(depLineNums);
		}

		return backsliceLines;
	}

	/**
	 *
	 * @param linesToSliceFrom lines to slice from
	 * @return map: line -> lines in backwards dependency slice
	 */
	private Map<Integer, Collection<Integer>> generateForwardSlices(Collection<Integer> linesToSliceFrom)
	{
		Map<Integer, Collection<Integer>> backslices = new HashMap<>();

		for (int line : linesToSliceFrom)
		{
			Collection<Integer> slice = generateForwardSlice(line);
			backslices.put(line, slice);
		}

		return backslices;
	}

	private Map<Integer, Collection<Integer>> generateAllForwardSlices()
	{
		return generateForwardSlices(linesToNodesMap.keySet());
	}

	public Map<Integer, Collection<Integer>> getBackslices(Collection<Integer> linesToSliceFrom)
	{
		//map filtering code from https://beginnersbook.com/2017/10/java-8-filter-a-map-by-keys-and-values/
		Map<Integer, Collection<Integer>> slices = this.backSlices.entrySet().stream()
				.filter(map -> linesToSliceFrom.contains(map.getKey()))
				.collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));

		//ensure all lines of interest are present in the slices, even if the slice is empty
		for (int line : linesToSliceFrom)
		{
			if (! slices.containsKey(line))
				slices.put(line, Collections.emptySet());
		}

		return slices;
	}

	public Map<Integer, Collection<Integer>> getAllBackslices()
	{
		return new HashMap<>(backSlices);
	}
}
