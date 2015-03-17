/**
 * Edits - Edit Distance Textual Entailment Suite Copyright (C) 2011 Milen Kouylekov This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.edits.engines.distance;

import java.util.Comparator;

import org.edits.engines.EvaluationStatistics;

import lombok.Getter;

/**
 * 
 * @author Milen Kouylekov
 * 
 */
public class Threshold implements Comparator<Threshold> {

	private final boolean compareByValue;
	@Getter
	private double score;
	@Getter
	private EvaluationStatistics stats;
	@Getter
	private double threshold;
	@Getter
	private double value;

	public Threshold(boolean compareByValue) {
		super();
		this.compareByValue = compareByValue;
	}

	public Threshold(double score, EvaluationStatistics stats, double thrshold, double value) {
		super();
		this.score = score;
		this.stats = stats;
		this.threshold = thrshold;
		this.value = value;
		compareByValue = false;
	}

	@Override
	public int compare(Threshold o1, Threshold o2) {
		return compareByValue ? Double.compare(o1.getValue(), o2.getValue()) : Double.compare(o1.getScore(),
				o2.getScore());
	}

}
