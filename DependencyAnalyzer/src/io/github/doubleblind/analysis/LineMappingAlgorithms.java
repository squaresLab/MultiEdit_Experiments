package io.github.doubleblind.analysis;

import soot.Unit;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.pdg.PDGNode;
import soot.toolkits.graph.pdg.ProgramDependenceGraph;

import java.util.*;

public class LineMappingAlgorithms
{
	static Map<Unit, Integer> getUnitToLineMap(Map<Integer, Collection<Unit>> linesToUnitsMapping)
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

	static Map<Integer, Collection<Unit>> getLinesToUnitsMap(Iterable<Unit> units)
	{
		Map<Integer, Collection<Unit>> map = new HashMap<>();

		for (Unit unit : units)
		{
			int lineNum = unit.getJavaSourceStartLineNumber();

			//don't count line -1
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

	/**
	 *
	 * @param blockGraph blockGraph of analysis target
	 * @return a mapping of line numbers in the blockGraph to their corresponding Blocks
	 */
	private static Map<Integer, Collection<Block>> getLineToBlocksMap(BlockGraph blockGraph)
	{
		Map<Integer, Collection<Block>> map = new HashMap<>();

		for (Block block : blockGraph)
		{
			for (Unit unit : block)
			{
				int unitLineNum = unit.getJavaSourceStartLineNumber();

				if (unitLineNum == -1)
					continue;

				//if unitLineNum is previously unseen, add an empty value set to the map
				if (!map.containsKey(unitLineNum))
				{
					Collection<Block> blocksForUnitLineNum = new ArrayList<>();
					map.put(unitLineNum, blocksForUnitLineNum);
				}

				map.get(unitLineNum).add(block);
			}
		}

		return map;
	}

	/**
	 *
	 * @param pdg program dependence graph of analysis target
	 * @return a mapping of line numbers in the pdg to their corresponding PDGNodes
	 */
	static Map<Integer, Collection<PDGNode>> getLineToPDGNodesMap(ProgramDependenceGraph pdg)
	{
		Map<Integer, Collection<Block>> linesToBlocksMap = getLineToBlocksMap(pdg.getBlockGraph());

		Map<Integer, Collection<PDGNode>> linesToNodesMap = new HashMap<>();

		for(int lineNum : linesToBlocksMap.keySet())
		{
			Collection<PDGNode> nodesAtLineNum = new ArrayList<>();
			for(Block block : linesToBlocksMap.get(lineNum))
			{
				PDGNode node = pdg.getPDGNode(block);
				nodesAtLineNum.add(node);
			}
			linesToNodesMap.put(lineNum, nodesAtLineNum);
		}

		return linesToNodesMap;
	}

	/**
	 * Converts the data structure for faster access.
	 * @param linesToNodesMap map: line number -> PDGNodes at the line
	 * @return map: PDGNode -> line number that corresponds with the node
	 */
	static Map<PDGNode, Integer> getPDGNodeToLineMap(Map<Integer, Collection<PDGNode>> linesToNodesMap)
	{
		Map<PDGNode, Integer> nodeToLineMap = new HashMap<>();

		for(Integer line : linesToNodesMap.keySet())
		{
			for(PDGNode node : linesToNodesMap.get(line))
			{
				nodeToLineMap.put(node, line);
			}
		}

		return nodeToLineMap;
	}

	static <K, V> Map<V, Collection<K>> flipMap(Map<K, Collection<V>> map)
	{
		Map<V, Collection<K>> flip = new HashMap<>();
		for (K key : map.keySet())
		{
			for (V value : map.get(key))
			{
				if (! flip.containsKey(value))
					flip.put(value, new HashSet<>());

				flip.get(value).add(key);
			}
		}

		return flip;
	}

	/**
	 * @param slices
	 * @return whether there exists a dependency among the keys in the slices map
	 */
	public static boolean containsDependencyAmongKeys(Map<Integer, Collection<Integer>> slices)
	{
		Collection<Integer> lines = slices.keySet();
		for (int line : lines)
		{
			Collection<Integer> sliceOfLine = slices.get(line);
			if (! Collections.disjoint(sliceOfLine, lines))
				return true;
		}
		return false;
	}
}
