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

import lombok.Data;

/**
 * 
 * @author Milen Kouylekov
 * 
 */
@Data
public class ConfigurationValue {

	private boolean deletionZero = false;
	private boolean distance = false;
	private boolean formBool = false;
	private boolean idf = false;
	private boolean ignoreCase = false;
	private boolean lemmaBool = false;
	private boolean pairCosts = false;
	private boolean stopwords = false;
	private boolean wordCosts = false;

	public ConfigurationValue(boolean[] value) {
		distance = value[0];
		ignoreCase = value[1];
		stopwords = value[2];
		formBool = value[3];
		lemmaBool = value[4];

		if (value.length > 5) {
			idf = value[5];
		}
		if (value.length > 6) {
			wordCosts = value[6];
			pairCosts = value[7];
		}

		if (value.length == 8) {
			deletionZero = value[8];
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (isIgnoreCase())
			b.append("+ignore-case ");
		if (isDistance())
			b.append("+word-distance ");

		if (isPairCosts())
			b.append("+pair-size-weight ");

		if (isWordCosts())
			b.append("+word-size-weight ");

		if (isDeletionZero())
			b.append("+deletion-zero ");

		if (isStopwords())
			b.append("+stopwords ");

		if (isIdf())
			b.append("+idf ");

		if (isFormBool() && isLemmaBool()) {
			b.append("strategy:fal ");
		}
		if (isFormBool() && !isLemmaBool()) {
			b.append("strategy:f ");
		}
		if (!isFormBool() && isLemmaBool()) {
			b.append("strategy:l ");
		}
		if (!isFormBool() && !isLemmaBool()) {
			b.append("strategy:fol ");
		}

		return b.toString().trim();
	}

}
