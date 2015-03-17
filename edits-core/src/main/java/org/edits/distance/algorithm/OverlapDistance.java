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
public class OverlapDistance extends WeightedAlgorithm {

	private static final long serialVersionUID = 1L;

	@Override
	public EditDistanceAlgorithm clone() {
		return defaultCopy();
	}

	@Override
	public double distance(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h, String pairID) {
		double d = 0.0;
		double sum = 0;

		for (Annotation w2 : ha) {
			double min = Double.POSITIVE_INFINITY;
			double x = getWeightCalculator().weightH(w2, t, h);
			sum += x;
			for (Annotation w1 : ta) {
				double score = getMatcher().match(w1, w2, t, h, pairID);
				if (score == 0) {
					min = 0;
					break;
				}
				if (score < min)
					min = score;
			}
			d = d + min * x;
		}
		if (sum == 0)
			return d == 0 ? 0 : 1;
		return d / sum;
	}

}
