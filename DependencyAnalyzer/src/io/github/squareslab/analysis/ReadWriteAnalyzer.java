package io.github.squareslab.analysis;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.*;

class ReadWriteAnalyzer extends ForwardFlowAnalysis<Unit, Map<Unit, ReadWriteSets<Object>>>
{
	private Map<Unit, ReadWriteSets<Object>> analysisResult;

	/**
	 *
	 * @param graph
	 */
	public ReadWriteAnalyzer (UnitGraph graph)
	{
		super(graph);

		this.analysisResult = newInitialFlow();

		doAnalysis();
	}

	public Map<Unit, ReadWriteSets<Object>> getAnalysisResult()
	{
		return this.analysisResult;
	}

	private boolean isGetter(Unit unit)
	{
		if (unit instanceof InvokeStmt)
		{
			InvokeExpr invokeExpr = ((InvokeStmt) unit).getInvokeExpr();
			return isGetter(invokeExpr);
		}
		else
			return false;
	}

	private boolean isGetter(Value value)
	{
		if (value instanceof InvokeExpr)
		{
			SootMethod invokedMethod = ((InvokeExpr) value).getMethod();
			String methodName = invokedMethod.getName();
			return methodName.startsWith("get");
		}
		else
			return false;
	}

	private boolean isSetter(Unit unit)
	{
		if (unit instanceof InvokeStmt)
		{
			InvokeExpr invokeExpr = ((InvokeStmt) unit).getInvokeExpr();
			SootMethod invokedMethod = invokeExpr.getMethod();
			String methodName = invokedMethod.getName();
			return methodName.startsWith("set");
		}
		else
			return false;
	}

	private String getGetterSetterHeuristicName(InvokeExpr invokeExpr)
	{
		SootMethod invokedMethod = invokeExpr.getMethod();

		String className = invokedMethod.getDeclaringClass().getName();

		String methodName = invokedMethod.getName();
		assert methodName.length() >= 3;
		String attributeName = methodName.substring(3);

		return className + "." + attributeName;
	}

	/**
	 * Adds values contained in value to reads
	 * @param value
	 * @param reads
	 */
	private void addUsedValuesToReadsSet(Value value, Set<Object> reads)
	{
		List<ValueBox> usedValueBoxes = value.getUseBoxes();
		for(ValueBox valueBox : usedValueBoxes)
		{
			Value usedValue = valueBox.getValue();

			if (isGetter(usedValue))
			{
				String heuristicName = getGetterSetterHeuristicName((InvokeExpr) usedValue);
				reads.add(heuristicName);
			}
			//we don't track constants
			else if( ! (usedValue instanceof Constant))
			{
				reads.add(usedValue);
			}
		}
	}

	/**
	 * This is the flow function
	 * @param in
	 * @param unit
	 * @param out
	 */
	@Override
	protected void flowThrough
			(Map<Unit, ReadWriteSets<Object>> in, Unit unit, Map<Unit, ReadWriteSets<Object>> out)
	{
		Set<Object> reads = new HashSet<>();
		Set<Object> writes = new HashSet<>();

		if (isSetter(unit))
		{
			InvokeExpr invokeExpr = ((InvokeStmt) unit).getInvokeExpr();
			String heuristicNameOfSetVar = getGetterSetterHeuristicName(invokeExpr);
			writes.add(heuristicNameOfSetVar);
		}
		else if (unit instanceof AssignStmt)
		{
			AssignStmt assignStmt = (AssignStmt) unit;

			Value leftHandSide = assignStmt.getLeftOp();
			writes.add(leftHandSide);

			Value rightHandSide = assignStmt.getRightOp();
			addUsedValuesToReadsSet(rightHandSide, reads);
		}
		else if (unit instanceof IfStmt)
		{
			Value ifCondition = ((IfStmt) unit).getCondition();
			addUsedValuesToReadsSet(ifCondition, reads);
		}
		else if (unit instanceof SwitchStmt)
		{
			Value switchKey = ((SwitchStmt) unit).getKey();
			addUsedValuesToReadsSet(switchKey, reads);

			//switch cases in Java must be constants; therefore, we don't analyze them.
		}
		else if (unit instanceof ReturnStmt)
		{
			//treat returns as a special case of assignment where we don't know the LHS
			Value returnValue = ((ReturnStmt) unit).getOp();
			addUsedValuesToReadsSet(returnValue, reads);
		}


		//duplicate and update lattice
		copy(in, out);

		ReadWriteSets<Object> newLatticeValue = new ReadWriteSets<>(reads, writes);
		//I don't think I need to worry about overwriting old values
		out.put(unit, newLatticeValue);

		//update analysis result (might or might not be redundant)
		Map<Unit, ReadWriteSets<Object>> updatedAnalysisResult = new HashMap<>();
		merge(out, this.analysisResult, updatedAnalysisResult);
		this.analysisResult = updatedAnalysisResult;
	}

	@Override
	protected Map<Unit, ReadWriteSets<Object>> newInitialFlow()
	{
		return new HashMap<>();
	}

	@Override
	protected void merge
			(Map<Unit, ReadWriteSets<Object>> in1, Map<Unit, ReadWriteSets<Object>> in2, Map<Unit, ReadWriteSets<Object>> out)
	{

		for(Unit key : in1.keySet())
		{
			//copy over
			out.put(key, in1.get(key));
		}

		for(Unit key : in2.keySet())
		{
			ReadWriteSets<Object> in2Sets = in2.get(key);
			if (out.containsKey(key))
			{
				//merge out (copy of in1) with in2

				ReadWriteSets<Object> in1Sets = out.get(key);
				ReadWriteSets<Object> mergedSets = in1Sets.mergeWith(in2Sets);
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
	protected void copy
			(Map<Unit, ReadWriteSets<Object>> source, Map<Unit, ReadWriteSets<Object>> dest)
	{
		for(Unit key : source.keySet())
		{
			dest.put(key, source.get(key)); //ReadWriteSets are immutable; thus, copying references should be safe
		}
	}
}
