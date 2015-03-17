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
public class LCS extends EditDistanceAlgorithm {

	private static final long serialVersionUID = 1L;

	@Override
	public EditDistanceAlgorithm clone() {
		return defaultCopy();
	}

	@Override
	public double distance(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h, String pairID) {
		int[][] num = new int[ta.size() + 1][ha.size() + 1];
		for (int i = 1; i <= ta.size(); i++)
			for (int j = 1; j <= ha.size(); j++)
				if (getMatcher().match(ta.get(i - 1), ha.get(j - 1), t, h, pairID) == 0)
					num[i][j] = 1 + num[i - 1][j - 1];
				else
					num[i][j] = Math.max(num[i - 1][j], num[i][j - 1]);

		double lcs = num[ta.size()][ha.size()];

		double normalization = ha.size();

		return init(lcs, normalization);
	}

}
