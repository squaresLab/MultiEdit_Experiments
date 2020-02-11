package io.github.squareslab.analysis;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public class DataDependencyAnalysis extends BodyTransformer
{
	public static final String ANALYSIS_NAME = "jap.DataDependencyAnalysis";

	private Collection<Integer> lineNumsOfInterest;

	public DataDependencyAnalysis(Collection<Integer> lineNumbersOfInterest)
	{
		super();

		this.lineNumsOfInterest = lineNumbersOfInterest;
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
	 *
	 * @param unit unit
	 * @return the read and write sets of the unit
	 */
	private ReadWriteSets<Object> getReadWriteSets(Unit unit)
	{
		Set<Object> reads = new HashSet<>();
		Set<Object> writes = new HashSet<>();


		if (unit instanceof InvokeStmt)
		{
			InvokeExpr invokeExpr = ((InvokeStmt) unit).getInvokeExpr();

			if (isSetter(unit))
			{
				String heuristicNameOfSetVar = getGetterSetterHeuristicName(invokeExpr);
				writes.add(heuristicNameOfSetVar);
			}

			//capture any arguments to method calls as reads
			for (Value arg : invokeExpr.getArgs())
				addUsedValuesToReadsSet(arg, reads);
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

		return new ReadWriteSets<>(reads, writes);
	}

	private Map<Unit, ReadWriteSets<Object>> getReadWriteSets(Iterable<Unit> units)
	{
		Map<Unit, ReadWriteSets<Object>> map = new HashMap<>();
		for(Unit unit : units)
		{
			map.put(unit, getReadWriteSets(unit));
		}

		return map;
	}

	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options)
	{
		UnitGraph graph = new ExceptionalUnitGraph(body);

		Map<Unit, ReadWriteSets<Object>> readWriteMap = getReadWriteSets(graph);

		return;
	}
}

