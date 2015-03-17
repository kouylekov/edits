package org.edits.example;

import java.io.File;
import java.util.List;

import lombok.Data;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.edits.EntailmentEngineModel;
import org.edits.FileTools;
import org.edits.distance.algorithm.CosineSimilarity;
import org.edits.distance.algorithm.EditDistanceAlgorithm;
import org.edits.distance.algorithm.OverlapDistance;
import org.edits.distance.algorithm.TokenEditDistance;
import org.edits.distance.match.DefaultMatcher;
import org.edits.distance.match.SimpleRulesMatcher;
import org.edits.engines.EntailmentEngine;
import org.edits.engines.EvaluationStatistics;
import org.edits.engines.weka.BinnaryWekaEngine;
import org.edits.engines.weka.WekaEntailmentEngine;
import org.edits.engines.weka.features.DistanceFeature;
import org.edits.engines.weka.features.Feature;
import org.edits.engines.weka.features.NegationFeature;
import org.edits.etaf.AnnotatedEntailmentPair;
import org.edits.etaf.EntailmentCorpus;
import org.edits.etaf.ObjectFactory;
import org.edits.genetic.OptimizedDistanceFeature;
import org.edits.nlp.OpenNLP;
import org.edits.rules.IndexRulesSource;
import org.edits.rules.RulesIndexGenerator;
import org.edits.rules.RulesSource;
import org.edits.wordnet.WordnetRulesSource;

import com.google.common.collect.Lists;

@Data
public class RunExperiment {

	private static final String ANNOTATED_PREFIX = "annotated_";
	private static final String MODEL_PREFIX = "model_";
	private static final String RULES_PREFIX = "rules_";

	public static void main(String[] args) throws Exception {
		System.setProperty("log4j.defaultInitOverride", "true");
		LogManager.resetConfiguration();
		ConsoleAppender ca = new ConsoleAppender(new PatternLayout("%-5p - %m%n"));
		ca.setName("edits");
		LogManager.getRootLogger().addAppender(ca);
		Options ops = new Options();
		ops.addOption("optimize", false, "");
		ops.addOption("balance", false, "");
		ops.addOption("rules", false, "");
		ops.addOption("debug", false, "");
		ops.addOption("wordnet", true, "");

		CommandLine commandLine = new BasicParser().parse(ops, args);

		if (!commandLine.hasOption("debug"))
			ca.setThreshold(Level.INFO);

		RunExperiment res = new RunExperiment(commandLine.getArgs()[0], commandLine.hasOption("balance"),
				commandLine.hasOption("optimize"), commandLine.hasOption("rules"));

		if (commandLine.hasOption("wordnet"))
			res.setRulesSource(new WordnetRulesSource(commandLine.getOptionValue("wordnet")));

		res.train(commandLine.getArgs()[1]);
	}

	private final boolean balancedDataset;

	private RulesSource indexSource;

	private boolean optimize;

	private final String path;
	private RulesSource rulesSource;

	private final boolean useRules;

	public RunExperiment(String path, boolean balancedDataset, boolean optimize, boolean useRules) {
		super();
		this.path = path;
		this.balancedDataset = balancedDataset;
		this.optimize = optimize;
		this.useRules = useRules;
	}

	public WekaEntailmentEngine balancedEngine() {
		List<Feature> features = Lists.newArrayList();
		features.add(new NegationFeature());
		features.add(constructFeature(new TokenEditDistance()));
		features.add(constructFeature(new OverlapDistance()));
		features.add(constructFeature(new CosineSimilarity()));
		WekaEntailmentEngine engine = new WekaEntailmentEngine(features);
		return engine;
	}

	public Feature constructFeature(EditDistanceAlgorithm algorithm) {
		if (useRules)
			algorithm.setMatcher(new SimpleRulesMatcher(indexSource));
		else
			algorithm.setMatcher(new DefaultMatcher());

		return optimize ? new OptimizedDistanceFeature(algorithm, false, 50) : new DistanceFeature(algorithm);
	}

	private void createRules(List<String> filenames) throws Exception {

		if (new File(path + RULES_PREFIX + filenames.get(0)).exists())
			return;

		if (rulesSource == null)
			rulesSource = new WordnetRulesSource();

		List<AnnotatedEntailmentPair> in = Lists.newArrayList();

		for (String filename : filenames) {
			org.edits.etaf.ObjectFactory factory = new org.edits.etaf.ObjectFactory();
			EntailmentCorpus training = (EntailmentCorpus) factory.load(path + ANNOTATED_PREFIX + filename);
			in.addAll(training.getAnnotated());
		}
		RulesIndexGenerator generator = new RulesIndexGenerator(rulesSource, true);
		generator.setThreads(1);
		generator.generateIndex(path + RULES_PREFIX + filenames.get(0), in);

	}

	private void evaluateTest(String testFilename) {

	}

	public void test(String filename, String testFilename) throws Exception {
		tokenize(filename);
		tokenize(testFilename);
		createRules(Lists.newArrayList(new String[] { filename, testFilename }));
		train(filename);
		evaluateTest(testFilename);
	}

	public void tokenize(String filename) throws Exception {
		File input = new File(path, filename);
		File output = new File(path, ANNOTATED_PREFIX + filename);
		if (output.exists())
			return;
		ObjectFactory ff = new ObjectFactory();
		OpenNLP tagger = OpenNLP.getInstance();
		EntailmentCorpus ec = (EntailmentCorpus) ff.load(input.getAbsolutePath());
		ec.annotate(tagger);
		ff.marshal(output.getAbsolutePath(), ff.createEntailmentCorpus(ec), true);
	}

	public EntailmentEngine train(String filename) throws Exception {
		tokenize(filename);
		if (useRules)
			createRules(Lists.newArrayList(new String[] { filename }));
		return trainAnnotated(filename);
	}

	private EntailmentEngine trainAnnotated(String filename) throws Exception {

		if (useRules)
			indexSource = new IndexRulesSource(path + RULES_PREFIX + filename);

		org.edits.etaf.ObjectFactory factory = new org.edits.etaf.ObjectFactory();
		EntailmentCorpus training = (EntailmentCorpus) factory.load(path + ANNOTATED_PREFIX + filename);

		EntailmentEngine engine = null;

		if (balancedDataset)
			engine = balancedEngine();
		else
			engine = unbalancedEngine();
		EvaluationStatistics stats = engine.train(training.getAnnotated());
		System.out.println(stats);
		EntailmentEngineModel model = new EntailmentEngineModel(OpenNLP.class.getName(), engine, stats);
		FileTools.write(model, path + MODEL_PREFIX + filename, true);

		return engine;

	}

	private EntailmentEngine unbalancedEngine() {
		return new BinnaryWekaEngine(balancedEngine());
	}
}
