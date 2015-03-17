package org.edits.engines.weka.features;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.edits.distance.algorithm.EditDistanceAlgorithm;
import org.edits.engines.OptimizationGoal;
import org.edits.etaf.AnnotatedEntailmentPair;
import org.edits.etaf.AnnotatedText;

@ToString
@EqualsAndHashCode(callSuper = false)
public class DistanceFeature extends Feature {

	private static final long serialVersionUID = 1L;

	@Getter
	private EditDistanceAlgorithm algorithm;
	private String name;

	@Getter
	@Setter
	private boolean reverseDistance;

	public DistanceFeature(EditDistanceAlgorithm algorithm2) {
		reverseDistance = false;
		algorithm = algorithm2;
		name = algorithm.getClass().getName();
	}

	@Override
	public Feature clone() {
		return new DistanceFeature(algorithm.clone());
	}

	@Override
	public double doubleValue(AnnotatedEntailmentPair pair) {
		double scoreFinal = -1;
		for (Object text : pair.getT()) {
			for (Object hypothesis : pair.getH()) {
				double score = -1;
				if (reverseDistance)
					score = algorithm.distance((AnnotatedText) hypothesis, (AnnotatedText) text, pair.getId());
				else
					score = algorithm.distance((AnnotatedText) text, (AnnotatedText) hypothesis, pair.getId());
				if (scoreFinal == -1 || score < scoreFinal) {
					scoreFinal = score;
				}
			}
		}
		return scoreFinal;
	}

	@Override
	public void init(List<AnnotatedEntailmentPair> training, OptimizationGoal goal) throws Exception {
	}

	@Override
	public boolean isNumeric() {
		return true;
	}

	@Override
	public String name() {
		return name;
	}

	public void setAlgorithm(EditDistanceAlgorithm algorithm) {
		this.algorithm = algorithm;
		name = algorithm.getClass().getName();
	}
}
