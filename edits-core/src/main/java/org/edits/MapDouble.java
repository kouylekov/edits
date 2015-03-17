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
package org.edits;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * 
 * @author Milen Kouylekov
 * 
 */
public class MapDouble extends HashMap<String, Double> implements Comparator<String> {

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(String o1, String o2) {
		return Double.compare(get(o1), get(o2));
	}

	public Double get(String s) {
		if (containsKey(s))
			return super.get(s);
		put(s, 0.0);
		return 0.0;
	}

	public void increment(String a) {
		put(a, get(a) + 1.0);
	}

	public void increment(String a, int value) {
		put(a, get(a) + value);
	}

	public List<String> ordered() {
		List<String> values = Lists.newArrayList(this.keySet());
		Collections.sort(values, this);
		return values;
	}
}
