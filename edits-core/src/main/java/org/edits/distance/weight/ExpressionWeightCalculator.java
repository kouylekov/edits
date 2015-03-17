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
package org.edits.distance.weight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

import com.google.common.collect.Lists;

import de.congrace.exp4j.ExpressionBuilder;

/**
 * 
 * @author Milen Kouylekov
 * 
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ExpressionWeightCalculator implements WeightCalculator {
	public static final long serialVersionUID = 1L;

	public static ExpressionWeightCalculator exampleCalculator() {
		ExpressionWeightCalculator c = new ExpressionWeightCalculator();
		List<NamedWeightCalculator> calculators = Lists.newArrayList();
		calculators.add(new MapWeight());
		calculators.add(new StopWord());
		calculators.add(PosWeight.posWeightNoun());
		calculators.add(PosWeight.posWeightVerb());
		calculators.add(PosWeight.posWeightAdjective());
		String weightH = "1+" + new MapWeight().name() + "+" + StopWord.STOPWORDS + "+" + PosWeight.NOUN + "+"
				+ PosWeight.VERB + "+" + PosWeight.ADJECTIVE;
		String weightT = "1+" + new MapWeight().name() + "+" + StopWord.STOPWORDS + "+" + PosWeight.NOUN + "+"
				+ PosWeight.VERB + "+" + PosWeight.ADJECTIVE;
		c.getCalculators().addAll(calculators);
		c.setWeightH(weightH);
		c.setWeightT(weightT);
		return c;
	}

	private List<NamedWeightCalculator> calculators;

	private String weightH;

	private String weightT;

	public ExpressionWeightCalculator() {
		calculators = Lists.newArrayList();
		weightH = "1";
		weightT = "1";
	}

	@Override
	public WeightCalculator clone() {
		ExpressionWeightCalculator ec = new ExpressionWeightCalculator();
		List<NamedWeightCalculator> newList = Lists.newArrayList();
		for (NamedWeightCalculator nc : calculators)
			newList.add((NamedWeightCalculator) nc.clone());
		ec.setCalculators(newList);
		ec.setWeightH(weightH);
		ec.setWeightT(weightT);
		return ec;
	}

	private double weight(Annotation node, String weightString, AnnotatedText t, AnnotatedText h, boolean isT) {
		Map<String, Double> valuesMap = new HashMap<String, Double>();
		for (NamedWeightCalculator c : calculators)
			valuesMap.put(c.name(), isT ? c.weightT(node, t, h) : c.weightH(node, t, h));
		ExpressionBuilder calc = new ExpressionBuilder(weightString).withVariables(valuesMap);
		try {
			double valuex = calc.build().calculate();
			return valuex;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
			return 0;
		}
	}

	@Override
	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h) {
		return weight(node, weightH, t, h, false);
	}

	@Override
	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h) {
		return weight(node, weightT, t, h, true);
	}
}
