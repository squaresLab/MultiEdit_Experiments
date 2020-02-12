import io.github.squareslab.Main;
import io.github.squareslab.analysis.ControlDependencyAnalysis;
import io.github.squareslab.analysis.DataDependencyAnalysis;
import io.github.squareslab.common.Utils;
import org.junit.Test;
import soot.PackManager;
import soot.Transform;

import java.util.ArrayList;
import java.util.Collection;


public class TestHashMutablePDG
{
	@Test
	public void testUsageScreen()
	{
		String[] args = {};
		Main.main(args);
	}

	@Test
	public void testNoAnalysisChosen()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/IShouldntAppear.out";
		String[] args = {"-tcp", testCp, "-t", target, "-o", output, "-Om", "-lines", "170"};
		Main.main(args);
	}

	@Test
	public void testNoOutputTypeChosen()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/IShouldntAppear.out";
		String[] args = {"-Dc", "-tcp", testCp, "-t", target, "-o", output, "-lines", "170"};
		Main.main(args);
	}

	@Test
	public void testNoAnalysisOrOutputTypeChosen()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/IShouldntAppear.out";
		String[] args = {"-tcp", testCp, "-t", target, "-o", output, "-lines", "170"};
		Main.main(args);
	}

	@Test
	public void testControlDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testControlDependency.out";
		String[] args = {"-Dc", "-tcp", testCp, "-t", target, "-o", output, "-Om", "-lines", "170"};
		Main.main(args);
	}

	@Test
	public void testFlowDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testFlowDependency.out";
		String[] args = {"-Df", "-tcp", testCp, "-t", target, "-o", output, "-Om", "-lines", "170"};
		Main.main(args);
	}

	@Test
	public void testSimultaneousControlAndDataDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testSimultaneousControlDataDependency.out";
		String[] args = {"-Dc", "-Df", "-tcp", testCp, "-t", target, "-o", output, "-Om", "-lines", "170"};
		Main.main(args);
	}
}
