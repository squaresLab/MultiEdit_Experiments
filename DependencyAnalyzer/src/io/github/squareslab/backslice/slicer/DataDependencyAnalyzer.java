package io.github.squareslab.backslice.slicer;

import soot.Unit;
import soot.Value;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

import java.util.HashMap;
import java.util.Map;

public class DataDependencyAnalyzer extends BackwardFlowAnalysis<Unit, Map<Unit, ReadWriteSets<Value>>>
{
	Unit startPoint;

	public DataDependencyAnalyzer(UnitGraph graph, Unit startPoint)
	{
		super(graph);

		this.startPoint = startPoint;

		doAnalysis();
	}


	/**
	 * This is the flow function
	 * @param in
	 * @param unit
	 * @param out
	 */
	@Override
	protected void flowThrough(Map<Unit, ReadWriteSets<Value>> in, Unit unit, Map<Unit, ReadWriteSets<Value>> out)
	{

	}

	@Override
	protected Map<Unit, ReadWriteSets<Value>> newInitialFlow()
	{
		return new HashMap<>();
	}

	@Override
	protected void merge(Map<Unit, ReadWriteSets<Value>> in1, Map<Unit, ReadWriteSets<Value>> in2, Map<Unit, ReadWriteSets<Value>> out)
	{
		for(Unit key : in1.keySet())
		{
			//copy over
			out.put(key, in1.get(key));
		}

		for(Unit key : in2.keySet())
		{
			ReadWriteSets<Value> in2Sets = in2.get(key);
			if (out.containsKey(key))
			{
				//merge out (copy of in1) with in2

				ReadWriteSets<Value> in1Sets = out.get(key);
				ReadWriteSets<Value> mergedSets = in1Sets.mergeWith(in2Sets);
				out.put(key, mergedSets);
			}
			else
			{
				//copy over
				out.put(key, in2Sets);
			}
		}
	}

	@Override
	protected void copy(Map<Unit, ReadWriteSets<Value>> source, Map<Unit, ReadWriteSets<Value>> dest)
	{
		for(Unit key : source.keySet())
		{
			dest.put(key, source.get(key)); //ReadWriteSets are immutable; thus, copying references should be safe
		}
	}
}
