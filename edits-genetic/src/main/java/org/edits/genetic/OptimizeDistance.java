package org.edits.genetic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import org.edits.distance.algorithm.EditDistanceAlgorithm;
import org.edits.engines.thread.EditsThread;
import org.edits.etaf.AnnotatedEntailmentPair;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.impl.BooleanGene;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.job.MaxFunction;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;

@Log4j
public class OptimizeDistance extends MaxFunction {

	private static final long serialVersionUID = 1L;

	private final EditDistanceAlgorithm algorithm;
	private Map<String, Double> cache;
	@Getter
	private final AlgorithmInitializator initializator;
	private final int iterations;
	private GeneticResult result;
	private List<GeneticResult> results;
	private final List<AnnotatedEntailmentPair> training;

	public OptimizeDistance(EditDistanceAlgorithm algorithm_, List<AnnotatedEntailmentPair> training_, int iterations_) {
		algorithm = algorithm_;
		iterations = iterations_;
		initializator = new AlgorithmInitializator(algorithm_);
		training = training_;
		cache = new HashMap<String, Double>();
	}

	@Override
	public double evaluate(IChromosome a_subject) {
		EditsChromosome ec = new EditsChromosome(a_subject);
		String key = ec.key();
		if (cache.containsKey(key))
			return cache.get(key);
		try {
			initializator.initialize(algorithm, ec);
			log.debug(AlgorithmInitializator.toString(algorithm));
			final AtomicDouble distd = new AtomicDouble(0);

			EditsThread<AnnotatedEntailmentPair> thread = new EditsThread<AnnotatedEntailmentPair>() {

				@Override
				public void process(AnnotatedEntailmentPair p) throws Exception {
					double score = Double.parseDouble(p.getAttributes().get("score"));
					double ns = 5 * (1 - algorithm.distance(p.getT().get(0), p.getH().get(0), p.getId()));
					distd.addAndGet(Math.abs(score - ns));
				}
			};

			thread.start(training.iterator());

			double dist = 5.0 - distd.get() / training.size();
			GeneticResult r = new GeneticResult(dist, ec);
			log.debug(r.getValue());
			if (result == null || result.getValue() < dist)
				result = r;
			cache.put(key, dist);
			results.add(r);
			return dist;
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		cache.put(key, 0.0);
		return 0;
	}

	public GeneticResult run(boolean useBooleanGenes) throws Exception {
		results = Lists.newArrayList();
		result = null;
		cache = new HashMap<String, Double>();
		Configuration.reset();
		DefaultConfiguration gaConf = new DefaultConfiguration();
		int chromeSize = initializator.size(algorithm);
		IChromosome sampleChromosome = null;

		if (useBooleanGenes)
			sampleChromosome = new Chromosome(gaConf, new BooleanGene(gaConf), chromeSize);
		else
			sampleChromosome = new Chromosome(gaConf, new DoubleGene(gaConf, 0, 1), chromeSize);

		gaConf.setSampleChromosome(sampleChromosome);
		gaConf.setPopulationSize(20);
		gaConf.setFitnessFunction(this);
		Genotype genotype = Genotype.randomInitialGenotype(gaConf);
		genotype.evolve(iterations);
		return result;
	}
}