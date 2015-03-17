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

import java.util.List;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

/**
 * @author Milen Kouylekov
 */
public class TokenEditDistance extends WeightedAlgorithm {

	private static final long serialVersionUID = 1L;

	private static double minimum(double a, double b, double c) {
		double mi = a < b ? a : b;
		return c < mi ? c : mi;
	}

	@Override
	public EditDistanceAlgorithm clone() {
		return defaultCopy();
	}

	private double delete(Annotation node, AnnotatedText t, AnnotatedText h) {
		return getWeightCalculator().weightT(node, t, h);
	}

	@Override
	public double distance(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h, String pairID) {

		double[][] d = new double[ta.size() + 1][ha.size() + 1];
		d[0][0] = 0.0;

		for (int i = 1; i < ta.size() + 1; i++) {
			d[i][0] = d[i - 1][0] + delete(ta.get(i - 1), t, h);
		}

		for (int j = 1; j < ha.size() + 1; j++)
			d[0][j] = d[0][j - 1] + insert(ha.get(j - 1), t, h);

		for (int i = 1; i < ta.size() + 1; i++) {
			for (int j = 1; j < ha.size() + 1; j++) {
				double a = d[i - 1][j] + delete(ta.get(i - 1), t, h);
				double b = d[i][j - 1] + insert(ha.get(j - 1), t, h);
				double c = d[i - 1][j - 1] + substitute(ta.get(i - 1), ha.get(j - 1), t, h, pairID);
				d[i][j] = minimum(a, b, c);
			}
		}
		double norm = d[d.length - 1][0] + d[0][d[0].length - 1];

		double dist = d[d.length - 1][d[0].length - 1];

		return init(dist, norm);
	}

	private double insert(Annotation node, AnnotatedText t, AnnotatedText h) {
		return getWeightCalculator().weightH(node, t, h);
	}

	private double substitute(Annotation node1, Annotation node2, AnnotatedText t, AnnotatedText h, String pairID) {
		return getMatcher().match(node1, node2, t, h, pairID) * (insert(node2, t, h) + delete(node1, t, h));
	}
}
