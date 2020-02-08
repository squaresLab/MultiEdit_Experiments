package io.github.squareslab.backslice;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;
import soot.toolkits.graph.pdg.ProgramDependenceGraph;

import java.util.*;

public class IntraproceduralPDGAnalysis extends BodyTransformer
{
	private Collection<Integer> lineNumsOfInterest;

	IntraproceduralPDGAnalysis(Collection<Integer> lineNumbersOfInterest)
	{
		super();

		this.lineNumsOfInterest = lineNumbersOfInterest;
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
	private static Map<Integer, Collection<PDGNode>> getLineToPDGNodesMap(ProgramDependenceGraph pdg)
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
	private static Map<PDGNode, Integer> getNodeToLineMap(Map<Integer, Collection<PDGNode>> linesToNodesMap)
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

	/**
	 *
	 * @param line line of interest
	 * @param pdg
	 * @param linesToNodesMap map: line number -> PDGNodes at the line
	 * @return
	 */
	private static Collection<PDGNode> getDependents
			(int line, ProgramDependenceGraph pdg, Map<Integer, Collection<PDGNode>> linesToNodesMap)
	{
		//if we don't have any pdg nodes for that line, return an empty collection
		if( ! linesToNodesMap.keySet().contains(line))
			return Collections.emptyList();

		Collection<PDGNode> dependents = new ArrayList<>();

		for (PDGNode node : linesToNodesMap.get(line))
			dependents.addAll(node.getDependents());

		return dependents;
	}

	private static Map<Integer, Collection<Integer>> getDependentLines
			(Collection<Integer> lines, ProgramDependenceGraph pdg, Map<Integer, Collection<PDGNode>> linesToNodesMap)
	{
		return null;
	}

	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options)
	{

		SootMethod method = body.getMethod();
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		ProgramDependenceGraph pdg = new HashMutablePDG(unitGraph);

		Map<Integer, Collection<PDGNode>> linesToNodesMap = getLineToPDGNodesMap(pdg);

		for(int line : this.lineNumsOfInterest)
		{
			Collection<PDGNode> dependents = getDependents(line, pdg, linesToNodesMap);
			System.out.print(""); //no-op
		}

		System.out.printf("Analyzed %s,\n\tfound %d nodes\n", method.getSignature(), linesToNodesMap.size());
	}
}