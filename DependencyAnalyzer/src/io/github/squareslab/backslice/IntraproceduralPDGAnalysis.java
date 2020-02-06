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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IntraproceduralPDGAnalysis extends BodyTransformer
{
	private Collection<Integer> lineNumsOfInterest;

	IntraproceduralPDGAnalysis(Collection<Integer> lineNumbersOfInterest)
	{
		super();

		this.lineNumsOfInterest = lineNumbersOfInterest;
	}

	private static Collection<Block> getBlocksOfInterest(BlockGraph blockGraph, Collection<Integer> lineNumbersOfInterest)
	{
		Collection<Block> blocksOfInterest = new ArrayList<>();

		for (Block block : blockGraph)
		{
			for (Unit unit : block)
			{
				int unitLineNum = unit.getJavaSourceStartLineNumber();
				if(lineNumbersOfInterest.contains(unitLineNum))
				{
					blocksOfInterest.add(block);
				}
			}
		}

		return blocksOfInterest;
	}

	private static Collection<PDGNode> getPDGNodesOfInterest(ProgramDependenceGraph pdg, Collection<Integer> lineNumbersOfInterest)
	{
		Collection<Block> blocksOfInterest = getBlocksOfInterest(pdg.getBlockGraph(), lineNumbersOfInterest);

		Collection<PDGNode> pdgNodesOfInterest = new ArrayList<>();

		for(Block block : blocksOfInterest)
		{
			PDGNode pdgNodeOfBlock = pdg.getPDGNode(block);
			pdgNodesOfInterest.add(pdgNodeOfBlock);
		}

		return pdgNodesOfInterest;
	}

	protected void backslice(ProgramDependenceGraph pdg, PDGNode nodeOfInterest)
	{
		//TODO: implement
	}

	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options)
	{

		SootMethod method = body.getMethod();
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		ProgramDependenceGraph pdg = new HashMutablePDG(unitGraph);

		Collection<PDGNode> pdgNodesOfInterest = getPDGNodesOfInterest(pdg, this.lineNumsOfInterest);

		System.out.printf("Done with %d, found %d nodes\n", method.getJavaSourceStartLineNumber(), pdgNodesOfInterest.size());
	}
}
