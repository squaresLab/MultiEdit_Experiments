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
	public void testControlDependency()
	{
		String[] args = {"test-resources", "org.apache.commons.math3.util.ContinuedFraction", "000", "170"};
		Main.main(args);
	}

	@Test
	public void testFlowDependency()
	{
		String[] args = {"test-resources", "org.apache.commons.math3.util.ContinuedFraction", "100", "170"};
		Main.main(args);
	}
}
