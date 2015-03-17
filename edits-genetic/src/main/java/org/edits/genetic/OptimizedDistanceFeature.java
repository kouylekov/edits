package org.edits.genetic;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;

import org.edits.distance.algorithm.EditDistanceAlgorithm;
import org.edits.distance.match.CachedMatcher;
import org.edits.distance.match.Matcher;
import org.edits.engines.OptimizationGoal;
import org.edits.engines.weka.features.DistanceFeature;
import org.edits.engines.weka.features.Feature;
import org.edits.etaf.AnnotatedEntailmentPair;

@Data
@EqualsAndHashCode(callSuper = false)
@Log4j
public class OptimizedDistanceFeature extends DistanceFeature {

	private static final long serialVersionUID = 1L;

	private int iterations;

	private boolean useBooleanGene;

	public OptimizedDistanceFeature(EditDistanceAlgorithm algorithm2) {
		this(algorithm2, false, 50);
	}

	public OptimizedDistanceFeature(EditDistanceAlgorithm algorithm2, boolean useBooleanGene_, int iterations_) {
		super(algorithm2);
		iterations = iterations_;
		useBooleanGene = useBooleanGene_;
	}

	@Override
	public Feature clone() {
		return new OptimizedDistanceFeature(getAlgorithm().clone());
	}

	@Override
	public void init(List<AnnotatedEntailmentPair> training, OptimizationGoal goal) throws Exception {

		Matcher m = getAlgorithm().getMatcher();
		boolean stripCache = false;
		if (m instanceof CachedMatcher) {
			getAlgorithm().setMatcher(((CachedMatcher) m).getMatcher());
			stripCache = true;
		}

		log.info("Genetic Algorithm Started with algorithm " + getAlgorithm().getClass().getName());
		GeneticSearcher searcher = new GeneticSearcher(getAlgorithm(), goal, training, iterations);
		GeneticResult result = searcher.run(useBooleanGene);
		searcher.getInitializator().initialize(getAlgorithm(), result.getChromosome());
		if (stripCache)
			getAlgorithm().setMatcher(new CachedMatcher(getAlgorithm().getMatcher()));
		log.info("Result:\n" + AlgorithmInitializator.toString(getAlgorithm()) + "Score: " + result.getValue());
	}
}
