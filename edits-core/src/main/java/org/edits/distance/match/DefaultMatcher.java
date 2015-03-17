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
package org.edits.distance.match;

import lombok.Data;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

/**
 * 
 * @author Milen Kouylekov
 * 
 */
@Data
public class DefaultMatcher implements Matcher {

	public enum MatchWords {
		FORM, FORM_AND_LEMMA, FORM_OR_LEMMA, LEMMA
	}

	private static final long serialVersionUID = 1L;

	private static double distance(String s, String t) {
		if (s.length() == 0)
			return t.length();
		if (t.length() == 0)
			return s.length();

		int[][] d = new int[s.length() + 1][t.length() + 1];

		for (int i = 0; i < s.length() + 1; i++)
			d[i][0] = i;

		for (int j = 0; j < t.length() + 1; j++)
			d[0][j] = j;

		for (int i = 1; i < s.length() + 1; i++)
			for (int j = 1; j <= t.length(); j++)
				d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1),
						d[i - 1][j - 1] + (s.charAt(i - 1) == t.charAt(j - 1) ? 0 : 1));
		return d[s.length()][t.length()];
	}

	public static double distance(String s1, String s2, boolean normalize) {
		return !normalize ? distance(s1, s2) : distance(s1, s2) / ((double) s1.length() + s2.length());
	}

	private static boolean equals(String s1, String s2, boolean ignoreCase) {
		if (s1 == null || s2 == null)
			return false;
		if (ignoreCase)
			return s1.equalsIgnoreCase(s2);
		return s1.equals(s2);
	}

	private boolean distanceWord = false;

	private boolean ignoreCase = false;

	private MatchWords strategy = MatchWords.FORM_OR_LEMMA;

	@Override
	public Matcher copy() {
		DefaultMatcher dm = new DefaultMatcher();
		dm.setDistanceWord(distanceWord);
		dm.setIgnoreCase(ignoreCase);
		dm.setStrategy(strategy);
		return dm;
	}

	private Double distance(Annotation w1, Annotation w2) {
		String t1 = w1.getForm();
		String t2 = w2.getForm();
		return distance(t1, t2, true);
	}

	private boolean equals(Annotation w1, Annotation w2) {
		if (w1 == null || w2 == null)
			return false;
		switch (strategy) {
		case FORM:
			return equals(w1.getForm(), w2.getForm(), ignoreCase);
		case LEMMA:
			return equals(w1.getLemma(), w2.getLemma(), ignoreCase);
		case FORM_OR_LEMMA:
			return equals(w1.getForm(), w2.getForm(), ignoreCase) || equals(w1.getLemma(), w2.getLemma(), ignoreCase);
		case FORM_AND_LEMMA:
			return equals(w1.getForm(), w2.getForm(), ignoreCase) && equals(w1.getLemma(), w2.getLemma(), ignoreCase);
		}
		return false;
	}

	@Override
	public double match(Annotation node1, Annotation node2, AnnotatedText t, AnnotatedText h, String pairID) {
		if (equals(node1, node2))
			return 0.0;
		double value = 1.0;
		if (distanceWord)
			value = value * distance(node1, node2);
		return value;
	}

}
