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
package org.edits.distance.algorithm;

import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

import com.google.common.collect.Lists;

/**
 * @author Milen Kouylekov
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Rouge extends EditDistanceAlgorithm {

	public enum Type {
		L, S, W
	}

	private static final long serialVersionUID = 1L;

	private static double combinationsN2(int n) {
		return n * (n - 1) / 2.0;
	}

	private double beta = 1;

	private Type type;

	@Override
	public EditDistanceAlgorithm clone() {
		Rouge r = (Rouge) defaultCopy();
		r.setType(type);
		r.setBeta(beta);
		return r;
	}

	public double distance(double score, double size1, double size2) {
		double precision = score / size2;
		double recall = score / size1;
		double distance = (1.0 + beta * beta) * (precision * recall) / (recall + beta * beta * precision);
		return 1 - distance;
	}

	@Override
	public double distance(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h, String pairID) {
		switch (type) {
		case L:
			return distanceL(ta, ha, t, h, pairID);
		case W:
			return distanceW(ta, ha, t, h, pairID);
		case S:
			return distanceS(ta, ha, t, h, pairID);
		}
		return 0;
	}

	public double distanceL(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h, String pairID) {
		int[][] num = new int[ta.size() + 1][ha.size() + 1];
		for (int i = 1; i <= ta.size(); i++)
			for (int j = 1; j <= ha.size(); j++)
				if (getMatcher().match(ta.get(i - 1), ha.get(j - 1), t, h, pairID) == 0)
					num[i][j] = 1 + num[i - 1][j - 1];
				else
					num[i][j] = Math.max(num[i - 1][j], num[i][j - 1]);
		double lcs = num[ta.size()][ha.size()];
		if (lcs == 0)
			return 1;
		return distance(lcs, ta.size(), ha.size());
	}

	public double distanceS(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h, String pairID) {

		List<Annotation> s1 = skipBigrams(ta);
		List<Annotation> s2 = skipBigrams(ha);
		double count = 0;
		for (Annotation o1 : s1) {
			for (Annotation o2 : s2) {
				if (getMatcher().match(o1, o2, t, h, pairID) == 0) {
					count++;
					break;
				}
			}
		}

		if (count == 0)
			return 1;

		return distance(count, combinationsN2(s1.size()), combinationsN2(s2.size()));
	}

	public double distanceW(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h, String pairID) {

		double[][] c = new double[ta.size() + 1][ha.size() + 1];
		double[][] w = new double[ta.size() + 1][ha.size() + 1];
		for (double[] x : c)
			Arrays.fill(x, 0.0);
		for (double[] x : w)
			Arrays.fill(x, 0.0);

		for (int i = 1; i <= ta.size(); i++) {
			for (int j = 1; j <= ha.size(); j++) {
				if (getMatcher().match(ta.get(i - 1), ha.get(j - 1), t, h, pairID) == 0) {
					double k = w[i - 1][j - 1];
					c[i][j] = c[i - 1][j - 1] + f(k + 1) - f(k);
					w[i][j] = k;
				} else {
					if (c[i - 1][j] > c[i][j - 1]) {
						c[i][j] = c[i - 1][j];
						w[i][j] = 0;
					} else {
						c[i][j] = c[i][j - 1];
						w[i][j] = 0;
					}
				}
			}
		}
		double lcs = c[ta.size()][ha.size()];
		if (lcs == 0)
			return 1;
		double precision = fmin(lcs / f(ha.size()));
		double recall = fmin(lcs / f(ta.size()));
		double distance = (1.0 + beta * beta) * (precision * recall) / (recall + beta * beta * precision);
		return 1 - distance;
	}

	private double f(double number) {
		return number * number;
	}

	private double fmin(double number) {
		return Math.sqrt(number);
	}

	private Annotation formBigram(Annotation w1, Annotation w2) {
		Annotation w = new Annotation();
		w.setForm(w1.getForm() + " " + w2.getForm());
		w.setLemma(w1.getLemma() + " " + w2.getLemma());
		return w;
	}

	private List<Annotation> skipBigrams(List<Annotation> list) {

		List<Annotation> out = Lists.newArrayList();

		for (int i = 0; i < list.size() - 1; i++) {
			for (int j = i + 1; j < list.size(); j++) {
				out.add(formBigram(list.get(i), list.get(j)));
			}
		}
		return out;
	}
}
