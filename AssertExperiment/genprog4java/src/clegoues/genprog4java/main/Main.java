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

package clegoues.genprog4java.main;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


import clegoues.genprog4java.fitness.Fitness;
import clegoues.util.ConfigurationBuilder;

public class Main {

	protected static Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) throws IOException {
		Fitness fitnessEngine = null;
		assert (args.length > 0);
		BasicConfigurator.configure();

		ConfigurationBuilder.register( Configuration.token );
		ConfigurationBuilder.register( Fitness.token );

		ConfigurationBuilder.parseArgs( args );
		Configuration.saveOrLoadTargetFiles();
		ConfigurationBuilder.storeProperties();

		File workDir = new File(Configuration.outputDir);
		if (!workDir.exists())
			workDir.mkdir();
		logger.info("Configuration file loaded");

		fitnessEngine = new Fitness();  // Fitness must be created before rep!
		//Localization localization = new DefaultLocalization(baseRep);
		
		System.out.println("Classes passed: ");
		System.out.println(fitnessEngine.testFitness(0));
		for(String key : Fitness.methodPassed.keySet()) {
			System.out.println(key+" "+Fitness.methodPassed.get(key));
		}
		for(String key : Fitness.assertionPassed.keySet()) {
			System.out.println(key+" "+Fitness.assertionPassed.get(key));
		}
		for(String key : Fitness.partialAssertionPassed.keySet()) {
			System.out.println(key+" "+Fitness.partialAssertionPassed.get(key));
		}
		System.exit(0);
		/*
		switch(Search.searchStrategy.trim()) {

		case "brute": searchEngine = new BruteForce<JavaEditOperation>(fitnessEngine);
		break;
		case "trp": searchEngine = new RandomSingleEdit<JavaEditOperation>(fitnessEngine);
		break;
		case "oracle": searchEngine = new OracleSearch<JavaEditOperation>(fitnessEngine);
		break;
		case "ga":
		default: searchEngine = new GeneticProgramming<JavaEditOperation>(fitnessEngine);
		break;
		}
		incomingPopulation = new Population<JavaEditOperation>(); 

		try {
			searchEngine.doSearch(baseRep, incomingPopulation);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		int elapsed = getElapsedTime(startTime);
		logger.info("\nTotal elapsed time: " + elapsed + "\n");
		Runtime.getRuntime().exit(0);*/
	}

	private static int getElapsedTime(long start) {
		return (int) (System.currentTimeMillis() - start) / 1000;
	}
}
