/**
 * Edits - Edit Distance Textual Entailment Suite Copyright (C) 2011 Milen
 * Kouylekov This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.edits;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.edits.distance.algorithm.EditDistanceAlgorithm;
import org.edits.engines.EntailmentEngine;
import org.edits.engines.EvaluationResult;
import org.edits.engines.EvaluationStatistics;
import org.edits.engines.thread.ThreadExecutor;
import org.edits.engines.weka.WekaEntailmentEngine;
import org.edits.engines.weka.features.DistanceFeature;
import org.edits.engines.weka.features.Feature;
import org.edits.etaf.AnnotatedEntailmentPair;
import org.edits.etaf.EntailmentCorpus;
import org.edits.etaf.EntailmentPair;
import org.edits.etaf.ObjectFactory;
import org.edits.target.FileTarget;
import org.edits.target.Target;

import com.google.common.collect.Lists;

/**
 * @author Milen Kouylekov
 */
public class Edits {

	private static final String ALGORITHM = "algorithm";
	private static final String ANNOTATOR = "annotator";
	private static final String MODEL = "model";
	private static final String OPTIMIZE = "optimize";
	private static final String OUTPUT = "output";
	private static final String OVERWRITE = "force";
	private static final String SERIALIZE = "serialize";
	private static final String TEST = "test";
	private static boolean verbose = false;
	private static final String VERBOSE = "verbose";
	private static final String VERSION = "4.0-SNAPSHOT";

	private static EntailmentEngine createEngine(CommandLine script)
			throws Exception {
		WekaEntailmentEngine engine = null;
		String[] algorithms = script.getOptionValues("algorithm");
		if (algorithms == null || algorithms.length == 0)
			engine = new WekaEntailmentEngine();
		else {
			List<Feature> features = Lists.newArrayList();
			features = Lists.newArrayList();
			for (String s : algorithms)
				features.add(new DistanceFeature((EditDistanceAlgorithm) Class
						.forName(s).newInstance()));
			engine = new WekaEntailmentEngine(features);
		}
		return engine;
	}

	private static EntailmentCorpus input(List<String> all) throws Exception {
		EntailmentCorpus out = new EntailmentCorpus();
		ObjectFactory f = new ObjectFactory();
		for (String s : all) {
			EntailmentCorpus c = (EntailmentCorpus) f.load(s);
			out.getPair().addAll(c.getPair());
		}
		return out;
	}

	public static boolean isVerbose() {
		return verbose;
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("log4j.defaultInitOverride", "true");
		LogManager.resetConfiguration();
		Edits.setVerbose(false);
		new Edits().handle(args);
	}

	private static Options options() {
		Options ops = new Options();
		Option o = null;

		o = new Option("v", VERBOSE, false, "Verbose messages");
		o.setRequired(false);
		ops.addOption(o);

		o = new Option("m", MODEL, true,
				"Loads serialized classifier from the file specified by this option");
		o.setRequired(false);
		ops.addOption(o);

		o = new Option("s", SERIALIZE, true,
				"Serialize the classifier in the file specfied by this option");
		o.setRequired(false);
		ops.addOption(o);

		o = new Option("o", OUTPUT, true,
				"Output the decisions of the training or the test set");
		o.setRequired(false);
		ops.addOption(o);

		o = new Option("t", TEST, true,
				"Tests the classifier against a test set in the file specfied by this option");
		o.setRequired(false);
		ops.addOption(o);

		o = new Option("f", OVERWRITE, false, "Overwrite output files");
		o.setRequired(false);
		ops.addOption(o);

		o = new Option("p", OPTIMIZE, false,
				"Finds the best configuration for each algorithm using genetic alorithm");
		o.setRequired(false);
		ops.addOption(o);

		o = new Option("a", ALGORITHM, true,
				"Algorithm to use to create the classifier it can be more than one");
		o.setRequired(false);
		ops.addOption(o);

		o = new Option("ta", ANNOTATOR, true, "Text Annotator");
		o.setRequired(false);
		ops.addOption(o);
		return ops;
	}

	private static void saveModel(String annString, String modelFile,
			EntailmentEngine engine, EvaluationStatistics stats)
			throws Exception {
		EntailmentEngineModel med = new EntailmentEngineModel(annString,
				engine, stats);
		med.setEngine(engine);
		med.setStatistics(stats);
		FileTools.write(med, modelFile, true);
	}

	public static void setVerbose(boolean verbose_) {
		verbose = verbose_;
		if (verbose)
			PropertyConfigurator.configure(Edits.class.getClassLoader()
					.getResource("log4j/log4j-verbose.properties"));
		else
			PropertyConfigurator.configure(Edits.class.getClassLoader()
					.getResource("log4j/log4j.properties"));
	}

	private void execute(CommandLine script) throws Exception {
		String annString = "org.edits.LuceneTokenizer";
		if (script.hasOption(ANNOTATOR))
			annString = script.getOptionValue(ANNOTATOR);
		EditsTextAnnotator annotator = (EditsTextAnnotator) ClassLoader
				.getSystemClassLoader().loadClass(annString).newInstance();

		String output = script.getOptionValue(OUTPUT);

		if (output != null) {
			File f = new File(output);
			if (f.exists() && !script.hasOption(OVERWRITE))
				throw new Exception(
						"The file in which you want to output decisions already exists");
		}

		String serialize = script.getOptionValue(SERIALIZE);
		if (serialize != null) {
			File f = new File(serialize);
			if (f.exists() && !script.hasOption(OVERWRITE))
				throw new Exception(
						"The file in which you want to serialize the model already exists");
		}

		EntailmentEngine engine;

		if (script.hasOption(MODEL)) {
			EntailmentEngineModel model = (EntailmentEngineModel) FileTools
					.read(script.getOptionValue(MODEL));
			engine = model.getEngine();
			if (!script.hasOption(TEST)) {
				System.out.println();
				System.out.println("=== Performance On Training ===\n");
				System.out.println(model.getStatistics());
				return;
			}
		} else {
			engine = createEngine(script);
			List<String> files = FileTools.inputFiles(script.getArgs());
			List<EntailmentPair> all = input(files).getPair();
			List<AnnotatedEntailmentPair> ps = EntailmentCorpus.annotate(
					annotator, all);
			EvaluationStatistics stats = engine.train(ps);
			if (serialize != null)
				saveModel(annString, output, engine, stats);
			System.out.println("=== Performance On Training ===\n");
			System.out.println(stats);
		}

		if (!script.hasOption(TEST))
			return;

		ObjectFactory f = new ObjectFactory();
		EntailmentCorpus c = (EntailmentCorpus) f.load(script
				.getOptionValue(TEST));
		c.annotate(annotator);
		EvaluationStatistics stats = null;
		ThreadExecutor mte = null;

		if (output != null) {
			Target<EvaluationResult> result = new FileTarget<EvaluationResult>(
					output);
			mte = new ThreadExecutor(engine, result);
			mte.run(c.getAnnotated());
			result.close();
		} else {
			mte = new ThreadExecutor(engine, false);
			mte.run(c.getAnnotated());
		}
		stats = mte.statistics();

		System.out.println("=== Performance On Test ===\n");
		System.out.println(stats);
	}

	private void handle(String[] args) throws Exception {
		if (args.length == 0) {
			printInfo(false);
			System.exit(0);
		}
		Options options = options();
		CommandLine commandLine = new BasicParser().parse(options, args);
		long start = System.currentTimeMillis();
		execute(commandLine);
		long end = System.currentTimeMillis();
		Progress.print(start, end);
	}

	private void printInfo(boolean longo) throws Exception {
		Options options = options();
		StringWriter result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		String title = "EDITS - Edit Distance Textual Entailment Suite - "
				+ Edits.VERSION;
		printWriter.println(title + "\n");
		HelpFormatter usageFormatter = new HelpFormatter();
		usageFormatter.printHelp(printWriter, 80, " ", " ", options, 2, 4, " ",
				true);
		printWriter.println("");
		printWriter.close();
		System.out.println(result.toString());
	}
}
