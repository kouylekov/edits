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

import java.util.Collection;
import java.util.Map;

import org.edits.engines.EvaluationStatistics;

import lombok.Getter;
import lombok.Setter;

import com.google.common.collect.Maps;

/**
 * @author Milen Kouylekov
 */
public class ModelInfo {

	public static ModelInfo mergeDistanceModels(Collection<ModelInfo> models) {
		if (models.size() == 1)
			return models.iterator().next();
		ModelInfo info = new ModelInfo();
		for (ModelInfo info2 : models) {
			info.getThreshold().putAll(info2.getThreshold());
			info.getStatistics().add(info2.getStatistics());
		}
		info.getStatistics().calculate();
		return info;
	}

	@Getter
	@Setter
	private EvaluationStatistics statistics;

	@Getter
	private final Map<String, Double> threshold;

	public ModelInfo() {
		threshold = Maps.newHashMap();
		statistics = new EvaluationStatistics();
	}

}
