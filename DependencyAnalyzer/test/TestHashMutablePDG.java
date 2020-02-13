import io.github.squareslab.Main;
import io.github.squareslab.analysis.ControlDependencyAnalysis;
import io.github.squareslab.analysis.DataDependencyAnalysis;
import io.github.squareslab.common.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import soot.Pack;
import soot.PackManager;
import soot.Transform;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;


public class TestHashMutablePDG
{
	@Before
	public void beforeTest()
	{
		Utils.forbidSystemExitCall();
	}

	@After
	public void afterTest()
	{
		Utils.enableSystemExitCall();
	}

	@Test(expected = Utils.ExitTrappedException.class)
	public void testUsageScreen()
	{
		String[] args = {};
		Main.main(args);
	}

	@Test(expected = Utils.ExitTrappedException.class)
	public void testNoAnalysisChosen()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/IShouldntAppear.out";
		String[] args = {"-tcp", testCp, "-t", target, "-o", output, "-Om", "-lines", "170"};
		Main.main(args);
	}

	@Test(expected = Utils.ExitTrappedException.class)
	public void testNoOutputTypeChosen()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/IShouldntAppear.out";
		String[] args = {"-Dc", "-tcp", testCp, "-t", target, "-o", output, "-lines", "170"};
		Main.main(args);
	}

	@Test(expected = Utils.ExitTrappedException.class)
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

	@Test(expected = UnsupportedOperationException.class)
	public void testSimultaneousControlAndDataDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testSimultaneousControlDataDependency.out";
		String[] args = {"-Dc", "-Df", "-tcp", testCp, "-t", target, "-o", output, "-Om", "-lines", "170"};
		Main.main(args);
	}

	@Test
	public void testMultiLinesDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testMultiLinesDependency.out";
		String[] args = {"-Dc", "-tcp", testCp, "-t", target, "-o", output, "-Om", "-lines", "173", "139"};
		Main.main(args);
	}

	@Test
	public void testAllLinesDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testAllLinesDependency.out";
		String[] args = {"-Dc", "-tcp", testCp, "-t", target, "-o", output, "-Om"};
		Main.main(args);
	}
}
