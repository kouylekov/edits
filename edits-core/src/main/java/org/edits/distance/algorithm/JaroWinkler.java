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

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

/**
 * @author Milen Kouylekov
 */
public class JaroWinkler extends EditDistanceAlgorithm {

	private static final long serialVersionUID = 1L;

	@Override
	public EditDistanceAlgorithm clone() {
		return defaultCopy();
	}

	@Override
	public double distance(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h, String pairID) {

		int len1 = ta.size();
		int len2 = ha.size();

		int searchRange = Math.max(0, Math.max(len1, len2) / 2 - 1);

		boolean[] matched1 = new boolean[len1];
		Arrays.fill(matched1, false);
		boolean[] matched2 = new boolean[len2];
		Arrays.fill(matched2, false);

		int numCommon = 0;
		for (int i = 0; i < len1; ++i) {
			int start = Math.max(0, i - searchRange);
			int end = Math.min(i + searchRange + 1, len2);
			for (int j = start; j < end; ++j) {
				if (matched2[j])
					continue;
				if (getMatcher().match(ta.get(i), ha.get(j), t, h, pairID) != 0)
					continue;
				matched1[i] = true;
				matched2[j] = true;
				++numCommon;
				break;
			}
		}
		if (numCommon == 0)
			return 1.0;

		int numHalfTransposed = 0;
		int j = 0;
		for (int i = 0; i < len1; ++i) {
			if (!matched1[i])
				continue;
			while (!matched2[j])
				++j;
			if (getMatcher().match(ta.get(i), ha.get(j), t, h, pairID) != 0)
				++numHalfTransposed;
			++j;
		}
		int numTransposed = numHalfTransposed / 2;

		double numCommonD = numCommon;
		double weight = (numCommonD / len1 + numCommonD / len2 + (numCommon - numTransposed) / numCommonD) / 3.0;

		if (weight <= 0.7)
			return 1.0 - weight;

		int max = Math.min((len1 + len2) / 3, Math.min(len1, len2));
		int pos = 0;
		while (pos < max && getMatcher().match(ta.get(pos), ha.get(pos), t, h, pairID) == 0)
			++pos;
		if (pos == 0)
			return 1 - weight;
		return 1 - (weight + 0.1 * pos * (1.0 - weight));
	}
}
