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
package org.edits.distance.weight;

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
public class DefaultWeightCalculator implements WeightCalculator {
	public static final long serialVersionUID = 1L;
	private double valueH;
	private double valueT;

	public DefaultWeightCalculator() {
		valueH = 1;
		valueT = 1;
	}

	@Override
	public WeightCalculator clone() {
		DefaultWeightCalculator dc = new DefaultWeightCalculator();
		dc.setValueH(valueH);
		dc.setValueT(valueT);
		return dc;
	}

	@Override
	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h) {
		return valueH;
	}

	@Override
	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h) {
		return valueT;
	}
}
