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

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

/**
 * 
 * @author Milen Kouylekov
 * 
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NgramDistance extends WeightedAlgorithm {

	private static final long serialVersionUID = 1L;
	private final int ngram = 3;

	@Override
	public EditDistanceAlgorithm clone() {
		return defaultCopy();
	}

	@Override
	public double distance(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h, String pairID) {
		final int sl = ta.size();
		final int tl = ha.size();

		if (sl == 0 || tl == 0)
			return sl == tl ? 0.0 : 1.0;

		double cost = 0;
		if (sl < ngram || tl < ngram) {
			for (int i = 0, ni = Math.min(sl, tl); i < ni; i++) {
				if (getMatcher().match(ta.get(i), ha.get(i), t, h, pairID) == 0) {
					cost++;
				}
			}
			return 1 - cost / Math.max(sl, tl);
		}

		Annotation[] sa = new Annotation[sl + ngram - 1];
		double p[];
		double d[];
		double _d[];

		for (int i = 0; i < sa.length; i++) {
			if (i < ngram - 1) {
				sa[i] = null;
			} else {
				sa[i] = ta.get(i - ngram + 1);
			}
		}
		p = new double[sl + 1];
		d = new double[sl + 1];

		Object[] t_j = new Annotation[ngram];

		for (int i = 0; i <= sl; i++) {
			p[i] = i;
		}

		for (int j = 1; j <= tl; j++) {
			if (j < ngram) {
				for (int ti = 0; ti < ngram - j; ti++) {
					t_j[ti] = null; // add prefix
				}
				for (int ti = ngram - j; ti < ngram; ti++) {
					t_j[ti] = ha.get(ti - (ngram - j));
				}
			} else {
				t_j = ha.subList(j - ngram, j).toArray();
			}
			d[0] = j;
			for (int i = 1; i <= sl; i++) {
				cost = 0;
				double tn = ngram;
				for (int ni = 0; ni < ngram; ni++) {
					if (t_j[ni] != null && sa[i - 1 + ni] != null
							&& getMatcher().match(sa[i - 1 + ni], (Annotation) t_j[ni], t, h, pairID) != 0) {
						if (t_j[ni] != null)
							cost += getWeightCalculator().weightH((Annotation) t_j[ni], t, h);
					} else if (sa[i - 1 + ni] == null) {
						tn--;
					}
				}
				double ec = (float) cost / tn;
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + ec);
			}
			_d = p;
			p = d;
			d = _d;
		}
		return p[sl] / Math.max(tl, sl);
	}
}
