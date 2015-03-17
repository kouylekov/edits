/**
 * Edits - Edit Distance Textual Entailment Suite Copyright (C) 2011 Milen Kouylekov This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.edits;

import java.io.Serializable;

import lombok.Data;

import org.edits.engines.EntailmentEngine;
import org.edits.engines.EvaluationStatistics;

@Data
public class EntailmentEngineModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5993904733998446064L;
	private String anotator;
	private EntailmentEngine engine;
	private EvaluationStatistics statistics;

	public EntailmentEngineModel() {

	}

	public EntailmentEngineModel(String anotator, EntailmentEngine engine, EvaluationStatistics statistics) {
		super();
		this.anotator = anotator;
		this.engine = engine;
		this.statistics = statistics;
	}

}
