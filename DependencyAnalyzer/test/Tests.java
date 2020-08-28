import io.github.doubleblind.Main;
import io.github.doubleblind.common.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class Tests
{
	final String SOOT_LIB = "jar/DependencyAnalyzer.jar";
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
		String[] args = {"-tcp", testCp, "-t", target, "-o", output, "-Om", "-lib", SOOT_LIB, "-lines", "170"};
		Main.main(args);
	}

	@Test(expected = Utils.ExitTrappedException.class)
	public void testNoOutputTypeChosen()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/IShouldntAppear.out";
		String[] args = {"-Dc", "-tcp", testCp, "-t", target, "-o", output, "-lib", SOOT_LIB, "-lines", "170"};
		Main.main(args);
	}

	@Test(expected = Utils.ExitTrappedException.class)
	public void testNoAnalysisOrOutputTypeChosen()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/IShouldntAppear.out";
		String[] args = {"-tcp", testCp, "-t", target, "-o", output, "-lib", SOOT_LIB, "-lines", "170"};
		Main.main(args);
	}

	@Test
	public void testControlDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testControlDependency.out";
		String[] args = {"-Dc", "-tcp", testCp, "-t", target, "-o", output, "-Om", "-lib", SOOT_LIB, "-lines", "170"};
		Main.main(args);
	}

	@Test
	public void testFlowDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testFlowDependency.out";
		String[] args = {"-Df", "-tcp", testCp, "-t", target, "-o", output, "-Om", "-lib", SOOT_LIB, "-lines", "170"};
		Main.main(args);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSimultaneousControlAndDataDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testSimultaneousControlDataDependency.out";
		String[] args = {"-Dc", "-Df", "-tcp", testCp, "-t", target, "-o", output, "-Om", "-lib", SOOT_LIB, "-lines", "170"};
		Main.main(args);
	}

	@Test
	public void testMultiLinesDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testMultiLinesDependency.out";
		String[] args = {"-Dc", "-tcp", testCp, "-t", target, "-o", output, "-Om", "-lib", SOOT_LIB, "-lines", "173", "139"};
		Main.main(args);
	}

	@Test
	public void testAllLinesDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testAllLinesDependency.out";
		String[] args = {"-Df", "-tcp", testCp, "-t", target, "-o", output, "-Om", "-Oe", "-lib", SOOT_LIB};
		Main.main(args);
	}

	@Test
	public void testExistsDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testExistsDependency.out";
		String[] args = {"-Df", "-tcp", testCp, "-t", target, "-o", output, "-Oe", "-lib", SOOT_LIB};
		Main.main(args);
	}

	@Test
	public void testNotExistsDependency()
	{
		String testCp = "test-resources/Math31b";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String output = "test-output/testNotExistsDependency.out";
		String[] args = {"-Df", "-tcp", testCp, "-t", target, "-o", output, "-Oe", "-lib", SOOT_LIB, "-lines", "45", "177"};
		Main.main(args);
	}

	@Test
	public void testPrintToStdOut()
	{
		String testCp = "test-resources/Math31b:jar/DependencyAnalyzer.jar";
		String target = "org.apache.commons.math3.util.ContinuedFraction";
		String[] args = {"-Df", "-tcp", testCp, "-t", target, "-Om", "-Oe", "-lib", SOOT_LIB};
		Main.main(args);
	}
}
