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

import com.google.common.collect.Lists;

/**
 * @author Milen Kouylekov
 */
public class CosineSimilarity extends WeightedAlgorithm {

	private static final long serialVersionUID = 1L;

	@Override
	public EditDistanceAlgorithm clone() {
		return defaultCopy();
	}

	@Override
	public double distance(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h, String pairID) {

		List<Annotation> vector = Lists.newArrayList();

		List<Double> v1 = Lists.newArrayList();
		List<Double> v2 = Lists.newArrayList();

		for (Annotation w : ha) {
			boolean found = false;
			for (int i = 0; i < vector.size() && !found; i++) {
				Annotation w2 = vector.get(i);
				if (getMatcher().match(w2, w, t, h, pairID) == 0) {
					found = true;
					v2.set(i, v2.get(i) + getWeightCalculator().weightH(w, t, h));
					break;
				}
			}
			if (!found) {
				vector.add(w);
				v1.add(0.0);
				v2.add(getWeightCalculator().weightH(w, t, h));
			}
		}

		for (Annotation w : ta) {
			boolean found = false;
			for (int i = 0; i < vector.size() && !found; i++) {
				Annotation w2 = vector.get(i);
				if (getMatcher().match(w2, w, t, h, pairID) == 0) {
					found = true;
					v1.set(i, v1.get(i) + getWeightCalculator().weightT(w, t, h));
					break;
				}
			}
			if (!found) {
				vector.add(w);
				v1.add(getWeightCalculator().weightT(w, t, h));
				v2.add(0.0);
			}
		}
		double dotproduct = 0;

		for (int i = 0; i < vector.size(); i++)
			dotproduct += v1.get(i) * v2.get(i);

		double magnitude1 = 0.0;
		double magnitude2 = 0.0;

		for (int i = 0; i < vector.size(); i++)
			magnitude1 += v1.get(i) * v1.get(i);

		for (int i = 0; i < vector.size(); i++)
			magnitude2 += v2.get(i) * v2.get(i);

		magnitude1 = Math.sqrt(magnitude1);
		magnitude2 = Math.sqrt(magnitude2);

		double magnitude = magnitude1 * magnitude2;

		double cosine = Math.sqrt(dotproduct) / magnitude;

		double distance = 1 - cosine;

		return distance;
	}
}
