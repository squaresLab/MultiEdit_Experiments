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
	 * @param lineNumbersOfInterest
	 * @return a mapping of line numbers in lineNumbersOfInterest to their corresponding Blocks in blockGraph
	 */
	private static Map<Integer, Collection<Block>> getBlocksOfInterest(BlockGraph blockGraph, Collection<Integer> lineNumbersOfInterest)
	{
		Map<Integer, Collection<Block>> blocksOfInterest = new HashMap<>();

		for (Block block : blockGraph)
		{
			for (Unit unit : block)
			{
				int unitLineNum = unit.getJavaSourceStartLineNumber();
				if(lineNumbersOfInterest.contains(unitLineNum))
				{
					if(! blocksOfInterest.containsKey(unitLineNum))
					{
						Collection<Block> blocksForUnitLineNum = new ArrayList<>();
						blocksOfInterest.put(unitLineNum, blocksForUnitLineNum);
					}

					blocksOfInterest.get(unitLineNum).add(block);
				}
			}
		}

		return blocksOfInterest;
	}

	/**
	 *
	 * @param pdg program dependence graph of analysis target
	 * @param lineNumbersOfInterest
	 * @return a mapping of line numbers in lineNumbersOfInterest to their corresponding PDGNodes
	 */
	private static Map<Integer, Collection<PDGNode>> getPDGNodesOfInterest(ProgramDependenceGraph pdg, Collection<Integer> lineNumbersOfInterest)
	{
		Map<Integer, Collection<Block>> blocksOfInterest = getBlocksOfInterest(pdg.getBlockGraph(), lineNumbersOfInterest);

		Map<Integer, Collection<PDGNode>> pdgNodesOfInterest = new HashMap<>();

		for(int lineNum : blocksOfInterest.keySet())
		{
			Collection<PDGNode> nodesAtLineNum = new ArrayList<>();
			for(Block block : blocksOfInterest.get(lineNum))
			{
				PDGNode node = pdg.getPDGNode(block);
				nodesAtLineNum.add(node);
			}
			pdgNodesOfInterest.put(lineNum, nodesAtLineNum);
		}

		return pdgNodesOfInterest;
	}

	private static Map<Integer, Collection<PDGNode>> getDependentPDGNodes
			(ProgramDependenceGraph pdg,
			 Map<Integer, Collection<PDGNode>> pdgNodesOfInterest)
	{
		Map<Integer, Collection<PDGNode>> dependentPDGNodes = new HashMap<>();

		for(Integer lineNum : pdgNodesOfInterest.keySet())
		{
			Collection<PDGNode> dependentsOfLineNum = new ArrayList<>();

			for(PDGNode node : pdgNodesOfInterest.get(lineNum))
			{
				Collection<PDGNode> dependentsOfNode = pdg.getDependents(node);
				dependentsOfLineNum.addAll(dependentsOfNode);
			}

			dependentPDGNodes.put(lineNum, dependentsOfLineNum);
		}

		return dependentPDGNodes;
	}

	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options)
	{

		SootMethod method = body.getMethod();
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		ProgramDependenceGraph pdg = new HashMutablePDG(unitGraph);

		Map<Integer, Collection<PDGNode>> pdgNodesOfInterest = getPDGNodesOfInterest(pdg, this.lineNumsOfInterest);
		Map<Integer, Collection<PDGNode>> nodeDependencies = getDependentPDGNodes(pdg, pdgNodesOfInterest);

		System.out.printf("Analyzed %s,\n\tfound %d nodes\n", method.getSignature(), pdgNodesOfInterest.size());
	}
}
