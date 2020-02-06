import io.github.squareslab.backslice.*;
import org.junit.Test;
import soot.Scene;

public class TestHashMutablePDG
{
	@Test
	public void test()
	{
		String[] args = {"test-resources", "org.apache.commons.math3.util.ContinuedFraction", "163", "164"};
		Main.main(args);
	}
}
