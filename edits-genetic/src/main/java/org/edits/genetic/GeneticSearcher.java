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
package org.edits.genetic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import org.edits.distance.algorithm.EditDistanceAlgorithm;
import org.edits.engines.EvaluationStatistics;
import org.edits.engines.OptimizationGoal;
import org.edits.engines.weka.WekaEntailmentEngine;
import org.edits.engines.weka.features.DistanceFeature;
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

/**
 * 
 * @author Milen Kouylekov
 * 
 */
@Log4j
public class GeneticSearcher extends MaxFunction {

	private static final long serialVersionUID = 1L;

	private final EditDistanceAlgorithm algorithm;
	private Map<String, Double> cache;
	private final OptimizationGoal goal;
	@Getter
	private final AlgorithmInitializator initializator;
	private final int iterations;
	private GeneticResult result;
	private List<GeneticResult> results;
	private final List<AnnotatedEntailmentPair> training;

	public GeneticSearcher(EditDistanceAlgorithm algorithm_, OptimizationGoal goal_,
			List<AnnotatedEntailmentPair> training_, int iterations_) {
		algorithm = algorithm_;
		goal = goal_;
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

			WekaEntailmentEngine engine = new WekaEntailmentEngine(new DistanceFeature(algorithm));
			engine.setGoal(goal);
			EvaluationStatistics stats = engine.train(training);
			double d = stats.value(goal);
			GeneticResult r = new GeneticResult(d, ec);
			log.debug(r.getValue());
			if (result == null || result.getValue() < d) {
				result = r;
				log.info("New maximum " + r.getValue());
			}
			cache.put(key, d);
			results.add(r);
			return d;
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
		Configuration gaConf = new DefaultConfiguration();
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