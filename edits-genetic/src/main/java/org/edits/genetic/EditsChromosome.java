package org.edits.genetic;

import java.util.Arrays;

import lombok.Getter;

import org.jgap.IChromosome;
import org.jgap.impl.BooleanGene;
import org.jgap.impl.DoubleGene;

public class EditsChromosome {
	@Getter
	private boolean[] booleanValue;
	@Getter
	private double[] doubleValue;

	public EditsChromosome(IChromosome a_subject) {
		if (a_subject.getGene(0) instanceof BooleanGene)
			initBoolean(a_subject);
		else
			initDouble(a_subject);
	}

	private void initBoolean(IChromosome a_subject) {
		booleanValue = new boolean[a_subject.size()];
		for (int i = 0; i < a_subject.size(); i++) {
			BooleanGene value = (BooleanGene) a_subject.getGene(a_subject.size() - (i + 1));
			booleanValue[i] = value.booleanValue();
		}
	}

	private void initDouble(IChromosome a_subject) {
		doubleValue = new double[a_subject.size()];
		for (int i = 0; i < a_subject.size(); i++) {
			DoubleGene value = (DoubleGene) a_subject.getGene(a_subject.size() - (i + 1));
			doubleValue[i] = value.doubleValue();
		}
	}

	public String key() {
		if (booleanValue != null)
			return Arrays.toString(booleanValue);
		return Arrays.toString(doubleValue);
	}
}
