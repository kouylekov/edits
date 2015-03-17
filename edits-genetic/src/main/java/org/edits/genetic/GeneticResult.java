/**
 * Edits - Edit Distance Textual Entailment Suite Copyright (C) 2011 Milen Kouylekov This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.edits.genetic;

import java.util.Comparator;

import lombok.Getter;
import lombok.ToString;

@ToString(includeFieldNames = true)
public class GeneticResult implements Comparator<GeneticResult> {

	@Getter
	private final EditsChromosome chromosome;
	@Getter
	private final double value;

	public GeneticResult(double training, EditsChromosome def) {
		super();
		this.value = training;
		chromosome = def;
	}

	@Override
	public int compare(GeneticResult o1, GeneticResult o2) {
		return Double.compare(o1.getValue(), o2.getValue());
	}

}
