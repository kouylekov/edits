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

import java.util.List;

import lombok.Getter;

import org.edits.distance.algorithm.CosineSimilarity;
import org.edits.distance.algorithm.EditDistanceAlgorithm;
import org.edits.distance.algorithm.TokenEditDistance;
import org.edits.distance.algorithm.WeightedAlgorithm;
import org.edits.distance.match.DefaultMatcher;
import org.edits.distance.match.DefaultMatcher.MatchWords;
import org.edits.distance.match.NestedMatcher;
import org.edits.distance.weight.ExpressionWeightCalculator;
import org.edits.distance.weight.MapWeight;
import org.edits.distance.weight.NamedWeightCalculator;
import org.edits.distance.weight.PosWeight;
import org.edits.distance.weight.StopWord;
import org.edits.distance.weight.WordsIn;

import com.google.common.collect.Lists;

public class AlgorithmInitializator {

	public static String toString(EditDistanceAlgorithm a) {
		DefaultMatcher matcher = (DefaultMatcher) (a.getMatcher() instanceof NestedMatcher ? ((NestedMatcher) a
				.getMatcher()).getMatcher() : a.getMatcher());

		StringBuilder b = new StringBuilder();
		b.append("Strategy: " + matcher.getStrategy() + "\n");
		b.append("IgnoreCase: " + matcher.isIgnoreCase() + "\n");
		b.append("Word Distance: " + matcher.isDistanceWord() + "\n");

		if (!(a instanceof WeightedAlgorithm))
			return b.toString();
		ExpressionWeightCalculator weightCalculator = (ExpressionWeightCalculator) ((WeightedAlgorithm) a)
				.getWeightCalculator();

		b.append("Weight T: " + weightCalculator.getWeightT() + "\n");
		b.append("Weight H: " + weightCalculator.getWeightH() + "\n");

		return b.toString();
	}

	@Getter
	private List<NamedWeightCalculator> calculators;

	public AlgorithmInitializator(EditDistanceAlgorithm algorithm) {
		if (!(algorithm instanceof WeightedAlgorithm))
			return;

		calculators = Lists.newArrayList();

		calculators.add(new StopWord());
		calculators.add(new MapWeight());
		calculators.add(PosWeight.posWeightNoun());
		calculators.add(PosWeight.posWeightVerb());
		calculators.add(PosWeight.posWeightAdjective());

		if (algorithm instanceof TokenEditDistance || algorithm instanceof CosineSimilarity) {
			calculators.add(WordsIn.wordsInH());
			calculators.add(WordsIn.wordsInT());
		}
	}

	public void initialize(EditDistanceAlgorithm module, boolean[] values) {

		DefaultMatcher matcher = (DefaultMatcher) (module.getMatcher() instanceof NestedMatcher ? ((NestedMatcher) module
				.getMatcher()).getMatcher() : module.getMatcher());

		boolean matchForm = values[0];
		boolean matchLemma = values[1];

		if (matchForm) {
			if (matchLemma)
				matcher.setStrategy(MatchWords.FORM_AND_LEMMA);
			else
				matcher.setStrategy(MatchWords.FORM);
		} else {
			if (matchLemma)
				matcher.setStrategy(MatchWords.LEMMA);
			else
				matcher.setStrategy(MatchWords.FORM_OR_LEMMA);
		}

		matcher.setIgnoreCase(values[2]);
		matcher.setDistanceWord(values[3]);

		if (!(module instanceof WeightedAlgorithm))
			return;

		ExpressionWeightCalculator weightCalculator = new ExpressionWeightCalculator();
		((WeightedAlgorithm) module).setWeightCalculator(weightCalculator);

		StringBuilder valueH = new StringBuilder();
		StringBuilder valueT = new StringBuilder();

		valueH.append(values[4] ? "1" : "0");
		valueT.append(values[5] ? "1" : "0");

		List<NamedWeightCalculator> newCal = Lists.newArrayList();

		int start = 0;
		if (calculators.get(0).name().equals(StopWord.STOPWORDS))
			start = 1;

		for (int i = start; i < calculators.size(); i++) {

			boolean val = values[i + 6];

			if (!val)
				continue;

			NamedWeightCalculator calc = calculators.get(i);
			valueH.append("+").append(calc.name());
			valueT.append("+").append(calc.name());
			newCal.add(calc);
		}

		if (start == 1 && values[6]) {
			String h = valueH.toString();
			valueH = new StringBuilder();
			valueH.append(StopWord.STOPWORDS + "*(" + h + ")");
			String t = valueT.toString();
			valueT = new StringBuilder();
			valueT.append(StopWord.STOPWORDS + "*(" + t + ")");
			newCal.add(calculators.get(0));
		}
		weightCalculator.setWeightH(valueH.toString());
		weightCalculator.setWeightT(valueT.toString());
		weightCalculator.setCalculators(newCal);
	}

	public void initialize(EditDistanceAlgorithm module, double[] values) {

		DefaultMatcher matcher = (DefaultMatcher) (module.getMatcher() instanceof NestedMatcher ? ((NestedMatcher) module
				.getMatcher()).getMatcher() : module.getMatcher());

		boolean matchForm = values[0] > 0.5;
		boolean matchLemma = values[1] > 0.5;

		if (matchForm) {
			if (matchLemma)
				matcher.setStrategy(MatchWords.FORM_AND_LEMMA);
			else
				matcher.setStrategy(MatchWords.FORM);
		} else {
			if (matchLemma)
				matcher.setStrategy(MatchWords.LEMMA);
			else
				matcher.setStrategy(MatchWords.FORM_OR_LEMMA);
		}

		matcher.setIgnoreCase(values[2] > 0.5);
		matcher.setDistanceWord(values[3] > 0.5);

		if (!(module instanceof WeightedAlgorithm))
			return;

		ExpressionWeightCalculator weightCalculator = new ExpressionWeightCalculator();
		((WeightedAlgorithm) module).setWeightCalculator(weightCalculator);

		StringBuilder valueH = new StringBuilder();
		StringBuilder valueT = new StringBuilder();

		valueH.append(values[4]);
		valueT.append(values[5]);

		List<NamedWeightCalculator> newCal = Lists.newArrayList();

		int start = 0;
		if (calculators.get(0).name().equals(StopWord.STOPWORDS))
			start = 1;

		for (int i = start; i < calculators.size(); i++) {

			double val = values[i + 6];

			NamedWeightCalculator calc = calculators.get(i);
			valueH.append("+").append(val).append("*").append(calc.name());
			valueT.append("+").append(val).append("*").append(calc.name());
			newCal.add(calc);
		}

		if (start == 1 && values[6] > 0.5) {
			String h = valueH.toString();
			valueH = new StringBuilder();
			valueH.append(StopWord.STOPWORDS + "*(" + h + ")");
			String t = valueT.toString();
			valueT = new StringBuilder();
			valueT.append(StopWord.STOPWORDS + "*(" + t + ")");
			newCal.add(calculators.get(0));
		}
		weightCalculator.setWeightH(valueH.toString());
		weightCalculator.setWeightT(valueT.toString());
		weightCalculator.setCalculators(newCal);

	}

	public void initialize(EditDistanceAlgorithm a, EditsChromosome ec) {
		if (ec.getBooleanValue() != null)
			initialize(a, ec.getBooleanValue());
		else
			initialize(a, ec.getDoubleValue());
	}

	public int size(EditDistanceAlgorithm algorithm) {
		if (!(algorithm instanceof WeightedAlgorithm))
			return 4;
		return 6 + calculators.size();
	}
}
