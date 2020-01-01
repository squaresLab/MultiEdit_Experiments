import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.*;
import org.junit.runner.RunWith;
import org.junit.runner.JUnitCore;
import org.junit.internal.TextListener;
import static org.junit.extensions.cpsuite.SuiteType.*;

@RunWith(ClasspathSuite.class)
@SuiteTypes({ JUNIT38_TEST_CLASSES, TEST_CLASSES })
public class RunAllSuite {
        public static void main(String args[]) {
                JUnitCore junit = new JUnitCore();
                junit.addListener(new TextListener(System.out));
                junit.run(RunAllSuite.class);
        }
}
