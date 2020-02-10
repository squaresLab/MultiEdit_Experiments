import io.github.squareslab.Main;
import org.junit.Test;

public class TestHashMutablePDG
{
	@Test
	public void test()
	{
		String[] args = {"test-resources", "org.apache.commons.math3.util.ContinuedFraction", "170"};
		Main.main(args);
	}
}
