package io.github.squareslab.common;

import soot.Main;
import soot.Body;
import soot.Unit;
import soot.NormalUnitPrinter;

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

	/** Builds up the appropriate arguments for invoking analysisToRun
	 * on classToAnalyze.  Mostly involves setting up a few command-line
	 * options and the classpath.
	 */
	public static String[] getSootArgs(String analysisToRun, String pathToSootLibs, String classpathToAnalysisTarget, String classToAnalyze) {
		String separator = System.getProperty("file.separator");
		String pathSeparator = System.getProperty("path.separator");
		String sootClasspath = pathToSootLibs + pathSeparator + classpathToAnalysisTarget;
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

	public static class ExitTrappedException extends SecurityException { }

	/** forbids System.exit() calls in Soot */
	// code courtesy of http://stackoverflow.com/questions/5401281/preventing-system-exit-from-api
	public static void forbidSystemExitCall() {
		final SecurityManager securityManager = new SecurityManager() {
			public void checkPermission( Permission permission ) {
				if( permission.getName().startsWith("exitVM") ) {
					throw new ExitTrappedException() ;
				}
			}
		};
		System.setSecurityManager( securityManager ) ;
	}

	public static void enableSystemExitCall() {
		System.setSecurityManager( null ) ;
	}
}