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
package org.edits.engines;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.edits.MapInteger;
import org.edits.engines.OptimizationGoal.Target;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * @author Milen Kouylekov
 * 
 */
public class EvaluationStatistics implements Serializable {

	private static final long serialVersionUID = 1L;

	private static String clean(Double number) {
		return clean(number, 4);
	}

	private static String clean(Double number, int i) {
		if (number == null)
			return "";
		if (i == 0)
			return "" + Math.rint(number);
		double x = 1;
		for (int k = 0; k < i; k++)
			x = x * 10;
		return "" + Math.round(number * x) / x;
	}

	private static String symbols(int x, String string) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < x; i++)
			b.append(string);
		return b.toString();
	}

	private static String tableToString(String[][] table) {

		Map<Integer, Integer> widths = new HashMap<Integer, Integer>();

		for (int i = 0; i < table.length; i++) {
			int max = 0;
			for (int j = 0; j < table[i].length; j++) {
				if (table[i][j].length() > max)
					max = table[i][j].length();
			}
			widths.put(i, max);
		}

		StringBuilder b = new StringBuilder();

		for (int j = 0; j < table[0].length; j++) {
			for (int i = 0; i < table.length; i++) {
				String s = table[i][j];
				int mw = widths.get(i);
				b.append(s);
				b.append(symbols(mw - s.length(), " "));
				if (i != table.length - 1)
					b.append("    ");
			}
			b.append("\n");
		}
		return b.toString();
	}

	private double accuracy;

	@Getter
	private List<String> classes;

	@Getter
	private int correct;
	private final MapInteger examples;

	private Map<String, Double> fmeasure;
	@Getter
	@Setter
	private String id;

	private final Map<String, MapInteger> omap;

	@Getter
	private final List<String[]> outputs;

	private Map<String, Double> precision;

	private Map<String, Double> recall;

	@Getter
	private final Map<String, EvaluationStatistics> subClasses;

	private int wrong;

	public EvaluationStatistics() {
		outputs = Lists.newArrayList();
		subClasses = Maps.newHashMap();
		omap = Maps.newHashMap();
		examples = new MapInteger();
	}

	public double accuracy() {
		return accuracy;
	}

	public void add(EvaluationStatistics statistics) {
		subClasses.put(statistics.getId(), statistics);
		outputs.addAll(statistics.getOutputs());
	}

	public void add(Map<String, MapInteger> map, String key, String key2) {
		MapInteger d = map.get(key);
		if (d == null) {
			d = new MapInteger();
			map.put(key, d);
		}
		d.increment(key2);
	}

	public synchronized void add(String benchmark, String assigned) {
		outputs.add(new String[] { benchmark, assigned });
	}

	public int count(String orig, String assigned) {
		return omap.get(orig).get(assigned);
	}

	public int examples(String cls) {
		return examples.get(cls);
	}

	public String exportToString() {
		StringBuilder b = new StringBuilder();
		for (String[] s : outputs) {
			b.append(s[0] + "\t" + s[1] + "\n");
		}
		return b.toString();
	}

	public double fmeasure(String t) {
		return fmeasure.get(t);
	}

	public void calculate() {
		Map<String, MapInteger> amap = Maps.newHashMap();
		for (String key[] : outputs) {
			String orig = key[0];
			examples.increment(orig);
			String assigned = key[1];
			add(omap, orig, assigned);
			add(amap, assigned, orig);
			if (!examples.containsKey(assigned))
				examples.put(assigned, 0);
		}

		classes = Lists.newArrayList(examples.keySet());
		Collections.sort(classes);

		for (String ex : examples.keySet()) {
			MapInteger i = omap.get(ex);
			if (i == null) {
				i = new MapInteger();
				omap.put(ex, i);
			}
			for (String esx : examples.keySet())
				if (!i.containsKey(esx))
					i.put(esx, 0);
			i = amap.get(ex);
			if (i == null) {
				i = new MapInteger();
				amap.put(ex, i);
			}
			for (String esx : examples.keySet())
				if (!i.containsKey(esx))
					i.put(esx, 0);
		}

		correct = 0;
		wrong = 0;
		for (String key : omap.keySet()) {
			for (String key2 : omap.get(key).keySet()) {
				if (key.equals(key2))
					correct += omap.get(key).get(key2);
				else
					wrong += omap.get(key).get(key2);
			}
		}
		fmeasure = Maps.newHashMap();
		precision = Maps.newHashMap();
		recall = Maps.newHashMap();
		accuracy = (double) correct / (double) (correct + wrong);

		for (String key : omap.keySet()) {
			double tp = omap.get(key).get(key);
			double fp = 0.0;
			if (amap.containsKey(key)) {
				for (String key2 : amap.get(key).keySet()) {
					if (!key2.equals(key))
						fp += amap.get(key).get(key2);
				}
			}
			double prec = 0;
			if (tp + fp > 0)
				prec = tp / (tp + fp);
			double fn = 0.0;
			for (String key2 : omap.get(key).keySet()) {
				if (!key2.equals(key))
					fn += omap.get(key).get(key2);
			}
			double rec = 0;
			if (tp + fn > 0)
				rec = tp / (tp + fn);
			double fme = 0;
			if (prec + rec > 0)
				fme = prec * rec * 2 / (prec + rec);
			precision.put(key, prec);
			recall.put(key, rec);
			fmeasure.put(key, fme);
		}
	}

	public double precision(String t) {
		return precision.get(t);
	}

	public double recall(String t) {
		return recall.get(t);
	}

	public EvaluationStatistics subclass(String name) {
		return subClasses.get(name);
	}

	@Override
	public String toString() {
		StringBuilder buff = new StringBuilder();
		int correct = getCorrect();
		int wrong = wrong();
		String[][] sv = new String[3][4];
		sv[0][0] = "Correctly Classified Examples";
		sv[1][0] = "" + correct;
		sv[2][0] = clean((double) correct / (double) (correct + wrong) * 100, 2) + "%";
		sv[0][1] = "Incorrectly Classified Examples";
		sv[1][1] = "" + wrong;
		sv[2][1] = clean((double) wrong / (double) (correct + wrong) * 100, 2) + "%";
		sv[0][2] = "";
		sv[1][2] = "";
		sv[2][2] = "";
		sv[0][3] = "Accuracy";
		sv[1][3] = "";
		sv[2][3] = clean(accuracy());
		buff.append(tableToString(sv) + "\n");
		buff.append("=== Detailed Performace Per Relation ===\n\n");
		sv = new String[5][getClasses().size() + 1];
		sv[0][0] = "Examples";
		sv[1][0] = "Precision";
		sv[2][0] = "Recall";
		sv[3][0] = "FMeasure";
		sv[4][0] = "Class";

		int z = 1;

		for (String cls : getClasses()) {
			sv[0][z] = "    " + examples(cls);
			sv[1][z] = clean(precision(cls));
			sv[2][z] = clean(recall(cls));
			sv[3][z] = clean(fmeasure(cls));
			sv[4][z] = cls.toString();
			z++;
		}

		buff.append(tableToString(sv) + "\n");

		buff.append("=== Confusion Matrix ===" + "\n\n");

		sv = new String[getClasses().size() + 1][getClasses().size() + 1];

		for (int i = 0; i < getClasses().size(); i++)
			sv[i][0] = getClasses().get(i);

		sv[getClasses().size()][0] = "<-- classified as";

		z = 1;
		for (String cls : getClasses()) {
			for (int i = 0; i < getClasses().size(); i++)
				sv[i][z] = "" + count(cls, getClasses().get(i));

			sv[getClasses().size()][z] = cls;
			z++;
		}

		buff.append(tableToString(sv));

		for (String key : getSubClasses().keySet()) {
			buff.append("=== " + key + " ===\n");
			buff.append(getSubClasses().get(key) + "\n");
		}

		return buff.toString();
	}

	public double value(OptimizationGoal goal) {
		if (Target.ACCURACY.equals(goal.getTarget()))
			return accuracy();
		if (Target.FMEASURE.equals(goal.getTarget()))
			return recall(goal.getRelation());
		if (Target.PRECISION.equals(goal.getTarget()))
			return precision(goal.getRelation());
		return fmeasure(goal.getRelation());
	}

	public int wrong() {
		return wrong;
	}
}
