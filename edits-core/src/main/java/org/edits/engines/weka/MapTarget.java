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
package org.edits.engines.weka;

import java.util.Map;

import lombok.Getter;

import org.edits.engines.EvaluationResult;
import org.edits.target.Target;

import com.google.common.collect.Maps;

/**
 * @author Milen Kouylekov
 */
public class MapTarget implements Target<EvaluationResult> {

	@Getter
	private final Map<String, Double> assigned;
	@Getter
	private final Map<String, Double> confidence;

	public MapTarget() {
		assigned = Maps.newHashMap();
		confidence = Maps.newHashMap();
	}

	@Override
	public void close() {
	}

	@Override
	public synchronized void handle(EvaluationResult res) {
		assigned.put(res.getId(), res.getAssigned().equals(BinnaryWekaEngine.NOT_VALUE) ? 0.0 : 1.0);
		confidence.put(res.getId(), res.getConfidence());
	}
}
