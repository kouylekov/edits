package org.edits.distance.algorithm;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.edits.distance.weight.DefaultWeightCalculator;
import org.edits.distance.weight.WeightCalculator;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class WeightedAlgorithm extends EditDistanceAlgorithm {
	private static final long serialVersionUID = 1L;

	private WeightCalculator weightCalculator;

	public WeightedAlgorithm() {
		super();
		weightCalculator = new DefaultWeightCalculator();
	}

	@Override
	public EditDistanceAlgorithm defaultCopy() {
		WeightedAlgorithm ds = (WeightedAlgorithm) super.defaultCopy();
		ds.setWeightCalculator(weightCalculator.clone());
		return ds;
	}

}
