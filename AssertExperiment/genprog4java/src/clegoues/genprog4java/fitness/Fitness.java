/*
 * Copyright (c) 2014-2015,
 *  Claire Le Goues     <clegoues@cs.cmu.edu>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package clegoues.genprog4java.fitness;

import static clegoues.util.ConfigurationBuilder.BOOLEAN;
import static clegoues.util.ConfigurationBuilder.DOUBLE;
import static clegoues.util.ConfigurationBuilder.STRING;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.junit.runner.Description;
import org.junit.runner.Request;

import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.main.Main;
import clegoues.util.ConfigurationBuilder;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class manages fitness evaluation for a variant of an arbitrary {@link clegoues.genprog4java.rep.Representation}.
 * Its duties consist of loading/tracking the test cases to be run and managing the sampling strategy, if applicable.
 * @author clegoues
 *
 */
@SuppressWarnings("rawtypes")
public class Fitness {
	protected static Logger logger = Logger.getLogger(Fitness.class);

	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	/** weight to give to negative test cases; note that this is <i>relative to the total number of positive tests</i>,
	 * not <i>absolute weight</i>.
	 */
	private static double negativeTestWeight = ConfigurationBuilder.of( DOUBLE )
			.withVarName( "negativeTestWeight" )
			.withDefault( "2.0" )
			.withHelp( "weighting to give results of negative test cases" )
			.inGroup( "Fitness Parameters" )
			.build();

	/** how much to sample the positive tests.  Negative tests are never sampled */
	private static double sample = ConfigurationBuilder.of( DOUBLE )
			.withVarName( "sample" )
			.withDefault( "1.0" )
			.withHelp( "fraction of the positive tests to sample" )
			.inGroup( "Fitness Parameters" )
			.build();

	/** controls when we regenerate a test sample (per variant or per generation) */
	private static String sampleStrategy = ConfigurationBuilder.of( STRING )
			.withVarName( "sampleStrategy" )
			.withDefault( "variant" )
			.withHelp( "strategy to use for resampling tests" )
			.inGroup( "Fitness Parameters" )
			.build();

	/** files listing the positive and negative tests */
	private static String posTestFile = ConfigurationBuilder.of( STRING )
			.withVarName( "posTestFile" )
			.withFlag( "positiveTests" )
			.withDefault( "pos.tests" )
			.withHelp( "file containing names of positive test classes" )
			.inGroup( "Fitness Parameters" )
			.build();

	private static String negTestFile = ConfigurationBuilder.of( STRING )
			.withVarName( "negTestFile" )
			.withFlag( "negativeTests" )
			.withDefault( "neg.tests" )
			.withHelp( "file containing names of negative test classes" )
			.inGroup( "Fitness Parameters" )
			.build();

	/** clear the test cache.  Primarily for debug purposes. */
	public static Boolean clearTestCache = ConfigurationBuilder.of(BOOLEAN )
			.withDefault("false")
			.withVarName("clearTestCache")
			.withHelp("clear the test cache")
			.inGroup("Fitness Parameters")
			.build();

	private enum TestGranularity {
		METHOD, CLASS
	};

	public static TestGranularity granularity = new ConfigurationBuilder<TestGranularity>()
			.withVarName("granularity")
			.withFlag("testGranularity")
			.withDefault("class")
			.withHelp("Granularity at which to run JUnit tests.  Default: Class")
			.inGroup("Fitness Parameters")
			.withCast(new ConfigurationBuilder.LexicalCast<TestGranularity> () {
				public TestGranularity parse(String value) {
					logger.debug("parsing granularity, value: " + value);
					switch(value.trim().toLowerCase()) {
					case "method" : return TestGranularity.METHOD;
					case "class":
					default: return TestGranularity.CLASS;
					}
				}
			}).build();



	/** this is necessary because of the generational sample strategy, which
	 *  resamples at generational boundaries.
	 */
	private static int generation = -1;

	/** store the sample and the unsampled portion of the test suite */
	private static List<TestCase> testSample = null;
	private static List<TestCase> restSample = null;

	/** public because {@link clegoues.genprog4java.rep.CachingRepresentation} gets at them
	 * for sanity checking.  There's probably a better way to do that, I suppose, but whatever.
	 */
	public static ArrayList<TestCase> positiveTests = new ArrayList<TestCase>();
	public static ArrayList<TestCase> negativeTests = new ArrayList<TestCase>();

	public static int numPositiveTests;
	public static int numNegativeTests;

	// persistent test cache
	private static HashMap<Integer, HashMap<TestCase, FitnessValue>> fitnessCache = new HashMap<Integer, HashMap<TestCase, FitnessValue>>();

	// FIXME: add some kind of runtime hook to serialize if the process gets killed prematurely.
	public static void serializeTestCache() {
		try {
			FileOutputStream fos = new FileOutputStream("testcache.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(Fitness.fitnessCache);
			oos.close();
			fos.close();
			logger.info("Serialized test cache to file testcache.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void deserializeTestCache(){
		File fl = new File("testcache.ser");
		HashMap<Integer, HashMap<TestCase, FitnessValue>> testCache = null;
		if(fl.isFile() && !clearTestCache){
			try
			{
				FileInputStream fis = new FileInputStream("testcache.ser");
				ObjectInputStream ois = new ObjectInputStream(fis);
				testCache = (HashMap) ois.readObject();
				ois.close();
				fis.close();
			}catch(IOException ioe)
			{
				ioe.printStackTrace();
			}catch(ClassNotFoundException c)
			{
				System.out.println("Class not found");
				c.printStackTrace();
			}
			System.out.println("Deserialized fitnessCache HashMap");
		} else {
			testCache = new HashMap<Integer, HashMap<TestCase, FitnessValue>>();
		}
		//System.out.println("hashmap is = " + testCache.entrySet().size() + "  " + testCache.toString());
		fitnessCache.putAll(testCache);
	}
	/**
	 * Loads the tests from specified files, initializes the sample vars to not be null.
	 * Samples properly when the search actually begins.
	 * Note that this <i>must</i> be called before the initial representation is
	 * constructed, otherwise the rep will not be able to test itself via sanity checking.
	 */
	public Fitness() {
		ArrayList<String> intermedPosTests = null, intermedNegTests = null;

		intermedPosTests = getTests(posTestFile);
		intermedNegTests = getTests(negTestFile);

		switch(Fitness.granularity) {
		case METHOD:
		break;
		case CLASS:
		default:
			filterTestClasses(intermedPosTests, intermedNegTests);
			filterTestClasses(intermedNegTests, intermedPosTests);
			break;
		}

		for(String posTest : intermedPosTests) {
			positiveTests.add(new TestCase(TestCase.TestType.POSITIVE, posTest));
		}

		for(String negTest : intermedNegTests) {
			negativeTests.add(new TestCase(TestCase.TestType.NEGATIVE, negTest));
		}

		Fitness.numPositiveTests = Fitness.positiveTests.size();
		Fitness.numNegativeTests = Fitness.negativeTests.size();
		testSample = new ArrayList<TestCase>(Fitness.positiveTests);
		restSample = new ArrayList<TestCase>();
		Fitness.deserializeTestCache();

		}


	/**
	 * JUnit is annoying.  Basically, a junit test within a larger test class can be failing.
	 * This method figures out if that's the way these tests are specified and, if so
	 * determines their class and then filters those classes out of the
	 * this method filters those classes out of the positive tests and adds them to the negative test list.
	 * Note that CLG considered just filtering out the individual methods and allowing the junittestrunner to run
	 * classes by method in addition to just by class.
	 * I didn't do it because the max test count is presently still the number of
	 * test classes specified in the test files and so we'd either need to actually count
	 * how many tests are being run in total or have the counts/weights be skewed by the one
	 * class file where we call the methods one at a time.
	 * @param toFilter list to filter
	 * @param filterBy stuff to filter out of toFilter
	 */
	private void filterTestClasses(ArrayList<String> toFilter, ArrayList<String> filterBy) {
		ArrayList<String> clazzesInFilterSet = new ArrayList<String>();
		ArrayList<String> removeFromFilterSet = new ArrayList<String>();

		// stuff in negative tests, must remove class from positive test list and add non-negative tests to list
		for(String specifiedMethod : filterBy) {
			if(specifiedMethod.contains("::")) {
				// remove from toFilter all tests that have this class name
				// remove from filterBy this particular entry and replace it with just the className
				String[] split = specifiedMethod.split("::");
				clazzesInFilterSet.add(split[0]);
				removeFromFilterSet.add(specifiedMethod);
			}
		}
		for(String removeFromFilterBy : removeFromFilterSet ) {
			filterBy.remove(removeFromFilterBy);
		}
		filterBy.addAll(clazzesInFilterSet);

		ArrayList<String> removeFromFilteredSet = new ArrayList<String>();
		for(String testNameInToFilter : toFilter ) {
			String clazzName = "";
			if(testNameInToFilter.contains("::")) {
				String[] split = testNameInToFilter.split("::");
				clazzName = split[0];
			} else {
				clazzName = testNameInToFilter;
			}
			if(clazzesInFilterSet.contains(clazzName)) {
				removeFromFilteredSet.add(testNameInToFilter);
			}
		}
		for(String removeFromFiltered : removeFromFilteredSet) {
			toFilter.remove(removeFromFiltered);
		}
	}

	/** As mentioned: JUnit is annoying.  If test granularity is set to "method", then,
	 * unlike the default behavior lo these many months, we actually run one method at a time.
	 * However, specifying tests one method at a time is also annoying (and not what D4J, at
	 * least, does by default for the passing tests). This method thus "explodes" the test classes
	 * into their constituent methods.
	 * 
	 * @param intermedPosTests
	 * @param intermedNegTests
	 * @throws MalformedURLException 
	 * @throws ClassNotFoundException 
	 */

	private URLClassLoader testLoader() throws MalformedURLException {
		String[] split = Configuration.testClassPath.split(":");
		URL[] urls = new URL[split.length];
		for(int i = 0; i < split.length; i++) {
			String s = split[i];
			File f = new File(s);
			URL url = f.toURI().toURL();
			urls[i] = url;
		}
		return new URLClassLoader(urls);
	}
	
	private static boolean looksLikeATest(Method m) {
		return (m.isAnnotationPresent(org.junit.Test.class) ||
				(m.getParameterTypes().length == 0 &&
				m.getReturnType().equals(Void.TYPE) &&
				Modifier.isPublic(m.getModifiers()) &&
				m.getName().startsWith("test")));
	}
    
	private ArrayList<String> getTestMethodsFromClazz(String clazzName, URLClassLoader testLoader) {
		ArrayList<String> realTests = new ArrayList<String>();
		try {
		Class<?> testClazz = Class.forName(clazzName, true, testLoader);
		try {
		TestSuite actualTest = (TestSuite) testClazz.getMethod("suite").invoke(testClazz);
		int numTests = actualTest.countTestCases();
		for(int i = 0; i < numTests; i++) {
			Test t = actualTest.testAt(i);
			String testName = t.toString();
			String[] split = testName.split(Pattern.quote("("));
			String methodName = split[0];
			realTests.add(methodName);
		}
		} catch (NoSuchMethodException |IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			// invoke of "suite" likely failed.  Try something else.
			  // Given a bunch of classes, find all of the JUnit test-methods defined by them.
		      for (Description test : Request.aClass(testClazz).getRunner().getDescription().getChildren()) {
		        // a parameterized atomic test case does not have a method name
		        if (test.getMethodName() == null) {
		          for (Method m : testClazz.getMethods()) {
		            // JUnit 3: an atomic test case is "public", does not return anything ("void"), has 0
		            // parameters and starts with the word "test"
		            // JUnit 4: an atomic test case is annotated with @Test
		            if (looksLikeATest(m)) {
		              realTests.add(m.getName()); // test.getDisplayName()
		            }
		          }
		        } else {
		          // non-parameterized atomic test case
		          realTests.add(test.getMethodName());
		        }
		      }
		    }
		} catch (ClassNotFoundException e) {
			logger.error("Test class " + clazzName + " not found in ExplodeTests!");
		}
		return realTests;
	}
		
	/** load tests from a file.  Does not check that the tests are valid, just that the file exists.
	 * If the file doesn't exist, kills the runtime to exit, because that means that things have gone VERY
	 * weird.
	 * @param filename file listing test classes or test class::methods, one per line.
	 */
	private ArrayList<String> getTests(String filename) {
		ArrayList<String> allLines = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			allLines = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				allLines.add(line);
			}
			br.close();
		} catch(IOException e) {
			logger.error("failed to read " + filename + " giving up");
			Runtime.getRuntime().exit(1);
		}
		return allLines;
	}

	/** testModel is used for certain kinds of search, namely TrpAutoRepair; it tracks
	 *  how "useful" tests have been in the past in terms of number of patches "killed"
	 *  and thus governs the order in which tests are run (when the feature is being used).
	 *  initializeModel needs to be called before it's used.
	 */
	private ArrayList<TestCase> testModel = null;

	public void initializeModel() {
		testModel = new ArrayList<TestCase>(Fitness.numNegativeTests + Fitness.numPositiveTests);
		for(TestCase negTest : Fitness.negativeTests) {
			negTest.incrementPatchesKilled();
			testModel.add(negTest);
		}
		for(TestCase posTest : Fitness.positiveTests) {
			testModel.add(posTest);
		}
		Collections.sort(testModel,Collections.reverseOrder());
	}

	
	
	public boolean singleTestCasePass( TestCase test) {
		String classp = ".:"+Configuration.GP4J_HOME+"/lib/junit-4.12.jar:"+Configuration.GP4J_HOME+"/lib/hamcrest-core-1.3.jar:"+ Configuration.GP4J_HOME+"/target/classes/" + ":" + Configuration.classTestFolder+":"+Configuration.testClassPath+":"+Configuration.libs;
		CommandLine command2 = CommandLine.parse("java -cp .:"+classp+" clegoues.genprog4java.fitness.JUnitTestRunner " + test.getTestName());
		//System.out.println(command2.toString());
		ExecuteWatchdog watchdog = new ExecuteWatchdog(96000);
		DefaultExecutor executor = new DefaultExecutor();
		String workingDirectory = System.getProperty("user.dir");
		executor.setWorkingDirectory(new File(workingDirectory));
		executor.setWatchdog(watchdog);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		executor.setExitValue(0);
		executor.setStreamHandler(new PumpStreamHandler(out));
		try {
			executor.execute(command2);
			out.flush();
		}catch(Throwable e) {
			
			e.printStackTrace();
			System.out.println(out.toString());
			return false;
		}
		return parseTestResults(out.toString()).isAllPassed();
	}
	
	private static FitnessValue parseTestResults(
			String output) {
		//System.out.println(output);
		String[] lines = output.split("\n");
		FitnessValue ret = new FitnessValue();
		ret.setTestClassName("aba");
		for (String line : lines) {
			try {
				if (line.startsWith("[SUCCESS]:")) {
					String[] tokens = line.split("[:\\s]+");
		//			System.out.println(tokens[1]);
					ret.setAllPassed(Boolean.parseBoolean(tokens[1]));
				}
			} catch (Exception e) {
				ret.setAllPassed(false);
			}

			try {
				if (line.startsWith("[TOTAL]:")) {
					String[] tokens = line.split("[:\\s]+");
					ret.setNumberTests(Integer.parseInt(tokens[1]));
				}
			} catch (NumberFormatException e) {
			}

			try {
				if (line.startsWith("[FAILURE]:")) {
					String[] tokens = line.split("[:\\s]+");
					ret.setNumTestsFailed(Integer.parseInt(tokens[1]));
				}
			} catch (NumberFormatException e) {
			}
		}

		return ret;
	}
	
	public List<String> myowntest( TestCase test) {
		String classp = ".:"+Configuration.GP4J_HOME+"/lib/junit-4.12.jar:"+Configuration.GP4J_HOME+"/lib/hamcrest-core-1.3.jar:"+ Configuration.GP4J_HOME+"/target/classes/" + ":" + Configuration.classTestFolder+":"+Configuration.testClassPath+":"+Configuration.libs;
		CommandLine command2 = CommandLine.parse("java -cp .:"+classp+" clegoues.genprog4java.fitness.JUnitTestRunner2 " + test.getTestName());
		System.out.println(command2.toString());
		ExecuteWatchdog watchdog = new ExecuteWatchdog(200000);
		DefaultExecutor executor = new DefaultExecutor();
		String workingDirectory = System.getProperty("user.dir");
		executor.setWorkingDirectory(new File(workingDirectory));
		executor.setWatchdog(watchdog);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		executor.setExitValue(0);
		executor.setStreamHandler(new PumpStreamHandler(out));
		List<String> list = new ArrayList<String>();
		try {
			executor.execute(CommandLine.parse("rm fail.arr"));
		}catch(Throwable e) {}
		try {
			executor.execute(command2);
			Scanner input = new Scanner(new File("fail.arr"));
			while(input.hasNext()) {
				list.add(input.nextLine());
			}
			//System.out.println(out.toString());
		}catch(Throwable e) {
			
			e.printStackTrace();
			System.out.println(out.toString());
			return null;
		}
		return list;
	}
	
	public static Map<String, Integer> methodPassed = new HashMap<String, Integer>();
	public static Map<String, Double> assertionPassed = new HashMap<String, Double>();
	public static Map<String, Double> partialAssertionPassed = new HashMap<String, Double>();
	
	public double assertDistance(TestCase test) {
		if(Configuration.ASSERT_MODE==0)return 0;
		
		List<String> list = myowntest( test);
		if(list==null)return 0;
		if(list.size()<1) {
			System.out.println("weirdest thing ever");
			throw new RuntimeException();
		}
		if(list.size()==1){
			System.out.println("Somehow passed here");
			return 1;
		}
		
		int totaltests = Integer.parseInt(list.get(0));
		list.remove(0);
		methodPassed.put(test.getTestName(), totaltests-list.size());
		for(String testmethod : list) {
			String classp = ".:"+Configuration.fakeJunitDir+"/target/classes:"+Configuration.GP4J_HOME+"/lib/hamcrest-core-1.3.jar:"+ Configuration.GP4J_HOME+"/target/classes/" + ":" + Configuration.classTestFolder+":"+Configuration.testClassPath+":"+Configuration.libs;
			CommandLine command2 = CommandLine.parse("java -cp .:"+classp+" clegoues.genprog4java.fitness.JUnitTestRunner " + test.getTestName()+"::"+testmethod);
			//System.out.println(command2.toString());
			ExecuteWatchdog watchdog = new ExecuteWatchdog(100000);
			DefaultExecutor executor = new DefaultExecutor();
			String workingDirectory = System.getProperty("user.dir");
			executor.setWorkingDirectory(new File(workingDirectory));
			executor.setWatchdog(watchdog);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			executor.setExitValue(0);
			executor.setStreamHandler(new PumpStreamHandler(out));
			try {
				executor.execute(CommandLine.parse("rm Temp.arr"));
			}catch(Throwable e) {}
			try {
				executor.execute(command2);
			}catch(Throwable e) {
				e.printStackTrace();
				//System.out.println(out.toString());
			}
			//System.out.println(out.toString());
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Temp.arr"));
				ArrayList<Double> dd = (ArrayList<Double>)ois.readObject();
				ois.close();
				assertionPassed.put(test.getTestName()+"::"+testmethod, dd.get(1));
				partialAssertionPassed.put(test.getTestName()+"::"+testmethod, dd.get(0));
			}catch(Exception io) {
				System.out.println(io.toString());
			}
		}
		
		return 0;
	}

	/** generates a new random sample of the positive tests. */
	private static void resample() {
		Long L = Math.round(sample * Fitness.numPositiveTests);
		int sampleSize = Integer.valueOf(L.intValue());
		Collections.shuffle(Fitness.positiveTests, Configuration.randomizer);
		List<TestCase> intSample = Fitness.positiveTests.subList(0,sampleSize); //0 inclusive to sampleSize exclusive
		List<TestCase> intRestSample = Fitness.positiveTests.subList(sampleSize, positiveTests.size()); // sampleSize inclusive to size exclusive
		Fitness.testSample.clear();
		Fitness.restSample.clear();
		for(TestCase test : intSample) {
			Fitness.testSample.add(test);
		}
		for(TestCase test : intRestSample) {
			Fitness.restSample.add(test);
		}
	}

	
	



	/** unsampled fitness.  Just test everything
	 *
	 * @param rep variant to test
	 * @param fac weight to give the negative tests
	 * @return Pair, where both elements of the pair are the same (full fitness)
	 */
	private Pair<Double, Double> testFitnessFull(
			double fac) {
		double fitness = 0.0;
		for (TestCase thisTest : Fitness.positiveTests) {
			if (singleTestCasePass( thisTest)) {
				fitness += 1.0;
			}
			else {
				fitness += assertDistance( thisTest);
			}
		}
		for (TestCase thisTest : Fitness.negativeTests) {
			if (singleTestCasePass( thisTest)) {
				
				fitness += fac;
			}
			else {
				fitness += assertDistance( thisTest);
			}
		}
		return  Pair.of(fitness, fitness);
	}

	/** computes fitness on a variant; only does so if the variant does not already
	 * know its fitness (will have been saved/returned by getFitness().  May implement sampling
	 * if specified.  Must always call rep.cleanup()
	 *
	 * @param generation what generation we're on.  Necessary in case we're doing
	 * generational fitness resampling.
	 * @param rep variant to test
	 * @return true if variant passes all test cases, false otherwise.
	 */
	public double testFitness(int generation) {

		/*
		 * Find the relative weight of positive and negative tests If
		 * negative_test_weight is 2 (the default), then the negative tests are
		 * worth twice as much, total, as the positive tests. This is the old
		 * ICSE'09 behavior, where there were 5 positives tests (worth 1 each)
		 * and 1 negative test (worth 10 points). 10:5 == 2:1.
		 */
		double fac = Fitness.numPositiveTests * Fitness.negativeTestWeight
				/ Fitness.numNegativeTests;
		double maxFitness = Fitness.numPositiveTests
				+ ((Fitness.numNegativeTests * fac));
		double curFit = 0;
		
		Pair<Double, Double> fitnessPair =  Pair.of(-1.0, -1.0);
		fitnessPair = this.testFitnessFull(1);
		/*if (Fitness.sample < 1.0) {
			if (((Fitness.sampleStrategy == "generation") && (Fitness.generation != generation)) ||
					(Fitness.sampleStrategy == "variant")) {
				Fitness.generation = generation;
				Fitness.resample();
			}
			fitnessPair = this.testFitnessSample(rep, fac);
		} else {
			fitnessPair = this.testFitnessFull(rep, fac);
		}*/
		
		return fitnessPair.getRight();

	}

	/** debug/convenience functionality; saves the tests that should be considered in scope.
	 * called from {@link clegoues.genprog4java.rep.CachingRepresentation}
	 * @param passingTests
	 */
	public static void printTestsInScope(ArrayList<TestCase> passingTests){
		String path = Fitness.posTestFile + ".inscope";
		//Set up to write to txt file
		FileWriter write = null;
		try {
			write = new FileWriter(path, false);
		} catch (IOException e) {
			logger.error("Error creating the file" + path);
			return;
		}
		PrintWriter printer = new PrintWriter(write);

		//Now write data to the file
		for(TestCase s : passingTests){
			printer.println(s);
		}
		printer.close();
	}

}
