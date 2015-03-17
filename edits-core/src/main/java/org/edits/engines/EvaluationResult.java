/**
 * Edits - Edit Distance Textual Entailment Suite Copyright (C) 2011 Milen Kouylekov This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.edits.engines;

import java.io.Serializable;
import java.util.Comparator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

/**
 * 
 * @author Milen Kouylekov
 * 
 */
@Data
@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.FIELD)
public class EvaluationResult implements Comparator<EvaluationResult>, Serializable {

	private static final long serialVersionUID = 1L;
	@XmlElement
	private String assigned;
	@XmlElement
	private String benchmark;
	@XmlElement
	private double confidence;
	@XmlElement
	private String id;
	@XmlElement
	private double score;

	public EvaluationResult() {

	}

	public EvaluationResult(String id, String assigned, String benchmark, double score, double confidence) {

		this.id = id;
		this.assigned = assigned;
		this.benchmark = benchmark;
		this.score = score;
		this.confidence = confidence;
	}

	@Override
	public int compare(EvaluationResult o1, EvaluationResult o2) {
		return o1.getId().compareTo(o2.getId());
	}
}
