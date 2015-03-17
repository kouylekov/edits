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

import java.util.List;

import org.edits.etaf.Annotation;

import com.google.common.collect.Lists;

/**
 * @author Milen Kouylekov
 */
public class SimpleTokenizer implements EditsTextAnnotator {

	public static String format(String text) {
		text = text.replace("\t", " ");
		text = text.replace("\n", " ");
		text = text.replace("\r", " ");

		String old = text;
		text = text.replace("  ", " ");
		while (old.length() > text.length()) {
			old = text;
			text = text.replace("  ", " ");
		}
		return text;
	}

	public List<Annotation> annotate(String text) {
		text = format(text);
		List<Annotation> out = Lists.newArrayList();
		String[] s = text.split(" ");
		for (String ss : s) {
			Annotation l = new Annotation();
			l.setForm(ss);
			l.setLemma(ss.toLowerCase());
			out.add(l);
		}
		return out;
	}
}
