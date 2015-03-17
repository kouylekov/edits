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

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.extern.log4j.Log4j;

import org.edits.distance.match.DefaultMatcher;
import org.edits.distance.match.Matcher;
import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

@Data
@Log4j
public abstract class EditDistanceAlgorithm implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	private Matcher matcher;
	private boolean normalize;

	public EditDistanceAlgorithm() {
		normalize = true;
		matcher = new DefaultMatcher();
	}

	@Override
	public abstract EditDistanceAlgorithm clone();

	public EditDistanceAlgorithm defaultCopy() {
		EditDistanceAlgorithm a = instance(this.getClass().getName());
		a.setMatcher(getMatcher().copy());
		a.setNormalize(normalize);
		return a;
	}

	public double distance(AnnotatedText t, AnnotatedText h, String pairID) {
		List<Annotation> w1 = t.getAnnotation();
		List<Annotation> w2 = h.getAnnotation();
		return distance(w1, w2, t, h, pairID);
	}

	public abstract double distance(List<Annotation> ta, List<Annotation> ha, AnnotatedText t, AnnotatedText h,
			String pairID);

	public double init(double dist, double norm) {
		if (!normalize)
			return dist;
		return dist / norm;
	}

	public EditDistanceAlgorithm instance(String name) {
		try {
			return (EditDistanceAlgorithm) this.getClass().getClassLoader().loadClass(this.getClass().getName())
					.newInstance();
		} catch (Exception e) {
			log.debug(e);
			throw new RuntimeException("Could not load algorithm " + name);
		}
	}
}
