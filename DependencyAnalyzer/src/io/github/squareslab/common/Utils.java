package io.github.squareslab.common;

import soot.Main;
import soot.Body;
import soot.Unit;
import soot.SootMethod;
import soot.SootField;
import soot.SootClass;
import soot.tagkit.Host;
import soot.NormalUnitPrinter;

import java.util.HashSet;
import java.util.Set;

import java.security.Permission;

/**
 * Various useful utilities.
 * Sourced from 2019 17-355 course materials.
 */
public class Utils {
	/** Converts a Unit from a given Body to a String */
	public static String toString(Unit unit, Body body) {
		NormalUnitPrinter printer = new NormalUnitPrinter(body);
		unit.toString(printer);
		return printer.output().toString();
	}

	/** reports a warning message for the given element of source code,
	 * using the ErrorMessage passed in.  The ErrorMessage, the name of
	 * the element if it is a declaration, and the line number
	 * are stored for later lookup.
	 */
	public static void reportWarning(Host element, ErrorMessage message) {
		System.out.print("warning: ");
		System.out.print(message.getErrorMessage());
		int line = element.getJavaSourceStartLineNumber();
		String name = getName(element);
		if (line == -1) {
			if (name != null) {
				System.out.println(" at the declaration of " + name);
			} else {
				System.out.println(" (line unknown)");
			}
		} else {
			System.out.print(" at line ");
			System.out.println(line);
		}
		errors.add(new ErrorReport(message, line, name));
	}

	/** Builds up the appropriate arguments for invoking analysisToRun
	 * on classToAnalyze.  Mostly involves setting up a few command-line
	 * options and the classpath.
	 */
	public static String[] getSootArgs(String analysisToRun, String classpathToAnalysisTarget, String classToAnalyze) {
		String separator = System.getProperty("file.separator");
		String pathSeparator = System.getProperty("path.separator");
		String rtJarPath = "lib" + separator + "rt.jar";
		rtJarPath += pathSeparator + "lib" + separator + "jce.jar";
		String sootClasspath = rtJarPath + pathSeparator + "out";
		sootClasspath += pathSeparator + classpathToAnalysisTarget;
		String [] args = { "-cp", sootClasspath, "-keep-line-number", "-f", "J", "-p", analysisToRun, "on", classToAnalyze };
		return args;
	}

	/** runs Soot with the arguments given,
	 * ensuring that Soot does not call System.exit() if we are invoked
	 * from JUnit */
	public static void runSoot(String[] args) {
		try {
			forbidSystemExitCall();
			Main.main(args);
			Utils.enableSystemExitCall();
		} catch (Utils.ExitTrappedException e) {
			// swallow the exception if Soot tried to exit directly; we'll exit soon anyway
		}
	}

	/** Resets the set of errors to be empty */
	public static void resetErrors() {
		errors = new HashSet<ErrorReport>();
	}

	/** Gets the set of all reported errors */
	public static Set<ErrorReport> getErrors() {
		return errors;
	}

	/** Returns the name of the element,
	 * or null if the element is not a declaration */
	public static String getName(Host element) {
		// wow, wouldn't it be nice if there were a Declaration interface
		// with the member getName()?
		if (element instanceof SootClass) {
			return ((SootClass)element).getName();
		} else if (element instanceof SootField) {
			return ((SootField)element).getName();
		} else if (element instanceof SootMethod) {
			return ((SootMethod)element).getName();
		} else {
			return null;
		}
	}

	private static HashSet<ErrorReport> errors = new HashSet<ErrorReport>();

	private static class ExitTrappedException extends SecurityException { }

	/** forbids System.exit() calls in Soot */
	// code courtesy of http://stackoverflow.com/questions/5401281/preventing-system-exit-from-api
	private static void forbidSystemExitCall() {
		final SecurityManager securityManager = new SecurityManager() {
			public void checkPermission( Permission permission ) {
				if( permission.getName().startsWith("exitVM") ) {
					throw new ExitTrappedException() ;
				}
			}
		};
		System.setSecurityManager( securityManager ) ;
	}

	private static void enableSystemExitCall() {
		System.setSecurityManager( null ) ;
	}
}