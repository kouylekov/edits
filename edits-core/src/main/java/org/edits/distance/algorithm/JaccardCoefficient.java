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
public class JaccardCoefficient extends EditDistanceAlgorithm {

	private static final long serialVersionUID = 1L;

	@Override
	public EditDistanceAlgorithm clone() {
		return defaultCopy();
	}

	@Override
	public double distance(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h, String pairID) {
		List<Annotation> vector = Lists.newArrayList();

		List<Integer> v1 = Lists.newArrayList();
		List<Integer> v2 = Lists.newArrayList();

		for (Annotation w : ta) {
			boolean found = false;
			for (int i = 0; i < vector.size() && !found; i++) {
				Annotation w2 = vector.get(i);
				if (getMatcher().match(w2, w, t, h, pairID) == 0) {
					found = true;
					v1.set(i, v1.get(i) + 1);
				}
			}
			if (!found) {
				vector.add(w);
				v1.add(1);
				v2.add(0);
			}
		}

		for (Annotation w : ha) {
			boolean found = false;
			for (int i = 0; i < vector.size() && !found; i++) {
				Annotation w2 = vector.get(i);
				if (getMatcher().match(w2, w, t, h, pairID) == 0) {
					found = true;
					v2.set(i, v2.get(i) + 1);
				}
			}
			if (!found) {
				vector.add(w);
				v1.add(0);
				v2.add(1);
			}
		}

		double intersection = 0.0;

		for (int i = 0; i < vector.size(); i++)
			if (v1.get(i) > 0 && v2.get(i) > 0)
				intersection++;
		double union = vector.size();
		return 1.0 - intersection / union;
	}
}
